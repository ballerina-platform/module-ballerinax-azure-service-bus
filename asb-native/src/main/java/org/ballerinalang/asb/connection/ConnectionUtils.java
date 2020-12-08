/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.asb.ASBConstants;
import org.ballerinalang.asb.ASBUtils;
import org.ballerinalang.jvm.api.values.BArray;
import org.ballerinalang.jvm.api.values.BMap;
import org.ballerinalang.jvm.api.values.BObject;
import org.ballerinalang.jvm.api.BValueCreator;
import org.ballerinalang.jvm.types.BArrayType;

import java.time.Duration;
import java.time.Instant;
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
     * Creates a Asb Sender Connection using the given connection parameters.
     *
     * @param connectionString Azure Service Bus Primary key string used to initialize the connection.
     * @param entityPath Resource entity path.
     * @return Asb Sender Connection object.
     */
    public static IMessageSender createSenderConnection(String connectionString, String entityPath) throws Exception {
        try {
            IMessageSender sender = ClientFactory.createMessageSenderFromConnectionStringBuilder(
                    new ConnectionStringBuilder(connectionString, entityPath));
            return sender;
        } catch (InterruptedException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        } catch (ServiceBusException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        }
    }

    /**
     * Closes the Asb Sender Connection using the given connection parameters.
     *
     * @param sender Created IMessageSender instance used to close the connection.
     */
    public static void closeSenderConnection(IMessageSender sender) throws Exception {
        try {
            sender.close();
        } catch (ServiceBusException e) {
            throw ASBUtils.returnErrorValue("object cannot be properly closed " + e.getMessage());
        }
    }

    /**
     * Creates a Asb Receiver Connection using the given connection parameters.
     *
     * @param connectionString Primary key string used to initialize the connection.
     * @param entityPath Resource entity path.
     * @return Asb Receiver Connection object.
     */
    public static IMessageReceiver createReceiverConnection(String connectionString, String entityPath)
            throws Exception {
        try {
            IMessageReceiver receiver = ClientFactory.createMessageReceiverFromConnectionStringBuilder(
                    new ConnectionStringBuilder(connectionString, entityPath), ReceiveMode.PEEKLOCK);
            return receiver;
        } catch (InterruptedException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        } catch (ServiceBusException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        }
    }

    /**
     * Closes the Asb Receiver Connection using the given connection parameters.
     *
     * @param receiver Created IMessageReceiver instance used to close the connection.
     */
    public static void closeReceiverConnection(IMessageReceiver receiver) throws Exception {
        try {
            receiver.close();
        } catch (ServiceBusException e) {
            throw ASBUtils.returnErrorValue("object cannot be properly closed " + e.getMessage());
        }
    }

    /**
     * Convert BMap to Map.
     *
     * @param map Input BMap used to convert to Map.
     * @return Converted Map object.
     */
    public static Map<String, String> toStringMap(BMap map) {
        Map<String, String> returnMap = new HashMap<>();
        if (map != null) {
            for (Object aKey : map.getKeys()) {
                returnMap.put(aKey.toString(), map.get(aKey).toString());
            }
        }
        return returnMap;
    }

    /**
     * Send Message with configurable parameters when Sender Connection is given as a parameter and
     * message content as a byte array.
     *
     * @param sender Input Sender connection.
     * @param content Input message content as byte array
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
    public static void sendMessage(IMessageSender sender, BArray content, String contentType, String messageId,
                                   String to, String replyTo, String replyToSessionId, String label, String sessionId,
                                   String correlationId, BMap<String, String> properties, int timeToLive)
            throws Exception {
        try {
            // Send messages to queue
            log.info("\tSending messages to ...\n" + sender.getEntityPath());
            IMessage message = new Message();
            message.setMessageId(messageId);
            message.setTimeToLive(Duration.ofMinutes(timeToLive));
            byte[] byteArray = content.getBytes();
            message.setBody(byteArray);
            message.setContentType(contentType);
            message.setMessageId(messageId);
            message.setTo(to);
            message.setReplyTo(replyTo);
            message.setReplyToSessionId(replyToSessionId);
            message.setLabel(label);
            message.setSessionId(sessionId);
            message.setCorrelationId(correlationId);
            Map<String,String> map = toStringMap(properties);
            message.setProperties(map);

            sender.send(message);
            log.info("\t=> Sent a message with messageId \n" + message.getMessageId());
        } catch (InterruptedException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        } catch (ServiceBusException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        }
    }

    /**
     * Send Message with configurable parameters when Sender Connection is given as a parameter and
     * message content as a byte array and optional parameters as a BMap.
     *
     * @param sender Input Sender connection.
     * @param content Input message content as byte array
     * @param parameters Input message optional parameters specified as a BMap
     * @param properties Input Message properties
     */
    public static void sendMessageWithConfigurableParameters(IMessageSender sender, BArray content,
                                                             BMap<String, String> parameters,
                                                             BMap<String, String> properties) throws Exception {
        Map<String,String> map = toStringMap(parameters);

        String contentType = valueToStringOrEmpty(map, CONTENT_TYPE);
        String messageId = map.get(MESSAGE_ID) != null ? map.get(MESSAGE_ID) : UUID.randomUUID().toString();
        String to = valueToStringOrEmpty(map, TO);
        String replyTo = valueToStringOrEmpty(map, REPLY_TO);
        String replyToSessionId = valueToStringOrEmpty(map, REPLY_TO_SESSION_ID);
        String label = valueToStringOrEmpty(map,LABEL);
        String sessionId = valueToStringOrEmpty(map, SESSION_ID);
        String correlationId = valueToStringOrEmpty(map, CORRELATION_ID);
        int timeToLive = map.get(TIME_TO_LIVE) != null ? Integer.parseInt(map.get(TIME_TO_LIVE)) : DEFAULT_TIME_TO_LIVE;

        try {
            // Send messages to queue
            log.info("\tSending messages to ...\n" + sender.getEntityPath());
            IMessage message = new Message();
            message.setMessageId(messageId);
            message.setTimeToLive(Duration.ofMinutes(timeToLive));
            byte[] byteArray = content.getBytes();
            message.setBody(byteArray);
            message.setContentType(contentType);
            message.setMessageId(messageId);
            message.setTo(to);
            message.setReplyTo(replyTo);
            message.setReplyToSessionId(replyToSessionId);
            message.setLabel(label);
            message.setSessionId(sessionId);
            message.setCorrelationId(correlationId);
            Map<String,String> propertiesMap = toStringMap(properties);
            message.setProperties(propertiesMap);

            sender.send(message);
            log.info("\t=> Sent a message with messageId \n" + message.getMessageId());
        } catch (InterruptedException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        } catch (ServiceBusException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        }
    }

    /**
     * Receive Message with configurable parameters as Map when Receiver Connection is given as a parameter and
     * message content as a byte array and return Message object.
     *
     * @param receiver Output Receiver connection.
     * @param serverWaitTime Specified server wait time in seconds to receive message.
     * @return Message Object of the received message.
     */
    public static Object receiveMessage(IMessageReceiver receiver, int serverWaitTime) throws Exception {
        try {
            log.info("\n\tWaiting up to 'serverWaitTime' seconds for messages from\n" + receiver.getEntityPath());

            IMessage receivedMessage = receiver.receive(Duration.ofSeconds(serverWaitTime));

            if (receivedMessage == null) {
                return null;
            }
            log.info("\t<= Received a message with messageId \n" + receivedMessage.getMessageId());
            log.info("\t<= Received a message with messageBody \n" +
                    new String(receivedMessage.getBody(), UTF_8));
            receiver.complete(receivedMessage.getLockToken());

            log.info("\tDone receiving messages from \n" + receiver.getEntityPath());

            BObject messageBObject = BValueCreator.createObjectValue(ASBConstants.PACKAGE_ID_ASB,
                    ASBConstants.MESSAGE_OBJECT);
            messageBObject.set(ASBConstants.MESSAGE_CONTENT, BValueCreator.createArrayValue(receivedMessage.getBody()));
            return messageBObject;
        } catch (InterruptedException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        } catch (ServiceBusException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        }
    }

    /**
     * Receive Messages with configurable parameters as Map when Receiver Connection is given as a parameter and
     * message content as a byte array and return Messages object.
     *
     * @param receiver Output Receiver connection.
     * @param serverWaitTime Specified server wait time in seconds to receive message.
     * @param maxMessageCount Maximum no. of messages in a batch
     * @return Message Object of the received message.
     */
    public static Object receiveMessages(IMessageReceiver receiver, int serverWaitTime, int maxMessageCount)
            throws Exception {
        try {
            // receive messages from queue or subscription
            String receivedMessageId = "";
            BArrayType sourceArrayType = null;
            BObject[] bObjectArray = new BObject[maxMessageCount];
            int i = 0;

            BObject messagesBObject = BValueCreator.createObjectValue(ASBConstants.PACKAGE_ID_ASB,
                    ASBConstants.MESSAGES_OBJECT);

            log.info("\n\tWaiting up to 'serverWaitTime' seconds for messages from " + receiver.getEntityPath());
            while (true) {
                IMessage receivedMessage = receiver.receive(Duration.ofSeconds(serverWaitTime));

                if (receivedMessage == null || i == maxMessageCount) {
                    break;
                }
                log.info("\t<= Received a message with messageId \n" + receivedMessage.getMessageId());
                log.info("\t<= Received a message with messageBody \n" +
                        new String(receivedMessage.getBody(), UTF_8));
                receiver.complete(receivedMessage.getLockToken());

                if (receivedMessageId.contentEquals(receivedMessage.getMessageId())) {
                    return ASBUtils.returnErrorValue("Received a duplicate message!");
                }
                receivedMessageId = receivedMessage.getMessageId();

                BObject messageBObject = BValueCreator.createObjectValue(ASBConstants.PACKAGE_ID_ASB,
                        ASBConstants.MESSAGE_OBJECT);
                messageBObject.set(ASBConstants.MESSAGE_CONTENT,
                        BValueCreator.createArrayValue(receivedMessage.getBody()));
                bObjectArray[i] = messageBObject;
                i = i + 1;
                sourceArrayType = new BArrayType(messageBObject.getType());
            }
            log.info("\tDone receiving messages from \n" + receiver.getEntityPath());
            if(sourceArrayType != null) {
                messagesBObject.set(ASBConstants.MESSAGES_CONTENT,
                        BValueCreator.createArrayValue(bObjectArray, sourceArrayType));
                messagesBObject.set(ASBConstants.DELIVERY_TAG, i);
            }
            return messagesBObject;
        } catch (InterruptedException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        } catch (ServiceBusException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        }
    }

    /**
     * Send Batch of Messages with configurable parameters when Sender Connection is given as a parameter and
     * message content as a byte array, optional parameters as a BMap and maximum message count in a batch as int.
     *
     * @param sender Input Sender connection.
     * @param content Input message content as byte array
     * @param parameters Input message optional parameters specified as a BMap
     * @param properties Input Message properties
     * @param maxMessageCount Maximum no. of messages in a batch
     */
    public static void sendBatchMessage(IMessageSender sender, BArray content, BMap<String, String> parameters,
                                        BMap<String, String> properties, int maxMessageCount) throws Exception {
        Map<String,String> map = toStringMap(parameters);

        String contentType = valueToStringOrEmpty(map, CONTENT_TYPE);
        String messageId = map.get(MESSAGE_ID) != null ? map.get(MESSAGE_ID) : UUID.randomUUID().toString();
        String to = valueToStringOrEmpty(map, TO);
        String replyTo = valueToStringOrEmpty(map, REPLY_TO);
        String replyToSessionId = valueToStringOrEmpty(map, REPLY_TO_SESSION_ID);
        String label = valueToStringOrEmpty(map,LABEL);
        String sessionId = valueToStringOrEmpty(map, SESSION_ID);
        String correlationId = valueToStringOrEmpty(map, CORRELATION_ID);
        int timeToLive = map.get(TIME_TO_LIVE) != null ? Integer.parseInt(map.get(TIME_TO_LIVE)) : DEFAULT_TIME_TO_LIVE;

        try {
            List<IMessage> messages = new ArrayList<>();

            for(int i = 0; i < maxMessageCount; i++) {
                IMessage message = new Message();
                messageId = map.get(MESSAGE_ID) != null ? map.get(MESSAGE_ID) : UUID.randomUUID().toString();
                message.setMessageId(messageId);
                message.setTimeToLive(Duration.ofMinutes(timeToLive));
                byte[] byteArray = content.get(i).toString().getBytes();
                message.setBody(byteArray);
                message.setContentType(contentType);
                message.setMessageId(messageId);
                message.setTo(to);
                message.setReplyTo(replyTo);
                message.setReplyToSessionId(replyToSessionId);
                message.setLabel(label);
                message.setSessionId(sessionId);
                message.setCorrelationId(correlationId);
                Map<String,String> propertiesMap = toStringMap(properties);
                message.setProperties(propertiesMap);

                messages.add(message);
                log.info("\t=> Sending a message with messageId \n" + message.getMessageId());
            }

            // Send messages to queue or topic
            log.info("\tSending messages to  ...\n" + sender.getEntityPath());
            sender.sendBatch(messages);
            log.info("\t=> Sent  messages\n" + messages.size());
        } catch (InterruptedException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        } catch (ServiceBusException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        }
    }

    /**
     * Receive Batch of Messages with configurable parameters as Map when Receiver Connection is given as a parameter,
     * message content as a byte array, maximum message count in a batch as int and return Messages object.
     *
     * @param receiver Output Receiver connection.
     * @param maxMessageCount Maximum no. of messages in a batch
     * @return Message Object of the received message.
     */
    public static Object receiveBatchMessage(IMessageReceiver receiver, int maxMessageCount) throws Exception {
        try {
            // receive messages from queue or subscription
            String receivedMessageId = "";
            BArrayType sourceArrayType = null;
            BObject[] bObjectArray = new BObject[maxMessageCount];
            int i = 0;

            BObject messagesBObject = BValueCreator.createObjectValue(ASBConstants.PACKAGE_ID_ASB,
                    ASBConstants.MESSAGES_OBJECT);

            log.info("\n\tWaiting up to default server time for messages from  ...\n" + receiver.getEntityPath());
            for(int j=0; j < maxMessageCount; j++) {
                IMessage receivedMessage = receiver.receive();

                if (receivedMessage == null) {
                    continue;
                }
                log.info("\t<= Received a message with messageId \n" + receivedMessage.getMessageId());
                log.info("\t<= Received a message with messageBody \n" +
                        new String(receivedMessage.getBody(), UTF_8));
                receiver.complete(receivedMessage.getLockToken());

                if (receivedMessageId.contentEquals(receivedMessage.getMessageId())) {
                    return ASBUtils.returnErrorValue("Received a duplicate message!");
                }
                receivedMessageId = receivedMessage.getMessageId();

                BObject messageBObject = BValueCreator.createObjectValue(ASBConstants.PACKAGE_ID_ASB,
                        ASBConstants.MESSAGE_OBJECT);
                messageBObject.set(ASBConstants.MESSAGE_CONTENT,
                        BValueCreator.createArrayValue(receivedMessage.getBody()));
                bObjectArray[i] = messageBObject;
                i = i + 1;
                sourceArrayType = new BArrayType(messageBObject.getType());
            }
            log.info("\tDone receiving messages from \n" + receiver.getEntityPath());
            if(sourceArrayType != null) {
                messagesBObject.set(ASBConstants.MESSAGES_CONTENT,
                        BValueCreator.createArrayValue(bObjectArray, sourceArrayType));
                messagesBObject.set(ASBConstants.DELIVERY_TAG, i);
            }
            return messagesBObject;
        } catch (InterruptedException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        } catch (ServiceBusException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        }
    }

    /**
     * Complete Messages from Queue or Subscription based on messageLockToken
     *
     * @param receiver Output Receiver connection.
     */
    public static void completeMessages(IMessageReceiver receiver)
            throws Exception {
        try {
            // receive messages from queue
            String receivedMessageId = "";

            log.info("\n\tWaiting up to default server time for messages from  ...\n" + receiver.getEntityPath());
            while (true) {
                IMessage receivedMessage = receiver.receive();

                if (receivedMessage == null) {
                    break;
                }
                log.info("\t<= Received a message with messageId \n" + receivedMessage.getMessageId());
                log.info("\t<= Completes a message with messageLockToken \n" +
                        receivedMessage.getLockToken());
                receiver.complete(receivedMessage.getLockToken());
                if (receivedMessageId.contentEquals(receivedMessage.getMessageId())) {
                    throw ASBUtils.returnErrorValue("Received a duplicate message!");
                }
                receivedMessageId = receivedMessage.getMessageId();
            }
            log.info("\tDone completing a message using its lock token from \n" + receiver.getEntityPath());
        } catch (InterruptedException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        } catch (ServiceBusException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        }
    }

    /**
     * Completes a single message from Queue or Subscription based on messageLockToken
     *
     * @param receiver Output Receiver connection.
     */
    public static void completeOneMessage(IMessageReceiver receiver) throws Exception {
        try {
            log.info("\nWaiting up to default server wait time for messages from  ...\n" +
                    receiver.getEntityPath());

            IMessage receivedMessage = receiver.receive();

            if (receivedMessage != null) {
                log.info("\t<= Received a message with messageId \n" + receivedMessage.getMessageId());
                log.info("\t<= Completes a message with messageLockToken \n" +
                        receivedMessage.getLockToken());
                receiver.complete(receivedMessage.getLockToken());

                log.info("\tDone completing a message using its lock token from \n" +
                        receiver.getEntityPath());
            } else {
                log.info("\tNo message in the queue\n");
            }
        } catch (InterruptedException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        } catch (ServiceBusException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        }
    }

    /**
     * Abandon message & make available again for processing from Queue or Subscription based on messageLockToken
     *
     * @param receiver Output Receiver connection.
     */
    public static void abandonMessage(IMessageReceiver receiver) throws Exception {
        try {
            log.info("\n\tWaiting up to default server wait time for messages from  ...\n" +
                    receiver.getEntityPath());
            IMessage receivedMessage = receiver.receive();

            if (receivedMessage != null) {
                log.info("\t<= Received a message with messageId \n" + receivedMessage.getMessageId());
                log.info("\t<= Abandon a message with messageLockToken \n" + receivedMessage.getLockToken());
                receiver.abandon(receivedMessage.getLockToken());

                log.info("\tDone abandoning a message using its lock token from \n" +
                        receiver.getEntityPath());
            } else {
                log.info("\t<= No message in the queue \n");
            }
        } catch (InterruptedException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        } catch (ServiceBusException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        }
    }

    /**
     * Dead-Letter the message & moves the message to the Dead-Letter Queue based on messageLockToken
     *
     * @param receiver Output Receiver connection.
     */
    public static void deadLetterMessage(IMessageReceiver receiver, String deadLetterReason,
                                         String deadLetterErrorDescription) throws Exception {
        try {
            log.info("\n\tWaiting up to default server wait time for messages from  ...\n" +
                    receiver.getEntityPath());
            IMessage receivedMessage = receiver.receive();

            if (receivedMessage != null) {
                log.info("\t<= Received a message with messageId \n" + receivedMessage.getMessageId());
                log.info("\t<= Dead-Letter a message with messageLockToken \n" + receivedMessage.getLockToken());
                receiver.deadLetter(receivedMessage.getLockToken(), deadLetterReason, deadLetterErrorDescription);

                log.info("\tDone dead-lettering a message using its lock token from \n" +
                        receiver.getEntityPath());
            } else {
                log.info("\t<= No message in the queue \n");
            }
        } catch (InterruptedException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        } catch (ServiceBusException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        }
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
        return value == null ? "" : value.toString();
    }

    public ConnectionUtils() {
    }

    public ConnectionUtils(String connectionString) {
        this.connectionString = connectionString;
    }
}
