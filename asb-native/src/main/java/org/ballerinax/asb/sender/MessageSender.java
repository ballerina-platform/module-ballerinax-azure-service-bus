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

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusMessageBatch;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.models.CreateMessageBatchOptions;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.ballerinax.asb.util.ASBConstants;
import org.ballerinax.asb.util.ASBUtils;

/**
 * This facilitates the client operations of MessageSender client in Ballerina.
 */
public class MessageSender {
    private static final Logger log = Logger.getLogger(MessageSender.class);
    private ServiceBusSenderClient sender;

    /**
     * Parameterized constructor for Message Sender (ServiceBusSenderClient).
     *
     * @param connectionString Azure service bus connection string.
     * @param queueName        QueueName
     * @param topicName        Topic Name
     * @throws ServiceBusException  on failure initiating IMessage Receiver in Azure
     *                              Service Bus instance.
     * @throws InterruptedException on failure initiating IMessage Receiver due to
     *                              thread interruption.
     */
    public MessageSender(String connectionString, String entityType, String topicOrQueueName, String logLevel)
            throws ServiceBusException, InterruptedException {
        log.setLevel(Level.toLevel(logLevel, Level.OFF));
        ServiceBusClientBuilder clientBuilder = new ServiceBusClientBuilder().connectionString(connectionString);
        if (!entityType.isEmpty() && entityType.equalsIgnoreCase("queue")) {
            this.sender = clientBuilder
                    .sender()
                    .queueName(topicOrQueueName)
                    .buildClient();
        } else if (!entityType.isEmpty() && entityType.equalsIgnoreCase("topic")) {
            this.sender = clientBuilder
                    .sender()
                    .topicName(topicOrQueueName)
                    .buildClient();
        }
        log.debug("ServiceBusSenderClient initialized");
    }

    /**
     * Send Message with configurable parameters when Sender Connection is given as
     * a parameter and
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
            byte[] byteArray = ((BArray) body).getBytes();
            ServiceBusMessage asbMessage = new ServiceBusMessage(byteArray);
            asbMessage.setContentType(ASBUtils.convertString(contentType));
            asbMessage.setMessageId(ASBUtils.convertString(messageId));
            asbMessage.setTo(ASBUtils.convertString(to));
            asbMessage.setReplyTo(ASBUtils.convertString(replyTo));
            asbMessage.setReplyToSessionId(ASBUtils.convertString(replyToSessionId));
            asbMessage.setSubject(ASBUtils.convertString(label));
            asbMessage.setSessionId(ASBUtils.convertString(sessionId));
            asbMessage.setCorrelationId(ASBUtils.convertString(correlationId));
            asbMessage.setPartitionKey(ASBUtils.convertString(partitionKey));
            if (timeToLive != null) {
                asbMessage.setTimeToLive(Duration.ofSeconds((long) timeToLive));
            }
            Map<String, Object> map = ASBUtils.toMap((BMap) properties);
            asbMessage.getApplicationProperties().putAll(map);
            sender.sendMessage(asbMessage);
            if (log.isDebugEnabled()) {
                log.debug("Sent the message successfully");
            }
            return null;
        } catch (ServiceBusException e) {
            return ASBUtils.returnErrorValue(e.getClass().getSimpleName(), e);
        }
    }

    /**
     * Send Batch of Messages with configurable parameters when Sender Connection is
     * given as a parameter and
     * batch message record as a BMap.
     *
     * @param messages Input batch message record as a BMap
     * @return An error if failed send the message.
     */
    public Object sendBatch(BMap<BString, Object> messages) {
        try {
            Map<String, Object> messagesMap = ASBUtils.toObjectMap((BMap) messages);
            BArray messageArray = (BArray) messagesMap.get("messages");
            Collection<ServiceBusMessage> messageBatch = new ArrayList<>();
            for (int i = 0; i < messageArray.getLength(); i++) {
                BMap messageBMap = (BMap) messageArray.get(i);
                Map<String, Object> messageMap = ASBUtils.toObjectMap(messageBMap);
                byte[] byteArray = ((BArray) messageMap.get(ASBConstants.BODY)).getBytes();
                ServiceBusMessage asbMessage = new ServiceBusMessage(byteArray);
                asbMessage.setContentType(ASBUtils.convertString(messageMap, ASBConstants.CONTENT_TYPE));
                asbMessage
                        .setMessageId(ASBUtils.convertString(messageMap.get(ASBConstants.MESSAGE_ID)));
                asbMessage.setTo(ASBUtils.convertString(messageMap, ASBConstants.TO));
                asbMessage.setReplyTo(ASBUtils.convertString(messageMap, ASBConstants.REPLY_TO));
                asbMessage.setReplyToSessionId(
                        ASBUtils.convertString(messageMap, ASBConstants.REPLY_TO_SESSION_ID));
                asbMessage.setSubject(ASBUtils.convertString(messageMap, ASBConstants.LABEL));
                asbMessage.setSessionId(ASBUtils.convertString(messageMap, ASBConstants.SESSION_ID));
                asbMessage
                        .setCorrelationId(ASBUtils.convertString(messageMap, ASBConstants.CORRELATION_ID));
                asbMessage
                        .setPartitionKey(ASBUtils.convertString(messageMap, ASBConstants.PARTITION_KEY));
                if (messageMap.get(ASBConstants.TIME_TO_LIVE) != null) {
                    asbMessage
                            .setTimeToLive(Duration.ofSeconds((long) messageMap.get(ASBConstants.TIME_TO_LIVE)));
                }
                Map<String, Object> map = ASBUtils.toMap((BMap) messageMap.get(ASBConstants.APPLICATION_PROPERTIES));
                asbMessage.getApplicationProperties().putAll(map);
                messageBatch.add(asbMessage);
            }
            ServiceBusMessageBatch currentBatch = sender.createMessageBatch(new CreateMessageBatchOptions());
            for (ServiceBusMessage message : messageBatch) {
                if (currentBatch.tryAddMessage(message)) {
                    continue;
                }
                
                // The batch is full, so we create a new batch and send the batch.
                sender.sendMessages(currentBatch);
                currentBatch = sender.createMessageBatch();

                // Add that message that we couldn't before.
                if (!currentBatch.tryAddMessage(message)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Message is too large for an empty batch. Skipping. Max size: "
                                + currentBatch.getMaxSizeInBytes() + ". Message: " +
                                message.getBody().toString());
                    }
                }
            }
            sender.sendMessages(currentBatch);
            log.debug("Sent the batch message successfully");
            return null;
        } catch (ServiceBusException e) {
            return ASBUtils.returnErrorValue(e.getClass().getSimpleName(), e);
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
            log.debug("Closed the sender");
            return null;
        } catch (Exception e) {
            return ASBUtils.returnErrorValue(e.getClass().getSimpleName(), e);
        }
    }
}
