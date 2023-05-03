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

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.models.AmqpAnnotatedMessage;
import com.azure.core.amqp.models.AmqpMessageBodyType;
import com.azure.core.util.IterableStream;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusReceiverClientBuilder;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.models.DeadLetterOptions;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BHandle;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.ballerinax.asb.util.ASBConstants;
import org.ballerinax.asb.util.ASBErrorCreator;
import org.ballerinax.asb.util.ASBUtils;
import org.ballerinax.asb.util.ModuleUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.ballerinax.asb.util.ASBConstants.APPLICATION_PROPERTY_KEY;
import static org.ballerinax.asb.util.ASBConstants.BODY;
import static org.ballerinax.asb.util.ASBConstants.CONTENT_TYPE;
import static org.ballerinax.asb.util.ASBConstants.CORRELATION_ID;
import static org.ballerinax.asb.util.ASBConstants.DEAD_LETTER_ERROR_DESCRIPTION;
import static org.ballerinax.asb.util.ASBConstants.DEAD_LETTER_REASON;
import static org.ballerinax.asb.util.ASBConstants.DEAD_LETTER_SOURCE;
import static org.ballerinax.asb.util.ASBConstants.DELIVERY_COUNT;
import static org.ballerinax.asb.util.ASBConstants.ENQUEUED_SEQUENCE_NUMBER;
import static org.ballerinax.asb.util.ASBConstants.ENQUEUED_TIME;
import static org.ballerinax.asb.util.ASBConstants.LABEL;
import static org.ballerinax.asb.util.ASBConstants.LOCK_TOKEN;
import static org.ballerinax.asb.util.ASBConstants.MESSAGE_ID;
import static org.ballerinax.asb.util.ASBConstants.PARTITION_KEY;
import static org.ballerinax.asb.util.ASBConstants.RECEIVE_AND_DELETE;
import static org.ballerinax.asb.util.ASBConstants.REPLY_TO;
import static org.ballerinax.asb.util.ASBConstants.REPLY_TO_SESSION_ID;
import static org.ballerinax.asb.util.ASBConstants.SEQUENCE_NUMBER;
import static org.ballerinax.asb.util.ASBConstants.SESSION_ID;
import static org.ballerinax.asb.util.ASBConstants.STATE;
import static org.ballerinax.asb.util.ASBConstants.TIME_TO_LIVE;
import static org.ballerinax.asb.util.ASBConstants.TO;
import static org.ballerinax.asb.util.ASBUtils.addMessageFieldIfPresent;
import static org.ballerinax.asb.util.ASBUtils.convertAMQPToJava;
import static org.ballerinax.asb.util.ASBUtils.convertJavaToBValue;
import static org.ballerinax.asb.util.ASBUtils.getRetryOptions;
import static org.ballerinax.asb.util.ASBUtils.getValueWithIntendedType;

/**
 * This facilitates the client operations of MessageReceiver client in
 * Ballerina.
 */
public class MessageReceiver {

    private static final Logger LOGGER = Logger.getLogger(MessageReceiver.class);

    /**
     * Initializes the MessageReceiver client.
     *
     * @param connectionString         Azure service bus connection string.
     * @param queueName                QueueName
     * @param topicName                Topic Name
     * @param subscriptionName         Subscription Name
     * @param receiveMode              Receive Mode as PeekLock or Receive&Delete.
     * @param maxAutoLockRenewDuration Max lock renewal duration under Peek Lock mode.
     *                                 Setting to 0 disables auto-renewal.
     *                                 For RECEIVE_AND_DELETE mode, auto-renewal is disabled.
     * @throws ServiceBusException on failure initiating IMessage Receiver in Azure
     *                             Service Bus instance.
     */
    public static Object initializeReceiver(String connectionString, String queueName,
                                            String topicName, String subscriptionName,
                                            String receiveMode, long maxAutoLockRenewDuration,
                                            String logLevel, BMap<BString, Object> retryConfigs) {
        try {
            LOGGER.setLevel(Level.toLevel(logLevel, Level.OFF));
            AmqpRetryOptions retryOptions = getRetryOptions(retryConfigs);
            ServiceBusReceiverClientBuilder receiverClientBuilder = new ServiceBusClientBuilder()
                    .connectionString(connectionString)
                    .retryOptions(retryOptions)
                    .receiver();
            if (!queueName.isEmpty()) {
                if (Objects.equals(receiveMode, RECEIVE_AND_DELETE)) {
                    receiverClientBuilder.receiveMode(ServiceBusReceiveMode.RECEIVE_AND_DELETE)
                            .queueName(queueName);
                } else {
                    receiverClientBuilder.receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
                            .queueName(queueName)
                            .maxAutoLockRenewDuration(Duration.ofSeconds(maxAutoLockRenewDuration));
                }
            } else if (!subscriptionName.isEmpty() && !topicName.isEmpty()) {
                if (Objects.equals(receiveMode, RECEIVE_AND_DELETE)) {
                    receiverClientBuilder.receiveMode(ServiceBusReceiveMode.RECEIVE_AND_DELETE)
                            .topicName(topicName)
                            .subscriptionName(subscriptionName);
                } else {
                    receiverClientBuilder.receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
                            .topicName(topicName)
                            .subscriptionName(subscriptionName)
                            .maxAutoLockRenewDuration(Duration.ofSeconds(maxAutoLockRenewDuration));
                }
            }
            LOGGER.debug("ServiceBusReceiverClient initialized");
            return receiverClientBuilder.buildClient();
        } catch (BError e) {
            return ASBErrorCreator.fromBError(e);
        } catch (ServiceBusException e) {
            return ASBErrorCreator.fromASBException(e);
        } catch (Exception e) {
            return ASBErrorCreator.fromUnhandledException(e);
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
    public static Object receive(BObject endpointClient, Object serverWaitTime,
                                 BTypedesc expectedType) {
        try {
            ServiceBusReceiverClient receiver = getReceiverFromBObject(endpointClient);
            IterableStream<ServiceBusReceivedMessage> receivedMessages;
            if (serverWaitTime != null) {
                receivedMessages = receiver.receiveMessages(1, Duration.ofSeconds((long) serverWaitTime));
            } else {
                receivedMessages = receiver.receiveMessages(1);
            }

            ServiceBusReceivedMessage receivedMessage = null;
            for (ServiceBusReceivedMessage message : receivedMessages) {
                receivedMessage = message;
            }
            if (receivedMessage == null) {
                return null;
            }

            LOGGER.debug("Received message with messageId: " + receivedMessage.getMessageId());
            RecordType expectedRecordType = ASBUtils.getRecordType(expectedType);
            return constructExpectedMessageRecord(endpointClient, receivedMessage, expectedRecordType);
        } catch (BError e) {
            return ASBErrorCreator.fromBError(e);
        } catch (ServiceBusException e) {
            return ASBErrorCreator.fromASBException(e);
        } catch (Exception e) {
            return ASBErrorCreator.fromUnhandledException(e);
        }
    }

    /**
     * Returns only the message payload(i.e. body) for the given endpoint client.
     *
     * @param endpointClient Ballerina ASB client object
     * @param serverWaitTime Specified server wait time in seconds to receive message
     * @return message payload
     */

    public static Object receivePayload(BObject endpointClient, Object serverWaitTime,
                                        BTypedesc expectedType) {
        try {
            ServiceBusReceiverClient receiver = getReceiverFromBObject(endpointClient);
            IterableStream<ServiceBusReceivedMessage> receivedMessages;
            if (serverWaitTime != null) {
                receivedMessages = receiver.receiveMessages(1, Duration.ofSeconds((long) serverWaitTime));
            } else {
                receivedMessages = receiver.receiveMessages(1);
            }

            ServiceBusReceivedMessage receivedMessage = null;
            for (ServiceBusReceivedMessage message : receivedMessages) {
                receivedMessage = message;
            }
            if (receivedMessage == null) {
                return null;
            }

            LOGGER.debug("Received message with messageId: " + receivedMessage.getMessageId());
            Object messageBody = getMessagePayload(receivedMessage);
            if (messageBody instanceof byte[]) {
                return getValueWithIntendedType((byte[]) messageBody, expectedType.getDescribingType());
            } else {
                Optional<Object> bValue = convertJavaToBValue(receivedMessage.getMessageId(), messageBody);
                return bValue.orElseGet(() ->
                        ErrorCreator.createError(StringUtils.fromString("Failed to bind the received ASB message " +
                                "value to the expected Ballerina type: '" + expectedType.toString() + "'")));
            }
        } catch (BError e) {
            return ASBErrorCreator.fromBError(e);
        } catch (ServiceBusException e) {
            return ASBErrorCreator.fromASBException(e);
        } catch (Exception e) {
            return ASBErrorCreator.fromUnhandledException(e);
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
    public static Object receiveBatch(BObject endpointClient, Object maxMessageCount, Object serverWaitTime) {
        try {
            ServiceBusReceiverClient receiver = getReceiverFromBObject(endpointClient);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Waiting up to 'serverWaitTime' seconds for messages from " + receiver.getEntityPath());
            }
            return getReceivedMessageBatch(endpointClient, maxMessageCount, serverWaitTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (BError e) {
            return ASBErrorCreator.fromBError(e);
        } catch (ServiceBusException e) {
            return ASBErrorCreator.fromASBException(e);
        } catch (Exception e) {
            return ASBErrorCreator.fromUnhandledException(e);
        }
    }

    /**
     * Completes Messages from Queue or Subscription based on messageLockToken.
     *
     * @param endpointClient Ballerina ASB client object
     * @param lockToken      Message lock token.
     * @return An error if failed to complete the message.
     */
    public static Object complete(BObject endpointClient, BString lockToken) {
        try {
            ServiceBusReceiverClient receiver = getReceiverFromBObject(endpointClient);
            ServiceBusReceivedMessage message = (ServiceBusReceivedMessage) endpointClient
                    .getNativeData(lockToken.getValue());
            receiver.complete(message);
            LOGGER.debug("Completed the message(Id: " + message.getMessageId() + ") with lockToken " + lockToken);
            return null;
        } catch (BError e) {
            return ASBErrorCreator.fromBError(e);
        } catch (ServiceBusException e) {
            return ASBErrorCreator.fromASBException(e);
        } catch (Exception e) {
            return ASBErrorCreator.fromUnhandledException(e);
        }
    }

    /**
     * Abandons message & make available again for processing from Queue or Subscription, based on messageLockToken.
     *
     * @param endpointClient Ballerina ASB client object
     * @param lockToken      Message lock token.
     * @return An error if failed to abandon the message.
     */
    public static Object abandon(BObject endpointClient, BString lockToken) {
        try {
            ServiceBusReceiverClient receiver = getReceiverFromBObject(endpointClient);
            ServiceBusReceivedMessage message = (ServiceBusReceivedMessage) endpointClient
                    .getNativeData(lockToken.getValue());
            receiver.abandon(message);
            LOGGER.debug(String.format("Done abandoning a message(Id: %s) using its lock token from \n%s",
                    message.getMessageId(), receiver.getEntityPath()));
            return null;
        } catch (BError e) {
            return ASBErrorCreator.fromBError(e);
        } catch (ServiceBusException e) {
            return ASBErrorCreator.fromASBException(e);
        } catch (Exception e) {
            return ASBErrorCreator.fromUnhandledException(e);
        }
    }

    /**
     * Dead-Letter the message & moves the message to the Dead-Letter Queue based on messageLockToken.
     *
     * @param endpointClient             Ballerina ASB client object
     * @param lockToken                  Message lock token.
     * @param deadLetterReason           The dead letter reason.
     * @param deadLetterErrorDescription The dead letter error description.
     * @return An error if failed to dead letter the message.
     */
    public static Object deadLetter(BObject endpointClient, BString lockToken, Object deadLetterReason,
                                    Object deadLetterErrorDescription) {
        try {
            ServiceBusReceiverClient receiver = getReceiverFromBObject(endpointClient);
            ServiceBusReceivedMessage message = (ServiceBusReceivedMessage) endpointClient
                    .getNativeData(lockToken.getValue());
            DeadLetterOptions options = new DeadLetterOptions()
                    .setDeadLetterErrorDescription(ASBUtils.convertString(deadLetterErrorDescription));
            options.setDeadLetterReason(ASBUtils.convertString(deadLetterReason));
            receiver.deadLetter(message, options);
            LOGGER.debug(String.format("Done dead-lettering a message(Id: %s) using its lock token from %s",
                    message.getMessageId(), receiver.getEntityPath()));
            return null;
        } catch (BError e) {
            return ASBErrorCreator.fromBError(e);
        } catch (ServiceBusException e) {
            return ASBErrorCreator.fromASBException(e);
        } catch (Exception e) {
            return ASBErrorCreator.fromUnhandledException(e);
        }
    }

    /**
     * Defer the message in a Queue or Subscription based on messageLockToken.
     *
     * @param endpointClient Ballerina ASB client object
     * @param lockToken      Message lock token.
     * @return An error if failed to defer the message.
     */
    public static Object defer(BObject endpointClient, BString lockToken) {
        try {
            ServiceBusReceiverClient receiver = getReceiverFromBObject(endpointClient);
            ServiceBusReceivedMessage message = (ServiceBusReceivedMessage) endpointClient
                    .getNativeData(lockToken.getValue());
            receiver.defer(message);
            LOGGER.debug(String.format("Done deferring a message(Id: %s) using its lock token from %s",
                    message.getMessageId(), receiver.getEntityPath()));
            return null;
        } catch (BError e) {
            return ASBErrorCreator.fromBError(e);
        } catch (ServiceBusException e) {
            return ASBErrorCreator.fromASBException(e);
        } catch (Exception e) {
            return ASBErrorCreator.fromUnhandledException(e);
        }
    }

    /**
     * Receives a deferred Message. Deferred messages can only be received by using sequence number and return
     * Message object.
     *
     * @param endpointClient Ballerina ASB client object
     * @param sequenceNumber Unique number assigned to a message by Service Bus. The
     *                       sequence number is a unique 64-bit
     *                       integer assigned to a message as it is accepted and
     *                       stored by the broker and functions as
     *                       its true identifier.
     * @return The received Message or null if there is no message for given sequence number.
     */
    public static Object receiveDeferred(BObject endpointClient, int sequenceNumber) {
        try {
            ServiceBusReceiverClient receiver = getReceiverFromBObject(endpointClient);
            ServiceBusReceivedMessage receivedMessage = receiver.receiveDeferredMessage(sequenceNumber);
            if (receivedMessage == null) {
                return null;
            }
            LOGGER.debug("Received deferred message using its sequenceNumber from " + receiver.getEntityPath());
            return constructExpectedMessageRecord(endpointClient, receivedMessage, null);
        } catch (BError e) {
            return ASBErrorCreator.fromBError(e);
        } catch (ServiceBusException e) {
            return ASBErrorCreator.fromASBException(e);
        } catch (Exception e) {
            return ASBErrorCreator.fromUnhandledException(e);
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
    public static Object renewLock(BObject endpointClient, BString lockToken) {
        try {
            ServiceBusReceivedMessage message = (ServiceBusReceivedMessage) endpointClient
                    .getNativeData(lockToken.getValue());
            ServiceBusReceiverClient receiver = getReceiverFromBObject(endpointClient);
            receiver.renewMessageLock(message);
            LOGGER.debug(String.format("Done renewing a message(Id: %s) using its lock token from %s",
                    message.getMessageId(), receiver.getEntityPath()));
            return null;
        } catch (BError e) {
            return ASBErrorCreator.fromBError(e);
        } catch (ServiceBusException e) {
            return ASBErrorCreator.fromASBException(e);
        } catch (Exception e) {
            return ASBErrorCreator.fromUnhandledException(e);
        }
    }

    /**
     * Closes the Asb Receiver Connection using the given connection parameters.
     *
     * @return An error if failed to close the receiver.
     */
    public static Object closeReceiver(BObject endpointClient) {
        try {
            ServiceBusReceiverClient receiver = getReceiverFromBObject(endpointClient);
            receiver.close();
            LOGGER.debug("Closed the receiver");
            return null;
        } catch (BError e) {
            return ASBErrorCreator.fromBError(e);
        } catch (ServiceBusException e) {
            return ASBErrorCreator.fromASBException(e);
        } catch (Exception e) {
            return ASBErrorCreator.fromUnhandledException(e);
        }
    }

    /**
     * Converts the received message to the contextually expected Ballerina record type (or to anydata, if not
     * specified).
     *
     * @param endpointClient Ballerina client object
     * @param message        Received Message
     */
    private static BMap<BString, Object> constructExpectedMessageRecord(BObject endpointClient,
                                                                        ServiceBusReceivedMessage message,
                                                                        RecordType expectedType) {
        Map<String, Object> map = populateOptionalFieldsMap(message);
        Object messageBody = getMessagePayload(message);
        if (messageBody instanceof byte[]) {
            if (expectedType != null) {
                map.put(BODY, getValueWithIntendedType((byte[]) messageBody, expectedType.getFields().get(BODY)
                        .getFieldType()));
            } else {
                map.put(BODY, getValueWithIntendedType((byte[]) messageBody, PredefinedTypes.TYPE_ANYDATA));
            }
        } else {
            map.put(BODY, messageBody);
        }
        endpointClient.addNativeData(message.getLockToken(), message);
        return createBRecordValue(map, expectedType);
    }

    private static Map<String, Object> populateOptionalFieldsMap(ServiceBusReceivedMessage message) {
        Map<String, Object> map = new HashMap<>();
        addMessageFieldIfPresent(map, CONTENT_TYPE, message.getContentType());
        addMessageFieldIfPresent(map, MESSAGE_ID, message.getMessageId());
        addMessageFieldIfPresent(map, TO, message.getTo());
        addMessageFieldIfPresent(map, REPLY_TO, message.getReplyTo());
        addMessageFieldIfPresent(map, REPLY_TO_SESSION_ID, message.getReplyToSessionId());
        addMessageFieldIfPresent(map, LABEL, message.getSubject());
        addMessageFieldIfPresent(map, SESSION_ID, message.getSessionId());
        addMessageFieldIfPresent(map, CORRELATION_ID, message.getCorrelationId());
        addMessageFieldIfPresent(map, PARTITION_KEY, message.getPartitionKey());
        addMessageFieldIfPresent(map, TIME_TO_LIVE, message.getTimeToLive().getSeconds());
        addMessageFieldIfPresent(map, SEQUENCE_NUMBER, message.getSequenceNumber());
        addMessageFieldIfPresent(map, LOCK_TOKEN, message.getLockToken());
        addMessageFieldIfPresent(map, DELIVERY_COUNT, message.getDeliveryCount());
        addMessageFieldIfPresent(map, ENQUEUED_TIME, message.getEnqueuedTime().toString());
        addMessageFieldIfPresent(map, ENQUEUED_SEQUENCE_NUMBER, message.getEnqueuedSequenceNumber());
        addMessageFieldIfPresent(map, DEAD_LETTER_ERROR_DESCRIPTION, message.getDeadLetterErrorDescription());
        addMessageFieldIfPresent(map, DEAD_LETTER_REASON, message.getDeadLetterReason());
        addMessageFieldIfPresent(map, DEAD_LETTER_SOURCE, message.getDeadLetterSource());
        addMessageFieldIfPresent(map, STATE, message.getState().toString());
        addMessageFieldIfPresent(map, APPLICATION_PROPERTY_KEY, getApplicationProperties(message));

        return map;
    }

    private static BMap<BString, Object> createBRecordValue(Map<String, Object> map, RecordType recordType) {
        if (recordType == null) {
            return ValueCreator.createRecordValue(ModuleUtils.getModule(), ASBConstants.MESSAGE_RECORD, map);
        } else {
            return ValueCreator.createRecordValue(recordType.getPackage(), recordType.getName(), map);
        }
    }

    /**
     * Prepares the message body content.
     *
     * @param receivedMessage ASB received message
     */
    private static Object getMessagePayload(ServiceBusReceivedMessage receivedMessage) {
        AmqpAnnotatedMessage rawAmqpMessage = receivedMessage.getRawAmqpMessage();
        AmqpMessageBodyType bodyType = rawAmqpMessage.getBody().getBodyType();
        switch (bodyType) {
            case DATA:
                return rawAmqpMessage.getBody().getFirstData();
            case VALUE:
                LOGGER.debug(String.format("Received a message with messageId: %s and AMQPMessageBodyType: %s",
                        receivedMessage.getMessageId(), bodyType));
                Object amqpValue = rawAmqpMessage.getBody().getValue();
                amqpValue = convertAMQPToJava(receivedMessage.getMessageId(), amqpValue);
                return amqpValue;
            default:
                throw new RuntimeException("Unsupported message body type: " + receivedMessage.getMessageId());
        }
    }

    private static BMap<BString, Object> getReceivedMessageBatch(BObject endpointClient, Object maxMessageCount,
                                                                 Object serverWaitTime)
            throws InterruptedException, ServiceBusException {
        ServiceBusReceiverClient receiver = getReceiverFromBObject(endpointClient);
        int maxCount = Long.valueOf(maxMessageCount.toString()).intValue();
        Object[] messages = new Object[maxCount];
        IterableStream<ServiceBusReceivedMessage> receivedMessageStream;
        if (serverWaitTime != null) {
            receivedMessageStream = receiver.receiveMessages(maxCount, Duration.ofSeconds((long) serverWaitTime));
        } else {
            receivedMessageStream = receiver.receiveMessages(maxCount);
        }

        int messageCount = 0;
        for (ServiceBusReceivedMessage receivedMessage : receivedMessageStream) {
            BMap<BString, Object> recordMap = constructExpectedMessageRecord(endpointClient, receivedMessage, null);
            messages[messageCount++] = ValueCreator.createRecordValue(ModuleUtils.getModule(),
                    ASBConstants.MESSAGE_RECORD, recordMap);
        }

        BMap<BString, Object> messageRecord = ValueCreator.createRecordValue(ModuleUtils.getModule(),
                ASBConstants.MESSAGE_RECORD);
        ArrayType sourceArrayType = TypeCreator.createArrayType(TypeUtils.getType(messageRecord));

        Map<String, Object> map = new HashMap<>();
        map.put("messageCount", messageCount);
        map.put("messages", ValueCreator.createArrayValue(messages, sourceArrayType));
        return ValueCreator.createRecordValue(ModuleUtils.getModule(), ASBConstants.MESSAGE_BATCH_RECORD, map);
    }

    private static BMap<BString, Object> getApplicationProperties(ServiceBusReceivedMessage message) {
        BMap<BString, Object> applicationPropertiesRecord = ValueCreator.createRecordValue(ModuleUtils.getModule(),
                ASBConstants.APPLICATION_PROPERTY_TYPE);
        MapType mapType = TypeCreator.createMapType(PredefinedTypes.TYPE_ANYDATA);
        BMap<BString, Object> applicationProperties = ValueCreator.createMapValue(mapType);
        for (Map.Entry<String, Object> property : message.getApplicationProperties().entrySet()) {
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
            applicationProperties.put(propertyKey, value);
        } else if (value instanceof Long) {
            applicationProperties.put(propertyKey, value);
        } else if (value instanceof Float) {
            applicationProperties.put(propertyKey, value);
        } else if (value instanceof Double) {
            applicationProperties.put(propertyKey, value);
        } else if (value instanceof Boolean) {
            applicationProperties.put(propertyKey, value);
        } else if (value instanceof Character) {
            applicationProperties.put(propertyKey, value);
        } else if (value instanceof Byte) {
            applicationProperties.put(propertyKey, value);
        } else if (value instanceof Short) {
            applicationProperties.put(propertyKey, value);
        } else {
            applicationProperties.put(propertyKey, StringUtils.fromString(value.toString()));
        }
    }

    private static ServiceBusReceiverClient getReceiverFromBObject(BObject receiverObject) {
        BHandle receiverHandle = (BHandle) receiverObject.get(StringUtils.fromString("receiverHandle"));
        return (ServiceBusReceiverClient) receiverHandle.getValue();
    }
}
