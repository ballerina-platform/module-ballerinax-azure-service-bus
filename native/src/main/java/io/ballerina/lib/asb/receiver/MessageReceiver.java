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

package io.ballerina.lib.asb.receiver;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.models.AmqpAnnotatedMessage;
import com.azure.core.amqp.models.AmqpMessageBodyType;
import com.azure.core.util.IterableStream;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.models.DeadLetterOptions;
import io.ballerina.lib.asb.util.ASBConstants;
import io.ballerina.lib.asb.util.ASBErrorCreator;
import io.ballerina.lib.asb.util.ASBUtils;
import io.ballerina.lib.asb.util.ModuleUtils;
import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.types.PredefinedTypes;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.runtime.api.creators.ValueCreator.createRecordValue;

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
            AmqpRetryOptions retryOptions = ASBUtils.getRetryOptions(retryConfigs);
            ServiceBusReceiverClient nativeReceiverClient = ASBUtils.constructReceiverClient(
                    retryOptions, connectionString, queueName, receiveMode, maxAutoLockRenewDuration, topicName,
                    subscriptionName, false);
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
    public static Object receive(Environment env, BObject receiverClient, Object serverWaitTime, BTypedesc expectedType,
                                 boolean deadLettered) {
        ServiceBusReceiverClient receiver = getNativeReceiver(receiverClient, deadLettered);
        return env.yieldAndRun(() -> {
            try {
                List<ServiceBusReceivedMessage> messages = receiver
                        .receiveMessages(1, Duration.ofSeconds((long) serverWaitTime))
                        .stream().toList();
                if (messages.isEmpty()) {
                    return null;
                }
                ServiceBusReceivedMessage message = messages.get(0);
                RecordType expectedRecordType = ASBUtils.getRecordType(expectedType);
                return constructExpectedMessageRecord(message, expectedRecordType);
            } catch (BError e) {
                return ASBErrorCreator.fromBError(e);
            } catch (ServiceBusException e) {
                return ASBErrorCreator.fromASBException(e);
            } catch (Exception e) {
                return ASBErrorCreator.fromUnhandledException(e);
            }
        });
    }

    /**
     * Returns only the message payload(i.e. body) for the given endpoint client.
     *
     * @param receiverClient Ballerina ASB client object
     * @param serverWaitTime Specified server wait time in seconds to receive message
     * @return message payload
     */
    public static Object receivePayload(Environment env, BObject receiverClient, Object serverWaitTime,
                                        BTypedesc expectedType, boolean deadLettered) {
        ServiceBusReceiverClient receiver = getNativeReceiver(receiverClient, deadLettered);
        return env.yieldAndRun(() -> {
            try {
                List<ServiceBusReceivedMessage> messages = receiver
                        .receiveMessages(1, Duration.ofSeconds((long) serverWaitTime))
                        .stream().toList();
                if (messages.isEmpty()) {
                    return null;
                }
                ServiceBusReceivedMessage message = messages.get(0);

                Object messageBody = getMessagePayload(message);
                if (messageBody instanceof byte[] binaryPayload) {
                    return ASBUtils.getValueWithIntendedType(binaryPayload,
                            expectedType.getDescribingType());
                } else {
                    Optional<Object> bValue = ASBUtils.convertJavaToBValue(message.getMessageId(), messageBody);
                    String payloadBindingErr = String.format(
                            "Failed to bind the received ASB message value to the expected Ballerina type: '%s'",
                            expectedType.toString());
                    return bValue.orElseGet(() -> ErrorCreator.createError(
                            StringUtils.fromString(payloadBindingErr)));
                }
            } catch (BError e) {
                return ASBErrorCreator.fromBError(e);
            } catch (ServiceBusException e) {
                return ASBErrorCreator.fromASBException(e);
            } catch (Exception e) {
                return ASBErrorCreator.fromUnhandledException(e);
            }
        });
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
    public static Object receiveBatch(Environment env, BObject receiverClient, long maxMessageCount,
                                      Object serverWaitTime, boolean deadLettered) {
        ServiceBusReceiverClient receiver = getNativeReceiver(receiverClient, deadLettered);
        return env.yieldAndRun(() -> {
            try {
                IterableStream<ServiceBusReceivedMessage> receivedMessageStream;
                if (Objects.isNull(serverWaitTime)) {
                    receivedMessageStream = receiver.receiveMessages((int) maxMessageCount);
                } else {
                    receivedMessageStream = receiver.receiveMessages(
                            (int) maxMessageCount, Duration.ofSeconds((long) serverWaitTime));
                }
                List<BMap<BString, Object>> bMessages = receivedMessageStream.stream().map(msg -> {
                    BMap<BString, Object> bMsg = constructExpectedMessageRecord(msg, null);
                    bMsg.addNativeData(ASBConstants.NATIVE_MESSAGE, msg);
                    return bMsg;
                }).toList();
                BMap<BString, Object> messageRecord = ValueCreator.createRecordValue(ModuleUtils.getModule(),
                        ASBConstants.MESSAGE_RECORD);
                ArrayType sourceArrayType = TypeCreator.createArrayType(TypeUtils.getType(messageRecord));

                Map<String, Object> value = new HashMap<>();
                value.put("messageCount", bMessages.size());
                value.put("messages", ValueCreator.createArrayValue(bMessages.toArray(new Object[0]), sourceArrayType));
                return createRecordValue(
                        ModuleUtils.getModule(), ASBConstants.MESSAGE_BATCH_RECORD, value);
            } catch (BError e) {
                return ASBErrorCreator.fromBError(e);
            } catch (ServiceBusException e) {
                return ASBErrorCreator.fromASBException(e);
            } catch (Exception e) {
                return ASBErrorCreator.fromUnhandledException(e);
            }
        });
    }

    /**
     * Completes Messages from Queue or Subscription based on messageLockToken.
     *
     * @param receiverClient Ballerina ASB client object
     * @param message        Message object.
     * @return An error if failed to complete the message.
     */
    public static Object complete(Environment env, BObject receiverClient, BMap<BString, Object> message) {
        ServiceBusReceivedMessage nativeMessage = getNativeMessage(message);
        ServiceBusReceiverClient receiver = getNativeReceiver(
                receiverClient, Objects.nonNull(nativeMessage.getDeadLetterReason()));
        return env.yieldAndRun(() -> {
            try {
                receiver.complete(nativeMessage);
                return null;
            } catch (BError e) {
                return ASBErrorCreator.fromBError(e);
            } catch (ServiceBusException e) {
                return ASBErrorCreator.fromASBException(e);
            } catch (Exception e) {
                return ASBErrorCreator.fromUnhandledException(e);
            }
        });
    }

    /**
     * Abandons message & make available again for processing from Queue or Subscription, based on messageLockToken.
     *
     * @param receiverClient Ballerina ASB client object
     * @param message        Message object.
     * @return An error if failed to abandon the message.
     */
    public static Object abandon(Environment env, BObject receiverClient, BMap<BString, Object> message) {
        ServiceBusReceivedMessage nativeMessage = getNativeMessage(message);
        ServiceBusReceiverClient receiver = getNativeReceiver(receiverClient, false);
        return env.yieldAndRun(() -> {
            try {
                receiver.abandon(nativeMessage);
                return null;
            } catch (BError e) {
                return ASBErrorCreator.fromBError(e);
            } catch (ServiceBusException e) {
                return ASBErrorCreator.fromASBException(e);
            } catch (Exception e) {
                return ASBErrorCreator.fromUnhandledException(e);
            }
        });
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
    public static Object deadLetter(Environment env, BObject receiverClient, BMap<BString, Object> message,
                                    Object deadLetterReason, Object deadLetterErrorDescription) {
        ServiceBusReceivedMessage nativeMessage = getNativeMessage(message);
        ServiceBusReceiverClient receiver = getNativeReceiver(receiverClient, false);
        return env.yieldAndRun(() -> {
            try {
                DeadLetterOptions options = new DeadLetterOptions()
                        .setDeadLetterErrorDescription(ASBUtils.convertString(deadLetterErrorDescription));
                options.setDeadLetterReason(ASBUtils.convertString(deadLetterReason));
                receiver.deadLetter(nativeMessage, options);
                return null;
            } catch (BError e) {
                return ASBErrorCreator.fromBError(e);
            } catch (ServiceBusException e) {
                return ASBErrorCreator.fromASBException(e);
            } catch (Exception e) {
                return ASBErrorCreator.fromUnhandledException(e);
            }
        });
    }

    /**
     * Defer the message in a Queue or Subscription based on messageLockToken.
     *
     * @param receiverClient Ballerina ASB client object
     * @param message        Message object.
     * @return An error if failed to defer the message.
     */
    public static Object defer(Environment env, BObject receiverClient, BMap<BString, Object> message) {
        ServiceBusReceivedMessage nativeMessage = getNativeMessage(message);
        ServiceBusReceiverClient receiver = getNativeReceiver(receiverClient, false);
        return env.yieldAndRun(() -> {
            try {
                receiver.defer(nativeMessage);
                return null;
            } catch (BError e) {
                return ASBErrorCreator.fromBError(e);
            } catch (ServiceBusException e) {
                return ASBErrorCreator.fromASBException(e);
            } catch (Exception e) {
                return ASBErrorCreator.fromUnhandledException(e);
            }
        });
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
    public static Object receiveDeferred(Environment env, BObject receiverClient, long sequenceNumber) {
        ServiceBusReceiverClient receiver = getNativeReceiver(receiverClient, false);
        return env.yieldAndRun(() -> {
            try {
                ServiceBusReceivedMessage message = receiver.receiveDeferredMessage(sequenceNumber);
                if (Objects.isNull(message)) {
                    return null;
                }
                return constructExpectedMessageRecord(message, null);
            } catch (BError e) {
                return ASBErrorCreator.fromBError(e);
            } catch (ServiceBusException e) {
                return ASBErrorCreator.fromASBException(e);
            } catch (Exception e) {
                return ASBErrorCreator.fromUnhandledException(e);
            }
        });
    }

    /**
     * The operation renews lock on a message in a queue or subscription based on
     * messageLockToken.
     *
     * @param receiverClient Ballerina ASB client object
     * @param message        Message object.
     * @return An error if failed to renewLock of the message.
     */
    public static Object renewLock(Environment env, BObject receiverClient, BMap<BString, Object> message) {
        ServiceBusReceivedMessage nativeMessage = getNativeMessage(message);
        ServiceBusReceiverClient receiver = getNativeReceiver(receiverClient, false);
        return env.yieldAndRun(() -> {
            try {
                receiver.renewMessageLock(nativeMessage);
                return null;
            } catch (BError e) {
                return ASBErrorCreator.fromBError(e);
            } catch (ServiceBusException e) {
                return ASBErrorCreator.fromASBException(e);
            } catch (Exception e) {
                return ASBErrorCreator.fromUnhandledException(e);
            }
        });
    }

    /**
     * Closes the Asb Receiver Connection using the given connection parameters.
     *
     * @return An error if failed to close the receiver.
     */
    public static Object closeReceiver(Environment env, BObject receiverClient) {
        ServiceBusReceiverClient receiver = getNativeReceiver(receiverClient, false);
        return env.yieldAndRun(() -> {
            try {
                receiver.close();
                return null;
            } catch (BError e) {
                return ASBErrorCreator.fromBError(e);
            } catch (ServiceBusException e) {
                return ASBErrorCreator.fromASBException(e);
            } catch (Exception e) {
                return ASBErrorCreator.fromUnhandledException(e);
            }
        });
    }

    /**
     * Converts the received message to the contextually expected Ballerina record type (or to anydata, if not
     * specified).
     *
     * @param message        Received Message
     */
    private static BMap<BString, Object> constructExpectedMessageRecord(ServiceBusReceivedMessage message,
                                                                       RecordType expectedType) {
        Map<String, Object> map = populateOptionalFieldsMap(message);
        Object messageBody = getMessagePayload(message);
        if (messageBody instanceof byte[]) {
            if (expectedType != null) {
                map.put(ASBConstants.BODY, ASBUtils.getValueWithIntendedType((byte[]) messageBody,
                        expectedType.getFields().get(ASBConstants.BODY)
                        .getFieldType()));
            } else {
                map.put(ASBConstants.BODY, ASBUtils.getValueWithIntendedType((byte[]) messageBody,
                        PredefinedTypes.TYPE_ANYDATA));
            }
        } else {
            map.put(ASBConstants.BODY, messageBody);
        }
        BMap<BString, Object> constructedMessage = createBRecordValue(map, expectedType);
        // Only add the native message if the message received in peek lock mode.
        if (!message.getLockToken().equals(ASBConstants.DEFAULT_MESSAGE_LOCK_TOKEN)) {
            constructedMessage.addNativeData(ASBConstants.NATIVE_MESSAGE, message);
        }
        return constructedMessage;
    }

    public static Map<String, Object> populateOptionalFieldsMap(ServiceBusReceivedMessage message) {
        Map<String, Object> map = new HashMap<>();
        ASBUtils.addFieldIfPresent(map, ASBConstants.CONTENT_TYPE, message.getContentType());
        ASBUtils.addFieldIfPresent(map, ASBConstants.MESSAGE_ID, message.getMessageId());
        ASBUtils.addFieldIfPresent(map, ASBConstants.TO, message.getTo());
        ASBUtils.addFieldIfPresent(map, ASBConstants.REPLY_TO, message.getReplyTo());
        ASBUtils.addFieldIfPresent(map, ASBConstants.REPLY_TO_SESSION_ID, message.getReplyToSessionId());
        ASBUtils.addFieldIfPresent(map, ASBConstants.LABEL, message.getSubject());
        ASBUtils.addFieldIfPresent(map, ASBConstants.SESSION_ID, message.getSessionId());
        ASBUtils.addFieldIfPresent(map, ASBConstants.CORRELATION_ID, message.getCorrelationId());
        ASBUtils.addFieldIfPresent(map, ASBConstants.PARTITION_KEY, message.getPartitionKey());
        ASBUtils.addFieldIfPresent(map, ASBConstants.TIME_TO_LIVE, message.getTimeToLive().getSeconds());
        ASBUtils.addFieldIfPresent(map, ASBConstants.SEQUENCE_NUMBER, message.getSequenceNumber());
        ASBUtils.addFieldIfPresent(map, ASBConstants.LOCK_TOKEN, message.getLockToken());
        ASBUtils.addFieldIfPresent(map, ASBConstants.DELIVERY_COUNT, message.getDeliveryCount());
        ASBUtils.addFieldIfPresent(map, ASBConstants.ENQUEUED_TIME, message.getEnqueuedTime().toString());
        ASBUtils.addFieldIfPresent(map, ASBConstants.ENQUEUED_SEQUENCE_NUMBER, message.getEnqueuedSequenceNumber());
        ASBUtils.addFieldIfPresent(map, ASBConstants.DEAD_LETTER_ERROR_DESCRIPTION,
                message.getDeadLetterErrorDescription());
        ASBUtils.addFieldIfPresent(map, ASBConstants.DEAD_LETTER_REASON, message.getDeadLetterReason());
        ASBUtils.addFieldIfPresent(map, ASBConstants.DEAD_LETTER_SOURCE, message.getDeadLetterSource());
        ASBUtils.addFieldIfPresent(map, ASBConstants.STATE, message.getState().toString());
        ASBUtils.addFieldIfPresent(map, ASBConstants.APPLICATION_PROPERTY_KEY, getApplicationProperties(message));

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
    public static Object getMessagePayload(ServiceBusReceivedMessage receivedMessage) {
        AmqpAnnotatedMessage rawAmqpMessage = receivedMessage.getRawAmqpMessage();
        AmqpMessageBodyType bodyType = rawAmqpMessage.getBody().getBodyType();
        switch (bodyType) {
            case DATA:
                return rawAmqpMessage.getBody().getFirstData();
            case VALUE:
                LOGGER.debug(String.format("Received a message with messageId: %s and AMQPMessageBodyType: %s",
                        receivedMessage.getMessageId(), bodyType));
                Object amqpValue = rawAmqpMessage.getBody().getValue();
                amqpValue = ASBUtils.convertAMQPToJava(receivedMessage.getMessageId(), amqpValue);
                return amqpValue;
            default:
                throw new RuntimeException("Unsupported message body type: " + receivedMessage.getMessageId());
        }
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

    private static ServiceBusReceiverClient getNativeReceiver(BObject bReceiver, boolean isDeadLetter) {
        if (isDeadLetter) {
            return (ServiceBusReceiverClient) getDeadLetterMessageReceiverFromBObject(bReceiver);
        }
        return (ServiceBusReceiverClient) bReceiver.getNativeData(ASBConstants.RECEIVER_CLIENT);
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
                AmqpRetryOptions retryOptions = ASBUtils.getRetryOptions(retryConfigs);
                ServiceBusReceiverClient nativeReceiverClient = ASBUtils.constructReceiverClient(retryOptions,
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
        return (ServiceBusReceivedMessage) message.getNativeData(ASBConstants.NATIVE_MESSAGE);
    }
}
