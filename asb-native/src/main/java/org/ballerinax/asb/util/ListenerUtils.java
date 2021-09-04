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

package org.ballerinax.asb.util;

import com.microsoft.azure.servicebus.ClientFactory;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import org.ballerinax.asb.MessageDispatcher;

import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.ballerinax.asb.MessageDispatcher.*;
import static org.ballerinax.asb.util.ASBConstants.*;

/**
 * Util class used to bridge the listener capabilities of the Asb connector's native code and the Ballerina API.
 */
public class ListenerUtils {
    private static final Logger log = Logger.getLogger(ListenerUtils.class.getName());

    private static Runtime runtime;

    private static boolean started = false;
    private static boolean serviceAttached = false;
    private static ArrayList<BObject> services = new ArrayList<>();
    private static ArrayList<BObject> startedServices = new ArrayList<>();

    /**
     * Initialize the ballerina listener object.
     *
     * @param listenerBObject Ballerina listener object.
     */
    public static void init(BObject listenerBObject) {
        listenerBObject.addNativeData(ASBConstants.CONSUMER_SERVICES, services);
        listenerBObject.addNativeData(ASBConstants.STARTED_SERVICES, startedServices);
    }

    /**
     * Attaches the service to the Asb listener endpoint.
     *
     * @param listenerBObject Ballerina listener object..
     * @param service         Ballerina service instance.
     * @return An error if failed to create IMessageReceiver connection instance.
     */
    public static Object registerListener(Environment environment, BObject listenerBObject, BObject service) {
        runtime = environment.getRuntime();
        try {
            String connectionString = getConnectionStringFromConfig(service);
            String entityPath = getQueueNameFromConfig(service);
            String receiveMode = getReceiveModeFromConfig(service);
            IMessageReceiver receiver;
            if (receiveMode.equals(RECEIVEANDDELETE)) {
                receiver = ClientFactory.createMessageReceiverFromConnectionStringBuilder(
                        new ConnectionStringBuilder(connectionString, entityPath), ReceiveMode.RECEIVEANDDELETE);
            } else {
                receiver = ClientFactory.createMessageReceiverFromConnectionStringBuilder(
                        new ConnectionStringBuilder(connectionString, entityPath), ReceiveMode.PEEKLOCK);
            }
            listenerBObject.addNativeData(ASBConstants.CONNECTION_NATIVE_OBJECT, receiver);
        } catch (InterruptedException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        } catch (ServiceBusException e) {
            throw ASBUtils.returnErrorValue("Current thread was interrupted while waiting "
                    + e.getMessage());
        }
        IMessageReceiver receiveClient =
                (IMessageReceiver) listenerBObject.getNativeData(ASBConstants.CONNECTION_NATIVE_OBJECT);
        if (service == null) {
            return null;
        }
        if (isStarted()) {
            services = (ArrayList<BObject>) listenerBObject.getNativeData(ASBConstants.CONSUMER_SERVICES);
            startReceivingMessages(service, listenerBObject, receiveClient);
        }
        services.add(service);
        return null;
    }

    /**
     * Starts consuming the messages on all the attached services.
     *
     * @param listenerBObject Ballerina listener object.
     */
    public static Object start(Environment environment, BObject listenerBObject) {
        runtime = environment.getRuntime();
        IMessageReceiver iMessageReceiver =
                (IMessageReceiver) listenerBObject.getNativeData(ASBConstants.CONNECTION_NATIVE_OBJECT);
        @SuppressWarnings(ASBConstants.UNCHECKED)
        ArrayList<BObject> services =
                (ArrayList<BObject>) listenerBObject.getNativeData(ASBConstants.CONSUMER_SERVICES);
        @SuppressWarnings(ASBConstants.UNCHECKED)
        ArrayList<BObject> startedServices =
                (ArrayList<BObject>) listenerBObject.getNativeData(ASBConstants.STARTED_SERVICES);
        if (services == null || services.isEmpty()) {
            return null;
        }
        for (BObject service : services) {
            if (startedServices == null || !startedServices.contains(service)) {
                serviceAttached = true;
                MessageDispatcher messageDispatcher =
                        new MessageDispatcher(service, runtime, iMessageReceiver);
                messageDispatcher.receiveMessages(listenerBObject);
            }
        }
        started = true;
        return null;
    }

    /**
     * Stops consuming messages and detaches the service from the Asb Listener endpoint.
     *
     * @param listenerBObject Ballerina listener object..
     * @param service         Ballerina service instance.
     * @return An error if failed detaching the service.
     */
    public static Object detach(BObject listenerBObject, BObject service) {
        IMessageReceiver iMessageReceiver =
                (IMessageReceiver) listenerBObject.getNativeData(ASBConstants.CONNECTION_NATIVE_OBJECT);
        @SuppressWarnings(ASBConstants.UNCHECKED)
        ArrayList<BObject> startedServices =
                (ArrayList<BObject>) listenerBObject.getNativeData(ASBConstants.STARTED_SERVICES);
        @SuppressWarnings(ASBConstants.UNCHECKED)
        ArrayList<BObject> services =
                (ArrayList<BObject>) listenerBObject.getNativeData(ASBConstants.CONSUMER_SERVICES);
        String queueName = (String) service.getNativeData(ASBConstants.QUEUE_NAME.getValue());

        serviceAttached = false;
        log.info("Consumer service unsubscribed from the queue " + queueName);

        listenerBObject.addNativeData(ASBConstants.CONSUMER_SERVICES, removeFromList(services, service));
        listenerBObject.addNativeData(ASBConstants.STARTED_SERVICES, removeFromList(startedServices, service));
        return null;
    }

    /**
     * Stops consuming messages through all consumer services by terminating the connection.
     *
     * @param listenerBObject Ballerina listener object.
     */
    public static Object stop(BObject listenerBObject) {
        IMessageReceiver iMessageReceiver =
                (IMessageReceiver) listenerBObject.getNativeData(ASBConstants.CONNECTION_NATIVE_OBJECT);
        if (iMessageReceiver == null) {
            return ASBUtils.returnErrorValue("IMessageReceiver is not properly initialised.");
        } else {
            try {
                serviceAttached = false;
                iMessageReceiver.close();
                log.info("Consumer service stopped");
            } catch (ServiceBusException e) {
                return ASBUtils.returnErrorValue("Error occurred while stopping the service");
            }
        }
        return null;
    }

    /**
     * Stops consuming messages through all the consumer services and terminates the connection with server.
     *
     * @param listenerBObject Ballerina listener object.
     */
    public static Object abortConnection(BObject listenerBObject) {
        IMessageReceiver iMessageReceiver =
                (IMessageReceiver) listenerBObject.getNativeData(ASBConstants.CONNECTION_NATIVE_OBJECT);
        if (iMessageReceiver == null) {
            return ASBUtils.returnErrorValue("IMessageReceiver is not properly initialised.");
        } else {
            try {
                iMessageReceiver.close();
                log.info("Consumer service stopped");
            } catch (ServiceBusException e) {
                return ASBUtils.returnErrorValue("Error occurred while stopping the service");
            }
        }
        return null;
    }

    /**
     * Get the receiver used by the listener.
     *
     * @param listenerBObject Ballerina listener object.
     */
    public static Object getReceiver(BObject listenerBObject) {
        IMessageReceiver iMessageReceiver =
                (IMessageReceiver) listenerBObject.getNativeData(ASBConstants.CONNECTION_NATIVE_OBJECT);
        if (iMessageReceiver == null) {
            return ASBUtils.returnErrorValue("IMessageReceiver is not properly initialised.");
        }
        return iMessageReceiver;
    }

    /**
     * Starts consuming the messages by calling the message dispatcher.
     *
     * @param service  Ballerina service instance.
     * @param listener Ballerina listener object.
     */
    private static void startReceivingMessages(BObject service, BObject listener, IMessageReceiver iMessageReceiver) {
        MessageDispatcher messageDispatcher =
                new MessageDispatcher(service, runtime, iMessageReceiver);
        messageDispatcher.receiveMessages(listener);

    }

    /**
     * Complete Messages from Queue or Subscription based on messageLockToken
     *
     * @param receiver  Output Receiver connection.
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
     * @param receiver  Output Receiver connection.
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
     * @param receiver                   Output Receiver connection.
     * @param lockToken                  Message lock token.
     * @param deadLetterReason           The dead letter reason.
     * @param deadLetterErrorDescription The dead letter error description.
     */
    public static Object deadLetter(IMessageReceiver receiver, Object lockToken, Object deadLetterReason,
                                    Object deadLetterErrorDescription) {
        try {
            log.info("\t<= Dead-Letter a message with messageLockToken \n" + lockToken);
            receiver.deadLetter(UUID.fromString(lockToken.toString()), ASBUtils.valueToEmptyOrToString(deadLetterReason),
                    ASBUtils.valueToEmptyOrToString(deadLetterErrorDescription));
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
     * @param receiver  Output Receiver connection.
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
     * @param receiver       Output Receiver connection.
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
            propValues[0] = ASBUtils.toBMap(receivedMessage.getProperties());
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
     * @param receiver  Output Receiver connection.
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
     * @param receiver      Output Receiver connection.
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
     * Removes a given element from the provided array list and returns the resulting list.
     *
     * @param arrayList   The original list
     * @param objectValue Element to be removed
     * @return Resulting list after removing the element
     */
    public static ArrayList<BObject> removeFromList(ArrayList<BObject> arrayList, BObject objectValue) {
        if (arrayList != null) {
            arrayList.remove(objectValue);
        }
        return arrayList;
    }

    private static boolean isStarted() {
        return started;
    }

    public static boolean isServiceAttached() {
        return serviceAttached;
    }
}
