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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.asb.connection;

import com.microsoft.azure.servicebus.*;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.types.BArrayType;
import org.ballerinalang.asb.ASBUtils;
import org.ballerinalang.asb.ModuleUtils;

import java.time.Duration;
import java.util.*;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.ballerinalang.asb.ASBConstants.*;

/**
 * Util class used to bridge the Asb connector's native code and the Ballerina API.
 */
public class ConnectionUtils {
    private static final Logger log = Logger.getLogger(ConnectionUtils.class.getName());

    private String connectionString;

    /**
     * Convert BMap to String Map.
     *
     * @param map Input BMap used to convert to Map.
     * @return Converted Map object.
     */
    public static Map<String, Object> toStringMap(BMap map) {
        Map<String, Object> returnMap = new HashMap<>();
        if (map != null) {
            for (Object aKey : map.getKeys()) {
                returnMap.put(aKey.toString(), map.get(aKey).toString());
            }
        }
        return returnMap;
    }

    /**
     * Convert BMap to Object Map.
     *
     * @param map Input BMap used to convert to Map.
     * @return Converted Map object.
     */
    public static Map<String, Object> toObjectMap(BMap map) {
        Map<String, Object> returnMap = new HashMap<>();
        if (map != null) {
            for (Object aKey : map.getKeys()) {
                returnMap.put(aKey.toString(), map.get(aKey));
            }
        }
        return returnMap;
    }

    /**
     * Convert Map to BMap.
     *
     * @param map Input Map used to convert to BMap.
     * @return Converted BMap object.
     */
    public static BMap<BString, Object> toBMap(Map map) {
        BMap<BString, Object> returnMap = ValueCreator.createMapValue();
        if (map != null) {
            for (Object aKey : map.keySet().toArray()) {
                returnMap.put(StringUtils.fromString(aKey.toString()),
                        StringUtils.fromString(map.get(aKey).toString()));
            }
        }
        return returnMap;
    }

    /**
     * Creates a Asb Sender Connection using the given connection parameters.
     *
     * @param connectionString Azure Service Bus Primary key string used to initialize the connection.
     * @param entityPath Resource entity path.
     * @return Asb Sender Connection object.
     */
    public static Object createSender(String connectionString, String entityPath) {
        try {
            IMessageSender sender = ClientFactory.createMessageSenderFromConnectionStringBuilder(
                    new ConnectionStringBuilder(connectionString, entityPath));
            return sender;
        } catch (InterruptedException | ServiceBusException e) {
            return ASBUtils.returnErrorValue("Current thread was interrupted while waiting: "
                    + e.getMessage());
        } catch (Exception e) {
            return ASBUtils.returnErrorValue("Sender Connection Creation Failed: " + e.getMessage());
        }
    }

    /**
     * Closes the Asb Sender Connection using the given connection parameters.
     *
     * @param sender Created IMessageSender instance used to close the connection.
     */
    public static Object closeSender(IMessageSender sender) {
        try {
            sender.close();
        } catch (ServiceBusException e) {
            return ASBUtils.returnErrorValue("object cannot be properly closed " + e.getMessage());
        }
        return null;
    }

    /**
     * Creates a Asb Receiver Connection using the given connection parameters.
     *
     * @param connectionString Primary key string used to initialize the connection.
     * @param entityPath Resource entity path.
     * @return Asb Receiver Connection object.
     */
    public static Object createReceiver(String connectionString, String entityPath, Object receiveMode) {
        try {
            IMessageReceiver receiver;
            if (receiveMode.toString() == RECEIVEANDDELETE) {
                receiver = ClientFactory.createMessageReceiverFromConnectionStringBuilder(
                        new ConnectionStringBuilder(connectionString, entityPath), ReceiveMode.RECEIVEANDDELETE);
            } else {
                receiver = ClientFactory.createMessageReceiverFromConnectionStringBuilder(
                        new ConnectionStringBuilder(connectionString, entityPath), ReceiveMode.PEEKLOCK);
            }
            return receiver;
        } catch (InterruptedException | ServiceBusException e) {
            return ASBUtils.returnErrorValue("Current thread was interrupted while waiting: "
                    + e.getMessage());
        } catch (Exception e) {
            return ASBUtils.returnErrorValue("Receiver Connection Creation Failed: " + e.getMessage());
        }
    }

    /**
     * Closes the Asb Receiver Connection using the given connection parameters.
     *
     * @param receiver Created IMessageReceiver instance used to close the connection.
     */
    public static Object closeReceiver(IMessageReceiver receiver) {
        try {
            receiver.close();
        } catch (ServiceBusException e) {
            return ASBUtils.returnErrorValue("object cannot be properly closed " + e.getMessage());
        }
        return null;
    }

    /**
     * Send Message with configurable parameters when Sender Connection is given as a parameter and
     * message content as a byte array.
     *
     * @param sender Input Sender connection.
     * @param body Input message content as byte array
     * @param contentType Input message content type
     * @param messageId Input Message Id
     * @param to Input Message to
     * @param replyTo Input Message reply to
     * @param replyToSessionId Identifier of the session to reply to
     * @param label Input Message label
     * @param sessionId Input Message session Id
     * @param correlationId Input Message correlationId
     * @param properties Input Message properties
     * @param timeToLive Input Message time to live in minutes
     */
    public static Object send(IMessageSender sender, Object body, Object contentType, Object messageId,
                              Object to, Object replyTo, Object replyToSessionId, Object label, Object sessionId,
                              Object correlationId, Object partitionKey, Object timeToLive, Object properties) {
        try {
            // Send message to queue or topic
            log.info("\tSending messages to ...\n" + sender.getEntityPath());
            IMessage message = new Message();
            byte[] byteArray = ((BArray) body).getBytes();
            message.setBody(byteArray);
//            message.setMessageBody(new Message(byteArray).getMessageBody()); // Will use in next release
            message.setContentType(valueToEmptyOrToString(contentType));
            message.setMessageId(valueToEmptyOrToString(messageId));
            message.setTo(valueToEmptyOrToString(to));
            message.setReplyTo(valueToEmptyOrToString(replyTo));
            message.setReplyToSessionId(valueToEmptyOrToString(replyToSessionId));
            message.setLabel(valueToEmptyOrToString(label));
            message.setSessionId(valueToEmptyOrToString(sessionId));
            message.setCorrelationId(valueToEmptyOrToString(correlationId));
            message.setPartitionKey(valueToEmptyOrToString(partitionKey));
            if (timeToLive != null) {
                message.setTimeToLive(Duration.ofSeconds((long) timeToLive));
            }
            Map<String, Object> map = toStringMap((BMap) properties);
            message.setProperties(map);

            sender.send(message);
            log.info("\t=> Sent a message with messageId \n" + message.getMessageId());
        } catch (InterruptedException | ServiceBusException e) {
            return ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        } catch (Exception e) {
            return ASBUtils.returnErrorValue("Exception occurred " + e.getMessage());
        }
        return null;
    }

    /**
     * Send Batch of Messages with configurable parameters when Sender Connection is given as a parameter and
     * batch message record as a BMap.
     *
     * @param sender Input Sender connection.
     * @param messages Input batch message record as a BMap
     */
    public static Object sendBatch(IMessageSender sender, BMap<BString, Object> messages) {
        try {
            // Send batch of messages to queue or topic
            log.info("\tSending messages to ...\n" + sender.getEntityPath());
            Map<String, Object> messagesMap = toObjectMap((BMap) messages);
            long messageCount = (long) messagesMap.get("messageCount");
            BArray messageArray = (BArray) messagesMap.get("messages");

            Collection<IMessage> messageBatch = new ArrayList<>();

            for (int i = 0; i < messageArray.getLength(); i++) {
                BMap messageBMap = (BMap) messageArray.get(i);
                Map<String, Object> messageMap = toObjectMap(messageBMap);

                IMessage message = new Message();
                byte[] byteArray = ((BArray) messageMap.get(BODY)).getBytes();
                message.setBody(byteArray);
                message.setContentType(valueToStringOrEmpty(messageMap, CONTENT_TYPE));
                message.setMessageId(valueToEmptyOrToString(messageMap.get(MESSAGE_ID)));
                message.setTo(valueToStringOrEmpty(messageMap, TO));
                message.setReplyTo(valueToStringOrEmpty(messageMap, REPLY_TO));
                message.setReplyToSessionId(valueToStringOrEmpty(messageMap, REPLY_TO_SESSION_ID));
                message.setLabel(valueToStringOrEmpty(messageMap, LABEL));
                message.setSessionId(valueToStringOrEmpty(messageMap, SESSION_ID));
                message.setCorrelationId(valueToStringOrEmpty(messageMap, CORRELATION_ID));
                message.setPartitionKey(valueToStringOrEmpty(messageMap, PARTITION_KEY));
                if (messageMap.get(TIME_TO_LIVE) != null) {
                    message.setTimeToLive(Duration.ofSeconds((long) messageMap.get(TIME_TO_LIVE)));
                }
                Map<String, Object> map = toStringMap((BMap) messageMap.get(APPLICATION_PROPERTIES));
                message.setProperties(map);

                messageBatch.add(message);
            }
            sender.sendBatch(messageBatch);
        } catch (InterruptedException | ServiceBusException e) {
            return ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        } catch (Exception e) {
            return ASBUtils.returnErrorValue("Exception occurred " + e.getMessage());
        }
        return null;
    }

    /**
     * Receive Message with configurable parameters as Map when Receiver Connection is given as a parameter and
     * server wait time in seconds to receive message and return Message object.
     *
     * @param receiver Output Receiver connection.
     * @param serverWaitTime Specified server wait time in seconds to receive message.
     * @return Message Object of the received message.
     */
    public static Object receive(IMessageReceiver receiver, Object serverWaitTime) {
        try {
            // receive message from queue or subscription
            log.info("\n\tWaiting up to 'serverWaitTime' seconds for messages from\n" + receiver.getEntityPath());

            IMessage receivedMessage;
            if (serverWaitTime != null) {
                receivedMessage = receiver.receive(Duration.ofSeconds((long) serverWaitTime));
            } else {
                receivedMessage = receiver.receive();
            }

            if (receivedMessage == null) {
                return null;
            }
            log.info("\t<= Received a message with messageId \n" + receivedMessage.getMessageId());
            log.info("\t<= Received a message with messageBody \n" +
                    new String(receivedMessage.getBody(), UTF_8));

            log.info("\tDone receiving messages from \n" + receiver.getEntityPath());

            Object[] values = new Object[14];
//            values[0] = ValueCreator.createArrayValue(receivedMessage.getMessageBody().getBinaryData().get(0));
            values[0] = ValueCreator.createArrayValue(receivedMessage.getBody());
            values[1] = StringUtils.fromString(receivedMessage.getContentType());
            values[2] = StringUtils.fromString(receivedMessage.getMessageId());
            values[3] = StringUtils.fromString(receivedMessage.getTo());
            values[4] = StringUtils.fromString(receivedMessage.getReplyTo());
            values[5] = StringUtils.fromString(receivedMessage.getReplyToSessionId());
            values[6] = StringUtils.fromString(receivedMessage.getLabel());
            values[7] = StringUtils.fromString(receivedMessage.getSessionId());
            values[8] = StringUtils.fromString(receivedMessage.getCorrelationId());
            values[9] = StringUtils.fromString(receivedMessage.getPartitionKey());
            values[10] = receivedMessage.getTimeToLive().getSeconds();
            values[11] = receivedMessage.getSequenceNumber();
            values[12] = StringUtils.fromString(receivedMessage.getLockToken().toString());
            BMap<BString, Object> applicationProperties =
                    ValueCreator.createRecordValue(ModuleUtils.getModule(), APPLICATION_PROPERTIES);
            Object[] propValues = new Object[1];
            propValues[0] = toBMap(receivedMessage.getProperties());
            values[13] = ValueCreator.createRecordValue(applicationProperties, propValues);
            BMap<BString, Object> messageRecord =
                    ValueCreator.createRecordValue(ModuleUtils.getModule(), MESSAGE_RECORD);
            return ValueCreator.createRecordValue(messageRecord, values);
        } catch (InterruptedException | ServiceBusException e) {
            return ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        } catch (Exception e) {
            return ASBUtils.returnErrorValue("Exception occurred " + e.getMessage());
        }
    }

    /**
     * Receive Batch of Messages with configurable parameters as Map when Receiver Connection is given as a parameter,
     * maximum message count in a batch as int, server wait time in seconds and return Batch Message object.
     *
     * @param receiver Output Receiver connection.
     * @param maxMessageCount Maximum no. of messages in a batch
     * @return Batch Message Object of the received batch of messages.
     */
    public static Object receiveBatch(IMessageReceiver receiver, Object maxMessageCount, Object serverWaitTime) {
        try {
            // receive batch of messages from queue or subscription
            log.info("\n\tWaiting up to 'serverWaitTime' seconds for messages from\n" + receiver.getEntityPath());

            int messageCount = 0;
            int maxCount = Long.valueOf(maxMessageCount.toString()).intValue();
            BArrayType sourceArrayType = null;

            Object[] messagesRecordValues = new Object[2];
            Object[] messages = new Object[maxCount];

            Collection<IMessage> receivedMessages;
            if (serverWaitTime != null) {
                receivedMessages = receiver.receiveBatch(maxCount, Duration.ofSeconds((long) serverWaitTime));
            } else {
                receivedMessages = receiver.receiveBatch(maxCount);
            }

            if (receivedMessages == null) {
                return null;
            }

            BMap<BString, Object> applicationProperties =
                    ValueCreator.createRecordValue(ModuleUtils.getModule(), APPLICATION_PROPERTIES);
            BMap<BString, Object> messageRecord =
                    ValueCreator.createRecordValue(ModuleUtils.getModule(), MESSAGE_RECORD);

            for (IMessage receivedMessage : receivedMessages) {
                Object[] values = new Object[14];
                values[0] = ValueCreator.createArrayValue(receivedMessage.getBody());
                values[1] = StringUtils.fromString(receivedMessage.getContentType());
                values[2] = StringUtils.fromString(receivedMessage.getMessageId());
                values[3] = StringUtils.fromString(receivedMessage.getTo());
                values[4] = StringUtils.fromString(receivedMessage.getReplyTo());
                values[5] = StringUtils.fromString(receivedMessage.getReplyToSessionId());
                values[6] = StringUtils.fromString(receivedMessage.getLabel());
                values[7] = StringUtils.fromString(receivedMessage.getSessionId());
                values[8] = StringUtils.fromString(receivedMessage.getCorrelationId());
                values[9] = StringUtils.fromString(receivedMessage.getPartitionKey());
                values[10] = receivedMessage.getTimeToLive().getSeconds();
                values[11] = receivedMessage.getSequenceNumber();
                values[12] = StringUtils.fromString(receivedMessage.getLockToken().toString());

                Object[] propValues = new Object[1];
                propValues[0] = toBMap(receivedMessage.getProperties());
                values[13] = ValueCreator.createRecordValue(applicationProperties, propValues);

                messages[messageCount] = ValueCreator.createRecordValue(messageRecord, values);
                messageCount = messageCount + 1;
            }
            sourceArrayType = new BArrayType(TypeUtils.getType(messageRecord));
            messagesRecordValues[0] = messageCount;
            messagesRecordValues[1] = ValueCreator.createArrayValue(messages, sourceArrayType);

            BMap<BString, Object> messagesRecord =
                    ValueCreator.createRecordValue(ModuleUtils.getModule(), MESSAGE_BATCH_RECORD);
            return ValueCreator.createRecordValue(messagesRecord, messagesRecordValues);
        } catch (InterruptedException | ServiceBusException e) {
            return ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        } catch (Exception e) {
            return ASBUtils.returnErrorValue("Exception occurred " + e.getMessage());
        }
    }

    /**
     * Complete Messages from Queue or Subscription based on messageLockToken
     *
     * @param receiver Output Receiver connection.
     * @param lockToken Message lock token.
     */
    public static Object complete(IMessageReceiver receiver, Object lockToken) {
        try {
            log.info("\t<= Completes a message with messageLockToken \n" + lockToken);
            receiver.complete(UUID.fromString(lockToken.toString()));
            log.info("\tDone completing a message using its lock token from \n" +
                    receiver.getEntityPath());
        } catch (InterruptedException | ServiceBusException e) {
            return ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        } catch (Exception e) {
            return ASBUtils.returnErrorValue("Exception occurred " + e.getMessage());
        }
        return null;
    }

    /**
     * Abandon message & make available again for processing from Queue or Subscription based on messageLockToken
     *
     * @param receiver Output Receiver connection.
     * @param lockToken Message lock token.
     */
    public static Object abandon(IMessageReceiver receiver, Object lockToken) {
        try {
            log.info("\t<= Abandon a message with messageLockToken \n" + lockToken);
            receiver.abandon(UUID.fromString(lockToken.toString()));
            log.info("\tDone abandoning a message using its lock token from \n" +
                    receiver.getEntityPath());
        } catch (InterruptedException | ServiceBusException e) {
            return ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        } catch (Exception e) {
            return ASBUtils.returnErrorValue("Exception occurred " + e.getMessage());
        }
        return null;
    }

    /**
     * Dead-Letter the message & moves the message to the Dead-Letter Queue based on messageLockToken
     *
     * @param receiver Output Receiver connection.
     * @param lockToken Message lock token.
     * @param deadLetterReason The dead letter reason.
     * @param deadLetterErrorDescription The dead letter error description.
     */
    public static Object deadLetter(IMessageReceiver receiver, Object lockToken, Object deadLetterReason,
                                    Object deadLetterErrorDescription) {
        try {
            log.info("\t<= Dead-Letter a message with messageLockToken \n" + lockToken);
            receiver.deadLetter(UUID.fromString(lockToken.toString()), valueToEmptyOrToString(deadLetterReason),
                    valueToEmptyOrToString(deadLetterErrorDescription));
            log.info("\tDone dead-lettering a message using its lock token from \n" +
                    receiver.getEntityPath());
        } catch (InterruptedException | ServiceBusException e) {
            return ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        } catch (Exception e) {
            return ASBUtils.returnErrorValue("Exception occurred " + e.getMessage());
        }
        return null;
    }

    /**
     * Defer the message in a Queue or Subscription based on messageLockToken
     *
     * @param receiver Output Receiver connection.
     * @param lockToken Message lock token.
     */
    public static Object defer(IMessageReceiver receiver, Object lockToken) {
        try {
            log.info("\t<= Defer a message with messageLockToken \n" + lockToken);
            receiver.defer(UUID.fromString(lockToken.toString()));
            log.info("\tDone deferring a message using its lock token from \n" +
                    receiver.getEntityPath());
        } catch (InterruptedException | ServiceBusException e) {
            return ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        } catch (Exception e) {
            return ASBUtils.returnErrorValue("Exception occurred " + e.getMessage());
        }
        return null;
    }

    /**
     * Receives a deferred Message. Deferred messages can only be received by using sequence number and return
     * Message object.
     *
     * @param receiver Output Receiver connection.
     * @param sequenceNumber Unique number assigned to a message by Service Bus. The sequence number is a unique 64-bit
     *                       integer assigned to a message as it is accepted and stored by the broker and functions as
     *                       its true identifier.
     * @return The received Message or null if there is no message for given sequence number.
     */
    public static Object receiveDeferred(IMessageReceiver receiver, int sequenceNumber) {
        try {
            log.info("\n\tWaiting up to default server Wait Time for messages from\n" + receiver.getEntityPath());

            IMessage receivedMessage = receiver.receiveDeferredMessage(sequenceNumber);

            if (receivedMessage == null) {
                return null;
            }
            log.info("\t<= Received a message with messageId \n" + receivedMessage.getMessageId());
            log.info("\t<= Received a message with messageBody \n" +
                    new String(receivedMessage.getBody(), UTF_8));

            log.info("\tDone receiving messages from \n" + receiver.getEntityPath());

            Object[] values = new Object[14];
            values[0] = ValueCreator.createArrayValue(receivedMessage.getBody());
            values[1] = StringUtils.fromString(receivedMessage.getContentType());
            values[2] = StringUtils.fromString(receivedMessage.getMessageId());
            values[3] = StringUtils.fromString(receivedMessage.getTo());
            values[4] = StringUtils.fromString(receivedMessage.getReplyTo());
            values[5] = StringUtils.fromString(receivedMessage.getReplyToSessionId());
            values[6] = StringUtils.fromString(receivedMessage.getLabel());
            values[7] = StringUtils.fromString(receivedMessage.getSessionId());
            values[8] = StringUtils.fromString(receivedMessage.getCorrelationId());
            values[9] = StringUtils.fromString(receivedMessage.getPartitionKey());
            values[10] = receivedMessage.getTimeToLive().getSeconds();
            values[11] = receivedMessage.getSequenceNumber();
            values[12] = StringUtils.fromString(receivedMessage.getLockToken().toString());
            BMap<BString, Object> applicationProperties =
                    ValueCreator.createRecordValue(ModuleUtils.getModule(), APPLICATION_PROPERTIES);
            Object[] propValues = new Object[1];
            propValues[0] = toBMap(receivedMessage.getProperties());
            values[13] = ValueCreator.createRecordValue(applicationProperties, propValues);
            BMap<BString, Object> messageRecord =
                    ValueCreator.createRecordValue(ModuleUtils.getModule(), MESSAGE_RECORD);
            return ValueCreator.createRecordValue(messageRecord, values);
        } catch (InterruptedException | ServiceBusException e) {
            return ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        } catch (Exception e) {
            return ASBUtils.returnErrorValue("Exception occurred " + e.getMessage());
        }
    }

    /**
     * The operation renews lock on a message in a queue or subscription based on messageLockToken.
     *
     * @param receiver Output Receiver connection.
     * @param lockToken Message lock token.
     */
    public static Object renewLock(IMessageReceiver receiver, Object lockToken) {
        try {
            log.info("\t<= Renew message with messageLockToken \n" + lockToken);
            receiver.renewMessageLock(UUID.fromString(lockToken.toString()));
            log.info("\tDone renewing a message using its lock token from \n" +
                    receiver.getEntityPath());
        } catch (InterruptedException | ServiceBusException e) {
            return ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        } catch (Exception e) {
            return ASBUtils.returnErrorValue("Exception occurred " + e.getMessage());
        }
        return null;
    }

    /**
     * Set the prefetch count of the receiver.
     * Prefetch speeds up the message flow by aiming to have a message readily available for local retrieval when and
     * before the application asks for one using Receive. Setting a non-zero value prefetches PrefetchCount
     * number of messages. Setting the value to zero turns prefetch off. For both PEEKLOCK mode and
     * RECEIVEANDDELETE mode, the default value is 0.
     *
     * @param receiver Output Receiver connection.
     * @param prefetchCount The desired prefetch count.
     */
    public static Object setPrefetchCount(IMessageReceiver receiver, int prefetchCount) {
        try {
            receiver.setPrefetchCount(prefetchCount);
        } catch (ServiceBusException e) {
            return ASBUtils.returnErrorValue("Setting the prefetch value failed" + e.getMessage());
        }
        return null;
    }

    /**
     * Get the map value as string or as empty based on the key.
     *
     * @param map Input map.
     * @param key Input key.
     * @return map value as a string or empty.
     */
    private static String valueToStringOrEmpty(Map<String, ?> map, String key) {
        Object value = map.get(key);
        return value == null ? null : value.toString();
    }

    /**
     * Get the value as string or as empty based on the object value.
     *
     * @param value Input value.
     * @return value as a string or empty.
     */
    private static String valueToEmptyOrToString(Object value) {
        return (value == null || value.toString() == "") ? null : value.toString();
    }

    public ConnectionUtils() {
    }

    public ConnectionUtils(String connectionString) {
        this.connectionString = connectionString;
    }
}
