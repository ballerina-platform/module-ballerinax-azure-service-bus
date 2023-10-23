/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.values.BObject;
import org.ballerinax.asb.util.ASBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * ASB message listener representation binding it to a Ballerina service.
 */
public class MessageListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageListener.class);

    private Runtime runtime;
    private ArrayList<BObject> services = new ArrayList<>();
    private Map<BObject, MessageDispatcher> dispatcherSet = new HashMap<BObject, MessageDispatcher>();
    private BObject caller;
    private ServiceBusClientBuilder sharedConnectionBuilder;
    private boolean started = false;

    /**
     * Initializes Azure Service Bus listener. This creates a connection to the pointed
     * Azure Service Bus. Actual listeners will get created with the information
     * of attached services.
     *
     * @param connectionString Azure service bus connection string.
     */
    public MessageListener(String connectionString, String logLevel) {
        this.sharedConnectionBuilder = new ServiceBusClientBuilder().connectionString(connectionString);
        LOGGER.debug("ServiceBusListnerClient initialized");
    }

    /**
     * Attaches Caller object to the listener. 
     * 
     * @param caller object represeting Ballerina Caller 
     */
    public void externalInit(BObject caller) {
        this.caller = caller; 
    }

    /**
     * Attaches the service to the ASB listener endpoint. Here, a new ASB message processor client 
     * is created internally with the message dispatcher, but not started.
     *
     * @param environment     Ballerina runtime
     * @param listenerBObject Ballerina listener object
     * @param service         Ballerina service instance
     * @return An error if failed to create IMessageReceiver connection instance
     */
    public Object attach(Environment environment, BObject listenerBObject, BObject service) {
        try {
            runtime = environment.getRuntime();
            if (service == null) {
                throw new IllegalArgumentException("Service object is null. Cannot attach to the listener");
            }
            if (!services.contains(service)) {
                // We only create the dispatcher object here, but not start
                MessageDispatcher msgDispatcher = new MessageDispatcher(runtime, service, caller,
                        sharedConnectionBuilder);
                services.add(service);
                dispatcherSet.put(service, msgDispatcher);
            } else {
                throw new IllegalStateException("Service already attached.");
            }
            return null;
        } catch (IllegalStateException | IllegalArgumentException ex) {
            return ASBUtils.createErrorValue("Error when attaching service to the listener and stating processor.", ex);
        } catch (Exception ex) {
            return ASBUtils.createErrorValue("An unexpected error occurred.", ex);
        }
    }

    /**
     * Starts consuming the messages on all the attached 
     * services if not already started.
     *
     * @param listenerBObject Ballerina listener object
     * @return An error if failed to start the listener
     */
    public Object start(BObject listenerBObject) {
        if (services.isEmpty()) {
            return ASBUtils.createErrorValue("No attached services found");
        }
        for (BObject service : services) {
            try {
                startMessageDispatch(service);
            } catch (Exception e) {
                return ASBUtils.createErrorValue("Error while starting message listening for service = ", e);
            }
        }
        started = true;
        return null;
    }

    /**
     * Stops consuming messages and detaches the service from the ASB Listener
     * endpoint.
     *
     * @param listenerBObject Ballerina listener object.
     * @param service         Ballerina service instance.
     * @return An error if failed detaching the service.
     */
    public Object detach(BObject listenerBObject, BObject service) {
        try {
            stopMessageDispatch(service);
        } catch (Exception e) {
            return ASBUtils.createErrorValue("Error while closing the processor client id = " + dispatcherSet
                    .get(service).getProcessorClient().getIdentifier() + " upon detaching service");
        }
        services.remove(service);
        dispatcherSet.remove(service);
        return null;
    }

    /**
     * Stops consuming messages through all consumer services by terminating the
     * listeners and connection.
     *
     * @param listenerBObject Ballerina listener object.
     * @return An error if listener fails to stop.
     */
    public Object stop(BObject listenerBObject) {
        if (!started) {
            return ASBUtils.createErrorValue("Listener has not started.");
        } else {
            for (BObject service : services) {
                stopMessageDispatch(service);
            }
            services.clear();
            dispatcherSet.clear();
        }
        return null;
    }

    /**
     * Stops consuming messages through all the consumer services and terminates the
     * listeners and the connection with server.
     *
     * @param listenerBObject Ballerina listener object.
     * @return An error if listener fails to abort the connection.
     */
    public Object forceStop(BObject listenerBObject) {
        stop(listenerBObject);
        return null;
    }

    private void stopMessageDispatch(BObject service) {
        MessageDispatcher msgDispatcher = dispatcherSet.get(service);
        msgDispatcher.stopListeningAndDispatching();
    }

    private void startMessageDispatch(BObject service) {
        MessageDispatcher msgDispatcher = dispatcherSet.get(service);
        if (!msgDispatcher.isRunning()) {
            msgDispatcher.startListeningAndDispatching();
        }
    }
}
