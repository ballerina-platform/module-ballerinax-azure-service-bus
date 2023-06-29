// Copyright (c) 2023 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 LLC. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

# Azure service bus message batch representation.
#
# + messageCount - Number of messages in a batch  
# + messages - Array of Azure service bus message representation (Array of Message records)
@display {label: "Batch Message"}
public type MessageBatch record {|
    @display {label: "Message Count"}
    int messageCount = -1;
    @display {label: "Array of Messages"}
    Message[] messages = [];
|};

# Azure service bus Message representation.
#
# + body - Message body, Here the connector supports AMQP message body types - DATA and VALUE, However, DATA type message bodies  
# will be received in Ballerina Byte[] type. VALUE message bodies can be any primitive AMQP type. therefore, the connector  
# supports for string, int or byte[]. Please refer Azure docs (https://learn.microsoft.com/en-us/java/api/com.azure.core.amqp.models.amqpmessagebody?view=azure-java-stable)  
# and AMQP docs (https://qpid.apache.org/amqp/type-reference.html#PrimitiveTypes)  
# + contentType - Message content type, with a descriptor following the format of `RFC2045`, (e.g. `application/json`) (optional)
# + messageId - Message Id (optional)  
# + to - Message to (optional)  
# + replyTo - Message reply to (optional)  
# + replyToSessionId - Identifier of the session to reply to (optional)  
# + label - Message label (optional)  
# + sessionId - Message session Id (optional)  
# + correlationId - Message correlationId (optional)  
# + partitionKey - Message partition key (optional)  
# + timeToLive - Message time to live in seconds (optional)  
# + sequenceNumber - Message sequence number (optional)  
# + lockToken - Message lock token (optional)  
# + applicationProperties - Message broker application specific properties (optional)  
# + deliveryCount - Number of times a message has been delivered in a queue/subscription  
# + enqueuedTime - Timestamp indicating when a message was added to the queue/subscription 
# + enqueuedSequenceNumber - Sequence number assigned to a message when it is added to the queue/subscription 
# + deadLetterErrorDescription - Error description of why a message went to a dead-letter queue  
# + deadLetterReason - Reason why a message was moved to a dead-letter queue  
# + deadLetterSource - Original queue/subscription where the message was before being moved to the dead-letter queue 
# + state - Current state of a message in the queue/subscription, could be "Active", "Scheduled", "Deferred", etc.
@display {label: "Message"}
public type Message record {|
    @display {label: "Message Body"}
    anydata body;
    @display {label: "Content Type"}
    string contentType = BYTE_ARRAY;
    @display {label: "Message Id"}
    string messageId?;
    @display {label: "To"}
    string to?;
    @display {label: "Reply To"}
    string replyTo?;
    @display {label: "Reply To Session Id"}
    string replyToSessionId?;
    @display {label: "Label"}
    string label?;
    @display {label: "Session Id"}
    string sessionId?;
    @display {label: "Correlation Id"}
    string correlationId?;
    @display {label: "Partition Key"}
    string partitionKey?;
    @display {label: "Time To Live"}
    int timeToLive?;
    @display {label: "Sequence Number"}
    readonly int sequenceNumber?;
    @display {label: "Lock Token"}
    readonly string lockToken?;
    ApplicationProperties applicationProperties?;
    @display {label: "Delivery Count"}
    int deliveryCount?;
    @display {label: "Enqueued Time"}
    string enqueuedTime?;
    @display {label: "Enqueued SequenceNumber"}
    int enqueuedSequenceNumber?;
    @display {label: "DeadLetter Error Description"}
    string deadLetterErrorDescription?;
    @display {label: "DeadLetter Reason"}
    string deadLetterReason?;
    @display {label: "DeadLetter Source"}
    string deadLetterSource?;
    @display {label: "Message State"}
    string state?;
|};

# Azure service bus message, application specific properties representation.
#
# + properties - Key-value pairs for each brokered property (optional)
@display {label: "Application Properties"}
public type ApplicationProperties record {|
    @display {label: "Properties"}
    map<anydata> properties?;
|};

# Configurations used to create an `asb:Connection`.
#
# + connectionString - Service bus connection string with Shared Access Signatures  
#                      ConnectionString format: 
#                      Endpoint=sb://namespace_DNS_Name;EntityPath=EVENT_HUB_NAME;
#                      SharedAccessKeyName=SHARED_ACCESS_KEY_NAME;SharedAccessKey=SHARED_ACCESS_KEY or  
#                      Endpoint=sb://namespace_DNS_Name;EntityPath=EVENT_HUB_NAME;
#                      SharedAccessSignatureToken=SHARED_ACCESS_SIGNATURE_TOKEN 
# + isDeadLetterQueue - To mark whether it is dead letter queue
# + entityConfig -  This field holds the configuration details of either a topic or a queue. The type of the entity is 
#                   determined by the entityType field. The actual configuration details are stored in either a 
#                   TopicSubsConfig or a QueueConfig record  
# + receiveMode - This field holds the receive modes(RECEIVE_AND_DELETE/PEEK_LOCK) for the connection. The receive mode determines how messages are 
# retrieved from the entity. The default value is PEEK_LOCK  
# + maxAutoLockRenewDuration - Max lock renewal duration under PEEK_LOCK mode in seconds. Setting to 0 disables auto-renewal. 
#                              For RECEIVE_AND_DELETE mode, auto-renewal is disabled. Default 300 seconds.
# + amqpRetryOptions - Retry configurations related to underlying AMQP message receiver
@display {label: "Receiver Connection Config"}
public type ASBServiceReceiverConfig record {
    @display {label: "ConnectionString"}
    string connectionString;
    @display {label: "Entity Configuration"}
    TopicSubsConfig|QueueConfig entityConfig;
    @display {label: "Dead letter queue"}
    boolean isDeadLetterQueue = false;
    @display {label: "Receive Mode"}
    ReceiveMode receiveMode = PEEK_LOCK;
    @display {label: "Max Auto Lock Renew Duration"}
    int maxAutoLockRenewDuration = 300;
    @display {label: "AMQP retry configurations"}
    AmqpRetryOptions amqpRetryOptions = {};
};

# Set of options that can be specified to influence how the retry attempts are made.
#
# + maxRetries - Maximum number of retry attempts  
# + delay - Delay between retry attempts in seconds 
# + maxDelay - Maximum permissible delay between retry attempts in seconds
# + tryTimeout - Maximum duration to wait for completion of a single attempt in seconds  
# + retryMode - Approach to use for calculating retry delays
public type AmqpRetryOptions record {|
    @display {
        label: "Max retry attempts"
    }
    int maxRetries = 3;
    @display {
        label: "Duration between retries"
    }
    decimal delay = 10;
    @display {
        label: "Maximum duration between retries"
    }
    decimal maxDelay = 60;
    @display {
        label: "Timeout duration for retry attempt"
    }
    decimal tryTimeout = 60;
    @display {
        label: "Approach to calculated the retry"
    }
    AmqpRetryMode retryMode = FIXED;
|};

# The type of approach to apply when calculating the delay between retry attempts.
public enum AmqpRetryMode {
    # Retry attempts happen at fixed intervals; each delay is a consistent duration.
    @display {
        label: "Retry on fixed intervals"
    }
    FIXED,
    # Retry attempts will delay based on a backoff strategy, where each attempt will increase the duration that it waits before retrying.
    @display {
        label: "Retry based on a backoff strategy"
    }
    EXPONENTIAL
};

# This record holds the configuration details of a topic and its associated subscription in Azure Service Bus
#
# + topicName - A string field that holds the name of the topic  
# + subscriptionName - A string field that holds the name of the subscription associated with the topic
@display {label: "Topic/Subscriptions Configurations"}
public type TopicSubsConfig record {
    @display {label: "Topic Name"}
    string topicName;
    @display {label: "Subscription Name"}
    string subscriptionName;
};

# This record holds the configuration details of a queue in Azure Service Bus
#
# + queueName - A string field that holds the name of the queue
@display {label: "Queue Configurations"}
public type QueueConfig record {
    @display {label: "Queue Name"}
    string queueName;
};

# Holds the configuration details needed to create a sender connection to Azure Service Bus
#
# + entityType - An enumeration value of type EntityType, which specifies whether the connection is for a topic or a queue. 
#                The valid values are TOPIC and QUEUE  
# + topicOrQueueName - A string field that holds the name of the topic or queue
# + connectionString - A string field that holds the Service Bus connection string with Shared Access Signatures.
# + amqpRetryOptions - Retry configurations related to underlying AMQP message sender
@display {label: "Sender Connection Config"}
public type ASBServiceSenderConfig record {
    @display {label: "EntityType"}
    EntityType entityType;
    @display {label: "Queue/Topic Name"}
    string topicOrQueueName;
    @display {label: "ConnectionString"}
    string connectionString;
    @display {label: "AMQP retry configurations"}
    AmqpRetryOptions amqpRetryOptions = {};
};

# Represents Custom configurations for the ASB connector
#
# + logLevel - Enables the connector debug log prints (log4j log levels), default: OFF
public type CustomConfiguration record {
    @display {label: "Log Level"}
    LogLevel logLevel = OFF;
};

# Message entity types
public enum EntityType {
    @display {label: "Queue"}
    QUEUE = "queue",
    @display {label: "Topic"}
    TOPIC = "topic"
}

# Message receiver modes
public enum ReceiveMode {
    @display {label: "RECEIVE AND DELETE"}
    RECEIVE_AND_DELETE = "RECEIVEANDDELETE",
    @display {label: "PEEK LOCK"}
    PEEK_LOCK = "PEEKLOCK"
}

# Log levels
public enum LogLevel {
    @display {label: "DEBUG"}
    DEBUG,
    @display {label: "INFO"}
    INFO,
    @display {label: "WARNING"}
    WARNING,
    @display {label: "ERROR"}
    ERROR,
    @display {label: "FATAL"}
    FATAL,
    @display {label: "OFF"}
    OFF
}
