/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinax.asb.listener;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import org.ballerinax.asb.util.ASBErrorCreator;

import java.time.Duration;
import java.util.Objects;

/**
 * {@code NativeListener} provides the utility methods for the Ballerina ASB listener implementation.
 */
public final class NativeListener {
    private static final String NATIVE_CLIENT = "nativeClient";
    private static final String BALLERINA_SERVICE_OBJ = "serviceObject";

    private NativeListener() {
    }

    public Object externInit(Environment env, BObject bListener, BMap<BString, Object> config) {
        try {
            ServiceBusProcessorClient nativeClient = constructNativeClient(bListener, config, env.getRuntime());
            bListener.addNativeData(NATIVE_CLIENT, nativeClient);
        } catch (Exception e) {
            return ASBErrorCreator.createError(
                    String.format("Error occurred while initializing the listener: %s", e.getMessage()), e);
        }
        return null;
    }

    private static ServiceBusProcessorClient constructNativeClient(BObject bListener, BMap<BString, Object> config,
                                                                   Runtime bRuntime) {
        ListenerConfiguration listenerConfigs = new ListenerConfiguration(config);
        ServiceBusClientBuilder.ServiceBusProcessorClientBuilder clientBuilder = new ServiceBusClientBuilder()
                .connectionString(listenerConfigs.connectionString())
                .retryOptions(listenerConfigs.retryOptions())
                .processor()
                .receiveMode(listenerConfigs.receiveMode())
                .prefetchCount(listenerConfigs.prefetchCount())
                .maxAutoLockRenewDuration(Duration.ofSeconds(listenerConfigs.maxAutoLockRenewDuration()))
                .maxConcurrentCalls(listenerConfigs.maxConcurrency())
                .processMessage(new MessageConsumer(bListener, bRuntime))
                .processError(new ErrorConsumer(bListener, bRuntime));
        if (!listenerConfigs.autoComplete()) {
            clientBuilder.disableAutoComplete();
        }
        if (listenerConfigs.entityConfig() instanceof TopicConfig topicConfig) {
            clientBuilder.topicName(topicConfig.topic()).subscriptionName(topicConfig.subscription());
        } else if (listenerConfigs.entityConfig() instanceof QueueConfig queueConfig) {
            clientBuilder.queueName(queueConfig.queue());
        }
        return clientBuilder.buildProcessorClient();
    }

    public static Object attach(BObject bListener, BObject bService, Object name) {
        bListener.addNativeData(BALLERINA_SERVICE_OBJ, bService);
        return null;
    }

    public static Object detach(BObject bListener, BObject bService) {
        bListener.addNativeData(BALLERINA_SERVICE_OBJ, null);
        return null;
    }

    public static Object start(BObject bListener) {
        BObject bService = getBallerinaSvc(bListener);
        try {
            Object nativeClient = bListener.getNativeData(NATIVE_CLIENT);
            if (Objects.isNull(nativeClient)) {
                return ASBErrorCreator.createError("Could not find the native client used by the listener");
            }
            ((ServiceBusProcessorClient) nativeClient).start();
        } catch (Exception e) {
            return ASBErrorCreator.createError(
                    String.format("Error occurred while starting the listener: %s", e.getMessage()), e);
        }
        return null;
    }

    public static BObject getBallerinaSvc(BObject bListener) {
        Object bService = bListener.getNativeData(BALLERINA_SERVICE_OBJ);
        if (Objects.isNull(bService)) {
            throw ASBErrorCreator.createError("Could not find the `asb:Service` attached to the listener");
        }
        return (BObject) bService;
    }

    public static Object gracefulStop(BObject bListener) {
        return stopListener(bListener);
    }

    public static Object immediateStop(BObject bListener) {
        return stopListener(bListener);
    }

    private static Object stopListener(BObject bListener) {
        try {
            Object nativeClient = bListener.getNativeData(NATIVE_CLIENT);
            if (Objects.isNull(nativeClient)) {
                return ASBErrorCreator.createError("Could not find the native client used by the listener");
            }
            ((ServiceBusProcessorClient) nativeClient).stop();
            ((ServiceBusProcessorClient) nativeClient).close();
        } catch (Exception e) {
            return ASBErrorCreator.createError(
                    String.format("Error occurred while stopping the listener: %s", e.getMessage()), e);
        }
        return null;
    }
}
