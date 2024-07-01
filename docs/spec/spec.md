# Specification: Ballerina `asb` Library

_Authors_: @ayeshLK \
_Reviewers_: @NipunaRanasinghe @niveathika @RDPerera \
_Created_: 2024/05/31 \
_Updated_: 2024/06/03 \
_Edition_: Swan Lake 

## Introduction  

This is the specification for the `asb` library of [Ballerina language](https://ballerina.io/), which provides the 
functionality to send and receive messages by connecting to an Azure service bus instance.

The `asb` library specification has evolved and may continue to evolve in the future. The released versions of the 
specification can be found under the relevant GitHub tag.

If you have any feedback or suggestions about the library, start a discussion via a GitHub issue or in the Discord 
server. Based on the outcome of the discussion, the specification and implementation can be updated. Community feedback 
is always welcome. Any accepted proposal which affects the specification is stored under `/docs/proposals`. Proposals 
under discussion can be found with the label `type/proposal` in Github.

The conforming implementation of the specification is released to Ballerina Central. Any deviation from the specification is considered a bug.

## Contents

1. [Overview](#1-overview)
2. [Message](#2-message)
3. [Message sender](#3-message-sender)
    * 3.1. [Configurations](#31-configurations)
    * 3.2. [Initialization](#32-initialization)
    * 3.3. [Functions](#33-functions)
4. [Message receiver](#4-message-receiver)
    * 4.1. [Configurations](#41-configurations)
    * 4.2. [Initialization](#42-initialization)
    * 4.3. [Functions](#43-functions)
5. [Message listener](#5-message-listener)
    * 5.1. [Configurations](#51-configurations)
    * 5.2. [Initialization](#52-initialization)
    * 5.3. [Functions](#53-functions)
    * 5.4. [Caller](#54-caller)
        * 5.4.1. [Functions](#541-functions)
    * 5.5. [Validations](#55-validations)
        * 5.5.1. [Attaching multiple services to a listener](#551-attaching-multiple-services-to-a-listener)
        * 5.5.2. [Using `autoComplete` mode](#552-using-autocomplete-mode)
    * 5.6. [Usage](#56-usage)
6. [Administrator](#6-administrator)
    * 6.1. [Initialization](#61-initialization)
    * 6.2. [Functions](#62-functions)


## 1. Overview

Azure Service Bus is a highly reliable cloud messaging service from Microsoft that enables communication between 
distributed applications through messages. It supports complex messaging features like FIFO messaging, publish/subscribe 
models, and session handling, making it an ideal choice for enhancing application connectivity and scalability.

This specification details the usage of Azure Service Bus in the context of queues and topics, enabling developers to build 
robust distributed applications and microservices. These components facilitate the parallel, scalable, and fault-tolerant 
handling of messages even in the face of network issues or service interruptions.

Ballerina `asb` provides several core APIs:

- `asb:MessageSender` - used to publish messages to Azure service bus queue or a topic.
- `asb:MessageReceiver` - used to receive messages from an Azure service bus queue or a topic.
- `asb:Listener` - used to asynchronously receive messages from an Azure service bus queue or topic.
- `asb:Administrator` - used to perform administrative actions on an Azure service bus resource.

## 2. Message

An Azure Service Bus message is a unit of communication that carries data for exchange between components that interact with Microsoft's Azure service bus resource. Messages can encapsulate different types of data, including text, binary, and custom-defined objects structured as key-value pairs.

A Message Batch in Azure Service Bus is a collection of messages that are sent as a single batched transaction to optimize performance and resource utilization. This approach is useful when sending multiple messages to the same queue or topic to ensure atomicity and reduce network calls.

- `ApplicationProperties` record represents the application-specific properties.

```ballerina
public type ApplicationProperties record {|
    # Key-value pairs for each brokered property (optional)
    map<anydata> properties?;
|};
```

- `Message` record represents the Azure service bus message.

```ballerina
public type Message record {|
    # Message body, Here the connector supports AMQP message body types - DATA and VALUE, However, DATA type message bodies  
    # will be received in Ballerina Byte[] type. VALUE message bodies can be any primitive AMQP type. therefore, the connector  
    # supports for string, int or byte[]. Please refer Azure docs (https://learn.microsoft.com/en-us/java/api/com.azure.core.amqp.models.amqpmessagebody?view=azure-java-stable)  
    # and AMQP docs (https://qpid.apache.org/amqp/type-reference.html#PrimitiveTypes)
    anydata body;
    # Message content type, with a descriptor following the format of `RFC2045`, (e.g. `application/json`) (optional)
    string contentType = BYTE_ARRAY;
    # Message Id (optional)
    string messageId?;
    # The `to` address (optional) 
    string to?;
    # The `to` address (optional)
    string replyTo?;
    # The `ReplyToGroupId` property value of this message (optional) 
    string replyToSessionId?;
    # Message label (optional)
    string label?;
    # Message session Id (optional)
    string sessionId?;
    # Message correlationId (optional) 
    string correlationId?;
    # Message partition key (optional)  
    string partitionKey?;
    # Message time to live in seconds (optional) 
    int timeToLive?;
    # Message sequence number (optional)  
    readonly int sequenceNumber?;
    # Message lock token (optional) 
    readonly string lockToken?;
    # Message broker application specific properties (optional) 
    ApplicationProperties applicationProperties?;
    # Number of times a message has been delivered in a queue/subscription 
    int deliveryCount?;
    # Timestamp indicating when a message was added to the queue/subscription
    string enqueuedTime?;
    # Sequence number assigned to a message when it is added to the queue/subscription
    int enqueuedSequenceNumber?;
    # Error description of why a message went to a dead-letter queue 
    string deadLetterErrorDescription?;
    # Reason why a message was moved to a dead-letter queue  
    string deadLetterReason?;
    # Original queue/subscription where the message was before being moved to the dead-letter queue 
    string deadLetterSource?;
    # Current state of a message in the queue/subscription, could be "Active", "Scheduled", "Deferred", etc.
    string state?;
|};
```

- `MessageBatch` record represents an Azure service bus message batch.

```ballerina
public type MessageBatch record {|
    # Number of messages in the batch
    int messageCount = -1;
    # Array of Azure service bus message representation (Array of Message records)
    Message[] messages = [];
|};
```

## 3. Message sender

A message sender is responsible for sending Azure service bus messages to a queue or topic on the Azure service bus instance.

### 3.1. Configurations

- `EntityType` enum represents the Azure service bus resource entity type.

```ballerina
public enum EntityType {
    QUEUE = "queue",
    TOPIC = "topic"
}
```

- `AmqpRetryOptions` represents options that can be used to specify the underlying client retry behavior.

```ballerina
public type AmqpRetryOptions record {|
    # Maximum number of retry attempts
    int maxRetries = 3;
    # Delay between retry attempts in seconds
    decimal delay = 10;
    # Maximum permissible delay between retry attempts in seconds
    decimal maxDelay = 60;
    # Maximum duration to wait for completion of a single attempt in seconds
    decimal tryTimeout = 60;
    # Approach to use for calculating retry delays
    AmqpRetryMode retryMode = asb:FIXED;
|};

# Represents the type of approach to apply when calculating the delay between retry attempts.
public enum AmqpRetryMode {
    # Retry attempts happen at fixed intervals; each delay is a consistent duration.
    FIXED,
    # Retry attempts will delay based on a backoff strategy, where each attempt will increase the duration that it waits before retrying.
    EXPONENTIAL
};
```

- `ASBServiceSenderConfig` record represents the Azure service bus message sender configurations.

```ballerina
public type ASBServiceSenderConfig record {
    # An enumeration value of type EntityType, which specifies whether the connection is for a topic or a queue. 
    # The valid values are TOPIC and QUEUE
    EntityType entityType;
    # A string field that holds the name of the topic or queue
    string topicOrQueueName;
    # A string field that holds the Service Bus connection string with Shared Access Signatures
    string connectionString;
    # Retry configurations related to underlying AMQP message sender
    AmqpRetryOptions amqpRetryOptions = {};
};
```

### 3.2. Initialization

- The `asb:MessageSender` can be initialized by providing the `asb:ASBServiceSenderConfig`.

```ballerina
# Initializes an Azure service bus message sender.
# ```
# configurable string connectionString = ?;
# asb:ASBServiceSenderConfig senderConfig = {
#   connectionString: connectionString,
#   entityType: asb:QUEUE,
#   topicOrQueueName: "testQueue1" 
# };
# asb:MessageSender sender = check new(senderConfig);
# ```
#
# + config - Azure service bus sender configuration
# + return - The `asb:MessageSender` or an `asb:Error` if the initialization failed
public isolated function init(asb:ASBServiceSenderConfig config) returns asb:Error?;
```

### 3.3. Functions

- To send a message to a queue or topic, the `send` function can be used.

```ballerina
# Send message to queue or topic with a message body.
# ```
# check sender->send({body: "Sample text message", contentType: asb:TEXT});
# ```
# 
# + message - Azure service bus message representation (`asb:Message` record)
# + return - An `asb:Error` if failed to send message or else `()`
isolated remote function send(asb:Message message) returns asb:Error?;
```

- To send a message to a queue or topic with a message body, the `sendPayload` function can be used.

```ballerina
# Send message to queue or topic with a message body.
# ```
# check sender->sendPayload("Sample text message");
# ```
#
# + messagePayload - Message body
# + return - An `asb:Error` if failed to send message or else `()`
isolated remote function sendPayload(anydata messagePayload) returns asb:Error?;
```

- To send a scheduled message to a queue or topic, the `schedule` function can be used.

```ballerina
# Sends a scheduled message to the Azure Service Bus entity this sender is connected to. 
# A scheduled message is enqueued and made available to receivers only at the scheduled enqueue time.
# ```
# time:Civil scheduledTime = check time:civilFromString("2007-12-03T10:15:30.00Z");
# check sender->send({body: "Sample text message", contentType: asb:TEXT}, scheduledTime);
# ```
#
# + message - Message to be scheduled  
# + scheduledEnqueueTime - Datetime at which the message should appear in the Service Bus queue or topic
# + return - The sequence number of the scheduled message which can be used to cancel the scheduling of the message
isolated remote function schedule(asb:Message message, time:Civil scheduledEnqueueTime) returns int|asb:Error;
```

- To cancel the enqueuing of a scheduled message, the `cancel` function can be used.

```ballerina
# Cancels the enqueuing of a scheduled message, if they are not already enqueued.
# ```
# check sender->cancel(1);
# ```
#
# + sequenceNumber - The sequence number of the message to cancel
# + return - An `asb:Error` if the message could not be cancelled or else `()`.
isolated remote function cancel(int sequenceNumber) returns asb:Error?;
```

- To send a batch of messages to a queue or topic, the `sendBatch` function can be used.

```ballerina
# Send batch of messages to queue or topic.
# ```
# asb:MessageBatch batch = ...;
# check sender->sendBatch(batch);
# ```
#
# + messageBatch - Azure service bus batch message representation (`asb:MessageBatch` record)
# + return - An `asb:Error` if failed to send message or else `()`
isolated remote function sendBatch(asb:MessageBatch messageBatch) returns asb:Error?;
```

- To close the ASB sender connection, the `close` function can be used.

```ballerina
# Closes the ASB sender connection.
# ```
# check sender->close();
# ```
#
# + return - An `asb:Error` if failed to close connection or else `()`
isolated remote function close() returns asb:Error?;
```

## 4. Message receiver

A message receiver is responsible for receiving Azure service bus messages from a queue or topic/subscription on the Azure service bus instance.

### 4.1. Configurations

- `TopicSubsConfig` record represents the configuration details of a topic and its associated subscription.

```ballerina
public type TopicSubsConfig record {
    # A string field that holds the name of the topic
    string topicName;
    # A string field that holds the name of the subscription associated with the topic
    string subscriptionName;
};
```

- `QueueConfig` record represents the configuration details of a queue.

```ballerina
public type QueueConfig record {
    # the configuration details of a queue
    string queueName;
};
```

- `ReceiveMode` enum represents the possible receive modes for a Azure service bus message receiver.

```ballerina
public enum ReceiveMode {
    RECEIVE_AND_DELETE, PEEK_LOCK
}
```

- `ASBServiceReceiverConfig` record represents the Azure service bus message receiver configurations.

```ballerina
public type ASBServiceReceiverConfig record {
    # A string field that holds the Service Bus connection string with Shared Access Signatures
    string connectionString;
    # This field holds the configuration details of either a topic or a queue. The type of the entity is
    # determined by the entityType field. The actual configuration details are stored in either a
    # TopicSubsConfig or a QueueConfig record
    TopicSubsConfig|QueueConfig entityConfig;
    # This field holds the receive modes(RECEIVE_AND_DELETE/PEEK_LOCK) for the connection. The receive mode determines 
    # how messages are retrieved from the entity. The default value is PEEK_LOCK 
    ReceiveMode receiveMode = asb:PEEK_LOCK;
    # Max lock renewal duration under PEEK_LOCK mode in seconds. Setting to 0 disables auto-renewal
    int maxAutoLockRenewDuration = 300;
    # Retry configurations related to underlying AMQP message receiver
    AmqpRetryOptions amqpRetryOptions = {};
};
```

### 4.2. Initialization

- The `asb:MessageReceiver` can be initialized by providing the `asb:ASBServiceReceiverConfig`.

```ballerina
# Initializes an Azure service bus message receiver.
# ```
# configurable string connectionString = ?;
# asb:ASBServiceReceiverConfig receiverConfig = {
#     connectionString: connectionString,
#     entityConfig: {
#         queueName: "testQueue1"
#     },
#     receiveMode: asb:PEEK_LOCK
# };
# asb:MessageReceiver receiver = check new(receiverConfig);
# ```
#
# + config - Azure service bus receiver configuration.
# + return - The `asb:MessageReceiver` or an `asb:Error` if the initialization failed
public isolated function init(asb:ASBServiceReceiverConfig config) returns asb:Error?;
```

### 4.3. Functions

- To receive a message from a queue or subscription, the `receive` function can be used.

```ballerina
# Receive message from queue or subscription.
# ```
# asb:Message? message = check receiver->receive();
# ```
#
# + serverWaitTime - Specified server wait time in seconds to receive message (optional)
# + T - Expected type of the message. This can be either a `asb:Message` or a subtype of it.
# + deadLettered - If set to `true`, messages from dead-letter queue will be received. (optional)
# + return - A `asb:Message` record if message is received, `()` if no message is in the queue or else an `asb:Error`
# if failed to receive message
isolated remote function receive(int? serverWaitTime = 60, typedesc<Message> T = <>, boolean deadLettered = false) 
        returns T|asb:Error?;
```

- To receive a message payload from a queue or subscription, the `receivePayload` function can be used.

```ballerina
# Receive message payload from queue or subscription.
# ```
# string messagePayload = check receiver->receivePayload();
# ```
#
# + serverWaitTime - Specified server wait time in seconds to receive message (optional)
# + T - Expected type of the message. This can be any subtype of `anydata` type
# + deadLettered - If set to `true`, messages from dead-letter queue will be received. (optional)
# + return - A `asb:Message` record if message is received, `()` if no message is in the queue or else an `asb:Error`
# if failed to receive message
isolated remote function receivePayload(int? serverWaitTime = 60, typedesc<anydata> T = <>, boolean deadLettered = false) 
        returns T|asb:Error;
```

- To receive a batch of messages from a queue or subscription, the `receiveBatch` function can be used.

```ballerina
# Receive batch of messages from queue or subscription.
# ```
# asb:MessageBatch batch = check receiver->receiveBatch(10);
# ```
#
# + maxMessageCount - Maximum message count to receive in a batch
# + serverWaitTime - Specified server wait time in seconds to receive message (optional)
# + deadLettered - If set to `true`, messages from dead-letter queue will be received. (optional)
# + return - A `asb:MessageBatch` record if batch is received, `()` if no batch is in the queue or else an `asb:Error`
# if failed to receive batch
isolated remote function receiveBatch(int maxMessageCount, int? serverWaitTime = (), boolean deadLettered = false) 
        returns asb:MessageBatch|asb:Error?;
```

- To mark an Azure service bus message as complete, the `complete` function can be used.

```ballerina
# Complete message from queue or subscription based on messageLockToken. Declares the message processing to be 
# successfully completed, removing the message from the queue.
# ```
# asb:Message message = ...;
# check receiver->complete(message);
# ```
#
# + message - `asb:Message` record
# + return - An `asb:Error` if failed to complete message or else `()`
isolated remote function complete(asb:Message message) returns asb:Error?;
```

- To mark an Azure service bus message as abandon, the `abandon` function can be used.

```ballerina
# Abandon message from queue or subscription based on messageLockToken. Abandon processing of the message for 
# the time being, returning the message immediately back to the queue to be picked up by another (or the same) 
# receiver.
# ```
# asb:Message message = ...;
# check receiver->abandon(message);
# ```
# 
# + message - `asb:Message` record
# + return - An `asb:Error` if failed to abandon message or else `()`
isolated remote function abandon(asb:Message message) returns asb:Error?;
```

- To move an Azure service bus message to the dead-letter queue, the `deadLetter` function can be used.

```ballerina
# Dead-Letter the message & moves the message to the Dead-Letter Queue based on messageLockToken. Transfer 
# the message from the primary queue into a special "dead-letter sub-queue".
# ```
# asb:Message message = ...;
# check receiver->deadLetter(message);
# ```
#
# + message - `asb:Message` record
# + deadLetterReason - The deadletter reason (optional)
# + deadLetterErrorDescription - The deadletter error description (optional)
# + return - An `asb:Error` if failed to deadletter message or else `()`
isolated remote function deadLetter(asb:Message message, string deadLetterReason = "DEADLETTERED_BY_RECEIVER", string?  deadLetterErrorDescription = ()) returns asb:Error?;
```

- To mark an Azure service bus message as deferred, the `defer` function can be used.

```ballerina
# Defer the message in a Queue or Subscription based on messageLockToken.  It prevents the message from being 
# directly received from the queue by setting it aside such that it must be received by sequence number.
# ```
# asb:Message message = ...;
# int sequenceNumber = check receiver->defer(message);
# ```
# 
# + message - `asb:Message` record
# + return - An `asb:Error` if failed to defer message or else sequence number
isolated remote function defer(asb:Message message) returns int|asb:Error;
```

- To receive a deferred message, the `receiveDeferred` function can be used.

```ballerina
# Receives a deferred Message. Deferred messages can only be received by using sequence number and return
# Message object.
# ```
# asb:Message? message = check receiver->receiveDeferred(1);
# ```
# 
# + sequenceNumber - Unique number assigned to a message by Service Bus. The sequence number is a unique 64-bit
# integer assigned to a message as it is accepted and stored by the broker and functions as
# its true identifier.
# + return - An `asb:Error` if failed to receive deferred message, a Message record if successful or else `()`
isolated remote function receiveDeferred(int sequenceNumber) returns asb:Message|asb:Error?;
```

- To renew the lock on a message in a queue or subscription, the `renewLock` function can be used.

```ballerina
# The operation renews lock on a message in a queue or subscription based on messageLockToken.
# ```
# asb:Message message = ...;
# check receiver->renewLock(message);
# ```
# 
# + message - `asb:Message` record
# + return - An `asb:Error` if failed to renew message or else `()`
isolated remote function renewLock(asb:Message message) returns asb:Error?;
```

- To close the ASB receiver connection, the `close` function can be used.

```ballerina
# Closes the ASB receiver connection.
# ```
# check receiver->close();
# ```
#
# + return - An `asb:Error` if failed to close connection or else `()`
isolated remote function close() returns asb:Error?;
```

## 5. Message listener

An Azure Service Bus message listener is a mechanism for asynchronously receiving messages in an event-driven manner. Rather than continuously polling for messages, a message listener can be set up. When a message arrives at a designated queue or topic on the Azure Service Bus, the listener's callback method is automatically invoked.

### 5.1. Configurations

- `ListenerConfiguration` record represents the Azure service bus message listener configurations.

```ballerina
public type ListenerConfiguration record {|
    *ASBServiceReceiverConfig;
    # Enables auto-complete and auto-abandon of received messages
    boolean autoComplete = true;
    # The number of messages to prefetch
    int prefetchCount = 0;
    # Max concurrent messages that this listener should process
    int maxConcurrency = 1;
|};
```

### 5.2. Initialization

- The `asb:Listener` can be initialized by providing the `asb:ListenerConfiguration`.

```ballerina
# Creates a new `asb:Listener`.
# ```
# listener asb:Listener asbListener = check new (
#   connectionString = "xxxxxxxx",
#   entityConfig = {
#       queueName: "test-queue"
#   },
#   autoComplete = false
# );
# ```
# 
# + config - ASB listener configurations
# + return - An `asb:Error` if an error is encountered or else '()'
public isolated function init(*asb:ListenerConfiguration config) returns asb:Error?;
```

### 5.3. Functions

- To attach a service to the listener, the `attach` function can be used.

```ballerina
# Attaches an `asb:Service` to a listener.
# ```
# check asbListener.attach(asbService);
# ```
# 
# + 'service - The service instance
# + name - Name of the service
# + return - An `asb:Error` if there is an error or else `()`
public isolated function attach(asb:Service 'service, string[]|string? name = ()) returns asb:Error?;
```

- To detach a service from the listener, the `detach` function can be used.

```ballerina
# Detaches an `asb:Service` from the the listener.
# ```
# check asbListener.detach(asbService);
# ```
#
# + 'service - The service to be detached
# + return - An `asb:Error` if there is an error or else `()`
public isolated function detach(asb:Service 'service) returns asb:Error?;
```

- To start the listener, the `'start` function can be used.

```ballerina
# Starts the `asb:Listener`.
# ```
# check asbListener.'start();
# ```
#
# + return - An `asb:Error` if there is an error or else `()`
public isolated function 'start() returns asb:Error?;
```

- To stop the listener gracefully, the `gracefulStop` function can be used.

```ballerina
# Stops the `asb:Listener` gracefully.
# ```
# check asbListener.gracefulStop();
# ```
#
# + return - An `asb:Error` if there is an error or else `()`
public isolated function gracefulStop() returns asb:Error?;
```

- To stop the listener immediately, the `immediateStop` function can be used.

```ballerina
# Stops the `asb:Listener` immediately.
# ```
# check asbListener.immediateStop();
# ```
#
# + return - An `asb:Error` if there is an error or else `()`
public isolated function immediateStop() returns Error?;
```

### 5.4. Caller

An `asb:Caller` can be used to mark messages as complete, abandon, deadLetter, or defer.

### 5.4.1. Functions

- To mark an Azure service bus message as complete, the `complete` function can be used.

```ballerina
# Complete message from queue or subscription based on messageLockToken. Declares the message processing to be 
# successfully completed, removing the message from the queue.
# ```
# check caller->complete(message);
# ```
#
# + return - An `asb:Error` if failed to complete message or else `()`
isolated remote function complete() returns asb:Error?;
```

- To mark an Azure service bus message as abandon, the `abandon` function can be used.

```ballerina
# Abandon message from queue or subscription based on messageLockToken. Abandon processing of the message for 
# the time being, returning the message immediately back to the queue to be picked up by another (or the same) 
# receiver.
# ```
# check caller->abandon();
# ```
# 
# + propertiesToModify - Message properties to modify
# + return - An `asb:Error` if failed to abandon message or else `()`
isolated remote function abandon(*record {|anydata...;|} propertiesToModify) returns asb:Error?;
```

- To move an Azure service bus message to the dead-letter queue, the `deadLetter` function can be used.

```ballerina
# Options to specify when sending an `asb:Message` received via `asb:ReceiveMode#PEEK_LOCK` to the dead-letter queue.
#
# + deadLetterReason - The deadletter reason
# + deadLetterErrorDescription - The deadletter error description
# + propertiesToModify - Message properties to modify
public type DeadLetterOptions record {|
    string deadLetterReason?;
    string deadLetterErrorDescription?;
    map<anydata> propertiesToModify?;
|};

# Dead-Letter the message & moves the message to the Dead-Letter Queue based on messageLockToken. Transfer 
# the message from the primary queue into a special "dead-letter sub-queue".
# ```
# check caller->deadLetter();
# ```
#
# + options - Options to specify while putting message in dead-letter queue
# + return - An `asb:Error` if failed to deadletter message or else `()`
isolated remote function deadLetter(*DeadLetterOptions options) returns asb:Error?;
```

- To mark an Azure service bus message as deferred, the `defer` function can be used.

```ballerina
# Defer the message in a Queue or Subscription based on messageLockToken.  It prevents the message from being 
# directly received from the queue by setting it aside such that it must be received by sequence number.
# ```
# check caller->defer();
# ```
# 
# + propertiesToModify - Message properties to modify
# + return - An `asb:Error` if failed to defer message or else sequence number
isolated remote function defer(*record {|anydata...;|} propertiesToModify) returns asb:Error?;
```

### 5.5. Validations

#### 5.5.1. Attaching multiple services to a listener

A listener is directly associated with a single native client, which in turn is tied to a specific Azure Service Bus (ASB) entity, either a queue or a topic. Consequently, linking multiple services to the same listener does not make sense. Therefore, if multiple services are attached to the same listener, a runtime error will be thrown. In the future, this should be validated during compilation by a compiler plugin.

#### 5.5.2. Using `autoComplete` mode

When `autoComplete` mode is enabled, explicit acknowledgment or rejection of messages (ack/nack) by the caller is unnecessary. Therefore, if `autoComplete` is active and the developer defines the `onMessage` method with an `asb:Caller` parameter, a runtime error will be thrown. In the future, this should be validated during compilation by a compiler plugin.

### 5.6. Usage

After initializing the `asb:Listener` an `asb:Service` can be attached to it.

```ballerina
service asb:Service on asbListener {

    isolated remote function onMessage(asb:Message message, asb:Caller caller) returns error? {
        // implement the message processing logic here
    }

    isolated remote function onError(asb:MessageRetrievalError 'error) returns error? {
        // implement error handling logic here
    }
}
```

## 6. Administrator

An administrator client is responsible for managing a Service Bus namespace.

### 6.1. Initialization

- The `asb:Administrator` can be initialized by providing the connection string.

```ballerina
# Initialize the Azure Service Bus Admin client.
# ```
# configurable string connectionString = ?;
# asb:Administrator admin = check new (connectionString);
# ```
# 
# + connectionString - Azure Service Bus connection string
# + return - The `asb:Administrator` or an `asb:Error` if the initialization failed
public isolated function init(string connectionString) returns asb:Error?;
```

### 6.2. Functions

- To create an ASB topic, the `createTopic` function can be used.

```ballerina
# Create a topic with the given name or name and options.
# ```
# asb:TopicProperties? topicProperties = check admin->createTopic("topic-1");
# ```
# 
# + topicName - Topic name
# + topicOptions - Topic options to create the topic.This should be a record of type CreateTopicOptions
# + return - Topic properties(Type of `asb:TopicProperies`) or error
isolated remote function createTopic(string topicName, *asb:CreateTopicOptions topicOptions) returns asb:TopicProperties|asb:Error?;
```

- To retrieve details of an ASB topic, the `getTopic` function can be used.

```ballerina
# Get the topic with the given name.
# ```
# asb:TopicProperties? topicProperties = check admin->getTopic("topic-1");
# ```
#
# + topicName - Topic name
# + return - Topic properties(Type of `asb:TopicProperies`) or error
isolated remote function getTopic(string topicName) returns asb:TopicProperties|asb:Error?;
```

- To update the configurations of an ASB topic, the `updateTopic` function can be used.

```ballerina
# Update the topic with the given options.
# ```
# asb:TopicProperties? topicProp = check admin->updateTopic("topic-1", supportOrdering = true);
# ```
#
# + topicName - Topic name
# + topicOptions - Topic options to update the topic.This should be a record of type UpdateTopicOptions
# + return - Topic properties(Type of `asb:TopicProperies`) or error
isolated remote function updateTopic(string topicName, *asb:UpdateTopicOptions topicOptions) returns asb:TopicProperties|asb:Error?;
```

- To list all the ASB topics, the `listTopics` function can be used.

```ballerina
# List the topics.
# ```
# asb:TopicList? topics = check admin->listTopics();
# ```
#
# + return - Topic list(Type of `asb:TopicList`) or error
isolated remote function listTopics() returns asb:TopicList|asb:Error?;
```

- To delete an ASB topic, the `deleteTopic` function can be used.

```ballerina
# Delete the topic with the given name.
# ```
# check admin->deleteTopic("topic-1");
# ```
# 
# + topicName - Topic name
# + return - an `asb:Error` or nil
isolated remote function deleteTopic(string topicName) returns asb:Error?;
```

- To check whether an ASB topic exists, the `topicExists` function can be used.

```ballerina
# Get the status of existance of a topic with the given name.
# ```
# boolean exists = check admin->topicExists("topic-1");
# ```
# 
# + topicName - Topic name
# + return - Boolean or error
isolated remote function topicExists(string topicName) returns boolean|asb:Error?;
```

- To create an ASB subscription, the `createSubscription` function can be used.

```ballerina
# Create a subscription with the given name or name and options.
# ```
# asb:SubscriptionProperties? subscriptionProperties = check admin->createSubscription("topic-1", "sub-a");
# ```
#
# + topicName - Name of the topic associated with subscription
# + subscriptionName - Name of the subscription
# + subscriptionOptions - Subscription options to create the subscription.This should be a record of type CreateSubscriptionOptions.
# + return - Subscription properties(Type of `asb:SubscriptionProperies`) or error
isolated remote function createSubscription(string topicName, string subscriptionName, *asb:CreateSubscriptionOptions subscriptionOptions) 
        returns asb:SubscriptionProperties|asb:Error?;
```

- To retrieve the details of an ASB subscription, the `getSubscription` function can be used.

```ballerina
# Get the subscription with the given name.
# ```
# asb:SubscriptionProperties? subscriptionProperties = check admin->getSubscription("topic-1", "sub-a");
# ```
#
# + topicName - Name of the topic associated with subscription
# + subscriptionName - Name of the subscription
# + return - Subscription properties(Type of `asb:SubscriptionProperies`) or error
isolated remote function getSubscription(string topicName, string subscriptionName) returns asb:SubscriptionProperties|asb:Error?;
```

- To update the configurations of an ASB subscription, the `updateSubscription` function can be used.

```ballerina
# Update the subscription with the given options.
# ```
# asb:SubscriptionProperties? subscriptionProperties = check admin->updateSubscription("topic-1", "sub-a", maxDeliveryCount = 10);
# ```
# 
# + topicName - Name of the topic associated with subscription
# + subscriptionName - Name of the subscription
# + subscriptionOptions - Subscription options to update the subscription.This should be a record of type UpdateSubscriptionOptions
# + return - Subscription properties(Type of `asb:SubscriptionProperies`) or error
isolated remote function updateSubscription(string topicName, string subscriptionName, *asb:UpdateSubscriptionOptions subscriptionOptions) 
        returns asb:SubscriptionProperties|asb:Error?;
```

- To list subscriptions for an ASB topic, the `listSubscriptions` function can be used.

```ballerina
# List the subscriptions.
# ```
# asb:SubscriptionList? subscriptions = check admin->listSubscriptions("topic-1");
# ```
#
# + topicName - Name of the topic associated with subscription
# + return - Subscription list(Type of `asb:SubscriptionList`) or error
isolated remote function listSubscriptions(string topicName) returns asb:SubscriptionList|asb:Error?;
```

- To delete an ASB subscription, the `deleteSubscription` function can be used.

```ballerina
# Delete the subscription with the given name.
# ```
# check admin->deleteSubscription("topic-1", "sub-a");
# ```
#
# + topicName - Topic name
# + subscriptionName - Subscription name
# + return - An `asb:Error` or nil
isolated remote function deleteSubscription(string topicName, string subscriptionName) returns asb:Error?;
```

- To check whether an ASB subscription exists, the `subscriptionExists` function can be used.

```ballerina
# Get the status of existance of a subscription with the given name.
# ```
# boolean exists = check admin->subscriptionExists("topic-1", "sub-a");
# ```
#
# + topicName - Topic name
# + subscriptionName - Subscription name
# + return - Boolean or error
isolated remote function subscriptionExists(string topicName, string subscriptionName) returns boolean|asb:Error?;
```

- To create a subscription rule, the `createRule` function can be used.

```ballerina
# Create a rule with the given name or name and options.
# ```
# asb:RuleProperties? properties = check admin->createRule("topic-1", "sub-a", "rule-1");
# ```
# 
# + topicName - Name of the topic associated with subscription
# + subscriptionName - Name of the subscription
# + ruleName - Name of the rule
# + ruleOptions - Rule options to create the rule.This should be a record of type CreateRuleOptions
# + return - Rule properties(Type of `asb:RuleProperies`) or error
isolated remote function createRule(string topicName, string subscriptionName, string ruleName, *asb:CreateRuleOptions ruleOptions) 
        returns asb:RuleProperties|asb:Error?;
```

- To retrieve the details for a subscription rule, the `getRule` function can be used.

```ballerina
# Get the rule with the given name.
# ```
# asb:RuleProperties? properties = check admin->getRule("topic-1", "sub-a", "rule-1");
# ```
#
# + topicName - Name of the topic associated with subscription
# + subscriptionName - Name of the subscription associated with rule
# + ruleName - Rule name
# + return - An `asb:Error` or nil
isolated remote function getRule(string topicName, string subscriptionName, string ruleName) returns asb:RuleProperties|asb:Error?;
```

- To update the configurations of a subscription rule, the `updateRule` can be used.

```ballerina
# Update the rule with the options.
# ```
# asb:SqlRule rule = ...;
# asb:RuleProperties? ruleProperties = check admin->updateRule("topic-1", "sub-a", "rule-1", rule = rule);
# ```
#
# + topicName - Name of the topic associated with subscription
# + subscriptionName - Name of the subscription associated with rule
 + ruleName - Rule name
# + ruleOptions - Rule options to update the rule.This should be a record of type UpdateRuleOptions
# + return - Rule properties(Type of `asb:RuleProperies`) or error
isolated remote function updateRule(string topicName, string subscriptionName, string ruleName, *asb:UpdateRuleOptions ruleOptions) 
        returns asb:RuleProperties|asb:Error?;
```

- To list the subscription rules, the `listRules` function can be used.

```ballerina
# List the rules.
# ```
# asb:RuleList? rules = check admin->listRules("topic-1", "sub-a");
# ```
#
# + topicName - Name of the topic associated with subscription
# + subscriptionName - Name of the subscription
# + return - Rule list(Type of `asb:RuleList`) or error
isolated remote function listRules(string topicName, string subscriptionName)returns asb:RuleList|asb:Error?;
```

- To delete a subscription rule, the `deleteRule` function can be used.

```ballerina
# Delete the rule with the given name.
# ```
# check admin->deleteRule("topic-1", "sub-a", "rule-1");
# ```
# + topicName - Name of the topic associated with subscription
# + subscriptionName - Name of the subscription associated with rule
# + ruleName - Rule name
# + return - An `asb:Error` or nil
isolated remote function deleteRule(string topicName, string subscriptionName, string ruleName) returns asb:Error?;
```

- To create an ASB queue, the `createQueue` function can be used.

```ballerina
# Create a queue with the given name or name and options.
# ```
# asb:QueueProperties queueProperties = check admin->createQueue("queue-1");
# ```
# 
# + queueName - Name of the queue
# + queueOptions - Queue options to create the queue.This should be a record of type CreateQueueOptions
# + return - Queue properties(Type of `asb:QueueProperties`) or error
isolated remote function createQueue(string queueName, *asb:CreateQueueOptions queueOptions) returns asb:QueueProperties|asb:Error?;
```

- To retrieve the details of an ASB queue, the `getQueue` function can be used.

```ballerina
# Get the queue with the given name.
# ```
# asb:QueueProperties? queueProperties = check admin->getQueue("queue-1");
# ```
#
# + queueName - Name of the queue
# + return - Queue properties(Type of `asb:QueueProperties`) or error
isolated remote function getQueue(string queueName) returns asb:QueueProperties|asb:Error?;
```

- To update the configurations of an ASB queue, the `updateQueue` function can be used.

```ballerina
# Update the queue with the options.
# ```
# asb:QueueProperties? queueProperties = check admin->updateQueue("queue-1", maxDeliveryCount = 10);
# ```
# 
# + queueName - Name of the queue
# + queueOptions - Queue options to update the queue.This should be a record of type UpdateQueueOptions
# + return - Queue properties(Type of `asb:QueueProperties`) or error
isolated remote function updateQueue(string queueName, *asb:UpdateQueueOptions queueOptions) returns asb:QueueProperties|asb:Error?;
```

- To list the available ASB queues, the `listQueues` function can be used.

```ballerina
# List the queues.
# ```
# asb:QueueList? queues = check admin->listQueues();
# ```
#
# + return - Queue list(Type of `asb:QueueList`) or error
isolated remote function listQueues() returns asb:QueueList|asb:Error?;
```

- To delete an ASB queue, the `deleteQueue` function can be used.

```ballerina
# Delete the queue with the given name.
# ```
# check admin->deleteQueue("queue-1");
# ```
#
# + queueName - Name of the queue
# + return - An `asb:Error` or nil
isolated remote function deleteQueue(string queueName) returns asb:Error?;
```

- To check whether an ASB queue exists, the `queueExists` function can be used.

```ballerina
# Check whether the queue exists.
# ```
# boolean exists = check admin->queueExists("queue-1");
# ```
#
# + queueName - Name of the queue
# + return - Boolean or error
isolated remote function queueExists(string queueName) returns boolean|asb:Error?;
```
