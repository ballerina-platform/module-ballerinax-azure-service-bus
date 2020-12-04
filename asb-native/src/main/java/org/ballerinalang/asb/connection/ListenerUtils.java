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
import org.ballerinalang.asb.MessageDispatcher;
import org.ballerinalang.jvm.api.BRuntime;
import org.ballerinalang.jvm.api.values.BObject;

import java.util.ArrayList;
import java.util.logging.Logger;

import static org.ballerinalang.asb.MessageDispatcher.getConnectionStringFromConfig;
import static org.ballerinalang.asb.MessageDispatcher.getQueueNameFromConfig;

/**
 * Util class used to bridge the listener capabilities of the Asb connector's native code and the Ballerina API.
 */
public class ListenerUtils {
    private static final Logger log = Logger.getLogger(ListenerUtils.class.getName());

    private static BRuntime runtime;

    private static boolean started = false;
    private static boolean serviceAttached = false;
    private static ArrayList<BObject> services = new ArrayList<>();
    private static ArrayList<BObject> startedServices = new ArrayList<>();

    /**
     * Initialize the ballerina listener object.
     *
     * @param listenerBObject Ballerina listener object.
     * @param iMessageReceiver Asb MessageReceiver instance.
     */
    public static void init(BObject listenerBObject, IMessageReceiver iMessageReceiver) {
        listenerBObject.addNativeData(ASBConstants.CONSUMER_SERVICES, services);
        listenerBObject.addNativeData(ASBConstants.STARTED_SERVICES, startedServices);
//        listenerBObject.addNativeData(AsbConstants.CONNECTION_NATIVE_OBJECT, iMessageReceiver);
    }

    /**
     * Attaches the service to the Asb listener endpoint.
     *
     * @param listenerBObject Ballerina listener object..
     * @param service Ballerina service instance.
     * @return An error if failed to create IMessageReceiver connection instance.
     */
    public static Object registerListener(BObject listenerBObject, BObject service) {
        runtime = BRuntime.getCurrentRuntime();
        try {
            String connectionString = getConnectionStringFromConfig(service);
            String entityPath = getQueueNameFromConfig(service);
            IMessageReceiver receiver = ClientFactory.createMessageReceiverFromConnectionStringBuilder(
                    new ConnectionStringBuilder(connectionString, entityPath), ReceiveMode.PEEKLOCK);
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
    public static Object start(BObject listenerBObject) {
        runtime = BRuntime.getCurrentRuntime();
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
     * @param service Ballerina service instance.
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
        if(iMessageReceiver == null) {
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
        if(iMessageReceiver == null) {
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
     * Starts consuming the messages by calling the message dispatcher.
     *
     * @param service Ballerina service instance.
     * @param listener Ballerina listener object.
     */
    private static void startReceivingMessages(BObject service, BObject listener, IMessageReceiver iMessageReceiver) {
        MessageDispatcher messageDispatcher =
                new MessageDispatcher(service, runtime, iMessageReceiver);
        messageDispatcher.receiveMessages(listener);

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
