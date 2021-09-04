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

package org.ballerinax.asb.sender;

import com.microsoft.azure.servicebus.ClientFactory;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageSender;
import com.microsoft.azure.servicebus.Message;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import org.apache.log4j.Logger;
import org.ballerinax.asb.listener.Caller;
import org.ballerinax.asb.util.ASBConstants;
import org.ballerinax.asb.util.ASBUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * This facilitates the client operations of MessageSender client in Ballerina.
 */
public class MessageSender {
    private static final Logger log = Logger.getLogger(Caller.class);
    String entityPath;
    IMessageSender sender;

    /**
     * Parameterized constructor for Message Sender (IMessageSender).
     *
     * @param connectionString Azure service bus connection string.
     * @param entityPath       Entity path (QueueName or SubscriptionPath).
     * @throws ServiceBusException  on failure initiating IMessage Receiver in Azure Service Bus instance.
     * @throws InterruptedException on failure initiating IMessage Receiver due to thread interruption.
     */
    public MessageSender(String connectionString, String entityPath) throws ServiceBusException, InterruptedException {
        this.entityPath = entityPath;
        this.sender = ClientFactory.createMessageSenderFromConnectionStringBuilder(
                new ConnectionStringBuilder(connectionString, entityPath));
    }

    /**
     * Send Message with configurable parameters when Sender Connection is given as a parameter and
     * message content as a byte array.
     *
     * @param body             Input message content as byte array
     * @param contentType      Input message content type
     * @param messageId        Input Message ID
     * @param to               Input Message to
     * @param replyTo          Input Message reply to
     * @param replyToSessionId Identifier of the session to reply to
     * @param label            Input Message label
     * @param sessionId        Input Message session Id
     * @param correlationId    Input Message correlationId
     * @param timeToLive       Input Message time to live in minutes
     * @param properties       Input Message properties
     * @return An error if failed send the message.
     */
    public Object send(Object body, Object contentType, Object messageId,
                       Object to, Object replyTo, Object replyToSessionId, Object label, Object sessionId,
                       Object correlationId, Object partitionKey, Object timeToLive, Object properties) {
        try {
            IMessage message = new Message();
            byte[] byteArray = ((BArray) body).getBytes();
            message.setMessageBody(new Message(byteArray).getMessageBody());
            message.setContentType(ASBUtils.valueToEmptyOrToString(contentType));
            message.setMessageId(ASBUtils.valueToEmptyOrToString(messageId));
            message.setTo(ASBUtils.valueToEmptyOrToString(to));
            message.setReplyTo(ASBUtils.valueToEmptyOrToString(replyTo));
            message.setReplyToSessionId(ASBUtils.valueToEmptyOrToString(replyToSessionId));
            message.setLabel(ASBUtils.valueToEmptyOrToString(label));
            message.setSessionId(ASBUtils.valueToEmptyOrToString(sessionId));
            message.setCorrelationId(ASBUtils.valueToEmptyOrToString(correlationId));
            message.setPartitionKey(ASBUtils.valueToEmptyOrToString(partitionKey));
            if (timeToLive != null) {
                message.setTimeToLive(Duration.ofSeconds((long) timeToLive));
            }
            Map<String, Object> map = ASBUtils.toStringMap((BMap) properties);
            message.setProperties(map);
            sender.send(message);
            if (log.isDebugEnabled()) {
                log.debug("\t=> Sent a message with messageId \n" + message.getMessageId());
            }
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ServiceBusException e) {
            return ASBUtils.returnErrorValue("Exception while sending batch messages" + e.getMessage());
        }
    }

    /**
     * Send Batch of Messages with configurable parameters when Sender Connection is given as a parameter and
     * batch message record as a BMap.
     *
     * @param messages Input batch message record as a BMap
     * @return An error if failed send the message.
     */
    public Object sendBatch(BMap<BString, Object> messages) {
        try {
            Map<String, Object> messagesMap = ASBUtils.toObjectMap((BMap) messages);
            long messageCount = (long) messagesMap.get("messageCount");
            BArray messageArray = (BArray) messagesMap.get("messages");
            Collection<IMessage> messageBatch = new ArrayList<>();
            for (int i = 0; i < messageArray.getLength(); i++) {
                BMap messageBMap = (BMap) messageArray.get(i);
                Map<String, Object> messageMap = ASBUtils.toObjectMap(messageBMap);
                IMessage message = new Message();
                byte[] byteArray = ((BArray) messageMap.get(ASBConstants.BODY)).getBytes();
                message.setMessageBody(new Message(byteArray).getMessageBody());
                message.setContentType(ASBUtils.valueToStringOrEmpty(messageMap, ASBConstants.CONTENT_TYPE));
                message.setMessageId(ASBUtils.valueToEmptyOrToString(messageMap.get(ASBConstants.MESSAGE_ID)));
                message.setTo(ASBUtils.valueToStringOrEmpty(messageMap, ASBConstants.TO));
                message.setReplyTo(ASBUtils.valueToStringOrEmpty(messageMap, ASBConstants.REPLY_TO));
                message.setReplyToSessionId(ASBUtils.valueToStringOrEmpty(messageMap, ASBConstants.REPLY_TO_SESSION_ID));
                message.setLabel(ASBUtils.valueToStringOrEmpty(messageMap, ASBConstants.LABEL));
                message.setSessionId(ASBUtils.valueToStringOrEmpty(messageMap, ASBConstants.SESSION_ID));
                message.setCorrelationId(ASBUtils.valueToStringOrEmpty(messageMap, ASBConstants.CORRELATION_ID));
                message.setPartitionKey(ASBUtils.valueToStringOrEmpty(messageMap, ASBConstants.PARTITION_KEY));
                if (messageMap.get(ASBConstants.TIME_TO_LIVE) != null) {
                    message.setTimeToLive(Duration.ofSeconds((long) messageMap.get(ASBConstants.TIME_TO_LIVE)));
                }
                Map<String, Object> map = ASBUtils.toStringMap((BMap) messageMap.get(ASBConstants.APPLICATION_PROPERTIES));
                message.setProperties(map);
                messageBatch.add(message);
            }
            sender.sendBatch(messageBatch);
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ServiceBusException e) {
            return ASBUtils.returnErrorValue("Exception while sending batch messages" + e.getMessage());
        }
    }

    /**
     * Closes the Asb Sender Connection using the given connection parameters.
     *
     * @return @return An error if failed close the sender.
     */
    public Object closeSender() {
        try {
            sender.close();
            return null;
        } catch (ServiceBusException e) {
            return ASBUtils.returnErrorValue("Exception while closing the sender" + e.getMessage());
        }
    }
}
