/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinax.asb.receiver;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.models.AmqpAnnotatedMessage;
import com.azure.core.amqp.models.AmqpMessageBodyType;
import com.azure.core.util.IterableStream;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusReceiverClientBuilder;
import com.azure.messaging.servicebus.models.DeadLetterOptions;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.types.BArrayType;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.ballerinax.asb.util.ASBConstants;
import org.ballerinax.asb.util.ASBUtils;
import org.ballerinax.asb.util.ModuleUtils;

import static org.ballerinax.asb.util.ASBConstants.*;
import static org.ballerinax.asb.util.ASBUtils.getMapValue;

/**
 * This facilitates the client operations of MessageReceiver client in
 * Ballerina.
 */
public class MessageReceiver {
    private static final Logger log = Logger.getLogger(MessageReceiver.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private ServiceBusReceiverClient receiver;

    /**
     * Parameterized constructor for Message Receiver (IMessageReceiver).
     *
     * @param connectionString Azure service bus connection string.
     * @param queueName        QueueName
     * @param topicName        Topic Name
     * @param subscriptionName Subscription Name
     * @param receiveMode      Receive Mode as PeekLock or Receive&Delete.
     * @param maxAutoLockRenewDuration Max lock renewal duration under Peek Lock mode. Setting to 0 disables auto-renewal. 
     *                                  For RECEIVE_AND_DELETE mode, auto-renewal is disabled.
     * @throws ServiceBusException  on failure initiating IMessage Receiver in Azure
     *                              Service Bus instance.
     * @throws InterruptedException on failure initiating IMessage Receiver due to
     *                              thread interruption.
     */
    public MessageReceiver(String connectionString, String queueName, String topicName, String subscriptionName,
            String receiveMode, long maxAutoLockRenewDuration, String logLevel)
            throws ServiceBusException, InterruptedException {
        log.setLevel(Level.toLevel(logLevel, Level.OFF));
        ServiceBusReceiverClientBuilder receiverClientBuilder = new ServiceBusClientBuilder()
                .connectionString(connectionString)
                .receiver();
        if (!queueName.isEmpty()) {
            if (Objects.equals(receiveMode, RECEIVE_AND_DELETE)) {
                this.receiver = receiverClientBuilder
                        .receiveMode(ServiceBusReceiveMode.RECEIVE_AND_DELETE)
                        .queueName(queueName)
                        .buildClient();

            } else {
                this.receiver = receiverClientBuilder
                        .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
                        .queueName(queueName)
                        .maxAutoLockRenewDuration(Duration.ofSeconds(maxAutoLockRenewDuration))
                        .buildClient();
            }
        } else if (!subscriptionName.isEmpty() && !topicName.isEmpty()) {
            if (Objects.equals(receiveMode, RECEIVE_AND_DELETE)) {
                this.receiver = receiverClientBuilder
                        .receiveMode(ServiceBusReceiveMode.RECEIVE_AND_DELETE)
                        .topicName(topicName)
                        .subscriptionName(subscriptionName)
                        .buildClient();

            } else {
                this.receiver = receiverClientBuilder
                        .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
                        .topicName(topicName)
                        .subscriptionName(subscriptionName)
                        .maxAutoLockRenewDuration(Duration.ofSeconds(maxAutoLockRenewDuration))
                        .buildClient();
            }
        }
        log.debug("ServiceBusReceiverClient initialized");
    }

    public MessageReceiver(BMap<BString, Object> receiverConfig, BString logLevel) 
        throws ServiceBusException, InterruptedException {
        log.setLevel(Level.toLevel(logLevel.getValue(), Level.OFF));
        AmqpRetryOptions retryOptions = getRetryOptions(receiverConfig);
        ServiceBusReceiverClientBuilder receiverClientBuilder = new ServiceBusClientBuilder()
                .connectionString(receiverConfig.getStringValue(CONNECTION_STRING).getValue())
                .retryOptions(retryOptions)
                .receiver();
        String receiveMode = receiverConfig.getStringValue(RECEIVE_MODE).getValue();
        if (RECEIVE_AND_DELETE.equals(receiveMode)) {
            receiverClientBuilder
                    .receiveMode(ServiceBusReceiveMode.RECEIVE_AND_DELETE);
        } else {
            Long maxAutoLockRenewDuration = receiverConfig.getIntValue(MAX_AUTOLOCK_RENEW_DURATION);
            receiverClientBuilder
                    .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
                    .maxAutoLockRenewDuration(Duration.ofSeconds(maxAutoLockRenewDuration));
        }
        BMap<BString, Object> entityConfig = getMapValue(receiverConfig, ENTITY_CONFIG);
        updateClientEntityConfig(receiverClientBuilder, entityConfig);
        this.receiver = receiverClientBuilder.buildClient();
        log.debug("ServiceBusReceiverClient initialized");
    }

    private AmqpRetryOptions getRetryOptions(BMap<BString, Object> receiverConfig) {
        BMap<BString, Object> retryConfigs = getMapValue(receiverConfig, AMQP_RETRY_OPTIONS);
        Long maxRetries = retryConfigs.getIntValue(MAX_RETRIES);
        BigDecimal delayConfig = ((BDecimal) retryConfigs.get(DELAY)).decimalValue();
        BigDecimal maxDelay = ((BDecimal) retryConfigs.get(MAX_DELAY)).decimalValue();
        BigDecimal tryTimeout = ((BDecimal) retryConfigs.get(TRY_TIMEOUT)).decimalValue();
        String retryMode = retryConfigs.getStringValue(RETRY_MODE).getValue();
        return new AmqpRetryOptions()
                .setMaxRetries(maxRetries.intValue())
                .setDelay(Duration.ofSeconds(delayConfig.intValue()))
                .setMaxDelay(Duration.ofSeconds(maxDelay.intValue()))
                .setTryTimeout(Duration.ofSeconds(tryTimeout.intValue()))
                .setMode(AmqpRetryMode.valueOf(retryMode));
    }

    private void updateClientEntityConfig(ServiceBusReceiverClientBuilder clientBuilder,
                                          BMap<BString, Object> entityConfig) {
        if (entityConfig.containsKey(QUEUE_NAME)) {
            clientBuilder
                    .queueName(entityConfig.getStringValue(QUEUE_NAME).getValue());
        } else {
            clientBuilder
                    .topicName(entityConfig.getStringValue(TOPIC_NAME).getValue())
                    .subscriptionName(entityConfig.getStringValue(SUBSCRIPTION_NAME).getValue());
        }
    }

    /**
     * Receive Message with configurable parameters as Map when Receiver Connection
     * is given as a parameter and
     * server wait time in seconds to receive message and return Message object.
     *
     * @param endpointClient Ballerina ASB client object
     * @param serverWaitTime Specified server wait time in seconds to receive
     *                       message.
     * @return Message Object of the received message.
     */
    public Object receive(BObject endpointClient, Object serverWaitTime) {
        try {
            ServiceBusReceivedMessage receivedMessage = null;
            IterableStream<ServiceBusReceivedMessage> receivedMessages;
            Iterator<ServiceBusReceivedMessage> iterator;
            if (serverWaitTime != null) {
                receivedMessages = this.receiver.receiveMessages(1, Duration.ofSeconds((long) serverWaitTime));
                iterator = receivedMessages.iterator();
                while (iterator.hasNext()) {
                    receivedMessage = iterator.next();
                }
            } else {
                receivedMessages = receiver.receiveMessages(1);
                iterator = receivedMessages.iterator();
                while (iterator.hasNext()) {
                    receivedMessage = iterator.next();
                }
            }
            if (receivedMessage == null) {
                return null;
            }
            log.debug("Received message with messageId: " + receivedMessage.getMessageId());
            return getReceivedMessage(endpointClient, receivedMessage);
        } catch (Exception e) {
            return ASBUtils.returnErrorValue(e.getClass().getSimpleName(), e);
        }
    }

    /**
     * Converts AMPQ Body value to Java objects
     * 
     * @param amqpValue AMQP Value type object
     * @return
     */
    public Object convertAMQPToJava(String messageId, Object amqpValue) {
        log.debug("Type of amqpValue object  of received message "+ messageId +" is " + amqpValue.getClass());
        Class<?> clazz = amqpValue.getClass();
        switch (clazz.getSimpleName()) {
            case "Integer":
                return (Integer) amqpValue;
            case "Long":
                return (Long) amqpValue;
            case "Float":
                return (Float) amqpValue;
            case "Double":
                return (Double) amqpValue;
            case "String":
                return (String) amqpValue;
            case "Boolean":
                return (Boolean) amqpValue;
            case "Byte":
                return (Byte) amqpValue;
            case "Short":
                return (Short) amqpValue;
            case "Character":
                return (Character) amqpValue;
            case "BigDecimal":
                return (BigDecimal) amqpValue;
            case "Date":
                return (java.util.Date) amqpValue;
            case "UUID":
                return (UUID) amqpValue;
            default:
                log.debug("The type of amqpValue object " + clazz + " is not supported");
                return null;
        }
    }    

    /**
     * Prepares the message body content
     * 
     * @param receivedMessage ASB received message
     * @return
     * @throws IOException
     */
    private Object getMessageContent(ServiceBusReceivedMessage receivedMessage) throws IOException {
        AmqpAnnotatedMessage rawAmqpMessage = receivedMessage.getRawAmqpMessage();
        AmqpMessageBodyType bodyType = rawAmqpMessage.getBody().getBodyType();
        switch (bodyType) {
            case DATA:
                return rawAmqpMessage.getBody().getFirstData();
            case VALUE:
                Object amqpValue = rawAmqpMessage.getBody().getValue();
                log.debug("Received a message with messageId " + receivedMessage.getMessageId()
                        + " AMQPMessageBodyType:" + bodyType);

                amqpValue = convertAMQPToJava(receivedMessage.getMessageId(), amqpValue);
                return amqpValue;
            default:
                throw new RuntimeException("Invalid message body type: " + receivedMessage.getMessageId());
        }
    }

    /**
     * @param endpointClient  Ballerina client object
     * @param receivedMessage Received Message
     * @return
     * @throws IOException
     */
    private BMap<BString, Object> getReceivedMessage(BObject endpointClient, ServiceBusReceivedMessage receivedMessage)
            throws IOException {
        Map<String, Object> map = new HashMap<>();
        Object body = getMessageContent(receivedMessage);
        if (body instanceof byte[]) {
            byte[] bodyA = (byte[]) body;
            map.put("body", ValueCreator.createArrayValue(bodyA));
        } else {
            map.put("body", body);
        }
        if (receivedMessage.getContentType() != null) {
            map.put("contentType", StringUtils.fromString(receivedMessage.getContentType()));
        }
        map.put("messageId", StringUtils.fromString(receivedMessage.getMessageId()));
        map.put("to", StringUtils.fromString(receivedMessage.getTo()));
        map.put("replyTo", StringUtils.fromString(receivedMessage.getReplyTo()));
        map.put("replyToSessionId", StringUtils.fromString(receivedMessage.getReplyToSessionId()));
        map.put("label", StringUtils.fromString(receivedMessage.getSubject()));
        map.put("sessionId", StringUtils.fromString(receivedMessage.getSessionId()));
        map.put("correlationId", StringUtils.fromString(receivedMessage.getCorrelationId()));
        map.put("partitionKey", StringUtils.fromString(receivedMessage.getPartitionKey()));
        map.put("timeToLive", (int) receivedMessage.getTimeToLive().getSeconds());
        map.put("sequenceNumber", (int) receivedMessage.getSequenceNumber());
        map.put("lockToken", StringUtils.fromString(receivedMessage.getLockToken()));
        map.put("deliveryCount", (int) receivedMessage.getDeliveryCount());
        map.put("enqueuedTime", StringUtils.fromString(receivedMessage.getEnqueuedTime().toString()));
        map.put("enqueuedSequenceNumber", (int) receivedMessage.getEnqueuedSequenceNumber());
        map.put("deadLetterErrorDescription", StringUtils.fromString(receivedMessage.getDeadLetterErrorDescription()));
        map.put("deadLetterReason", StringUtils.fromString(receivedMessage.getDeadLetterReason()));
        map.put("deadLetterSource", StringUtils.fromString(receivedMessage.getDeadLetterSource()));
        map.put("state", StringUtils.fromString(receivedMessage.getState().toString()));
        map.put("applicationProperties", getApplicationProperties(receivedMessage));
        BMap<BString, Object> createRecordValue = ValueCreator.createRecordValue(ModuleUtils.getModule(),
                ASBConstants.MESSAGE_RECORD, map);
        endpointClient.addNativeData(receivedMessage.getLockToken(), receivedMessage);
        return createRecordValue;
    }

    private static BMap<BString, Object> getApplicationProperties(ServiceBusReceivedMessage message) {
        BMap<BString, Object> applicationPropertiesRecord = ValueCreator.createRecordValue(ModuleUtils.getModule(),
                ASBConstants.APPLICATION_PROPERTY_TYPE);
        MapType mapType = TypeCreator.createMapType(PredefinedTypes.TYPE_ANYDATA);
        BMap<BString, Object> applicationProperties = ValueCreator.createMapValue(mapType);
        for (Map.Entry<String, Object> property: message.getApplicationProperties().entrySet()) {
            populateApplicationProperty(applicationProperties, property.getKey(), property.getValue());
        }
        return ValueCreator.createRecordValue(applicationPropertiesRecord, applicationProperties);
    }
    private static void populateApplicationProperty(BMap<BString, Object> applicationProperties,
                                                    String key, Object value) {
        BString propertyKey = StringUtils.fromString(key);
        if (value instanceof String) {
            applicationProperties.put(propertyKey, StringUtils.fromString((String) value));
        } else if (value instanceof Integer) {
            applicationProperties.put(propertyKey, (Integer) value);
        } else if (value instanceof Long) {
            applicationProperties.put(propertyKey, (Long) value);
        } else if (value instanceof Float) {
            applicationProperties.put(propertyKey, (Float) value);
        } else if (value instanceof Double) {
            applicationProperties.put(propertyKey, (Double) value);
        } else if (value instanceof Boolean) {
            applicationProperties.put(propertyKey, (Boolean) value);
        } else if (value instanceof Character) {
            applicationProperties.put(propertyKey, (Character) value);
        } else if (value instanceof Byte) {
            applicationProperties.put(propertyKey, (Byte) value);
        } else if (value instanceof Short) {
            applicationProperties.put(propertyKey, (Short) value);
        } else {
            applicationProperties.put(propertyKey, StringUtils.fromString(value.toString()));
        }
    }

    /**
     * Receive Batch of Messages with configurable parameters as Map when Receiver
     * Connection is given as a parameter,
     * maximum message count in a batch as int, server wait time in seconds and
     * return Batch Message object.
     *
     * @param endpointClient  Ballerina ASB client object
     * @param maxMessageCount Maximum no. of messages in a batch.
     * @param serverWaitTime  Server wait time.
     * @return Batch Message Object of the received batch of messages.
     */
    public Object receiveBatch(BObject endpointClient, Object maxMessageCount, Object serverWaitTime) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Waiting up to 'serverWaitTime' seconds for messages from " + receiver.getEntityPath());
            }
            return getReceivedMessageBatch(endpointClient, maxMessageCount, serverWaitTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            return ASBUtils.returnErrorValue(e.getClass().getSimpleName(), e);
        }
    }

    private BMap<BString, Object> getReceivedMessageBatch(BObject endpointClient, Object maxMessageCount,
            Object serverWaitTime)
            throws InterruptedException, ServiceBusException, IOException {
        int messageCount = 0;
        Map<String, Object> map = new HashMap<>();
        int maxCount = Long.valueOf(maxMessageCount.toString()).intValue();
        Object[] messages = new Object[maxCount];
        IterableStream<ServiceBusReceivedMessage> receivedMessageStream;
        if (serverWaitTime != null) {
            receivedMessageStream = receiver.receiveMessages(maxCount, Duration.ofSeconds((long) serverWaitTime));
        } else {
            receivedMessageStream = receiver.receiveMessages(maxCount);
        }
        BMap<BString, Object> messageRecord = ValueCreator.createRecordValue(ModuleUtils.getModule(),
                ASBConstants.MESSAGE_RECORD);
        for (ServiceBusReceivedMessage receivedMessage : receivedMessageStream) {
            BMap<BString, Object> recordMap = getReceivedMessage(endpointClient, receivedMessage);
            messages[messageCount] = ValueCreator.createRecordValue(ModuleUtils.getModule(),
                    ASBConstants.MESSAGE_RECORD, recordMap);
            messageCount = messageCount + 1;
        }
        BArrayType sourceArrayType = new BArrayType(TypeUtils.getType(messageRecord));
        map.put("messageCount", messageCount);
        map.put("messages", ValueCreator.createArrayValue(messages, sourceArrayType));
        return ValueCreator.createRecordValue(ModuleUtils.getModule(),
                ASBConstants.MESSAGE_BATCH_RECORD, map);
    }

    /**
     * Completes Messages from Queue or Subscription based on messageLockToken
     * 
     * @param endpointClient Ballerina ASB client object
     * @param lockToken      Message lock token.
     * @return An error if failed to complete the message.
     */
    public Object complete(BObject endpointClient, BString lockToken) {
        try {
            ServiceBusReceivedMessage message = (ServiceBusReceivedMessage) endpointClient
                    .getNativeData(lockToken.getValue());
            receiver.complete(message);
            log.debug("Completed the message(Id: " + message.getMessageId() + ") with lockToken " + lockToken);
            return null;
        } catch (Exception e) {
            return ASBUtils.returnErrorValue(e.getClass().getSimpleName(), e);
        }
    }

    /**
     * Abandons message & make available again for processing from Queue or
     * Subscription based on messageLockToken
     * 
     * @param endpointClient Ballerina ASB client object
     * @param lockToken      Message lock token.
     * @return An error if failed to abandon the message.
     */
    public Object abandon(BObject endpointClient, BString lockToken) {
        try {
            ServiceBusReceivedMessage message = (ServiceBusReceivedMessage) endpointClient
                    .getNativeData(lockToken.getValue());
            receiver.abandon(message);
            log.debug("Done abandoning a message(Id: " + message.getMessageId() + ") using its lock token from \n"
                    + receiver.getEntityPath());
            return null;
        } catch (Exception e) {
            return ASBUtils.returnErrorValue(e.getClass().getSimpleName(), e);
        }
    }

    /**
     * Dead-Letter the message & moves the message to the Dead-Letter Queue based on
     * messageLockToken
     *
     * @param endpointClient             Ballerina ASB client object
     * @param lockToken                  Message lock token.
     * @param deadLetterReason           The dead letter reason.
     * @param deadLetterErrorDescription The dead letter error description.
     * @return An error if failed to dead letter the message.
     */
    public Object deadLetter(BObject endpointClient, BString lockToken, Object deadLetterReason,
            Object deadLetterErrorDescription) {
        try {
            ServiceBusReceivedMessage message = (ServiceBusReceivedMessage) endpointClient
                    .getNativeData(lockToken.getValue());
            DeadLetterOptions options = new DeadLetterOptions()
                    .setDeadLetterErrorDescription(ASBUtils.convertString(deadLetterErrorDescription));
            options.setDeadLetterReason(ASBUtils.convertString(deadLetterReason));
            receiver.deadLetter(message, options);
            log.debug("Done dead-lettering a message(Id: " + message.getMessageId() + ") using its lock token from "
                    + receiver.getEntityPath());

            return null;
        } catch (Exception e) {
            return ASBUtils.returnErrorValue(e.getClass().getSimpleName(), e);
        }
    }

    /**
     * Defer the message in a Queue or Subscription based on messageLockToken
     *
     * @param endpointClient Ballerina ASB client object
     * @param lockToken      Message lock token.
     * @return An error if failed to defer the message.
     */
    public Object defer(BObject endpointClient, BString lockToken) {
        try {
            ServiceBusReceivedMessage message = (ServiceBusReceivedMessage) endpointClient
                    .getNativeData(lockToken.getValue());
            receiver.defer(message);
            log.debug("Done deferring a message(Id: " + message.getMessageId() + ") using its lock token from "
                    + receiver.getEntityPath());
            return null;
        } catch (Exception e) {
            return ASBUtils.returnErrorValue(e.getClass().getSimpleName(), e);
        }
    }

    /**
     * Receives a deferred Message. Deferred messages can only be received by using
     * sequence number and return
     * Message object.
     *
     * @param endpointClient Ballerina ASB client object
     * @param sequenceNumber Unique number assigned to a message by Service Bus. The
     *                       sequence number is a unique 64-bit
     *                       integer assigned to a message as it is accepted and
     *                       stored by the broker and functions as
     *                       its true identifier.
     * @return The received Message or null if there is no message for given
     *         sequence number.
     */
    public Object receiveDeferred(BObject endpointClient, int sequenceNumber) {
        try {
            ServiceBusReceivedMessage receivedMessage = receiver.receiveDeferredMessage(sequenceNumber);
            if (receivedMessage == null) {
                return null;
            }
            log.debug("Received deferred message using its sequenceNumber from " + receiver.getEntityPath());
            return getReceivedMessage(endpointClient, receivedMessage);
        } catch (Exception e) {
            return ASBUtils.returnErrorValue(e.getClass().getSimpleName(), e);
        }
    }

    /**
     * The operation renews lock on a message in a queue or subscription based on
     * messageLockToken.
     *
     * @param endpointClient Ballerina ASB client object
     * @param lockToken      Message lock token.
     * @return An error if failed to renewLock of the message.
     */
    public Object renewLock(BObject endpointClient, BString lockToken) {
        try {
            ServiceBusReceivedMessage message = (ServiceBusReceivedMessage) endpointClient
                    .getNativeData(lockToken.getValue());
            receiver.renewMessageLock(message);
            log.debug("Done renewing a message(Id: " + message.getMessageId() + ") using its lock token from "
                    + receiver.getEntityPath());
            return null;
        } catch (Exception e) {
            return ASBUtils.returnErrorValue(e.getClass().getSimpleName(), e);
        }
    }

    /**
     * Closes the Asb Receiver Connection using the given connection parameters.
     *
     * @return An error if failed to close the receiver.
     */
    public Object closeReceiver() {
        try {
            receiver.close();
            log.debug("Closed the receiver");
            return null;
        } catch (Exception e) {
            return ASBUtils.returnErrorValue(e.getClass().getSimpleName(), e);
        }
    }
}
