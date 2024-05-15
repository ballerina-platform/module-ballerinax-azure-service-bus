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

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

import static org.ballerinax.asb.util.ASBUtils.getRetryOptions;

/**
 * {@code ListenerConfiguration} contains the java representation of the Ballerina ASB listener configurations.
 *
 * @param connectionString ASB connection string.
 * @param entityConfig ASB entity related configurations.
 * @param receiveMode Receive mode of the underlying client.
 * @param maxAutoLockRenewDuration Amount of time to continue auto-renewing the lock.
 * @param maxConcurrency Max concurrent messages that this processor should process.
 * @param prefetchCount Prefetch count of the processor.
 * @param autoComplete Flag indicating auto-complete and auto-abandon of received messages is enabled.
 * @param amqpRetryOptions Retry options for Service Bus client.
 */
public record ListenerConfiguration(String connectionString, EntityConfig entityConfig,
                                    ServiceBusReceiveMode receiveMode, int maxAutoLockRenewDuration,
                                    int maxConcurrency, int prefetchCount, boolean autoComplete,
                                    AmqpRetryOptions amqpRetryOptions) {
    private static final BString CONNECTION_STRING = StringUtils.fromString("connectionString");
    private static final BString ENTITY_CONFIG = StringUtils.fromString("entityConfig");
    private static final BString ENTITY_CONFIG_QUEUE_NAME = StringUtils.fromString("queueName");
    private static final BString ENTITY_CONFIG_TOPIC_NAME = StringUtils.fromString("topicName");
    private static final BString ENTITY_CONFIG_SUBSCRIPTION_NAME = StringUtils.fromString("subscriptionName");
    private static final BString RECEIVE_MODE = StringUtils.fromString("receiveMode");
    private static final BString MAX_AUTO_LOCK_RENEW_DURATION = StringUtils.fromString("maxAutoLockRenewDuration");
    private static final BString MAX_CONCURRENCY = StringUtils.fromString("maxConcurrency");
    private static final BString PREFETCH_COUNT = StringUtils.fromString("prefetchCount");
    private static final BString AUTO_COMPLETE = StringUtils.fromString("autoComplete");
    private static final BString AMQP_RETRY_OPTIONS = StringUtils.fromString("amqpRetryOptions");


    @SuppressWarnings("unchecked")
    public ListenerConfiguration(BMap<BString, Object> configurations) {
        this(
                configurations.getStringValue(CONNECTION_STRING).getValue(),
                getEntityConfig((BMap<BString, Object>) configurations.getMapValue(ENTITY_CONFIG)),
                ServiceBusReceiveMode.valueOf(configurations.getStringValue(RECEIVE_MODE).getValue()),
                configurations.getIntValue(MAX_AUTO_LOCK_RENEW_DURATION).intValue(),
                configurations.getIntValue(MAX_CONCURRENCY).intValue(),
                configurations.getIntValue(PREFETCH_COUNT).intValue(),
                configurations.getBooleanValue(AUTO_COMPLETE),
                getRetryOptions((BMap<BString, Object>) configurations.getMapValue(AMQP_RETRY_OPTIONS))
        );
    }

    private static EntityConfig getEntityConfig(BMap<BString, Object> entityConfig) {
        if (entityConfig.containsKey(ENTITY_CONFIG_QUEUE_NAME)) {
            return new QueueConfig(entityConfig.getStringValue(ENTITY_CONFIG_QUEUE_NAME).getValue());
        }
        return new TopicConfig(
                entityConfig.getStringValue(ENTITY_CONFIG_TOPIC_NAME).getValue(),
                entityConfig.getStringValue(ENTITY_CONFIG_SUBSCRIPTION_NAME).getValue()
        );
    }
}
