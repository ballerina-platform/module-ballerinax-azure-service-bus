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

package org.ballerinax.asb.listener;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import org.apache.log4j.Logger;
import org.ballerinax.asb.util.ASBConstants;
import org.ballerinax.asb.util.ASBUtils;
import org.ballerinax.asb.util.ModuleUtils;

import java.util.UUID;

import static org.ballerinax.asb.util.ASBConstants.ASB_CALLER;

/**
 * Perform operations on dispatched messages.
 */
public class Caller {

    private static final Logger log = Logger.getLogger(Caller.class);

    /**
     * Complete Messages from Queue or Subscription based on messageLockToken
     *
     * @param caller    Ballerina Caller object.
     * @param lockToken Message lock token.
     * @return InterruptedException or ServiceBusException on failure to complete the message.
     */
    public static Object complete(BObject caller, Object lockToken) {
        try {
            IMessageReceiver receiver = (IMessageReceiver) caller.getNativeData(ASB_CALLER);
            receiver.complete(UUID.fromString(lockToken.toString()));
            if (log.isDebugEnabled()) {
                log.debug("\tDone completing a message using its lock token from \n" + receiver.getEntityPath());
            }
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ServiceBusException e) {
            return ASBUtils.returnErrorValue("Exception while completing message" + e.getMessage());
        }
    }

    /**
     * Abandon message & make available again for processing from Queue or Subscription based on messageLockToken
     *
     * @param caller    Ballerina Caller object.
     * @param lockToken Message lock token.
     * @return InterruptedException or ServiceBusException on failure to abandon the message.
     */
    public static Object abandon(BObject caller, Object lockToken) {
        try {
            IMessageReceiver receiver = (IMessageReceiver) caller.getNativeData(ASB_CALLER);
            receiver.abandon(UUID.fromString(lockToken.toString()));
            if (log.isDebugEnabled()) {
                log.debug("\tDone abandoning a message using its lock token from \n" + receiver.getEntityPath());
            }
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ServiceBusException e) {
            return ASBUtils.returnErrorValue("Exception while abandon message" + e.getMessage());
        }
    }

    /**
     * Dead-Letter the message & moves the message to the Dead-Letter Queue based on messageLockToken
     *
     * @param caller                     Ballerina Caller object.
     * @param lockToken                  Message lock token.
     * @param deadLetterReason           The dead letter reason.
     * @param deadLetterErrorDescription The dead letter error description.
     * @return InterruptedException or ServiceBusException on failure to dead letter the message.
     */
    public static Object deadLetter(BObject caller, Object lockToken, Object deadLetterReason,
                                    Object deadLetterErrorDescription) {
        try {
            IMessageReceiver receiver = (IMessageReceiver) caller.getNativeData(ASB_CALLER);
            receiver.deadLetter(UUID.fromString(lockToken.toString()), ASBUtils.valueToEmptyOrToString(deadLetterReason),
                    ASBUtils.valueToEmptyOrToString(deadLetterErrorDescription));
            if (log.isDebugEnabled()) {
                log.debug("\tDone dead-lettering a message using its lock token from \n" + receiver.getEntityPath());
            }
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ServiceBusException e) {
            return ASBUtils.returnErrorValue("Exception while dead lettering message" + e.getMessage());
        }
    }

    /**
     * Defer the message in a Queue or Subscription based on messageLockToken
     *
     * @param caller    Ballerina Caller object.
     * @param lockToken Message lock token.
     * @return InterruptedException or ServiceBusException on failure to defer the message.
     */
    public static Object defer(BObject caller, Object lockToken) {
        try {
            IMessageReceiver receiver = (IMessageReceiver) caller.getNativeData(ASB_CALLER);
            receiver.defer(UUID.fromString(lockToken.toString()));
            if (log.isDebugEnabled()) {
                log.debug("\tDone deferring a message using its lock token from \n" + receiver.getEntityPath());
            }
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ServiceBusException e) {
            return ASBUtils.returnErrorValue("Exception while deferring message" + e.getMessage());
        }
    }

    /**
     * Receives a deferred Message. Deferred messages can only be received by using sequence number and return
     * Message object.
     *
     * @param caller         Ballerina Caller object.
     * @param sequenceNumber Unique number assigned to a message by Service Bus. The sequence number is a unique 64-bit
     *                       integer assigned to a message as it is accepted and stored by the broker and functions as
     *                       its true identifier.
     * @return The received Message or null if there is no message for given sequence number.
     */
    public static Object receiveDeferred(BObject caller, int sequenceNumber) {
        try {
            IMessageReceiver receiver = (IMessageReceiver) caller.getNativeData(ASB_CALLER);
            IMessage receivedMessage = receiver.receiveDeferredMessage(sequenceNumber);
            if (receivedMessage == null) {
                return null;
            }
            if (log.isDebugEnabled()) {
                log.debug("\t<= Received a message with messageId \n" + receivedMessage.getMessageId());
                log.debug("\t<= Received a message with messageBody \n" + receivedMessage.getMessageBody());
                log.debug("\tDone receiving messages from \n" + receiver.getEntityPath());
            }
            return getMessageObject(receivedMessage);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ServiceBusException e) {
            return ASBUtils.returnErrorValue("Exception while receiving a deferred message" + e.getMessage());
        }
    }

    private static BMap<BString, Object> getMessageObject(IMessage receivedMessage) {
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
                ValueCreator.createRecordValue(ModuleUtils.getModule(), ASBConstants.APPLICATION_PROPERTIES);
        Object[] propValues = new Object[1];
        propValues[0] = ASBUtils.toBMap(receivedMessage.getProperties());
        values[13] = ValueCreator.createRecordValue(applicationProperties, propValues);
        BMap<BString, Object> messageRecord =
                ValueCreator.createRecordValue(ModuleUtils.getModule(), ASBConstants.MESSAGE_RECORD);
        return ValueCreator.createRecordValue(messageRecord, values);
    }

    /**
     * The operation renews lock on a message in a queue or subscription based on messageLockToken.
     *
     * @param caller    Ballerina Caller object.
     * @param lockToken Message lock token.
     * @return InterruptedException or ServiceBusException
     */
    public static Object renewLock(BObject caller, Object lockToken) {
        try {
            IMessageReceiver receiver = (IMessageReceiver) caller.getNativeData(ASB_CALLER);
            receiver.renewMessageLock(UUID.fromString(lockToken.toString()));
            if (log.isDebugEnabled()) {
                log.debug("\tDone renewing a message using its lock token from \n" + receiver.getEntityPath());
            }
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ServiceBusException e) {
            return ASBUtils.returnErrorValue("Exception while renewing a lock on a message" + e.getMessage());
        }
    }

    /**
     * Set the prefetch count of the receiver.
     * Prefetch speeds up the message flow by aiming to have a message readily available for local retrieval when and
     * before the application asks for one using Receive. Setting a non-zero value prefetches PrefetchCount
     * number of messages. Setting the value to zero turns prefetch off. For both PEEKLOCK mode and
     * RECEIVEANDDELETE mode, the default value is 0.
     *
     * @param caller        Ballerina Caller object.
     * @param prefetchCount The desired prefetch count.
     * @return ServiceBusException on failure to Set the prefetch count of the receiver.
     */
    public static Object setPrefetchCount(BObject caller, int prefetchCount) {
        try {
            IMessageReceiver receiver = (IMessageReceiver) caller.getNativeData(ASB_CALLER);
            receiver.setPrefetchCount(prefetchCount);
            return null;
        } catch (ServiceBusException e) {
            return ASBUtils.returnErrorValue("Exception while setting prefetch count of the receiver" + e.getMessage());
        }
    }
}
