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
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
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
import org.apache.log4j.Logger;
import org.ballerinax.asb.util.ASBConstants;
import org.ballerinax.asb.util.ASBUtils;
import org.ballerinax.asb.util.ModuleUtils;

import static org.ballerinax.asb.util.ASBConstants.RECEIVE_AND_DELETE;

/**
 * This facilitates the client operations of MessageReceiver client in
 * Ballerina.
 */
public class MessageReceiver {
    private static final Logger log = Logger.getLogger(MessageReceiver.class);
    ServiceBusReceiverClient receiver;

    /**
     * Parameterized constructor for Message Receiver (IMessageReceiver).
     *
     * @param connectionString Azure service bus connection string.
     * @param queueName        QueueName
     * @param topicName        Topic Name
     * @param subscriptionName Subscription Name
     * @param receiveMode      Receive Mode as PeekLock or Receive&Delete.
     * @throws ServiceBusException  on failure initiating IMessage Receiver in Azure
     *                              Service Bus instance.
     * @throws InterruptedException on failure initiating IMessage Receiver due to
     *                              thread interruption.
     */
    public MessageReceiver(String connectionString, String queueName, String topicName, String subscriptionName,
            String receiveMode)
            throws ServiceBusException, InterruptedException {
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
                        .buildClient();
            }
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
            if (log.isDebugEnabled()) {
                log.debug("Received a message with messageId: " + receivedMessage.getMessageId());
            }
            return getReceivedMessage(endpointClient, receivedMessage);
        } catch (ServiceBusException e) {
            log.error(ASBConstants.LOG_ERROR_MSG, e);
            return ASBUtils.returnErrorValue("ServiceBusException while receiving message" + e.getMessage());
        } catch (IOException e) {
            log.error(ASBConstants.LOG_ERROR_MSG, e);
            return ASBUtils.returnErrorValue("IOException while receiving message" + e.getMessage());
        } catch (Exception e) {
            log.error(ASBConstants.LOG_ERROR_MSG, e);
            return ASBUtils.returnErrorValue("Exception while receiving message" + e.getMessage());
        }
    }

    /**
     * Converts AMPQ Body value to Java objects
     * @param amqpValue AMQP Value type object
     * @return
     */
    public Object convertAMQPToJava(Object amqpValue) {
        if (log.isDebugEnabled()) {
            log.debug("Type of amqpValue object is " + amqpValue.getClass());
        }
        if (amqpValue instanceof Integer) {
            return (Integer) amqpValue;
        } else if (amqpValue instanceof Long) {
            return (Long) amqpValue;
        } else if (amqpValue instanceof Float) {
            return (Float) amqpValue;
        } else if (amqpValue instanceof Double) {
            return (Double) amqpValue;
        } else if (amqpValue instanceof String) {
            return (String) amqpValue;
        } else if (amqpValue instanceof Boolean) {
            return (Boolean) amqpValue;
        } else if (amqpValue instanceof Byte) {
            return (Byte) amqpValue;
        } else if (amqpValue instanceof Short) {
            return (Short) amqpValue;
        } else if (amqpValue instanceof Character) {
            return (Character) amqpValue;
        } else if (amqpValue instanceof BigDecimal) {
            return (BigDecimal) amqpValue;
        } else if (amqpValue instanceof java.util.Date) {
            return (java.util.Date) amqpValue;
        } else if (amqpValue instanceof UUID) {
            return (UUID) amqpValue;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Type of amqpValue object " + amqpValue.getClass() + " is not supported");
            }
            return null;
        }
    }

    /**
     * Prepares the message body content
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
                if (log.isDebugEnabled()) {
                    log.debug("Received a message with messageId" + receivedMessage.getMessageId() 
                    + "AMQPMessageBodyType:" + bodyType);
                }
                amqpValue = convertAMQPToJava(amqpValue);
                return amqpValue;
            default:
                throw new RuntimeException("Invalid message body type: " + receivedMessage.getMessageId());
        }
    }

    /**
     * @param endpointClient Ballerina client object
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
        BMap<BString, Object> applicationProperties = ValueCreator.createRecordValue(ModuleUtils.getModule(),
                ASBConstants.APPLICATION_PROPERTIES);
        Object appProperties = ASBUtils.toBMap(receivedMessage.getApplicationProperties());
        map.put("applicationProperties", ValueCreator.createRecordValue(applicationProperties, appProperties));
        BMap<BString, Object> createRecordValue = ValueCreator.createRecordValue(ModuleUtils.getModule(),
                ASBConstants.MESSAGE_RECORD, map);
        endpointClient.addNativeData(receivedMessage.getLockToken(), receivedMessage);
        return createRecordValue;
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
            log.error(ASBConstants.LOG_ERROR_MSG, e);
            Thread.currentThread().interrupt();
            return null;
        } catch (ServiceBusException e) {
            log.error(ASBConstants.LOG_ERROR_MSG, e);
            return ASBUtils.returnErrorValue("ServiceBus Exception while receiving messages" + e.getMessage());
        } catch (Exception e) {
            log.error(ASBConstants.LOG_ERROR_MSG, e);
            return ASBUtils.returnErrorValue("Exception while processing messages" + e.getMessage());
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
            if (log.isDebugEnabled()) {
                log.debug("Completes a message with messageLockToken \n" + lockToken);
            }
            return null;
        } catch (ServiceBusException e) {
            log.error(ASBConstants.LOG_ERROR_MSG, e);
            return ASBUtils.returnErrorValue("ServiceBusException while completing message " + e.getMessage());
        } catch (Exception e) {
            log.error(ASBConstants.LOG_ERROR_MSG, e);
            return ASBUtils.returnErrorValue("Exception while completing message " + e.getMessage());
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
            if (log.isDebugEnabled()) {
                log.debug("Done abandoning a message using its lock token from \n" + receiver.getEntityPath());
            }
            return null;
        } catch (ServiceBusException e) {
            log.error(ASBConstants.LOG_ERROR_MSG, e);
            return ASBUtils.returnErrorValue("Exception while abandoning message" + e.getMessage());
        } catch (Exception e) {
            log.error(ASBConstants.LOG_ERROR_MSG, e);
            return ASBUtils.returnErrorValue("Exception while abandoning message" + e.getMessage());
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
            if (log.isDebugEnabled()) {
                log.debug("Done dead-lettering a message using its lock token from " + receiver.getEntityPath());
            }
            return null;
        } catch (ServiceBusException e) {
            log.error(ASBConstants.LOG_ERROR_MSG, e);
            return ASBUtils.returnErrorValue("Exception while dead lettering message" + e.getMessage());
        } catch (Exception e) {
            log.error(ASBConstants.LOG_ERROR_MSG, e);
            return ASBUtils.returnErrorValue("Exception while dead lettering message" + e.getMessage());
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
            if (log.isDebugEnabled()) {
                log.debug("Done deferring a message using its lock token from " + receiver.getEntityPath());
            }
            return null;
        } catch (ServiceBusException e) {
            log.error(ASBConstants.LOG_ERROR_MSG, e);
            return ASBUtils.returnErrorValue("Exception while deferring message" + e.getMessage());
        } catch (Exception e) {
            log.error(ASBConstants.LOG_ERROR_MSG, e);
            return ASBUtils.returnErrorValue("Exception while deferring message" + e.getMessage());
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
            if (log.isDebugEnabled()) {
                log.debug("Receive deferred message using its sequenceNumber from " + receiver.getEntityPath());
            }
            return getReceivedMessage(endpointClient, receivedMessage);
        } catch (ServiceBusException e) {
            return ASBUtils.returnErrorValue("Exception while receiving a deferred message" + e.getMessage());
        } catch (Exception e) {
            return ASBUtils.returnErrorValue("Exception while receiving a deferred message" + e.getMessage());
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
            if (log.isDebugEnabled()) {
                log.debug("Done renewing a message using its lock token from " + receiver.getEntityPath());
            }
            return null;
        } catch (ServiceBusException e) {
            log.error(ASBConstants.LOG_ERROR_MSG, e);
            return ASBUtils.returnErrorValue("ServiceBusException while renewing a lock on a message" + e.getMessage());
        } catch (Exception e) {
            log.error(ASBConstants.LOG_ERROR_MSG, e);
            return ASBUtils.returnErrorValue("Exception while renewing a lock on a message" + e.getMessage());
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
            return null;
        } catch (ServiceBusException e) {
            log.error(ASBConstants.LOG_ERROR_MSG, e);
            return ASBUtils.returnErrorValue("ServiceBusException while closing the receiver" + e.getMessage());
        }
    }
}
