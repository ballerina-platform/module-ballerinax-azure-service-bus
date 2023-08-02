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

package org.ballerinax.asb.util;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;

/**
 * Asb Connector Constants.
 */
public class ASBConstants {

    // Message constant fields
    public static final String MESSAGE_RECORD = "Message";
    public static final String APPLICATION_PROPERTY_TYPE = "ApplicationProperties";
    // Message content data binding errors
    public static final String XML_CONTENT_ERROR = "Error while retrieving the xml content of the message. ";
    public static final String JSON_CONTENT_ERROR = "Error while retrieving the json content of the message. ";
    public static final String TEXT_CONTENT_ERROR = "Error while retrieving the string content of the message. ";
    public static final String INT_CONTENT_ERROR = "Error while retrieving the int content of the message. ";
    public static final String FLOAT_CONTENT_ERROR = "Error while retrieving the float content of the message. ";
    // Batch Message constant fields
    public static final String MESSAGE_BATCH_RECORD = "MessageBatch";
    public static final String MESSAGES_OBJECT = "Messages";
    public static final BString MESSAGES_CONTENT = StringUtils.fromString("messages");
    public static final BString MESSAGE_COUNT = StringUtils.fromString("messageCount");
    // Message receive modes
    public static final String PEEK_LOCK = "PEEKLOCK";
    public static final String RECEIVE_AND_DELETE = "RECEIVEANDDELETE";

    // Field names of the ASB Message request and response
    public static final String BODY = "body";
    public static final String CONTENT_TYPE = "contentType";
    public static final String MESSAGE_ID = "messageId";
    public static final String TO = "to";
    public static final String REPLY_TO = "replyTo";
    public static final String REPLY_TO_SESSION_ID = "replyToSessionId";
    public static final String LABEL = "label";
    public static final String SESSION_ID = "sessionId";
    public static final String CORRELATION_ID = "correlationId";
    public static final String TIME_TO_LIVE = "timeToLive";
    public static final String PARTITION_KEY = "partitionKey";
    public static final String SEQUENCE_NUMBER = "sequenceNumber";
    public static final String LOCK_TOKEN = "lockToken";
    public static final String DELIVERY_COUNT = "deliveryCount";
    public static final String ENQUEUED_TIME = "enqueuedTime";
    public static final String ENQUEUED_SEQUENCE_NUMBER = "enqueuedSequenceNumber";
    public static final String DEAD_LETTER_REASON = "deadLetterReason";
    public static final String DEAD_LETTER_SOURCE = "deadLetterSource";
    public static final String DEAD_LETTER_ERROR_DESCRIPTION = "deadLetterErrorDescription";
    public static final String STATE = "state";
    public static final String APPLICATION_PROPERTY_KEY = "applicationProperties";
    public static final String APPLICATION_PROPERTIES = "properties";

    public static final int DEFAULT_TIME_TO_LIVE = 60; // In seconds
    public static final String DEFAULT_MESSAGE_LOCK_TOKEN = "00000000-0000-0000-0000-000000000000";

    // listener constant fields
    public static final String CONSUMER_SERVICES = "consumer_services";
    public static final String ASB_CALLER = "asb_caller";
    public static final String STARTED_SERVICES = "started_services";
    public static final String FUNC_ON_MESSAGE = "onMessage";
    public static final String FUNC_ON_ERROR = "onError";
    public static final String DISPATCH_ERROR = "Error occurred while dispatching the message. ";
    public static final BString QUEUE_NAME = StringUtils.fromString("queueName");
    public static final BString TOPIC_NAME = StringUtils.fromString("topicName");
    public static final BString SUBSCRIPTION_NAME = StringUtils.fromString("subscriptionName");
    public static final BString CONNECTION_STRING = StringUtils.fromString("connectionString");
    public static final BString RECEIVE_MODE = StringUtils.fromString("receiveMode");
    public static final String CONNECTION_NATIVE_OBJECT = "asb_connection_object";
    public static final String SERVICE_CONFIG = "ServiceConfig";
    public static final BString ALIAS_QUEUE_CONFIG = StringUtils.fromString("entityConfig");
    public static final String UNCHECKED = "unchecked";

    // Retry configurations
    public static final BString AMQP_RETRY_OPTIONS = StringUtils.fromString("amqpRetryOptions");
    public static final BString MAX_RETRIES = StringUtils.fromString("maxRetries");
    public static final BString DELAY = StringUtils.fromString("delay");
    public static final BString MAX_DELAY = StringUtils.fromString("maxDelay");
    public static final BString TRY_TIMEOUT = StringUtils.fromString("tryTimeout");
    public static final BString RETRY_MODE = StringUtils.fromString("retryMode");

    // Error constant fields
    static final String ASB_ERROR = "Error";
}
