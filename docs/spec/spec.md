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
- `asb:Administrator` - used to perform administrative actions on an Azure Service Bus resource.
