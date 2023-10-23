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

import com.azure.core.amqp.models.AmqpAnnotatedMessage;
import com.azure.core.amqp.models.AmqpMessageBodyType;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusProcessorClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusFailureReason;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.MethodType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import org.ballerinax.asb.util.ASBConstants;
import org.ballerinax.asb.util.ASBUtils;
import org.ballerinax.asb.util.ModuleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Creates underlying listener and dispatches messages with data binding.
 */
public class MessageDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageDispatcher.class);

    private Runtime runtime;
    private BObject service;
    private BObject caller;
    private ServiceBusProcessorClient messageProcessor;
    private boolean isStarted = false;

    /**
     * Initializes the Message Dispatcher.
     *
     * @param service             Ballerina service instance
     * @param runtime             Ballerina runtime instance
     * @param sharedClientBuilder ASB message builder instance common to the
     *                            listener
     * @throws IllegalStateException    If input values are wrong
     * @throws IllegalArgumentException If queueName/topicname not set
     * @throws NullPointerException     If callbacks are not set
     */
     MessageDispatcher(Runtime runtime, BObject service,
                             BObject caller, ServiceBusClientBuilder sharedClientBuilder) {

        this.runtime = runtime;
        this.service = service;
        this.caller = caller;
        this.messageProcessor = createMessageProcessor(sharedClientBuilder);
        LOGGER.debug("ServiceBusMessageDispatcher initialized");
    }

    private ServiceBusProcessorClient createMessageProcessor(ServiceBusClientBuilder sharedClientBuilder) {
        String queueName = ASBUtils.getServiceConfigStringValue(service, ASBConstants.QUEUE_NAME_CONFIG_KEY);
        String topicName = ASBUtils.getServiceConfigStringValue(service, ASBConstants.TOPIC_NAME_CONFIG_KEY);
        String subscriptionName = ASBUtils.getServiceConfigStringValue(service,
                ASBConstants.SUBSCRIPTION_NAME_CONFIG_KEY);
        boolean isPeekLockModeEnabled = ASBUtils.isPeekLockModeEnabled(service);
        int maxConcurrentCalls = ASBUtils.getServiceConfigSNumericValue(service,
                ASBConstants.MAX_CONCURRENCY_CONFIG_KEY, ASBConstants.MAX_CONCURRENCY_DEFAULT);
        int prefetchCount = ASBUtils.getServiceConfigSNumericValue(service, ASBConstants.MSG_PREFETCH_COUNT_CONFIG_KEY,
                ASBConstants.MSG_PREFETCH_COUNT_DEFAULT);
        int maxAutoLockRenewDuration = ASBUtils.getServiceConfigSNumericValue(service,
                ASBConstants.LOCK_RENEW_DURATION_CONFIG_KEY, ASBConstants.LOCK_RENEW_DURATION_DEFAULT);

        LOGGER.debug(
                "Initializing message listener with PeekLockModeEnabled = " + isPeekLockModeEnabled
                        + ", maxConcurrentCalls- "
                        + maxConcurrentCalls + ", prefetchCount - " + prefetchCount
                        + ", maxAutoLockRenewDuration(seconds) - " + maxAutoLockRenewDuration);
        // create processor client using sharedClientBuilder attahed to the listener
        ServiceBusProcessorClientBuilder clientBuilder = sharedClientBuilder.processor()
                .maxConcurrentCalls(maxConcurrentCalls)
                .disableAutoComplete()
                .prefetchCount(prefetchCount);
        if (!queueName.isEmpty()) {
            if (isPeekLockModeEnabled) {
                clientBuilder
                        .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
                        .queueName(queueName)
                        .maxAutoLockRenewDuration(Duration.ofSeconds(maxAutoLockRenewDuration));
            } else {
                clientBuilder
                        .receiveMode(ServiceBusReceiveMode.RECEIVE_AND_DELETE)
                        .queueName(queueName);
            }
        } else if (!subscriptionName.isEmpty() && !topicName.isEmpty()) {
            if (isPeekLockModeEnabled) {
                clientBuilder
                        .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
                        .topicName(topicName)
                        .subscriptionName(subscriptionName)
                        .maxAutoLockRenewDuration(Duration.ofSeconds(maxAutoLockRenewDuration));
            } else {
                clientBuilder
                        .receiveMode(ServiceBusReceiveMode.RECEIVE_AND_DELETE)
                        .topicName(topicName)
                        .subscriptionName(subscriptionName);
            }
        }
        ServiceBusProcessorClient processorClient = clientBuilder.processMessage(t -> {
            try {
                this.processMessage(t);
            } catch (Exception e) {
                LOGGER.error("Exception occurred when processing the message", e);
            }
        }).processError(context -> {
            try {
                processError(context);
            } catch (Exception e) {
                LOGGER.error("Exception while processing the error found when processing the message", e);
            }
        }).buildProcessorClient();

        return processorClient;

    }

    /**
     * Starts receiving messages asynchronously and dispatch the messages to the
     * attached service.
     */
    public void startListeningAndDispatching() {

        this.messageProcessor.start();
        isStarted = true;
        LOGGER.debug("[Message Dispatcher]Receiving started, identifier: " + messageProcessor.getIdentifier());

    }

    /**
     * Stops receiving messages and close the undlying ASB listener.
     */
    public void stopListeningAndDispatching() {
        this.messageProcessor.stop();
        LOGGER.debug("[Message Dispatcher]Receiving stopped, identifier: " + messageProcessor.getIdentifier());
    }

    /**
     * Gets undeling ASB message listener instance.
     *
     * @return ServiceBusProcessorClient instance
     */
    public ServiceBusProcessorClient getProcessorClient() {
        return this.messageProcessor;
    }

    /**
     * Checks if dispatcher is running.
     *
     * @return true if dispatcher is listenering for messages
     */
    public boolean isRunning() {
        return isStarted;
    }

    /**
     * Handles the dispatching of message to the service.
     *
     * @param context ServiceBusReceivedMessageContext containing the ASB message
     */
    private void processMessage(ServiceBusReceivedMessageContext context) throws InterruptedException {
        MethodType method = this.getFunction(0, ASBConstants.FUNC_ON_MESSAGE);
        if (method == null) {
            return;
        }
        this.caller.addNativeData(context.getMessage().getLockToken(), context);
        dispatchMessage(context.getMessage());
    }

    private MethodType getFunction(int index, String functionName) {
        MethodType[] attachedFunctions = service.getType().getMethods();
        MethodType onMessageFunction = null;
        if (functionName.equals(attachedFunctions[index].getName())) {
            onMessageFunction = attachedFunctions[0];
        }
        return onMessageFunction;
    }

    /**
     * Handles and dispatches errors occured when receiving messages to the attahed
     * service.
     *
     * @param context ServiceBusErrorContext related to the ASB error
     * @throws IOException
     */
    private void processError(ServiceBusErrorContext context) throws InterruptedException {
        MethodType method = this.getFunction(1, ASBConstants.FUNC_ON_ERROR);
        if (method == null) {
            return;
        }
        Throwable throwable = context.getException();
        ServiceBusException exception = (throwable instanceof ServiceBusException) ? (ServiceBusException) throwable :
                new ServiceBusException(throwable, context.getErrorSource());
        ServiceBusFailureReason reason = exception.getReason();
        if (reason == ServiceBusFailureReason.MESSAGING_ENTITY_DISABLED
                || reason == ServiceBusFailureReason.MESSAGING_ENTITY_NOT_FOUND
                || reason == ServiceBusFailureReason.UNAUTHORIZED) {
            LOGGER.error("An error occurred when processing with reason: " + reason + " message: "
                    + exception.getMessage());
        }

        Exception e = (throwable instanceof Exception) ? (Exception) throwable : new Exception(throwable);
        BError error = ASBUtils.createErrorValue(e.getClass().getTypeName(), e);
        BMap<BString, Object> errorDetailBMap = getErrorMessage(context);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Callback callback = new ASBResourceCallback(countDownLatch);
        executeResourceOnError(callback, errorDetailBMap, true, error, true);
        countDownLatch.await();
    }

    /**
     * Dispatches message to the service.
     *
     * @param message Received azure service bus message instance.
     * @throws InterruptedException
     */
    private void dispatchMessage(ServiceBusReceivedMessage message) throws InterruptedException {
        BMap<BString, Object> messageBObject = null;
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Callback callback = new ASBResourceCallback(countDownLatch);
        messageBObject = getReceivedMessage(message);
        executeResourceOnMessage(callback, messageBObject, true, this.caller, true);
        countDownLatch.await();
    }

    /**
     * Prepares the message body content.
     *
     * @param receivedMessage ASB received message
     * @return Object containing message data
     */
    private Object getMessageContent(ServiceBusReceivedMessage receivedMessage) {
        AmqpAnnotatedMessage rawAmqpMessage = receivedMessage.getRawAmqpMessage();
        AmqpMessageBodyType bodyType = rawAmqpMessage.getBody().getBodyType();
        switch (bodyType) {
            case DATA:
                return rawAmqpMessage.getBody().getFirstData();
            case VALUE:
                Object amqpValue = rawAmqpMessage.getBody().getValue();
                LOGGER.debug("Received a message with messageId: " + receivedMessage.getMessageId()
                        + " AMQPMessageBodyType: {}"
                        + bodyType);
                amqpValue = ASBUtils.convertAMQPToJava(receivedMessage.getMessageId(), amqpValue);
                return amqpValue;
            default:
                throw new RuntimeException("Invalid message body type: " + receivedMessage.getMessageId());
        }
    }

    /**
     * Constructs Ballerina representaion of ASB message.
     *
     * @param receivedMessage Received ASB message
     * @return BMap<BString, Object> representing Ballerina record
     */
    private BMap<BString, Object> getReceivedMessage(ServiceBusReceivedMessage receivedMessage) {
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
        map.put("deliveryCount", (int) receivedMessage.getDeliveryCount());
        map.put("enqueuedTime", StringUtils.fromString(receivedMessage.getEnqueuedTime().toString()));
        map.put("enqueuedSequenceNumber", (int) receivedMessage.getEnqueuedSequenceNumber());
        map.put("deadLetterErrorDescription", StringUtils.fromString(receivedMessage.getDeadLetterErrorDescription()));
        map.put("deadLetterReason", StringUtils.fromString(receivedMessage.getDeadLetterReason()));
        map.put("deadLetterSource", StringUtils.fromString(receivedMessage.getDeadLetterSource()));
        map.put("state", StringUtils.fromString(receivedMessage.getState().toString()));
        BMap<BString, Object> applicationProperties = ValueCreator.createRecordValue(ModuleUtils.getModule(),
                ASBConstants.APPLICATION_PROPERTIES);
        Object appProperties = ASBUtils.toBMap(receivedMessage.getApplicationProperties());
        map.put("applicationProperties", ValueCreator.createRecordValue(applicationProperties, appProperties));
        BMap<BString, Object> createRecordValue = ValueCreator.createRecordValue(ModuleUtils.getModule(),
                ASBConstants.MESSAGE_RECORD, map);
        return createRecordValue;
    }

    /**
     * Constructs Ballerina representation of ASB error.
     *
     * @param context ServiceBusErrorContext containing error detail
     * @return BMap<BString, Object> representing Ballerina record
     */
    private BMap<BString, Object> getErrorMessage(ServiceBusErrorContext context) {
        Map<String, Object> map = new HashMap<>();
        map.put("entityPath", StringUtils.fromString(context.getEntityPath()));
        map.put("className", StringUtils.fromString(context.getClass().getSimpleName()));
        map.put("namespace", StringUtils.fromString(context.getFullyQualifiedNamespace()));
        map.put("errorSource", StringUtils.fromString(context.getErrorSource().toString()));
        Throwable throwable = context.getException();
        ServiceBusException exception = (throwable instanceof ServiceBusException) ? (ServiceBusException) throwable :
                new ServiceBusException(throwable, context.getErrorSource());
        ServiceBusFailureReason reason = exception.getReason();
        map.put("reason", StringUtils.fromString(reason.toString()));
        BMap<BString, Object> createRecordValue = ValueCreator.createRecordValue(ModuleUtils.getModule(),
                "ErrorContext", map);
        return createRecordValue;
    }

    private void executeResourceOnMessage(Callback callback, Object... args) {
        StrandMetadata metaData = new StrandMetadata(ModuleUtils.getModule().getOrg(),
                ModuleUtils.getModule().getName(), ModuleUtils.getModule().getMajorVersion(),
                ASBConstants.FUNC_ON_MESSAGE);
        executeResource(ASBConstants.FUNC_ON_MESSAGE, callback, metaData, args);
    }

    private void executeResourceOnError(Callback callback, Object... args) {
        StrandMetadata metaData = new StrandMetadata(ModuleUtils.getModule().getOrg(),
                ModuleUtils.getModule().getName(), ModuleUtils.getModule().getMajorVersion(),
                ASBConstants.FUNC_ON_ERROR);
        executeResource(ASBConstants.FUNC_ON_ERROR, callback, metaData, args);
    }

    private void executeResource(String function, Callback callback, StrandMetadata metaData,
                                 Object... args) {
        runtime.invokeMethodAsyncSequentially(service, function, null, metaData, callback, null,
                PredefinedTypes.TYPE_NULL, args);
    }
}


