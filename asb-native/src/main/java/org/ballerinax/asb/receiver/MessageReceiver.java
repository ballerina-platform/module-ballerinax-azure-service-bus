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
 * KINDither express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinax.asb.receiver;

import com.microsoft.azure.servicebus.ClientFactory;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.types.BArrayType;
import org.ballerinax.asb.sender.MessageSender;
import org.ballerinax.asb.util.ASBConstants;
import org.ballerinax.asb.util.ASBUtils;

import java.time.Duration;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

import static org.ballerinax.asb.util.ASBConstants.RECEIVEANDDELETE;

/**
 * This facilitates the client operations of MessageReceiver client in Ballerina.
 */
public class MessageReceiver {
    private final Logger log = Logger.getLogger(MessageSender.class.getName());
    IMessageReceiver receiver;
    String entityPath;

    public MessageReceiver(String connectionString, String entityPath, String receiveMode) throws ServiceBusException, InterruptedException {
        this.entityPath = entityPath;
        if (Objects.equals(receiveMode, RECEIVEANDDELETE)) {
            this.receiver = ClientFactory.createMessageReceiverFromConnectionStringBuilder(
                    new ConnectionStringBuilder(connectionString, entityPath), ReceiveMode.RECEIVEANDDELETE);
        } else {
            this.receiver = ClientFactory.createMessageReceiverFromConnectionStringBuilder(
                    new ConnectionStringBuilder(connectionString, entityPath), ReceiveMode.PEEKLOCK);
        }
    }

    /**
     * Receive Message with configurable parameters as Map when Receiver Connection is given as a parameter and
     * server wait time in seconds to receive message and return Message object.
     *
     * @param serverWaitTime Specified server wait time in seconds to receive message.
     * @return Message Object of the received message.
     */
    public Object receive(Object serverWaitTime) {
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
            log.info("\t<= Received a message with messageBody \n" + receivedMessage.getMessageBody());
            log.info("\tDone receiving messages from \n" + receiver.getEntityPath());
            Object[] values = new Object[14];
            values[0] = ValueCreator.createArrayValue(receivedMessage.getMessageBody().getBinaryData().get(0));
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
                    ValueCreator.createRecordValue(ASBConstants.PACKAGE_ID_ASB, ASBConstants.APPLICATION_PROPERTIES);
            Object[] propValues = new Object[1];
            propValues[0] = ASBUtils.toBMap(receivedMessage.getProperties());
            values[13] = ValueCreator.createRecordValue(applicationProperties, propValues);
            BMap<BString, Object> messageRecord =
                    ValueCreator.createRecordValue(ASBConstants.PACKAGE_ID_ASB, ASBConstants.MESSAGE_RECORD);
            return ValueCreator.createRecordValue(messageRecord, values);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ServiceBusException e) {
            return ASBUtils.returnErrorValue("Exception while receiving message" +e.getMessage());
        }
    }

    /**
     * Receive Batch of Messages with configurable parameters as Map when Receiver Connection is given as a parameter,
     * maximum message count in a batch as int, server wait time in seconds and return Batch Message object.
     *
     * @param maxMessageCount Maximum no. of messages in a batch
     * @return Batch Message Object of the received batch of messages.
     */
    public Object receiveBatch(Object maxMessageCount, Object serverWaitTime) {
        try {
            // receive batch of messages from queue or subscription
            log.info("\n\tWaiting up to 'serverWaitTime' seconds for messages from\n" + receiver.getEntityPath());
            int messageCount = 0;
            int maxCount = Long.valueOf(maxMessageCount.toString()).intValue();
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
                    ValueCreator.createRecordValue(ASBConstants.PACKAGE_ID_ASB, ASBConstants.APPLICATION_PROPERTIES);
            BMap<BString, Object> messageRecord =
                    ValueCreator.createRecordValue(ASBConstants.PACKAGE_ID_ASB, ASBConstants.MESSAGE_RECORD);
            for (IMessage receivedMessage : receivedMessages) {
                Object[] values = new Object[14];
                values[0] = ValueCreator.createArrayValue(receivedMessage.getMessageBody().getBinaryData().get(0));
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
                propValues[0] = ASBUtils.toBMap(receivedMessage.getProperties());
                values[13] = ValueCreator.createRecordValue(applicationProperties, propValues);
                messages[messageCount] = ValueCreator.createRecordValue(messageRecord, values);
                messageCount = messageCount + 1;
            }
            BArrayType sourceArrayType = new BArrayType(TypeUtils.getType(messageRecord));
            messagesRecordValues[0] = messageCount;
            messagesRecordValues[1] = ValueCreator.createArrayValue(messages, sourceArrayType);

            BMap<BString, Object> messagesRecord =
                    ValueCreator.createRecordValue(ASBConstants.PACKAGE_ID_ASB, ASBConstants.MESSAGE_BATCH_RECORD);

            return ValueCreator.createRecordValue(messagesRecord, messagesRecordValues);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ServiceBusException e) {
            return ASBUtils.returnErrorValue("Exception while receiving messages" +e.getMessage());
        }
    }

    /**
     * Complete Messages from Queue or Subscription based on messageLockToken
     *
     * @param lockToken Message lock token.
     */
    public Object complete(Object lockToken) {
        try {
            log.info("\t<= Completes a message with messageLockToken \n" + lockToken);
            receiver.complete(UUID.fromString(lockToken.toString()));
            log.info("\tDone completing a message using its lock token from \n" + receiver.getEntityPath());
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ServiceBusException e) {
            return ASBUtils.returnErrorValue("Exception while completing message" +e.getMessage());
        }
    }

    /**
     * Abandon message & make available again for processing from Queue or Subscription based on messageLockToken
     *
     * @param lockToken Message lock token.
     */
    public Object abandon(Object lockToken) {
        try {
            log.info("\t<= Abandon a message with messageLockToken \n" + lockToken);
            receiver.abandon(UUID.fromString(lockToken.toString()));
            log.info("\tDone abandoning a message using its lock token from \n" + receiver.getEntityPath());
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ServiceBusException e) {
            return ASBUtils.returnErrorValue("Exception while abandon message" +e.getMessage());
        }
    }

    /**
     * Dead-Letter the message & moves the message to the Dead-Letter Queue based on messageLockToken
     *
     * @param lockToken                  Message lock token.
     * @param deadLetterReason           The dead letter reason.
     * @param deadLetterErrorDescription The dead letter error description.
     */
    public Object deadLetter(Object lockToken, Object deadLetterReason,
                             Object deadLetterErrorDescription) {
        try {
            log.info("\t<= Dead-Letter a message with messageLockToken \n" + lockToken);
            receiver.deadLetter(UUID.fromString(lockToken.toString()), ASBUtils.valueToEmptyOrToString(deadLetterReason),
                    ASBUtils.valueToEmptyOrToString(deadLetterErrorDescription));
            log.info("\tDone dead-lettering a message using its lock token from \n" + receiver.getEntityPath());
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ServiceBusException e) {
            return ASBUtils.returnErrorValue("Exception while dead lettering message" +e.getMessage());
        }
    }

    /**
     * Defer the message in a Queue or Subscription based on messageLockToken
     *
     * @param lockToken Message lock token.
     */
    public Object defer(Object lockToken) {
        try {
            log.info("\t<= Defer a message with messageLockToken \n" + lockToken);
            receiver.defer(UUID.fromString(lockToken.toString()));
            log.info("\tDone deferring a message using its lock token from \n" +
                    receiver.getEntityPath());
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ServiceBusException e) {
            return ASBUtils.returnErrorValue("Exception while deferring message" +e.getMessage());
        }
    }

    /**
     * Receives a deferred Message. Deferred messages can only be received by using sequence number and return
     * Message object.
     *
     * @param sequenceNumber Unique number assigned to a message by Service Bus. The sequence number is a unique 64-bit
     *                       integer assigned to a message as it is accepted and stored by the broker and functions as
     *                       its true identifier.
     * @return The received Message or null if there is no message for given sequence number.
     */
    public Object receiveDeferred(int sequenceNumber) {
        try {
            log.info("\n\tWaiting up to default server Wait Time for messages from\n" + receiver.getEntityPath());
            IMessage receivedMessage = receiver.receiveDeferredMessage(sequenceNumber);
            if (receivedMessage == null) {
                return null;
            }
            log.info("\t<= Received a message with messageId \n" + receivedMessage.getMessageId());
            log.info("\t<= Received a message with messageBody \n" + receivedMessage.getMessageBody());
            log.info("\tDone receiving messages from \n" + receiver.getEntityPath());

            Object[] values = new Object[14];
            values[0] = ValueCreator.createArrayValue(receivedMessage.getMessageBody().getBinaryData().get(0));
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
                    ValueCreator.createRecordValue(ASBConstants.PACKAGE_ID_ASB, ASBConstants.APPLICATION_PROPERTIES);
            Object[] propValues = new Object[1];
            propValues[0] = ASBUtils.toBMap(receivedMessage.getProperties());
            values[13] = ValueCreator.createRecordValue(applicationProperties, propValues);
            BMap<BString, Object> messageRecord =
                    ValueCreator.createRecordValue(ASBConstants.PACKAGE_ID_ASB, ASBConstants.MESSAGE_RECORD);
            return ValueCreator.createRecordValue(messageRecord, values);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ServiceBusException e) {
            return ASBUtils.returnErrorValue("Exception while receiving a deferred message" +e.getMessage());
        }
    }

    /**
     * The operation renews lock on a message in a queue or subscription based on messageLockToken.
     *
     * @param lockToken Message lock token.
     */
    public Object renewLock(Object lockToken) {
        try {
            log.info("\t<= Renew message with messageLockToken \n" + lockToken);
            receiver.renewMessageLock(UUID.fromString(lockToken.toString()));
            log.info("\tDone renewing a message using its lock token from \n" +
                    receiver.getEntityPath());
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ServiceBusException e) {
            return ASBUtils.returnErrorValue("Exception while renewing a lock on a message" +e.getMessage());
        }
    }

    /**
     * Set the prefetch count of the receiver.
     * Prefetch speeds up the message flow by aiming to have a message readily available for local retrieval when and
     * before the application asks for one using Receive. Setting a non-zero value prefetches PrefetchCount
     * number of messages. Setting the value to zero turns prefetch off. For both PEEKLOCK mode and
     * RECEIVEANDDELETE mode, the default value is 0.
     *
     * @param prefetchCount The desired prefetch count.
     */
    public Object setPrefetchCount(int prefetchCount) {
        try {
            receiver.setPrefetchCount(prefetchCount);
            return null;
        } catch (ServiceBusException e) {
            return ASBUtils.returnErrorValue("Exception while setting prefetch count of the receiver" +e.getMessage());
        }
    }

    /**
     * Closes the Asb Receiver Connection using the given connection parameters.
     */
    public Object closeReceiver() {
        try {
            receiver.close();
            return null;
        } catch (ServiceBusException e) {
            return ASBUtils.returnErrorValue("Exception while closing the receiver" +e.getMessage());
        }
    }
}
