# Proposal: Introduce async message consumption functionality to Ballerina Azure service bus connector

_Authors_: @ayeshLK \
_Reviewers_: @daneshk @NipunaRanasinghe \
_Created_: 2024/05/07 \
_Updated_: 2024/05/14 \
_Issue_: [#6508](https://github.com/ballerina-platform/ballerina-library/issues/6508)

## Summary

Asynchronous message consumption is a standard practice used in message broker based systems. Hence, the 
Ballerina Azure service bus connector should support the async message consumption functionality.

## Goals

- Introduce async message consumption functionality to the Ballerina Azure service bus connector

## Motivation

In applications that rely on message brokers, the ability to process messages asynchronously is crucial for 
ensuring scalability, reliability, and responsiveness. While the Azure Service Bus Java SDK supports asynchronous 
message consumption, this capability is currently absent in the Ballerina connector. Therefore, it is essential to 
integrate this functionality into the Ballerina Azure Service Bus connector.

In Ballerina, asynchronous message consumption corresponds to a listener-based programming model. Consequently, 
the Ballerina Azure Service Bus connector must support listener-based message consumption capability.

## Description

As mentioned in the Goals section the purpose of this proposal is to introduce async message consumption 
functionality to the Ballerina Azure service bus connector.

The key functionalities expected from this change are as follows,

- Listener implementation which supports asynchronous message consumption from Azure service bus entity (topic or queue)
- Service type which supports message processing and error handling
- Caller implementation which supports ack/nack functionalities of the underlying client

Following is an example code segment with the proposed solution:

```ballerina
import ballerinax/asb;

listener asb:Listener asbListener = check new (
    connectionString = "<connection-string>",
    entityConfig = {
        queueName: "<queue-name>"
    },
    autoComplete = false
);

service asb:Service on asbListener {

    isolated remote function onMessage(asb:Message message, asb:Caller caller) returns error? {
        // implement the message processing logic here
    }

    isolated remote function onError(asb:MessageRetrievalError 'error) returns error? {
        // implement error handling logic here
    }
}
```

### API additions

The following configuration records will be introduced to the package:

```ballerina
# Represents Azure service bus listener configuration.
#
# + autoComplete - Enables auto-complete and auto-abandon of received messages 
# + prefetchCount - The number of messages to prefetch  
# + maxConcurrency - Max concurrent messages that this listener should process
public type ListenerConfiguration record {|
    *asb:ASBServiceReceiverConfig;
    boolean autoComplete = true;
    int prefetchCount = 0;
    int maxConcurrency = 1;
|};

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
```

The following error types and error details records will be introduced to the package:

```ballerina
# Error type to capture the errors occurred while retrieving messages in Azure service bus listener.
public type MessageRetrievalError distinct Error & error<ErrorContext>;

# Represents message retrieval error context.
#
# + entityPath - The entity path of the error source  
# + className - The name of the originating class    
# + namespace -  The namespace of the error source  
# + errorSource - The error source, such as a function or action name   
# + reason - The reason for the error
public type ErrorContext record {
    string entityPath;
    string className;
    string namespace;
    string errorSource;
    string reason;
};
```

The following service object and caller type will be introduced to the package:

```ballerina
# The ASB service type.
public type Service distinct service object {
    isolated remote function onMessage(asb:Message message, asb:Caller caller) returns error?;

    isolated remote function onError(asb:MessageRetrievalError 'error) returns error?;
};

# Represents a ASB caller, which can be used to mark messages as complete, abandon, deadLetter, or defer.
public type Caller distinct client object {

    # Complete message from queue or subscription based on messageLockToken. Declares the message processing to be 
    # successfully completed, removing the message from the queue.
    isolated remote function complete() returns asb:Error?;

    # Abandon message from queue or subscription based on messageLockToken. Abandon processing of the message for 
    # the time being, returning the message immediately back to the queue to be picked up by another (or the same) 
    # receiver.
    isolated remote function abandon(*record {|anydata...;|} propertiesToModify) returns asb:Error?;

    # Dead-Letter the message & moves the message to the Dead-Letter Queue based on messageLockToken. Transfer 
    # the message from the primary queue into a special "dead-letter sub-queue".
    isolated remote function deadLetter(*DeadLetterOptions options) returns asb:Error?;

    # Defer the message in a Queue or Subscription based on messageLockToken.  It prevents the message from being 
    # directly received from the queue by setting it aside such that it must be received by sequence number.
    isolated remote function defer(*record {|anydata...;|} propertiesToModify) returns asb:Error?;
};
```

### Runtime errors and validations

#### Attaching multiple services to a listener

A listener is directly associated with a single native client, which in turn is tied to a specific Azure Service Bus (ASB) entity, either a queue or a topic. Consequently, linking multiple services to the same listener does not make sense. Therefore, if multiple services are attached to the same listener, a runtime error will be thrown. In the future, this should be validated during compilation by a compiler plugin.

#### Using `autoComplete` mode

When `autoComplete` mode is enabled, explicit acknowledgment or rejection of messages (ack/nack) by the caller is unnecessary. Therefore, if `autoComplete` is active and the developer defines the `onMessage` method with an `asb:Caller` parameter, a runtime error will be thrown. In the future, this should be validated during compilation by a compiler plugin.

## Further improvements

- Introduce payload data-binding support for the `onMessage` method of the `asb:Service`
- Introduce compiler plugin validation for `asb:Serivce` type
