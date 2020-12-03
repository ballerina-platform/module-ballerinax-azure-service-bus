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
import org.ballerinalang.asb.ASBConstants;
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
        } catch (Exception e) {
            throw AsbUtils.returnErrorValue(e.getMessage());
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
        } catch (Exception e) {
            throw AsbUtils.returnErrorValue(e.getMessage());
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
        } catch (Exception e) {
            throw AsbUtils.returnErrorValue(e.getMessage());
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
        } catch (Exception e) {
            throw AsbUtils.returnErrorValue(e.getMessage());
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
     * @param label Input Message label
     * @param sessionId Input Message session Id
     * @param correlationId Input Message correlationId
     * @param properties Input Message properties
     * @param timeToLive Input Message time to live in minutes
     */
    public static void sendMessage(IMessageSender sender, BArray content, String contentType, String messageId,
                                   String to, String replyTo, String label, String sessionId, String correlationId,
                                   BMap<String, String> properties, int timeToLive) throws Exception {
        try {
            // Send messages to queue
            LOG.info("\tSending messages to ...\n" + sender.getEntityPath());
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

            sender.send(message);
            LOG.info("\t=> Sent a message with messageId \n" + message.getMessageId());
        } catch (Exception e) {
            throw AsbUtils.returnErrorValue(e.getMessage());
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
            LOG.info("\tSending messages to ...\n" + sender.getEntityPath());
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
            LOG.info("\t=> Sent a message with messageId \n" + message.getMessageId());
        } catch (Exception e) {
            throw AsbUtils.returnErrorValue(e.getMessage());
        }
    }

    /**
     * Receive Message with configurable parameters as Map when Receiver Connection is given as a parameter and
     * message content as a byte array and return Message object.
     *
     * @param receiver Output Receiver connection.
     * @return Message Object of the received message.
     */
    public static Object receiveMessage(IMessageReceiver receiver) throws Exception {
        try {
            LOG.info("\n\tWaiting up to 5 seconds for messages from ...\n" + receiver.getEntityPath());

            IMessage receivedMessage = receiver.receive(Duration.ofSeconds(5));

            if (receivedMessage == null) {
                return null;
            }
            LOG.info("\t<= Received a message with messageId \n" + receivedMessage.getMessageId());
            LOG.info("\t<= Received a message with messageBody \n" +
                    new String(receivedMessage.getBody(), UTF_8));
            receiver.complete(receivedMessage.getLockToken());

            LOG.info("\tDone receiving messages from \n" + receiver.getEntityPath());

            BObject messageBObject = BValueCreator.createObjectValue(ASBConstants.PACKAGE_ID_ASB,
                    ASBConstants.MESSAGE_OBJECT);
            messageBObject.set(ASBConstants.MESSAGE_CONTENT, BValueCreator.createArrayValue(receivedMessage.getBody()));
            return messageBObject;
        } catch (Exception e) {
            throw AsbUtils.returnErrorValue(e.getMessage());
        }
    }

    public ConUtils() {
    }

    public ConUtils(String connectionString) {
        this.connectionString = connectionString;
    }
}
