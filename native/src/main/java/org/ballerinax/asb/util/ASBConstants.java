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

package org.ballerinax.asb.util;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;

/**
 * Asb Connector Constants.
 */
public class ASBConstants {

    //Native Object Identifiers
    public static final String RECEIVER_CLIENT = "RECEIVER_CLIENT";
    public static final String ADMINISTRATOR_CLIENT = "ADMINISTRATOR_CLIENT";
    public static final String SENDER_CLIENT = "SENDER_CLIENT";
    public static final String DEAD_LETTER_RECEIVER_CLIENT = "DEAD_LETTER_RECEIVER_CLIENT";
    public static final String NATIVE_MESSAGE = "NATIVE_MESSAGE";

    //Receiver Client Init Data
    public static final String RECEIVER_CLIENT_CONNECTION_STRING = "CONNECTION_STRING";
    public static final String RECEIVER_CLIENT_RECEIVE_MODE = "RECEIVE_MODE";
    public static final String RECEIVER_CLIENT_TOPIC_NAME = "TOPIC_NAME";
    public static final String RECEIVER_CLIENT_SUBSCRIPTION_NAME = "SUBSCRIPTION_NAME";
    public static final String RECEIVER_CLIENT_QUEUE_NAME = "QUEUE_NAME";
    public static final String RECEIVER_CLIENT_MAX_AUTO_LOCK_RENEW_DURATION = "MAX_AUTO_LOCK_RENEW_DURATION";
    public static final String RECEIVER_CLIENT_LOG_LEVEL = "LOG_LEVEL";
    public static final String RECEIVER_CLIENT_RETRY_CONFIGS = "RETRY_CONFIGS";

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
    public static final String PROPERTIES = "properties";

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

    // Subscription/Topic/Queue Properties record fields
    public static final String SUBSCRIPTION_CREATED_RECORD = "SubscriptionProperties";
    public static final String TOPIC_CREATED_RECORD = "TopicProperties";
    public static final String QUEUE_CREATED_RECORD = "QueueProperties";

    // Create Queue Properties record fields : BString
    public static final BString QUEUE_RECORD_FIELD_AUTO_DELETE_ON_IDLE =
            StringUtils.fromString("autoDeleteOnIdle");
    public static final BString QUEUE_RECORD_FIELD_DEFAULT_MESSAGE_TIME_TO_LIVE =
            StringUtils.fromString("defaultMessageTimeToLive");
    public static final BString QUEUE_RECORD_FIELD_DUPLICATE_DETECTION_HISTORY_TIME_WINDOW =
            StringUtils.fromString("duplicateDetectionHistoryTimeWindow");
    public static final BString QUEUE_RECORD_FIELD_FORWARD_DEAD_LETTERED_MESSAGES_TO =
            StringUtils.fromString("forwardDeadLetteredMessagesTo");
    public static final BString QUEUE_RECORD_FIELD_FORWARD_TO =
            StringUtils.fromString("forwardTo");
    public static final BString QUEUE_RECORD_FIELD_LOCK_DURATION =
            StringUtils.fromString("lockDuration");
    public static final BString QUEUE_RECORD_FIELD_MAX_DELIVERY_COUNT =
            StringUtils.fromString("maxDeliveryCount");
    public static final BString QUEUE_RECORD_FIELD_MAX_MESSAGE_SIZE_IN_KILOBYTES =
            StringUtils.fromString("maxMessageSizeInKilobytes");
    public static final BString QUEUE_RECORD_FIELD_MAX_SIZE_IN_MEGABYTES =
            StringUtils.fromString("maxSizeInMegabytes");
    public static final BString QUEUE_RECORD_FIELD_NAME =
            StringUtils.fromString("name");
    public static final BString QUEUE_RECORD_FIELD_STATUS =
            StringUtils.fromString("status");
    public static final BString QUEUE_RECORD_FIELD_USER_METADATA =
            StringUtils.fromString("userMetadata");
    public static final BString QUEUE_RECORD_FIELD_ENABLE_BATCHED_OPERATIONS =
            StringUtils.fromString("enableBatchedOperations");
    public static final BString QUEUE_RECORD_FIELD_DEAD_LETTERING_ON_MESSAGE_EXPIRATION =
            StringUtils.fromString("deadLetteringOnMessageExpiration");
    public static final BString QUEUE_RECORD_FIELD_REQUIRES_DUPLICATE_DETECTION =
            StringUtils.fromString("requiresDuplicateDetection");
    public static final BString QUEUE_RECORD_FIELD_ENABLE_PARTITIONING =
            StringUtils.fromString("enablePartitioning");
    public static final BString QUEUE_RECORD_FIELD_REQUIRE_SESSION =
            StringUtils.fromString("requiresSession");

    // Create Queue Properties SubField Duration Fields : BString
    public static final BString DURATION_FIELD_SECONDS = StringUtils.fromString("seconds");
    public static final BString DURATION_FIELD_NANOSECONDS = StringUtils.fromString("nanoseconds");

    // Queue Properties record fields : String
    public static final String CREATED_QUEUE_RECORD_FIELD_AUTO_DELETE_ON_IDLE =
            "autoDeleteOnIdle";
    public static final String CREATED_QUEUE_RECORD_FIELD_DEFAULT_MESSAGE_TIME_TO_LIVE =
            "defaultMessageTimeToLive";
    public static final String CREATED_QUEUE_RECORD_FIELD_DUPLICATE_DETECTION_HISTORY_TIME_WINDOW =
            "duplicateDetectionHistoryTimeWindow";
    public static final String CREATED_QUEUE_RECORD_FIELD_FORWARD_DEAD_LETTERED_MESSAGES_TO =
            "forwardDeadLetteredMessagesTo";
    public static final String CREATED_QUEUE_RECORD_FIELD_FORWARD_TO =
            "forwardTo";
    public static final String CREATED_QUEUE_RECORD_FIELD_MAX_DELIVERY_COUNT =
            "maxDeliveryCount";
    public static final String CREATED_QUEUE_RECORD_FIELD_MAX_MESSAGE_SIZE_IN_KILOBYTES =
            "maxMessageSizeInKilobytes";
    public static final String CREATED_QUEUE_RECORD_FIELD_MAX_SIZE_IN_MEGABYTES =
            "maxSizeInMegabytes";
    public static final String CREATED_QUEUE_RECORD_FIELD_NAME =
            "name";
    public static final String CREATED_QUEUE_RECORD_FIELD_STATUS =
            "status";
    public static final String CREATED_QUEUE_RECORD_FIELD_USER_METADATA =
            "userMetadata";
    public static final String CREATED_QUEUE_RECORD_FIELD_ENABLE_BATCHED_OPERATIONS =
            "enableBatchedOperations";
    public static final String CREATED_QUEUE_RECORD_FIELD_REQUIRES_DUPLICATE_DETECTION =
            "requiresDuplicateDetection";
    public static final String CREATED_QUEUE_RECORD_FIELD_ENABLE_PARTITIONING =
            "enablePartitioning";
    public static final String CREATED_QUEUE_RECORD_FIELD_REQUIRE_SESSION =
            "requireSession";
    public static final String CREATED_QUEUE_RECORD_FIELD_LOCK_DURATION =
            "lockDuration";
    public static final String CREATED_QUEUE_RECORD_FIELD_DEAD_LETTERING_ON_MESSAGE_EXPIRATION =
            "deadLetteringOnMessageExpiration";

    // Queue Properties SubField AuthRules record fields : String
    public static final String CREATED_QUEUE_RECORD_FIELD_AUTHORIZATION_RULE = "AuthorizationRule";
    public static final String CREATED_QUEUE_RECORD_FIELD_AUTHORIZATION_RULES_CLAIM_TYPE = "claimType";
    public static final String CREATED_QUEUE_RECORD_FIELD_AUTHORIZATION_RULES_CLAIM_VALUE = "claimValue";
    public static final String CREATED_QUEUE_RECORD_FIELD_AUTHORIZATION_RULES_ACCESS_RIGHTS = "accessRights";
    public static final String CREATED_QUEUE_RECORD_FIELD_AUTHORIZATION_RULES_KEY_NAME = "keyName";
    public static final String CREATED_QUEUE_RECORD_FIELD_AUTHORIZATION_RULES_MODIFIED_AT = "modifiedAt";
    public static final String CREATED_QUEUE_RECORD_FIELD_AUTHORIZATION_RULES_CREATED_AT = "createdAt";
    public static final String CREATED_QUEUE_RECORD_FIELD_AUTHORIZATION_RULES_PRIMARY_KEY = "primaryKey";
    public static final String CREATED_QUEUE_RECORD_FIELD_AUTHORIZATION_RULES_SECONDARY_KEY = "secondaryKey";

    // Queue Properties SubField Duration record fields : String
    public static final String CREATED_QUEUE_RECORD_FIELD_DURATION = "Duration";
    public static final String CREATED_QUEUE_RECORD_FIELD_DURATION_SECONDS = "seconds";
    public static final String CREATED_QUEUE_RECORD_FIELD_DURATION_NANOSECONDS = "nanoseconds";

    //Queue List
    public static final String LIST_OF_QUEUES = "list";
    public static final String LIST_OF_QUEUES_RECORD = "QueueList";

    // Topic Options Record Fields to Create a Topic
    public static final BString TOPIC_RECORD_FIELD_AUTO_DELETE_ON_IDLE =
            StringUtils.fromString("autoDeleteOnIdle");
    public static final BString TOPIC_RECORD_FIELD_DEFAULT_MESSAGE_TIME_TO_LIVE =
            StringUtils.fromString("defaultMessageTimeToLive");
    public static final BString TOPIC_RECORD_FIELD_DUPLICATE_DETECTION_HISTORY_TIME_WINDOW =
            StringUtils.fromString("duplicateDetectionHistoryTimeWindow");
    public static final BString TOPIC_RECORD_FIELD_LOCK_DURATION =
            StringUtils.fromString("lockDuration");
    public static final BString TOPIC_RECORD_FIELD_MAX_DELIVERY_COUNT =
            StringUtils.fromString("maxDeliveryCount");
    public static final BString TOPIC_RECORD_FIELD_MAX_MESSAGE_SIZE_IN_KILOBYTES =
            StringUtils.fromString("maxMessageSizeInKilobytes");
    public static final BString TOPIC_RECORD_FIELD_MAX_SIZE_IN_MEGABYTES =
            StringUtils.fromString("maxSizeInMegabytes");
    public static final BString TOPIC_RECORD_FIELD_STATUS =
            StringUtils.fromString("status");
    public static final BString TOPIC_RECORD_FIELD_USER_METADATA =
            StringUtils.fromString("userMetadata");
    public static final BString TOPIC_RECORD_FIELD_ENABLE_BATCHED_OPERATIONS =
            StringUtils.fromString("enableBatchedOperations");
    public static final BString TOPIC_RECORD_FIELD_REQUIRES_DUPLICATE_DETECTION =
            StringUtils.fromString("requiresDuplicateDetection");
    public static final BString TOPIC_RECORD_FIELD_ENABLE_PARTITIONING =
            StringUtils.fromString("enablePartitioning");
    public static final BString TOPIC_RECORD_FIELD_REQUIRE_SESSION =
            StringUtils.fromString("requireSession");
    public static final BString TOPIC_RECORD_FIELD_ENABLE_ORDERING_SUPPORT =
            StringUtils.fromString("supportOrdering");

    // Created Topic Properties Record Fields
    public static final String CREATED_TOPIC_RECORD_FIELD_NAME = "name";
    public static final String CREATED_TOPIC_RECORD_FIELD_AUTHORIZATION_RULES = "authorizationRules";
    public static final String CREATED_TOPIC_RECORD_FIELD_AUTO_DELETE_ON_IDLE = "autoDeleteOnIdle";
    public static final String CREATED_TOPIC_RECORD_FIELD_DEFAULT_MESSAGE_TIME_TO_LIVE =
            "defaultMessageTimeToLive";
    public static final String CREATED_TOPIC_RECORD_FIELD_DUPLICATE_DETECTION_HISTORY_TIME_WINDOW =
            "duplicateDetectionHistoryTimeWindow";
    public static final String CREATED_TOPIC_RECORD_FIELD_MAX_MESSAGE_SIZE_IN_KILOBYTES =
            "maxMessageSizeInKilobytes";
    public static final String CREATED_TOPIC_RECORD_FIELD_MAX_SIZE_IN_MEGABYTES = "maxSizeInMegabytes";
    public static final String CREATED_TOPIC_RECORD_FIELD_STATUS = "status";
    public static final String CREATED_TOPIC_RECORD_FIELD_USER_METADATA = "userMetadata";
    public static final String CREATED_TOPIC_RECORD_FIELD_ENABLE_BATCHED_OPERATIONS =
            "enableBatchedOperations";
    public static final String CREATED_TOPIC_RECORD_FIELD_REQUIRES_DUPLICATE_DETECTION =
            "requiresDuplicateDetection";
    public static final String CREATED_TOPIC_RECORD_FIELD_ENABLE_PARTITIONING = "enablePartitioning";
    public static final String CREATED_TOPIC_RECORD_FIELD_SUPPORT_ORDERING = "supportOrdering";

    // Topic List
    public static final String LIST_OF_TOPICS = "list";
    public static final String LIST_OF_TOPICS_RECORD = "TopicList";

    // Subscription Options Record Fields to Create a Subscription
    public static final BString SUBSCRIPTION_RECORD_FIELD_AUTO_DELETE_ON_IDLE =
            StringUtils.fromString("autoDeleteOnIdle");
    public static final BString SUBSCRIPTION_RECORD_FIELD_DEFAULT_MESSAGE_TIME_TO_LIVE =
            StringUtils.fromString("defaultMessageTimeToLive");
    public static final BString SUBSCRIPTION_RECORD_FIELD_FORWARD_DEAD_LETTERED_MESSAGES_TO =
            StringUtils.fromString("forwardDeadLetteredMessagesTo");
    public static final BString SUBSCRIPTION_RECORD_FIELD_FORWARD_TO =
            StringUtils.fromString("forwardTo");
    public static final BString SUBSCRIPTION_RECORD_FIELD_LOCK_DURATION =
            StringUtils.fromString("lockDuration");
    public static final BString SUBSCRIPTION_RECORD_FIELD_MAX_DELIVERY_COUNT =
            StringUtils.fromString("maxDeliveryCount");
    public static final BString SUBSCRIPTION_RECORD_FIELD_STATUS =
            StringUtils.fromString("status");
    public static final BString SUBSCRIPTION_RECORD_FIELD_ENABLE_BATCHED_OPERATIONS =
            StringUtils.fromString("enableBatchedOperations");
    public static final BString SUBSCRIPTION_RECORD_FIELD_DEFAULT_RULE =
            StringUtils.fromString("defaultRule");
    public static final BString SUBSCRIPTION_RECORD_FIELD_DEAD_LETTERING_ON_MESSAGE_EXPIRATION =
            StringUtils.fromString("deadLetteringOnMessageExpiration");
    public static final BString SUBSCRIPTION_RECORD_FIELD_DEAD_LETTERING_ON_FILTER_EVALUATION_EXCEPTIONS =
            StringUtils.fromString("deadLetteringOnFilterEvaluationExceptions");
    public static final BString SUBSCRIPTION_RECORD_FIELD_USER_METADATA =
            StringUtils.fromString("userMetadata");
    public static final BString SUBSCRIPTION_RECORD_FIELD_SESSION_REQUIRED =
            StringUtils.fromString("requiresSession");

    //Subscription Properties Record Fields
    public static final String CREATED_SUBSCRIPTION_RECORD_FIELD_AUTO_DELETE_ON_IDLE =
            "autoDeleteOnIdle";
    public static final String CREATED_SUBSCRIPTION_RECORD_FIELD_DEFAULT_MESSAGE_TIME_TO_LIVE =
            "defaultMessageTimeToLive";
    public static final String CREATED_SUBSCRIPTION_RECORD_FIELD_FORWARD_DEAD_LETTERED_MESSAGES_TO =
            "forwardDeadLetteredMessagesTo";
    public static final String CREATED_SUBSCRIPTION_RECORD_FIELD_FORWARD_TO =
            "forwardTo";
    public static final String CREATED_SUBSCRIPTION_RECORD_FIELD_LOCK_DURATION =
            "lockDuration";
    public static final String CREATED_SUBSCRIPTION_RECORD_FIELD_MAX_DELIVERY_COUNT =
            "maxDeliveryCount";
    public static final String CREATED_SUBSCRIPTION_RECORD_FIELD_STATUS =
            "status";
    public static final String CREATED_SUBSCRIPTION_RECORD_FIELD_SUBSCRIPTION_NAME =
            "subscriptionName";
    public static final String CREATED_SUBSCRIPTION_RECORD_FIELD_TOPIC_NAME =
            "topicName";
    public static final String CREATED_SUBSCRIPTION_RECORD_FIELD_USER_METADATA =
            "userMetadata";
    public static final String CREATED_SUBSCRIPTION_RECORD_FIELD_ENABLE_BATCHED_OPERATIONS =
            "enableBatchedOperations";
    public static final String CREATED_SUBSCRIPTION_RECORD_FIELD_DEAD_LETTERING_ON_MESSAGE_EXPIRATION =
            "deadLetteringOnMessageExpiration";
    public static final String CREATED_SUBSCRIPTION_RECORD_FIELD_DEAD_LETTERING_ON_FILTER_EVALUATION_EXCEPTIONS =
            "deadLetteringOnFilterEvaluationExceptions";
    public static final String CREATED_SUBSCRIPTION_RECORD_FIELD_SESSION_REQUIRED =
            "requiresSession";

    // Subscription List
    public static final String LIST_OF_SUBSCRIPTIONS = "list";
    public static final String LIST_OF_SUBSCRIPTIONS_RECORD = "SubscriptionList";

    // Rule Options Record Fields to Create a Rule
    public static final BString RECORD_FIELD_SQL_RULE = StringUtils.fromString("rule");
    public static final BString RECORD_FIELD_ACTION = StringUtils.fromString("action");
    public static final BString RECORD_FIELD_FILTER = StringUtils.fromString("filter");

    // Rule Properties Record Fields
    public static final String CREATED_RULE_RECORD = "RuleProperties";
    public static final String CREATED_RULE_RECORD_FIELD_NAME = "name";
    public static final String CREATED_RULE_RECORD_FIELD = "SqlRule";
    public static final String CREATED_RULE_RECORD_FIELD_TYPE_NAME = "rule";
    public static final String CREATED_RULE_RECORD_FIELD_ACTION = "action";
    public static final String CREATED_RULE_RECORD_FIELD_FILTER = "filter";

    // Rule List
    public static final String LIST_OF_RULES = "list";
    public static final String LIST_OF_RULES_RECORD = "RuleList";

    // Asynchronous Listener
    public static final String APPLICATION_PROPERTIES = "ApplicationProperties";
    public static final BString PEEK_LOCK_ENABLE_CONFIG_KEY = StringUtils.fromString("peekLockModeEnabled");
    public static final String ENTITY_CONFIG_KEY = "entityConfig";
    public static final String QUEUE_NAME_CONFIG_KEY = "queueName";
    public static final String TOPIC_NAME_CONFIG_KEY = "topicName";
    public static final String SUBSCRIPTION_NAME_CONFIG_KEY = "subscriptionName";
    public static final String MAX_CONCURRENCY_CONFIG_KEY = "maxConcurrency";
    public static final String MSG_PREFETCH_COUNT_CONFIG_KEY = "prefetchCount";
    public static final String LOCK_RENEW_DURATION_CONFIG_KEY = "maxAutoLockRenewDuration";
    public static final String LOG_LEVEL_CONGIG_KEY = "logLevel";
    public static final String EMPTY_STRING = "";
    public static final int MAX_CONCURRENCY_DEFAULT = 1;
    public static final int LOCK_RENEW_DURATION_DEFAULT = 300;
    public static final int MSG_PREFETCH_COUNT_DEFAULT = 0;
}
