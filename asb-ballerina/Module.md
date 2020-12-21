Connects to Microsoft Azure Service Bus using Ballerina.

# Module Overview
Microsoft Azure Service Bus is a fully managed enterprise message broker with message queues and publish-subscribe 
topics. The primary wire protocol for Service Bus is Advanced Messaging Queueing Protocol (AMQP) 1.0, an open ISO/IEC standard. Service Bus provides a Microsoft supported native API and this module make use of this public API. Service Bus service APIs access the Service Bus service directly, and perform various management operations at the entity level, 
rather than at the namespace level (such as sending a message to a queue). This module supports these basic operations. These APIs use SAS authentication and this module supports SAS authentication.  

# Compatibility
|                     |    Version                  |
|:-------------------:|:---------------------------:|
| Ballerina Language  | Swan-Lake-Preview8          |
| Service Bus API     | v1.2.8                      |

# Supported Operations

## Operations on Connecting to the ASB
The `ballerinax/asb` module contains operations related to connecting with the azure service bus. It includes operations 
to create SenderConnection, close SenderConnection, create ReceiverConnection, close ReceiverConnection. 

## Operations on Queue
The `ballerinax/asb` module contains operations on azure service bus Queue. It includes operations to send message to 
queue, receive message from queue, receive messages from queue, send batch of messages to queue, receive batch of 
messages from queue, complete messages from queue, complete message from queue, and abandon message from queue.  

## Operations on Topic/Subscription
The `ballerinax/asb` module contains operations on azure service bus Topic/Subscription. It includes operations to send message to topic, receive message from subscription, receive messages from subscription, send batch of messages to 
topic, receive batch of messages from subscription, complete messages from subscription, complete message from subscription, and abandon message from subscription.  

## Listener Capabilities
The `ballerinax/asb` module contains operations related to asynchronous message listening capabilities from the azure service bus. It includes operations to attach service, start listening, detach service, graceful stop listening, and immediate stop listening.

# Configuration
Instantiate the connector by giving authorization credentials that a client application can use to send/receive messages 
to/from the queue/topic/subscription.

## Getting the authorization credentials

1. Make sure you have an Azure subscription. If you don't have an Azure subscription, you can create a 
[free account](https://azure.microsoft.com/en-us/free/) before you begin.

2. [Create a namespace in the Azure portal](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-portal#create-a-namespace-in-the-azure-portal)

3. [Get the connection string](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-portal#get-the-connection-string)

4. [Create a queue in the Azure portal](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-portal#create-a-queue-in-the-azure-portal)


# Sample
First, import the `ballerinax/asb` module into the Ballerina project.

```ballerina
import ballerinax/asb;
```

You can now make the connection configuration using the connection string and entity path.
```ballerina
asb:ConnectionConfiguration connectionConfiguration = {
        connectionString: "connection_string",
        entityPath: "entity_path"
};
```

You can now make a sender connection using the connection configuration.
```ballerina
asb:SenderConnection? senderConnection = new (connectionConfiguration);
```

You can now make a receiver connection using the connection configuration.
```ballerina
asb:ReceiverConnection? receiverConnection = new (connectionConfiguration);
```

You can now send a message to the configured asure service bus entity.  
```ballerina
if (senderConnection is SenderConnection) {
    checkpanic senderConnection.sendMessageWithConfigurableParameters(byteContent, parameters, properties);
} else {
    test:assertFail("Asb sender connection creation failed.");
}
```

You can now receive message from the configured asure service bus entity. 
```ballerina
if (receiverConnection is ReceiverConnection) {
    asb:Message|asb:Error messageReceived = receiverConnection.receiveMessage(serverWaitTime);
    if (messageReceived is Message) {
        string messageRead = checkpanic messageReceived.getTextContent();
        log:printInfo("Reading Received Message : " + messageRead);
    } else {
        test:assertFail("Receiving message via Asb receiver connection failed.");
    }
} else {
    test:assertFail("Asb receiver connection creation failed.");
}
```

You can now close the sender connection.
```ballerina
if (receiverConnection is ReceiverConnection) {
    checkpanic receiverConnection.closeReceiverConnection();
}
```

You can now close the receiver connection.
```ballerina
if (senderConnection is SenderConnection) {
    checkpanic senderConnection.closeSenderConnection();
}
```
