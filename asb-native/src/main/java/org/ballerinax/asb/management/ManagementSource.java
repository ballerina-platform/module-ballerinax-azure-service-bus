/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KINDither express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinax.asb.manager;

import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.administration.MessageCountDetails;
import com.azure.messaging.servicebus.administration.NamespaceInfo;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClient;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClientBuilder;
import com.azure.messaging.servicebus.administration.models.QueueProperties;
import com.azure.messaging.servicebus.administration.models.SubscriptionProperties;
import com.azure.messaging.servicebus.administration.models.TopicProperties;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import org.ballerinax.asb.util.ASBConstants;
import org.ballerinax.asb.util.ASBErrorCreator;
import org.ballerinax.asb.util.ModuleUtils;

/**
 * This facilitates the client operations of ASB Management client in
 * Ballerina.
 */
public class ManagementSource {
    ServiceBusAdministrationClient managementClient;

    public ManagementSource(String connectionString) {
        this.managementClient = new ServiceBusAdministrationClientBuilder()
                                    .connectionString(connectionString).buildClient();
    }

    public Object getNamespaceInfo() {
        try {
            NamespaceInfo namespaceInfo = managementClient.getNamespaceInfo();
            Object[] values = new Object[1];
            values[0] = StringUtils.fromString(namespaceInfo.getName());
            BMap<BString, Object> namespaceRecord =
                    ValueCreator.createRecordValue(ModuleUtils.getModule(),
                    ASBConstants.NAMESPACE_INFO);
            return ValueCreator.createRecordValue(namespaceRecord, values);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ServiceBusException e) {
            return ASBErrorCreator.fromASBException(e);
        }
    }

    public Object getMessageCountDetailsOfQueue(String entityPath) {
        try {
            MessageCountDetails messageCountDetails =
               managementClient.getQueueRuntimeInfo(entityPath).getMessageCountDetails();
            Object[] values = new Object[5];
            values[0] = messageCountDetails.getActiveMessageCount();
            values[1] = messageCountDetails.getDeadLetterMessageCount();
            values[2] = messageCountDetails.getScheduledMessageCount();
            values[3] = messageCountDetails.getTransferMessageCount();
            values[4] = messageCountDetails.getTransferDeadLetterMessageCount();
            BMap<BString, Object> messageCountDetailsRecord =
                    ValueCreator.createRecordValue(ModuleUtils.getModule(), ASBConstants.MESSAGE_COUNT_DETAILS);
            return ValueCreator.createRecordValue(messageCountDetailsRecord, values);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ServiceBusException e) {
            return ASBErrorCreator.fromASBException(e);
        }
    }

    public Object createQueue(String queueName) {
        try {
            QueueProperties queueProperties = managementClient.createQueue(queueName);
            BMap<BString, Object> queuePropertiesRecord =
                    ValueCreator.createRecordValue(ModuleUtils.getModule(),
                    ASBConstants.QUEUE_PROPERTIES);
            return ValueCreator.createRecordValue(queuePropertiesRecord, queueProperties);
        } catch (ServiceBusException e) {
            return ASBErrorCreator.fromASBException(e);
        }
    }

    public Object deleteQueue(String queueName) {
        try {
            managementClient.deleteQueue(queueName);
        } catch (Exception e) {
            return ASBErrorCreator.fromUnhandledException(e);
        }
        return null;
    }

    public Object createTopic(String topicName) {
        try {
            TopicProperties topicProperties = managementClient.createTopic(topicName);
            BMap<BString, Object> topicPropertiesRecord =
                    ValueCreator.createRecordValue(ModuleUtils.getModule(),
                    ASBConstants.TOPIC_PROPERTIES);
            return ValueCreator.createRecordValue(topicPropertiesRecord, topicProperties);
        } catch (ServiceBusException e) {
            return ASBErrorCreator.fromASBException(e);
        }
    }

    public Object deleteTopic(String topicName) {
        try {
            managementClient.deleteTopic(topicName);
        } catch (Exception e) {
            return ASBErrorCreator.fromUnhandledException(e);
        }
        return null;
    }

    public Object createSubscription(String topicName, String subscriptionName) {
        try {
            SubscriptionProperties subscriptionProperties =
                managementClient.createSubscription(topicName, subscriptionName);
            BMap<BString, Object> subscriptionPropertiesRecord =
                    ValueCreator.createRecordValue(ModuleUtils.getModule(),
                    ASBConstants.SUBSCRIPTION_PROPERTIES);
            return ValueCreator.createRecordValue(subscriptionPropertiesRecord, subscriptionProperties);
        } catch (ServiceBusException e) {
            return ASBErrorCreator.fromASBException(e);
        }
    }

    public Object deleteSubscription(String topicName, String subscriptionName) {
        try {
            managementClient.deleteSubscription(topicName, subscriptionName);
        } catch (Exception e) {
            return ASBErrorCreator.fromUnhandledException(e);
        }
        return null;
    }
}
