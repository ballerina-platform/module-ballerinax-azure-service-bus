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

import com.microsoft.azure.servicebus.ClientFactory;
import com.microsoft.azure.servicebus.IMessageReceiver;
import com.microsoft.azure.servicebus.ReceiveMode;
import com.microsoft.azure.servicebus.primitives.ConnectionStringBuilder;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.values.BObject;
import org.apache.log4j.Logger;
import org.ballerinax.asb.MessageDispatcher;
import org.ballerinax.asb.util.ASBConstants;
import org.ballerinax.asb.util.ASBUtils;

import java.util.ArrayList;

import static org.ballerinax.asb.util.ASBConstants.ASB_CALLER;
import static org.ballerinax.asb.util.ASBConstants.RECEIVEANDDELETE;

/**
 * Listens to incoming messages from Azure Service Bus.
 */
public class MessageListener {
    private static final Logger log = Logger.getLogger(Caller.class);

    private static Runtime runtime;
    private static boolean started = false;
    private static boolean serviceAttached = false;
    private static ArrayList<BObject> services = new ArrayList<>();
    private static ArrayList<BObject> startedServices = new ArrayList<>();
    IMessageReceiver receiver;
    private BObject caller;

    /**
     * Initialize Azure Service Bus listener.
     *
     * @param connectionString Azure service bus connection string.
     * @param entityPath       Entity path (QueueName or SubscriptionPath).
     * @param receiveMode      Receive Mode as PeekLock or Receive&Delete.
     * @throws ServiceBusException  on failure initiating IMessage Receiver in Azure Service Bus instance.
     * @throws InterruptedException on failure initiating IMessage Receiver due to thread interruption.
     */
    public MessageListener(String connectionString, String entityPath, String receiveMode) throws ServiceBusException, InterruptedException {
        if (receiveMode.equals(RECEIVEANDDELETE)) {
            this.receiver = ClientFactory.createMessageReceiverFromConnectionStringBuilder(
                    new ConnectionStringBuilder(connectionString, entityPath), ReceiveMode.RECEIVEANDDELETE);
        } else {
            this.receiver = ClientFactory.createMessageReceiverFromConnectionStringBuilder(
                    new ConnectionStringBuilder(connectionString, entityPath), ReceiveMode.PEEKLOCK);
        }
    }

    private static boolean isStarted() {
        return started;
    }

    public static boolean isServiceAttached() {
        return serviceAttached;
    }

    public void externalInit(Environment environment, BObject listenerBObject, BObject caller) {
        this.caller = caller;
        caller.addNativeData(ASB_CALLER, receiver);
    }

    /**
     * Attaches the service to the Asb listener endpoint.
     *
     * @param environment     Ballerina runtime.
     * @param listenerBObject Ballerina listener object..
     * @param service         Ballerina service instance.
     * @return An error if failed to create IMessageReceiver connection instance.
     */
    public Object registerListener(Environment environment, BObject listenerBObject, BObject service) {
        runtime = environment.getRuntime();
        listenerBObject.addNativeData(ASBConstants.CONSUMER_SERVICES, services);
        listenerBObject.addNativeData(ASBConstants.STARTED_SERVICES, startedServices);
        if (service == null) {
            return null;
        }
        if (isStarted()) {
            services = (ArrayList<BObject>) listenerBObject.getNativeData(ASBConstants.CONSUMER_SERVICES);
            startReceivingMessages(service, caller, listenerBObject, receiver);
        }
        services.add(service);
        return null;
    }

    /**
     * Starts consuming the messages on all the attached services.
     *
     * @param environment     Ballerina runtime.
     * @param listenerBObject Ballerina listener object
     * @return An error if failed to start the listener.
     */
    public Object start(Environment environment, BObject listenerBObject) {
        runtime = environment.getRuntime();
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
                        new MessageDispatcher(service, this.caller, runtime, receiver);
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
    public Object detach(BObject listenerBObject, BObject service) {
        @SuppressWarnings(ASBConstants.UNCHECKED)
        ArrayList<BObject> startedServices =
                (ArrayList<BObject>) listenerBObject.getNativeData(ASBConstants.STARTED_SERVICES);
        @SuppressWarnings(ASBConstants.UNCHECKED)
        ArrayList<BObject> services =
                (ArrayList<BObject>) listenerBObject.getNativeData(ASBConstants.CONSUMER_SERVICES);
        String queueName = (String) service.getNativeData(ASBConstants.QUEUE_NAME.getValue());
        serviceAttached = false;
        if (log.isDebugEnabled()) {
            log.debug("Consumer service unsubscribed from the queue " + queueName);
        }
        listenerBObject.addNativeData(ASBConstants.CONSUMER_SERVICES, removeFromList(services, service));
        listenerBObject.addNativeData(ASBConstants.STARTED_SERVICES, removeFromList(startedServices, service));
        return null;
    }

    /**
     * Stops consuming messages through all consumer services by terminating the connection.
     *
     * @param listenerBObject Ballerina listener object.
     * @return An error if listener fails to stop.
     */
    public Object stop(BObject listenerBObject) {
        if (receiver == null) {
            return ASBUtils.returnErrorValue("IMessageReceiver is not properly initialised.");
        } else {
            try {
                serviceAttached = false;
                receiver.close();
                if (log.isDebugEnabled()) {
                    log.debug("Consumer service stopped");
                }
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
     * @return An error if listener fails to abort the connection.
     */
    public Object abortConnection(BObject listenerBObject) {
        if (receiver == null) {
            return ASBUtils.returnErrorValue("IMessageReceiver is not properly initialised.");
        } else {
            try {
                receiver.close();
                if (log.isDebugEnabled()) {
                    log.debug("Consumer service aborted");
                }
            } catch (ServiceBusException e) {
                return ASBUtils.returnErrorValue("Error occurred while stopping the service");
            }
        }
        return null;
    }

    /**
     * Starts consuming the messages by calling the message dispatcher.
     *
     * @param service  Ballerina service instance.
     * @param listener Ballerina listener object.
     * @return An error if listener fails to start receiving messages.
     */
    private void startReceivingMessages(BObject service, BObject caller, BObject listener, IMessageReceiver iMessageReceiver) {
        MessageDispatcher messageDispatcher =
                new MessageDispatcher(service, caller, runtime, iMessageReceiver);
        messageDispatcher.receiveMessages(listener);

    }

    /**
     * Removes a given element from the provided array list and returns the resulting list.
     *
     * @param arrayList   The original list
     * @param objectValue Element to be removed
     * @return Resulting list after removing the element
     */
    private ArrayList<BObject> removeFromList(ArrayList<BObject> arrayList, BObject objectValue) {
        if (arrayList != null) {
            arrayList.remove(objectValue);
        }
        return arrayList;
    }
}
