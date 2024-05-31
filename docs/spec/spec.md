# Specification: Ballerina `asb` Library

_Authors_: @ayeshLK \
_Reviewers_: @NipunaRanasinghe @niveathika @RDPerera \
_Created_: 2024/05/31 \
_Updated_: 2024/05/31 \
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
