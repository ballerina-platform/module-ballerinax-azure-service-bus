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
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import io.ballerina.runtime.api.Environment;
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
    private static final String NATIVE_SVC_OBJ = "nativeSvcObject";

    private NativeListener() {
    }

    public static Object externInit(BObject bListener, BMap<BString, Object> config) {
        try {
            ServiceBusProcessorClient nativeClient = constructNativeClient(bListener, config);
            bListener.addNativeData(NATIVE_CLIENT, nativeClient);
        } catch (Exception e) {
            return ASBErrorCreator.createError(
                    String.format("Error occurred while initializing the listener: %s", e.getMessage()), e);
        }
        return null;
    }

    private static ServiceBusProcessorClient constructNativeClient(BObject bListener,
                                                                   BMap<BString, Object> listenerConfigs) {
        ListenerConfiguration configs = new ListenerConfiguration(listenerConfigs);
        ServiceBusClientBuilder.ServiceBusProcessorClientBuilder clientBuilder = new ServiceBusClientBuilder()
                .connectionString(configs.connectionString())
                .retryOptions(configs.amqpRetryOptions())
                .processor()
                .receiveMode(configs.receiveMode())
                .prefetchCount(configs.prefetchCount())
                .maxConcurrentCalls(configs.maxConcurrency())
                .processMessage(new MessageConsumer(bListener, configs.autoComplete()))
                .processError(new ErrorConsumer(bListener));
        // In the Ballerina listener-service mode, using the default auto-complete mode is impractical because the
        // actual outcomes are only determined at the callback level (following the execution of the remote method),
        // and the default auto-complete mode does not account for this. Therefore, we will disable the default
        // auto-complete mode in this context and introduce a manual auto-complete implementation instead. For further
        // details, refer to the `OnMessageAutoCompletableCallback`
        clientBuilder.disableAutoComplete();

        if (ServiceBusReceiveMode.PEEK_LOCK.equals(configs.receiveMode())) {
            clientBuilder.maxAutoLockRenewDuration(Duration.ofSeconds(configs.maxAutoLockRenewDuration()));
        }
        if (configs.entityConfig() instanceof TopicConfig topicConfig) {
            clientBuilder.topicName(topicConfig.topic()).subscriptionName(topicConfig.subscription());
        } else if (configs.entityConfig() instanceof QueueConfig queueConfig) {
            clientBuilder.queueName(queueConfig.queue());
        }
        return clientBuilder.buildProcessorClient();
    }

    public static Object attach(Environment env, BObject bListener, BObject bService, Object name) {
        Object svcObject = bListener.getNativeData(NATIVE_SVC_OBJ);
        if (Objects.nonNull(svcObject)) {
            return ASBErrorCreator.createError("Trying to attach multiple `asb:Service` objects to the same listener");
        }
        try {
            NativeBServiceAdaptor nativeBService = new NativeBServiceAdaptor(env.getRuntime(), bService, name);
            nativeBService.validate();
            bListener.addNativeData(NATIVE_SVC_OBJ, nativeBService);
        } catch (Exception e) {
            return ASBErrorCreator.createError(
                    String.format("Error occurred while attaching a service to the listener: %s", e.getMessage()), e);
        }
        return null;
    }

    public static Object detach(BObject bListener, BObject bService) {
        bListener.addNativeData(NATIVE_SVC_OBJ, null);
        return null;
    }

    public static Object start(BObject bListener) {
        NativeBServiceAdaptor bService = getBallerinaSvc(bListener);
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

    public static NativeBServiceAdaptor getBallerinaSvc(BObject bListener) {
        Object bService = bListener.getNativeData(NATIVE_SVC_OBJ);
        if (Objects.isNull(bService)) {
            throw ASBErrorCreator.createError("Could not find the `asb:Service` attached to the listener");
        }
        return (NativeBServiceAdaptor) bService;
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
