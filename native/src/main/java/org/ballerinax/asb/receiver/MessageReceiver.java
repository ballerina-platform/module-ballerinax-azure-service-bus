/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org).
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
 * KINDither express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinax.asb.receiver;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.models.AmqpAnnotatedMessage;
import com.azure.core.amqp.models.AmqpMessageBodyType;
import com.azure.core.util.IterableStream;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.models.DeadLetterOptions;
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
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;
import org.ballerinax.asb.util.ASBConstants;
import org.ballerinax.asb.util.ASBErrorCreator;
import org.ballerinax.asb.util.ASBUtils;
import org.ballerinax.asb.util.ModuleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.runtime.api.creators.ValueCreator.createRecordValue;
import static org.ballerinax.asb.util.ASBConstants.APPLICATION_PROPERTY_KEY;
import static org.ballerinax.asb.util.ASBConstants.BODY;
import static org.ballerinax.asb.util.ASBConstants.CONTENT_TYPE;
import static org.ballerinax.asb.util.ASBConstants.CORRELATION_ID;
import static org.ballerinax.asb.util.ASBConstants.DEAD_LETTER_ERROR_DESCRIPTION;
import static org.ballerinax.asb.util.ASBConstants.DEAD_LETTER_REASON;
import static org.ballerinax.asb.util.ASBConstants.DEAD_LETTER_SOURCE;
import static org.ballerinax.asb.util.ASBConstants.DEFAULT_MESSAGE_LOCK_TOKEN;
import static org.ballerinax.asb.util.ASBConstants.DELIVERY_COUNT;
import static org.ballerinax.asb.util.ASBConstants.ENQUEUED_SEQUENCE_NUMBER;
import static org.ballerinax.asb.util.ASBConstants.ENQUEUED_TIME;
import static org.ballerinax.asb.util.ASBConstants.LABEL;
import static org.ballerinax.asb.util.ASBConstants.LOCK_TOKEN;
import static org.ballerinax.asb.util.ASBConstants.MESSAGE_ID;
import static org.ballerinax.asb.util.ASBConstants.NATIVE_MESSAGE;
import static org.ballerinax.asb.util.ASBConstants.PARTITION_KEY;
import static org.ballerinax.asb.util.ASBConstants.RECEIVER_CLIENT;
import static org.ballerinax.asb.util.ASBConstants.REPLY_TO;
import static org.ballerinax.asb.util.ASBConstants.REPLY_TO_SESSION_ID;
import static org.ballerinax.asb.util.ASBConstants.SEQUENCE_NUMBER;
import static org.ballerinax.asb.util.ASBConstants.SESSION_ID;
import static org.ballerinax.asb.util.ASBConstants.STATE;
import static org.ballerinax.asb.util.ASBConstants.TIME_TO_LIVE;
import static org.ballerinax.asb.util.ASBConstants.TO;
import static org.ballerinax.asb.util.ASBUtils.addFieldIfPresent;
import static org.ballerinax.asb.util.ASBUtils.constructReceiverClient;
import static org.ballerinax.asb.util.ASBUtils.convertAMQPToJava;
import static org.ballerinax.asb.util.ASBUtils.convertJavaToBValue;
import static org.ballerinax.asb.util.ASBUtils.getRetryOptions;
import static org.ballerinax.asb.util.ASBUtils.getValueWithIntendedType;

/**
 * This facilitates the client operations of MessageReceiver client in
 * Ballerina.
 */
public class MessageReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageReceiver.class);

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
    public static Object initializeReceiver(BObject receiverClient, String connectionString, String queueName,
                                            String topicName, String subscriptionName,
                                            String receiveMode, long maxAutoLockRenewDuration,
                                            String logLevel, BMap<BString, Object> retryConfigs) {
        try {
            AmqpRetryOptions retryOptions = getRetryOptions(retryConfigs);
            ServiceBusReceiverClient nativeReceiverClient = constructReceiverClient(retryOptions, connectionString,
                    queueName, receiveMode, maxAutoLockRenewDuration, topicName, subscriptionName, false);
            setClientData(receiverClient, connectionString, queueName, topicName, subscriptionName, receiveMode,
                    maxAutoLockRenewDuration, logLevel, retryConfigs);
            setClient(receiverClient, nativeReceiverClient, false);
            LOGGER.debug("ServiceBusReceiverClient initialized");
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
     * Receive Message with configurable parameters as Map when Receiver Connection
     * is given as a parameter and
     * server wait time in seconds to receive message and return Message object.
     *
     * @param receiverClient Ballerina ASB client object
     * @param serverWaitTime Specified server wait time in seconds to receive
     *                       message.
     * @return Message Object of the received message.
     */
    public static Object receive(BObject receiverClient, Object serverWaitTime,
                                 BTypedesc expectedType, Object deadLettered) {
        try {
            ServiceBusReceiverClient receiver;
            if ((boolean) deadLettered) {
                receiver = (ServiceBusReceiverClient) getDeadLetterMessageReceiverFromBObject(receiverClient);
            } else {
                receiver = getReceiverFromBObject(receiverClient);
            }
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
            return constructExpectedMessageRecord(receiverClient, receivedMessage, expectedRecordType);
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
     * @param receiverClient Ballerina ASB client object
     * @param serverWaitTime Specified server wait time in seconds to receive message
     * @return message payload
     */

    public static Object receivePayload(BObject receiverClient, Object serverWaitTime,
                                        BTypedesc expectedType, Object deadLettered) {
        try {
            ServiceBusReceiverClient receiver;
            if ((boolean) deadLettered) {
                receiver = (ServiceBusReceiverClient) getDeadLetterMessageReceiverFromBObject(receiverClient);
            } else {
                receiver = getReceiverFromBObject(receiverClient);
            }
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
     * @param receiverClient  Ballerina ASB client object
     * @param maxMessageCount Maximum no. of messages in a batch.
     * @param serverWaitTime  Server wait time.
     * @return Batch Message Object of the received batch of messages.
     */
    public static Object receiveBatch(BObject receiverClient, Object maxMessageCount, Object serverWaitTime
            , Object deadLettered) {
        try {
            ServiceBusReceiverClient receiver;
            if ((boolean) deadLettered) {
                receiver = (ServiceBusReceiverClient) getDeadLetterMessageReceiverFromBObject(receiverClient);
            } else {
                receiver = getReceiverFromBObject(receiverClient);
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Waiting up to 'serverWaitTime' seconds for messages from " + receiver.getEntityPath());
            }
            return getReceivedMessageBatch(receiverClient, maxMessageCount, serverWaitTime, deadLettered);
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
     * @param receiverClient Ballerina ASB client object
     * @param message        Message object.
     * @return An error if failed to complete the message.
     */
    public static Object complete(BObject receiverClient, BMap<BString, Object> message) {
        try {
            ServiceBusReceivedMessage nativeMessage = getNativeMessage(message);
            ServiceBusReceiverClient receiver;
            if (nativeMessage.getDeadLetterReason() != null) {
                receiver = (ServiceBusReceiverClient) getDeadLetterMessageReceiverFromBObject(receiverClient);
            } else {
                receiver = getReceiverFromBObject(receiverClient);
            }
            receiver.complete(nativeMessage);
            LOGGER.debug("Completed the message(Id: " + nativeMessage.getMessageId() + ") with lockToken " +
                    nativeMessage.getLockToken());
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
     * @param receiverClient Ballerina ASB client object
     * @param message        Message object.
     * @return An error if failed to abandon the message.
     */
    public static Object abandon(BObject receiverClient, BMap<BString, Object> message) {
        try {
            ServiceBusReceiverClient receiver = getReceiverFromBObject(receiverClient);
            ServiceBusReceivedMessage nativeMessage = getNativeMessage(message);
            receiver.abandon(nativeMessage);
            LOGGER.debug(String.format("Done abandoning a message(Id: %s) using its lock token from %n%s",
                    nativeMessage.getMessageId(), receiver.getEntityPath()));
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
     * @param receiverClient             Ballerina ASB client object
     * @param message                    Message object.
     * @param deadLetterReason           The dead letter reason.
     * @param deadLetterErrorDescription The dead letter error description.
     * @return An error if failed to dead letter the message.
     */
    public static Object deadLetter(BObject receiverClient, BMap<BString, Object> message, Object deadLetterReason,
                                    Object deadLetterErrorDescription) {
        try {
            ServiceBusReceiverClient receiver = getReceiverFromBObject(receiverClient);
            ServiceBusReceivedMessage nativeMessage = getNativeMessage(message);
            DeadLetterOptions options = new DeadLetterOptions()
                    .setDeadLetterErrorDescription(ASBUtils.convertString(deadLetterErrorDescription));
            options.setDeadLetterReason(ASBUtils.convertString(deadLetterReason));
            receiver.deadLetter(nativeMessage, options);
            LOGGER.debug(String.format("Done dead-lettering a message(Id: %s) using its lock token from %s",
                    nativeMessage.getMessageId(), receiver.getEntityPath()));
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
     * @param receiverClient Ballerina ASB client object
     * @param message        Message object.
     * @return An error if failed to defer the message.
     */
    public static Object defer(BObject receiverClient, BMap<BString, Object> message) {
        try {
            ServiceBusReceiverClient receiver = getReceiverFromBObject(receiverClient);
            ServiceBusReceivedMessage nativeMessage = getNativeMessage(message);
            receiver.defer(nativeMessage);
            LOGGER.debug(String.format("Done deferring a message(Id: %s) using its lock token from %s",
                    nativeMessage.getMessageId(), receiver.getEntityPath()));
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
     * @param receiverClient Ballerina ASB client object
     * @param sequenceNumber Unique number assigned to a message by Service Bus. The
     *                       sequence number is a unique 64-bit
     *                       integer assigned to a message as it is accepted and
     *                       stored by the broker and functions as
     *                       its true identifier.
     * @return The received Message or null if there is no message for given sequence number.
     */
    public static Object receiveDeferred(BObject receiverClient, Object sequenceNumber) {
        try {
            ServiceBusReceiverClient receiver = getReceiverFromBObject(receiverClient);
            ServiceBusReceivedMessage receivedMessage = receiver.receiveDeferredMessage((long) sequenceNumber);
            if (receivedMessage == null) {
                return null;
            }
            LOGGER.debug("Received deferred message using its sequenceNumber from " + receiver.getEntityPath());
            return constructExpectedMessageRecord(receiverClient, receivedMessage, null);
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
     * @param receiverClient Ballerina ASB client object
     * @param message        Message object.
     * @return An error if failed to renewLock of the message.
     */
    public static Object renewLock(BObject receiverClient, BMap<BString, Object> message) {
        try {
            ServiceBusReceiverClient receiver = getReceiverFromBObject(receiverClient);
            ServiceBusReceivedMessage nativeMessage = getNativeMessage(message);
            receiver.renewMessageLock(nativeMessage);
            LOGGER.debug(String.format("Done renewing a message(Id: %s) using its lock token from %s",
                    nativeMessage.getMessageId(), receiver.getEntityPath()));
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
    public static Object closeReceiver(BObject receiverClient) {
        try {
            ServiceBusReceiverClient receiver = getReceiverFromBObject(receiverClient);
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
     * @param receiverClient Ballerina client object
     * @param message        Received Message
     */
    private static BMap<BString, Object> constructExpectedMessageRecord(BObject receiverClient,
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
        BMap<BString, Object> constructedMessage = createBRecordValue(map, expectedType);
        // Only add the native message if the message received in peek lock mode.
        if (!message.getLockToken().equals(DEFAULT_MESSAGE_LOCK_TOKEN)) {
            constructedMessage.addNativeData(NATIVE_MESSAGE, message);
        }
        return constructedMessage;
    }

    private static Map<String, Object> populateOptionalFieldsMap(ServiceBusReceivedMessage message) {
        Map<String, Object> map = new HashMap<>();
        addFieldIfPresent(map, CONTENT_TYPE, message.getContentType());
        addFieldIfPresent(map, MESSAGE_ID, message.getMessageId());
        addFieldIfPresent(map, TO, message.getTo());
        addFieldIfPresent(map, REPLY_TO, message.getReplyTo());
        addFieldIfPresent(map, REPLY_TO_SESSION_ID, message.getReplyToSessionId());
        addFieldIfPresent(map, LABEL, message.getSubject());
        addFieldIfPresent(map, SESSION_ID, message.getSessionId());
        addFieldIfPresent(map, CORRELATION_ID, message.getCorrelationId());
        addFieldIfPresent(map, PARTITION_KEY, message.getPartitionKey());
        addFieldIfPresent(map, TIME_TO_LIVE, message.getTimeToLive().getSeconds());
        addFieldIfPresent(map, SEQUENCE_NUMBER, message.getSequenceNumber());
        addFieldIfPresent(map, LOCK_TOKEN, message.getLockToken());
        addFieldIfPresent(map, DELIVERY_COUNT, message.getDeliveryCount());
        addFieldIfPresent(map, ENQUEUED_TIME, message.getEnqueuedTime().toString());
        addFieldIfPresent(map, ENQUEUED_SEQUENCE_NUMBER, message.getEnqueuedSequenceNumber());
        addFieldIfPresent(map, DEAD_LETTER_ERROR_DESCRIPTION, message.getDeadLetterErrorDescription());
        addFieldIfPresent(map, DEAD_LETTER_REASON, message.getDeadLetterReason());
        addFieldIfPresent(map, DEAD_LETTER_SOURCE, message.getDeadLetterSource());
        addFieldIfPresent(map, STATE, message.getState().toString());
        addFieldIfPresent(map, APPLICATION_PROPERTY_KEY, getApplicationProperties(message));

        return map;
    }

    private static BMap<BString, Object> createBRecordValue(Map<String, Object> map, RecordType recordType) {
        if (recordType == null) {
            return createRecordValue(ModuleUtils.getModule(), ASBConstants.MESSAGE_RECORD, map);
        } else {
            return createRecordValue(recordType.getPackage(), recordType.getName(), map);
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

    private static BMap<BString, Object> getReceivedMessageBatch(BObject receiverClient, Object maxMessageCount,
                                                                 Object serverWaitTime, Object deadLettered)
            throws InterruptedException, ServiceBusException {
        ServiceBusReceiverClient receiver;
        if ((boolean) deadLettered) {
            receiver = (ServiceBusReceiverClient) getDeadLetterMessageReceiverFromBObject(receiverClient);
        } else {
            receiver = getReceiverFromBObject(receiverClient);
        }
        int maxCount = Long.valueOf(maxMessageCount.toString()).intValue();
        IterableStream<ServiceBusReceivedMessage> receivedMessageStream;
        if (serverWaitTime != null) {
            receivedMessageStream = receiver.receiveMessages(maxCount, Duration.ofSeconds((long) serverWaitTime));
        } else {
            receivedMessageStream = receiver.receiveMessages(maxCount);
        }

        LinkedList<Object> receivedMessages = new LinkedList<>();
        for (ServiceBusReceivedMessage receivedMessage : receivedMessageStream) {
            BMap<BString, Object> recordMap = constructExpectedMessageRecord(receiverClient, receivedMessage, null);
            BMap<BString, Object> messageRecord = createRecordValue(ModuleUtils.getModule(),
                    ASBConstants.MESSAGE_RECORD, recordMap);
            messageRecord.addNativeData(NATIVE_MESSAGE, receivedMessage);
            receivedMessages.add(messageRecord);
        }

        BMap<BString, Object> messageRecord = ValueCreator.createRecordValue(ModuleUtils.getModule(),
                ASBConstants.MESSAGE_RECORD);
        ArrayType sourceArrayType = TypeCreator.createArrayType(TypeUtils.getType(messageRecord));

        Map<String, Object> map = new HashMap<>();
        map.put("messageCount", receivedMessages.size());
        map.put("messages", ValueCreator.createArrayValue(receivedMessages.toArray(new Object[0]), sourceArrayType));
        return createRecordValue(ModuleUtils.getModule(), ASBConstants.MESSAGE_BATCH_RECORD, map);
    }

    private static BMap<BString, Object> getApplicationProperties(ServiceBusReceivedMessage message) {
        BMap<BString, Object> applicationPropertiesRecord = createRecordValue(ModuleUtils.getModule(),
                ASBConstants.APPLICATION_PROPERTY_TYPE);
        MapType mapType = TypeCreator.createMapType(PredefinedTypes.TYPE_ANYDATA);
        BMap<BString, Object> applicationProperties = ValueCreator.createMapValue(mapType);
        for (Map.Entry<String, Object> property : message.getApplicationProperties().entrySet()) {
            populateApplicationProperty(applicationProperties, property.getKey(), property.getValue());
        }
        return createRecordValue(applicationPropertiesRecord, applicationProperties);
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
        return (ServiceBusReceiverClient) receiverObject.getNativeData(RECEIVER_CLIENT);
    }

    private static Object getDeadLetterMessageReceiverFromBObject(BObject receiverObject) {
        if (receiverObject.getNativeData(ASBConstants.DEAD_LETTER_RECEIVER_CLIENT) != null) {
            return receiverObject.getNativeData(ASBConstants.DEAD_LETTER_RECEIVER_CLIENT);
        } else {
            String connectionString = (String) receiverObject.getNativeData(
                    ASBConstants.RECEIVER_CLIENT_CONNECTION_STRING);
            String queueName = (String) receiverObject.getNativeData(ASBConstants.RECEIVER_CLIENT_QUEUE_NAME);
            String topicName = (String) receiverObject.getNativeData(ASBConstants.RECEIVER_CLIENT_TOPIC_NAME);
            String subscriptionName = (String) receiverObject.getNativeData(
                    ASBConstants.RECEIVER_CLIENT_SUBSCRIPTION_NAME);
            String receiveMode = (String) receiverObject.getNativeData(ASBConstants.RECEIVER_CLIENT_RECEIVE_MODE);
            long maxAutoLockRenewDuration = (long) receiverObject.getNativeData(
                    ASBConstants.RECEIVER_CLIENT_MAX_AUTO_LOCK_RENEW_DURATION);
            BMap<BString, Object> retryConfigs =
                    (BMap<BString, Object>) receiverObject.getNativeData(ASBConstants.RECEIVER_CLIENT_RETRY_CONFIGS);
            try {
                AmqpRetryOptions retryOptions = getRetryOptions(retryConfigs);
                ServiceBusReceiverClient nativeReceiverClient = constructReceiverClient(retryOptions,
                        connectionString, queueName, receiveMode, maxAutoLockRenewDuration, topicName,
                        subscriptionName, true);
                LOGGER.debug("ServiceBusReceiverClient initialized");
                setClient(receiverObject, nativeReceiverClient, true);
                return nativeReceiverClient;
            } catch (BError e) {
                return ASBErrorCreator.fromBError(e);
            } catch (ServiceBusException e) {
                return ASBErrorCreator.fromASBException(e);
            } catch (Exception e) {
                return ASBErrorCreator.fromUnhandledException(e);
            }
        }
    }

    private static void setClientData(BObject receiverObject, String connectionString, String queueName,
                                      String topicName, String subscriptionName,
                                      String receiveMode, long maxAutoLockRenewDuration,
                                      String logLevel, BMap<BString, Object> retryConfigs) {
        receiverObject.addNativeData(ASBConstants.RECEIVER_CLIENT_CONNECTION_STRING, connectionString);
        receiverObject.addNativeData(ASBConstants.RECEIVER_CLIENT_QUEUE_NAME, queueName);
        receiverObject.addNativeData(ASBConstants.RECEIVER_CLIENT_TOPIC_NAME, topicName);
        receiverObject.addNativeData(ASBConstants.RECEIVER_CLIENT_SUBSCRIPTION_NAME, subscriptionName);
        receiverObject.addNativeData(ASBConstants.RECEIVER_CLIENT_RECEIVE_MODE, receiveMode);
        receiverObject.addNativeData(ASBConstants.RECEIVER_CLIENT_MAX_AUTO_LOCK_RENEW_DURATION,
                maxAutoLockRenewDuration);
        receiverObject.addNativeData(ASBConstants.RECEIVER_CLIENT_LOG_LEVEL, logLevel);
        receiverObject.addNativeData(ASBConstants.RECEIVER_CLIENT_RETRY_CONFIGS, retryConfigs);
    }

    private static void setClient(BObject receiverObject, ServiceBusReceiverClient client, boolean isDeadLetter) {
        if (isDeadLetter) {
            receiverObject.addNativeData(ASBConstants.DEAD_LETTER_RECEIVER_CLIENT, client);
        } else {
            receiverObject.addNativeData(ASBConstants.RECEIVER_CLIENT, client);
        }
    }

    private static ServiceBusReceivedMessage getNativeMessage(BMap<BString, Object> message) {
        return (ServiceBusReceivedMessage) message.getNativeData(NATIVE_MESSAGE);
    }
}
