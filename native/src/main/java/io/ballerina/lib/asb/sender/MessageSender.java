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
 * KINDither express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.lib.asb.sender;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusSenderClientBuilder;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusMessageBatch;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.models.CreateMessageBatchOptions;
import io.ballerina.lib.asb.util.ASBConstants;
import io.ballerina.lib.asb.util.ASBErrorCreator;
import io.ballerina.lib.asb.util.ASBUtils;
import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.Future;
import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.ballerina.lib.asb.util.ASBUtils.getRetryOptions;

/**
 * This facilitates the client operations of MessageSender client in Ballerina.
 */
public class MessageSender {
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool(
            new SenderNetworkThreadFactory());

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageSender.class);

    /**
     * Parameterized constructor for Message Sender (ServiceBusSenderClient).
     *
     * @param connectionString Azure service bus connection string
     * @param topicOrQueueName Queue/topic name
     * @throws ServiceBusException on failure initiating IMessage Receiver in Azure Service Bus instance.
     */
    public static Object initializeSender(BObject senderClient, String connectionString, String entityType,
                                          String topicOrQueueName, String logLevel,
                                          BMap<BString, Object> retryConfigs) {
        try {
            AmqpRetryOptions retryOptions = getRetryOptions(retryConfigs);
            ServiceBusSenderClientBuilder senderClientBuilder = new ServiceBusClientBuilder()
                    .retryOptions(retryOptions)
                    .connectionString(connectionString)
                    .sender();
            if (entityType.equalsIgnoreCase("queue")) {
                senderClientBuilder.queueName(topicOrQueueName);
            } else if (entityType.equalsIgnoreCase("topic")) {
                senderClientBuilder.topicName(topicOrQueueName);
            }
            LOGGER.debug("ServiceBusSenderClient initialized");
            setClient(senderClient, senderClientBuilder.buildClient());
            return null;
        } catch (BError e) {
            return ASBErrorCreator.fromBError(e);
        } catch (ServiceBusException e) {
            return ASBErrorCreator.fromASBException(e);
        } catch (Exception e) {
            return ASBErrorCreator.fromUnhandledException(e);
        }
    }

    /**
     * Sends a message to the configured service bus queue or topic, using the java SDK.
     *
     * @param message Input message record as a BMap
     * @return An error if failed to send the message
     */
    public static Object send(Environment env, BObject senderClient, BMap<BString, Object> message) {
        ServiceBusSenderClient sender = getNativeSender(senderClient);
        Future future = env.markAsync();
        EXECUTOR_SERVICE.execute(() -> {
            try {
                ServiceBusMessage messageToSend = constructMessage(message);
                sender.sendMessage(messageToSend);
                future.complete(null);
            } catch (BError e) {
                BError bError = ASBErrorCreator.fromBError(e);
                future.complete(bError);
            } catch (ServiceBusException e) {
                BError bError = ASBErrorCreator.fromASBException(e);
                future.complete(bError);
            } catch (Exception e) {
                BError bError = ASBErrorCreator.fromUnhandledException(e);
                future.complete(bError);
            }
        });
        return null;
    }

    /**
     * Sends a scheduled message to the Azure Service Bus entity this sender is connected to. A scheduled message is
     * enqueued and made available to receivers only at the scheduled enqueue time.
     *
     * @param message      Input message record as a BMap
     * @param scheduleTime Input schedule time record as a BMap
     * @return An error if failed to send the message
     */
    public static Object schedule(Environment env, BObject senderClient, BMap<BString, Object> message,
                                  BMap<BString, Object> scheduleTime) {
        ServiceBusSenderClient sender = getNativeSender(senderClient);
        Future future = env.markAsync();
        EXECUTOR_SERVICE.execute(() -> {
            try {
                ServiceBusMessage messageToSend = constructMessage(message);
                Long sequenceNumber = sender.scheduleMessage(messageToSend, constructOffset(scheduleTime));
                future.complete(sequenceNumber);
            } catch (BError e) {
                BError bError = ASBErrorCreator.fromBError(e);
                future.complete(bError);
            } catch (ServiceBusException e) {
                BError bError = ASBErrorCreator.fromASBException(e);
                future.complete(bError);
            } catch (Exception e) {
                BError bError = ASBErrorCreator.fromUnhandledException(e);
                future.complete(bError);
            }
        });
        return null;
    }

    /**
     * Cancels the enqueuing of a scheduled message, if they are not already enqueued.
     *
     * @param sequenceNumber The sequence number of the message to cance
     * @return An error if failed to send the message
     */
    public static Object cancel(Environment env, BObject senderClient, long sequenceNumber) {
        ServiceBusSenderClient sender = getNativeSender(senderClient);
        Future future = env.markAsync();
        EXECUTOR_SERVICE.execute(() -> {
            try {
                sender.cancelScheduledMessage(sequenceNumber);
                future.complete(null);
            } catch (BError e) {
                BError bError = ASBErrorCreator.fromBError(e);
                future.complete(bError);
            } catch (ServiceBusException e) {
                BError bError = ASBErrorCreator.fromASBException(e);
                future.complete(bError);
            } catch (Exception e) {
                BError bError = ASBErrorCreator.fromUnhandledException(e);
                future.complete(bError);
            }
        });
        return null;
    }

    /**
     * Send Batch of Messages with configurable parameters when Sender Connection is
     * given as a parameter and
     * batch message record as a BMap.
     *
     * @param messages Input batch message record as a BMap
     * @return An error if failed send the message.
     */
    public static Object sendBatch(Environment env, BObject senderClient, BMap<BString, Object> messages) {
        ServiceBusSenderClient sender = getNativeSender(senderClient);
        Future future = env.markAsync();
        EXECUTOR_SERVICE.execute(() -> {
            try {
                Map<String, Object> messagesMap = ASBUtils.toObjectMap(messages);
                BArray messageArray = (BArray) messagesMap.get("messages");
                Collection<ServiceBusMessage> messageBatch = new ArrayList<>();
                for (int i = 0; i < messageArray.getLength(); i++) {
                    BMap<BString, Object> messageBMap = (BMap<BString, Object>) messageArray.get(i);
                    ServiceBusMessage asbMessage = constructMessage(messageBMap);
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
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Message is too large for an empty batch. Skipping. Max size: "
                                    + currentBatch.getMaxSizeInBytes() + ". Message: " +
                                    message.getBody().toString());
                        }
                    }
                }
                sender.sendMessages(currentBatch);
                future.complete(null);
            } catch (BError e) {
                BError bError = ASBErrorCreator.fromBError(e);
                future.complete(bError);
            } catch (ServiceBusException e) {
                BError bError = ASBErrorCreator.fromASBException(e);
                future.complete(bError);
            } catch (Exception e) {
                BError bError = ASBErrorCreator.fromUnhandledException(e);
                future.complete(bError);
            }
        });
        return null;
    }

    /**
     * Closes the Asb Sender Connection using the given connection parameters.
     *
     * @return @return An error if failed close the sender.
     */
    public static Object close(Environment env, BObject senderClient) {
        ServiceBusSenderClient sender = getNativeSender(senderClient);
        Future future = env.markAsync();
        EXECUTOR_SERVICE.execute(() -> {
            try {
                sender.close();
                future.complete(null);
            } catch (BError e) {
                BError bError = ASBErrorCreator.fromBError(e);
                future.complete(bError);
            } catch (ServiceBusException e) {
                BError bError = ASBErrorCreator.fromASBException(e);
                future.complete(bError);
            } catch (Exception e) {
                BError bError = ASBErrorCreator.fromUnhandledException(e);
                future.complete(bError);
            }
        });
        return null;
    }

    private static ServiceBusMessage constructMessage(BMap<BString, Object> message) {
        Object messageBody = message.get(StringUtils.fromString(ASBConstants.BODY));
        byte[] byteArray;
        Type type = TypeUtils.getType(messageBody);
        if (type.getTag() == TypeTags.STRING_TAG) {
            byteArray = ((BString) messageBody).getValue().getBytes(StandardCharsets.UTF_8);
        } else if (type.getTag() == TypeTags.INT_TAG) {
            byteArray = Integer.toString((int) messageBody).getBytes(StandardCharsets.UTF_8);
        } else {
            byteArray = ((BArray) messageBody).getBytes();
        }

        ServiceBusMessage asbMessage = new ServiceBusMessage(byteArray);

        if (message.containsKey(StringUtils.fromString(ASBConstants.CONTENT_TYPE))) {
            String contentType = message.getStringValue(StringUtils.fromString(ASBConstants.CONTENT_TYPE)).getValue();
            asbMessage.setContentType(contentType);
        }
        if (message.containsKey(StringUtils.fromString(ASBConstants.MESSAGE_ID))) {
            String messageId = message.getStringValue(StringUtils.fromString(ASBConstants.MESSAGE_ID)).getValue();
            asbMessage.setMessageId(messageId);
        }
        if (message.containsKey(StringUtils.fromString(ASBConstants.TO))) {
            String to = message.getStringValue(StringUtils.fromString(ASBConstants.TO)).getValue();
            asbMessage.setTo(to);
        }
        if (message.containsKey(StringUtils.fromString(ASBConstants.REPLY_TO))) {
            String replyTo = message.getStringValue(StringUtils.fromString(ASBConstants.REPLY_TO)).getValue();
            asbMessage.setReplyTo(replyTo);
        }
        if (message.containsKey(StringUtils.fromString(ASBConstants.REPLY_TO_SESSION_ID))) {
            String replyToSessionId = message.getStringValue(StringUtils.fromString(ASBConstants.REPLY_TO_SESSION_ID))
                    .getValue();
            asbMessage.setReplyToSessionId(replyToSessionId);
        }
        if (message.containsKey(StringUtils.fromString(ASBConstants.LABEL))) {
            String subject = message.getStringValue(StringUtils.fromString(ASBConstants.LABEL)).getValue();
            asbMessage.setSubject(subject);
        }
        if (message.containsKey(StringUtils.fromString(ASBConstants.SESSION_ID))) {
            String sessionId = message.getStringValue(StringUtils.fromString(ASBConstants.SESSION_ID)).getValue();
            asbMessage.setSessionId(sessionId);
        }
        if (message.containsKey(StringUtils.fromString(ASBConstants.CORRELATION_ID))) {
            String correlationId = message.getStringValue(StringUtils.fromString(ASBConstants.CORRELATION_ID))
                    .getValue();
            asbMessage.setCorrelationId(correlationId);
        }
        if (message.containsKey(StringUtils.fromString(ASBConstants.PARTITION_KEY))) {
            String partitionKey = message.getStringValue(StringUtils.fromString(ASBConstants.PARTITION_KEY)).getValue();
            asbMessage.setPartitionKey(partitionKey);
        }
        if (message.containsKey(StringUtils.fromString(ASBConstants.TIME_TO_LIVE))) {
            long timeToLive = message.getIntValue(StringUtils.fromString(ASBConstants.TIME_TO_LIVE));
            asbMessage.setTimeToLive(Duration.ofSeconds(timeToLive));
        }
        if (message.containsKey(StringUtils.fromString(ASBConstants.APPLICATION_PROPERTY_KEY))) {
            BMap<BString, Object> propertyBMap = (BMap<BString, Object>) message.get(StringUtils.fromString(
                    ASBConstants.APPLICATION_PROPERTY_KEY));
            Object propertyMap = propertyBMap.get(StringUtils.fromString(ASBConstants.PROPERTIES));
            Map<String, Object> map = ASBUtils.toMap((BMap) propertyMap);
            asbMessage.getApplicationProperties().putAll(map);
        }

        return asbMessage;
    }

    private static OffsetDateTime constructOffset(BMap<BString, Object> scheduleTime) {

        int year = ((Long) scheduleTime.get(StringUtils.fromString("year"))).intValue();
        int month = ((Long) scheduleTime.get(StringUtils.fromString("month"))).intValue();
        int day = ((Long) scheduleTime.get(StringUtils.fromString("day"))).intValue();
        int hour = ((Long) scheduleTime.get(StringUtils.fromString("hour"))).intValue();
        int minute = ((Long) scheduleTime.get(StringUtils.fromString("minute"))).intValue();
        int seconds = 0;
        int zoneOffsetHours = 0;
        int zoneOffsetMinutes = 0;

        if (scheduleTime.containsKey(StringUtils.fromString("second"))) {
            BDecimal secondsAsObject = (BDecimal) scheduleTime.get(StringUtils.fromString("second"));
            seconds = secondsAsObject.byteValue();
        }

        if (scheduleTime.containsKey(StringUtils.fromString("utcOffset"))) {
            BMap<BString, Object> utcOffsetBMap = (BMap<BString, Object>) scheduleTime
                    .get(StringUtils.fromString("utcOffset"));
            zoneOffsetHours = (int) utcOffsetBMap.get(StringUtils.fromString("hours"));
            zoneOffsetMinutes = (int) utcOffsetBMap.get(StringUtils.fromString("minutes"));
        }

        ZoneOffset zoneOffset = ZoneOffset.ofHoursMinutes(zoneOffsetHours, zoneOffsetMinutes);
        return OffsetDateTime.of(year, month, day, hour, minute, seconds, 0, zoneOffset);
    }

    private static void setClient(BObject senderObject, ServiceBusSenderClient client) {
        senderObject.addNativeData(ASBConstants.SENDER_CLIENT, client);
    }

    private static ServiceBusSenderClient getNativeSender(BObject senderObject) {
        return (ServiceBusSenderClient) senderObject.getNativeData(ASBConstants.SENDER_CLIENT);
    }
}
