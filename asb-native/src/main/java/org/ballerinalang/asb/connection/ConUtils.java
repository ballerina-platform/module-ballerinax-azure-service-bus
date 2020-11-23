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
import org.ballerinalang.asb.AsbConstants;
import org.ballerinalang.asb.AsbUtils;
import org.ballerinalang.jvm.api.values.BArray;
import org.ballerinalang.jvm.api.values.BMap;
import org.ballerinalang.jvm.api.values.BObject;
import org.ballerinalang.jvm.api.BValueCreator;

import java.time.Duration;
import java.util.*;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Util class used to bridge the Asb connector's native code and the Ballerina API.
 */
public class ConUtils {

    private static final Logger LOG = Logger.getLogger(ConUtils.class.getName());

    private String connectionString;

    public ConUtils() {
    }

    public ConUtils(String connectionString) {
        this.connectionString = connectionString;
    }

    // Create Sender Connection
    public static IMessageSender createSenderConnection(String connectionString, String entityPath) throws Exception {
        try {
            IMessageSender sender = ClientFactory.createMessageSenderFromConnectionStringBuilder(
                    new ConnectionStringBuilder(connectionString, entityPath));
            return sender;
        } catch (Exception e) {
            throw AsbUtils.returnErrorValue(e.getMessage());
        }
    }

    // Close Receiver Connection
    public static void closeSenderConnection(IMessageSender sender) throws Exception {
        try {
            sender.close();
        } catch (Exception e) {
            throw AsbUtils.returnErrorValue(e.getMessage());
        }
    }

    // Create Receiver Connection
    public static IMessageReceiver createReceiverConnection(String connectionString, String entityPath)
            throws Exception {
        try {
            IMessageReceiver receiver = ClientFactory.createMessageReceiverFromConnectionStringBuilder(
                    new ConnectionStringBuilder(connectionString, entityPath), ReceiveMode.PEEKLOCK);
            return receiver;
        } catch (Exception e) {
            throw AsbUtils.returnErrorValue(e.getMessage());
        }
    }

    // Close Receiver Connection
    public static void closeReceiverConnection(IMessageReceiver receiver) throws Exception {
        try {
            receiver.close();
        } catch (Exception e) {
            throw AsbUtils.returnErrorValue(e.getMessage());
        }
    }

    // Send Message with configurable parameters when Sender Connection is given as a parameter and
    // message content as a byte array
    public static void sendBytesMessageWithConfigurableParameters(IMessageSender sender, BArray content,
                                                                  String contentType, String messageId, String to,
                                                                  String replyTo, String label,
                                                                  String sessionId, String correlationId,
                                                                  BMap<String, String> properties, int timeToLive)
            throws Exception {
        try {
            // Send messages to queue
            System.out.printf("\tSending messages to %s ...\n", sender.getEntityPath());
            IMessage message = new Message();
            message.setMessageId(messageId);
            message.setTimeToLive(Duration.ofMinutes(timeToLive));
            byte[] byteArray = content.getBytes();
            message.setBody(byteArray);
            message.setContentType(contentType);
            message.setMessageId(messageId);
            message.setTo(to);
            message.setReplyTo(replyTo);
            message.setLabel(label);
            message.setSessionId(sessionId);
            message.setCorrelationId(correlationId);
            Map<String,String> map = toStringMap(properties);
            message.setProperties(map);
            System.out.println(map);

            sender.send(message);
            System.out.printf("\t=> Sent a message with messageId %s\n", message.getMessageId());
        } catch (Exception e) {
            throw AsbUtils.returnErrorValue(e.getMessage());
        }
    }

    // Convert BMap to Map
    public static Map<String, String> toStringMap(BMap map) {
        Map<String, String> returnMap = new HashMap<>();
        if (map != null) {
            for (Object aKey : map.getKeys()) {
                returnMap.put(aKey.toString(), map.get(aKey).toString());
            }
        }
        return returnMap;
    }

    // Send Message with configurable parameters as Map when Sender Connection is given as a parameter and
    // message content as a byte array
    public static void sendBytesMessageViaSenderConnectionWithConfigurableParameters(IMessageSender sender,
                                                                                     BArray content,
                                                                                     BMap<String, String> parameters,
                                                                                     BMap<String, String> properties)
            throws Exception {
        Map<String,String> map = toStringMap(parameters);

        String contentType = "";
        String messageId = UUID.randomUUID().toString();;
        String to = "";
        String replyTo = "";
        String label = "";
        String sessionId = "";
        String correlationId = "";
        int timeToLive = 1;
        if (map.containsKey("contentType")) {
            contentType = (String)map.get("contentType");
        }
        if (map.containsKey("messageId")) {
            messageId = (String) map.get("messageId");
        }
        if (map.containsKey("to")) {
            to = (String) map.get("to");
        }
        if (map.containsKey("replyTo")) {
            replyTo = (String) map.get("replyTo");
        }
        if (map.containsKey("label")) {
            label = (String) map.get("label");
        }
        if (map.containsKey("sessionId")) {
            sessionId = (String) map.get("sessionId");
        }
        if (map.containsKey("correlationId")) {
            correlationId = (String) map.get("correlationId");
        }
        if (map.containsKey("timeToLive")) {
            timeToLive = Integer.parseInt(map.get("timeToLive"));
        }

        try {
            // Send messages to queue
            LOG.info("\tSending messages to  ...\n");
            System.out.printf("\tSending messages to %s ...\n", sender.getEntityPath());
            LOG.info("\tSending messages to  ...\n");
            IMessage message = new Message();
            message.setMessageId(messageId);
            message.setTimeToLive(Duration.ofMinutes(timeToLive));
            byte[] byteArray = content.getBytes();
            message.setBody(byteArray);
            message.setContentType(contentType);
            message.setMessageId(messageId);
            message.setTo(to);
            message.setReplyTo(replyTo);
            message.setLabel(label);
            message.setSessionId(sessionId);
            message.setCorrelationId(correlationId);
            Map<String,String> propertiesMap = toStringMap(properties);
            message.setProperties(propertiesMap);

            sender.send(message);
            System.out.printf("\t=> Sent a message with messageId %s\n", message.getMessageId());
        } catch (Exception e) {
            throw AsbUtils.returnErrorValue(e.getMessage());
        }

    }

    // Receive Message with configurable parameters as Map when Receiver Connection is given as a parameter and
    // message content as a byte array and return message list
    public static ArrayList<IMessage> receiveBytesMessageViaReceiverConnectionWithConfigurableParameters(
            IMessageReceiver receiver) throws Exception {
        try {
            // receive messages from queue or subscription
            String receivedMessageId = "";

            ArrayList<IMessage> messages = new ArrayList<>();

            System.out.printf("\n\tWaiting up to 5 seconds for messages from %s ...\n", receiver.getEntityPath());
            while (true) {
                IMessage receivedMessage = receiver.receive(Duration.ofSeconds(5));

                if (receivedMessage == null) {
                    break;
                }
                System.out.printf("\t<= Received a message with messageId %s\n", receivedMessage.getMessageId());
                System.out.printf("\t<= Received a message with messageBody %s\n",
                        new String(receivedMessage.getBody(), UTF_8));
                receiver.complete(receivedMessage.getLockToken());
                messages.add(receivedMessage);
                if (receivedMessageId.contentEquals(receivedMessage.getMessageId())) {
                    throw new Exception("Received a duplicate message!");
                }
                receivedMessageId = receivedMessage.getMessageId();
            }
            System.out.printf("\tDone receiving messages from %s\n", receiver.getEntityPath());
            return messages;
        } catch (Exception e) {
            throw AsbUtils.returnErrorValue(e.getMessage());
        }
    }

    // Receive Message with configurable parameters as Map when Receiver Connection is given as a parameter and
    // message content as a byte array and return message list
    public static Object receiveOneBytesMessageViaReceiverConnectionWithConfigurableParameters(
            IMessageReceiver receiver) throws Exception {
        try {
            // receive messages from queue or subscription
            String receivedMessageId = "";

            System.out.printf("\n\tWaiting up to 5 seconds for messages from %s ...\n", receiver.getEntityPath());

            IMessage receivedMessage = receiver.receive(Duration.ofSeconds(5));

            if (receivedMessage == null) {
                return null;
            }
            System.out.printf("\t<= Received a message with messageId %s\n", receivedMessage.getMessageId());
            System.out.printf("\t<= Received a message with messageBody %s\n",
                    new String(receivedMessage.getBody(), UTF_8));
            receiver.complete(receivedMessage.getLockToken());
            if (receivedMessageId.contentEquals(receivedMessage.getMessageId())) {
                throw new Exception("Received a duplicate message!");
            }
            receivedMessageId = receivedMessage.getMessageId();

            System.out.printf("\tDone receiving messages from %s\n", receiver.getEntityPath());

            BObject messageBObject = BValueCreator.createObjectValue(AsbConstants.PACKAGE_ID_ASB,
                    AsbConstants.MESSAGE_OBJECT);
            messageBObject.set(AsbConstants.MESSAGE_CONTENT, BValueCreator.createArrayValue(receivedMessage.getBody()));

            return messageBObject;
        } catch (Exception e) {
            throw AsbUtils.returnErrorValue(e.getMessage());
        }
    }

    // check message
    public static void checkMessage(ArrayList<IMessage> messages) throws Exception {
        for (IMessage msg:messages
        ) {
            System.out.printf("\t<= Received a message with messageId %s\n", msg.getMessageId());
            System.out.printf("\t<= Received a message with messageBody %s\n", new String(msg.getBody(), UTF_8));
            System.out.printf("\t<= Received a message with contentType %s\n", msg.getContentType());
            System.out.printf("\t<= Received a message with to %s\n", msg.getTo());
            System.out.printf("\t<= Received a message with replyTo %s\n", msg.getReplyTo());
            System.out.printf("\t<= Received a message with label %s\n", msg.getLabel());
            System.out.printf("\t<= Received a message with sessionId %s\n", msg.getSessionId());
            System.out.printf("\t<= Received a message with correlationId %s\n", msg.getCorrelationId());
            System.out.printf("\t<= Received a message with timeToLive %s min\n", msg.getTimeToLive().toMinutes());
        }
        System.out.printf("\tDone viewing messages\n");
    }

    // Send batch of messages to Queue or Topic with Message Content input as Byte Array
    public static void sendBatchMessages(String connectionString, String entityPath, BArray content,
                                         int maxMessageCount) throws Exception {
        IMessageSender sender = ClientFactory.createMessageSenderFromConnectionStringBuilder(
                new ConnectionStringBuilder(connectionString, entityPath));

        List<IMessage> messages = new ArrayList<>();

        for(int i=0; i<maxMessageCount; i++){
            String messageId = UUID.randomUUID().toString();
            IMessage message = new Message();
            message.setMessageId(messageId);
            message.setTimeToLive(Duration.ofMinutes(1));
            byte[] byteArray = content.get(i).toString().getBytes();
            message.setBody(byteArray);

            messages.add(message);
            System.out.printf("\t=> Sending a message with messageId %s\n", message.getMessageId());
        }

        // Send messages to queue
        System.out.printf("\tSending messages to %s ...\n", sender.getEntityPath());
        sender.sendBatch(messages);
        System.out.printf("\t=> Sent %s messages\n", messages.size());

        sender.close();
    }

    // Receive batch of messages from Queue or Subscription with Message Content input as Byte Array
    public static void receiveBatchMessages(String connectionString, String entityPath, int maxMessageCount)
            throws Exception {
        IMessageReceiver receiver = ClientFactory.createMessageReceiverFromConnectionStringBuilder(
                new ConnectionStringBuilder(connectionString, entityPath), ReceiveMode.PEEKLOCK);

        // receive messages from queue
        String receivedMessageId = "";

        System.out.printf("\n\tWaiting up to 5 seconds for messages from %s ...\n", receiver.getEntityPath());
        for(int i=0; i<maxMessageCount; i++) {
            IMessage receivedMessage = receiver.receive(Duration.ofSeconds(5));

            if (receivedMessage == null) {
                continue;
            }
            System.out.printf("\t<= Received a message with messageId %s\n", receivedMessage.getMessageId());
            System.out.printf("\t<= Received a message with messageBody %s\n",
                    new String(receivedMessage.getBody(), UTF_8));
            receiver.complete(receivedMessage.getLockToken());
            if (receivedMessageId.contentEquals(receivedMessage.getMessageId())) {
                throw new Exception("Received a duplicate message!");
            }
            receivedMessageId = receivedMessage.getMessageId();
        }
        System.out.printf("\tDone receiving messages from %s\n", receiver.getEntityPath());

        receiver.close();
    }

    // -----------------------------------------------------------------------------------------------------------------

    // Send message to Queue or Topic
    public static void send(String connectionString, String entityPath, String content) throws Exception {
        IMessageSender sender = ClientFactory.createMessageSenderFromConnectionStringBuilder(
                new ConnectionStringBuilder(connectionString, entityPath));

        String messageId = UUID.randomUUID().toString();
        // Send messages to queue
        System.out.printf("\tSending messages to %s ...\n", sender.getEntityPath());
        IMessage message = new Message();
        message.setMessageId(messageId);
        message.setTimeToLive(Duration.ofMinutes(1));
        byte[] byteArray = content.getBytes();
        message.setBody(byteArray);
        sender.send(message);
        System.out.printf("\t=> Sent a message with messageId %s\n", message.getMessageId());

        sender.close();
    }

    // Receive message from Queue or Subscription
    public static void receive(String connectionString, String entityPath) throws Exception {
        IMessageReceiver receiver = ClientFactory.createMessageReceiverFromConnectionStringBuilder(
                new ConnectionStringBuilder(connectionString, entityPath), ReceiveMode.PEEKLOCK);

        // receive messages from queue
        String receivedMessageId = "";

        System.out.printf("\n\tWaiting up to 5 seconds for messages from %s ...\n", receiver.getEntityPath());
        while (true) {
            IMessage receivedMessage = receiver.receive(Duration.ofSeconds(5));

            if (receivedMessage == null) {
                break;
            }
            System.out.printf("\t<= Received a message with messageId %s\n", receivedMessage.getMessageId());
            System.out.printf("\t<= Received a message with messageBody %s\n",
                    new String(receivedMessage.getBody(), UTF_8));
            receiver.complete(receivedMessage.getLockToken());
            if (receivedMessageId.contentEquals(receivedMessage.getMessageId())) {
                throw new Exception("Received a duplicate message!");
            }
            receivedMessageId = receivedMessage.getMessageId();
        }
        System.out.printf("\tDone receiving messages from %s\n", receiver.getEntityPath());

        receiver.close();
    }

    // Send batch of messages to Queue or Topic
    public static void sendBatch(String connectionString, String entityPath, String content, int maxMessageCount)
            throws Exception {
        IMessageSender sender = ClientFactory.createMessageSenderFromConnectionStringBuilder(
                new ConnectionStringBuilder(connectionString, entityPath));

        List<IMessage> messages = new ArrayList<>();

        for(int i=0; i<maxMessageCount; i++){
            String messageId = UUID.randomUUID().toString();
            IMessage message = new Message();
            message.setMessageId(messageId);
            message.setTimeToLive(Duration.ofMinutes(1));
            String contentMod = content + Integer.toString(i);
            byte[] byteArray = contentMod.getBytes();
            message.setBody(byteArray);

            messages.add(message);
            System.out.printf("\t=> Sending a message with messageId %s\n", message.getMessageId());
        }

        // Send messages to queue
        System.out.printf("\tSending messages to %s ...\n", sender.getEntityPath());
        sender.sendBatch(messages);
        System.out.printf("\t=> Sent %s messages\n", messages.size());

        sender.close();
    }

    // Receive batch of messages from Queue or Subscription
    public static void receiveBatch(String connectionString, String entityPath, int maxMessageCount) throws Exception {
        IMessageReceiver receiver = ClientFactory.createMessageReceiverFromConnectionStringBuilder(
                new ConnectionStringBuilder(connectionString, entityPath), ReceiveMode.PEEKLOCK);

        // receive messages from queue
        String receivedMessageId = "";

        System.out.printf("\n\tWaiting up to 5 seconds for messages from %s ...\n", receiver.getEntityPath());
        for(int i=0; i<maxMessageCount; i++) {
            IMessage receivedMessage = receiver.receive(Duration.ofSeconds(5));

            if (receivedMessage == null) {
                continue;
            }
            System.out.printf("\t<= Received a message with messageId %s\n", receivedMessage.getMessageId());
            System.out.printf("\t<= Received a message with messageBody %s\n",
                    new String(receivedMessage.getBody(), UTF_8));
            receiver.complete(receivedMessage.getLockToken());
            if (receivedMessageId.contentEquals(receivedMessage.getMessageId())) {
                throw new Exception("Received a duplicate message!");
            }
            receivedMessageId = receivedMessage.getMessageId();
        }
        System.out.printf("\tDone receiving messages from %s\n", receiver.getEntityPath());

        receiver.close();
    }

    // Completes messages from Queue or Subscription based on messageLockToken
    public static void complete(String connectionString, String entityPath) throws Exception {
        IMessageReceiver receiver = ClientFactory.createMessageReceiverFromConnectionStringBuilder(
                new ConnectionStringBuilder(connectionString, entityPath), ReceiveMode.PEEKLOCK);

        // receive messages from queue
        String receivedMessageId = "";

        System.out.printf("\n\tWaiting up to 5 seconds for messages from %s ...\n", receiver.getEntityPath());
        while (true) {
            IMessage receivedMessage = receiver.receive(Duration.ofSeconds(5));

            if (receivedMessage == null) {
                break;
            }
            System.out.printf("\t<= Received a message with messageId %s\n", receivedMessage.getMessageId());
            System.out.printf("\t<= Completes a message with messageLockToken %s\n", receivedMessage.getLockToken());
            receiver.complete(receivedMessage.getLockToken());
            if (receivedMessageId.contentEquals(receivedMessage.getMessageId())) {
                throw new Exception("Received a duplicate message!");
            }
            receivedMessageId = receivedMessage.getMessageId();
        }
        System.out.printf("\tDone completing a message using its lock token from %s\n", receiver.getEntityPath());

        receiver.close();
    }

    // Completes message from Queue or Subscription based on messageLockToken
    public static void completeMessage(String connectionString, String entityPath) throws Exception {
        IMessageReceiver receiver = ClientFactory.createMessageReceiverFromConnectionStringBuilder(
                new ConnectionStringBuilder(connectionString, entityPath), ReceiveMode.PEEKLOCK);

        System.out.printf("\nWaiting up to default server wait time for messages from %s ...\n",
                receiver.getEntityPath());

        IMessage receivedMessage = receiver.receive();

        if (receivedMessage != null) {
            System.out.printf("\t<= Received a message with messageId %s\n", receivedMessage.getMessageId());
            System.out.printf("\t<= Completes a message with messageLockToken %s\n", receivedMessage.getLockToken());
            receiver.complete(receivedMessage.getLockToken());

            System.out.printf("\tDone completing a message using its lock token from %s\n", receiver.getEntityPath());
        } else {
            System.out.println("\tNo message in the queue\n");
        }

        receiver.close();
    }

    // Abandon message & make available again for processing from Queue or Subscription based on messageLockToken
    public static void abandon(String connectionString, String entityPath) throws Exception {
        IMessageReceiver receiver = ClientFactory.createMessageReceiverFromConnectionStringBuilder(
                new ConnectionStringBuilder(connectionString, entityPath), ReceiveMode.PEEKLOCK);

        System.out.printf("\n\tWaiting up to default server wait time for messages from %s ...\n",
                receiver.getEntityPath());
        IMessage receivedMessage = receiver.receive();

        if (receivedMessage != null) {
            System.out.printf("\t<= Received a message with messageId %s\n", receivedMessage.getMessageId());
            System.out.printf("\t<= Abandon a message with messageLockToken %s\n", receivedMessage.getLockToken());
            receiver.abandon(receivedMessage.getLockToken());

            System.out.printf("\tDone abandoning a message using its lock token from %s\n", receiver.getEntityPath());

        } else {
            System.out.println("\t<= No message in the queue \n");;
        }

        receiver.close();
    }

}
