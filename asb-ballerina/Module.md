## Overview

Azure Service Bus Ballerina Connector is used to connect with the [Azure Service Bus](https://docs.microsoft.com/en-us/azure/service-bus-messaging/) via Ballerina language easily which is a fully managed enterprise message broker with message queues and publish-subscribe 
topics.

This module can be used to send and receive messages from Service Bus queues, topics and subscriptions. Service Bus [data access libraries](https://docs.microsoft.com/en-us/java/api/overview/azure/servicebus?view=azure-java-stable#libraries-for-data-access) access the Service Bus service directly, and perform various data access operations at the entity level, rather than at the namespace level (such as sending a message to a queue).
This module also supports asynchronous message listening capabilities from the azure service bus. Service Bus provides a Microsoft supported [native Java API](https://docs.microsoft.com/en-us/java/api/overview/azure/servicebus?view=azure-java-stable) (SDK) and this module make use of this [public API](https://docs.microsoft.com/en-us/java/api/overview/azure/servicebus/client?view=azure-java-stable&preserve-view=true). This public API uses SAS authentication and this module supports SAS authentication.

This module supports [Service Bus SDK v3.5.1](https://docs.microsoft.com/en-us/java/api/overview/azure/servicebus/client?view=azure-java-stable&preserve-view=true). The source code on GitHub is located [here](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/servicebus/microsoft-azure-servicebus). The primary wire protocol for Service Bus is Advanced Messaging Queueing Protocol (AMQP) 1.0, an open ISO/IEC standard.

## Prerequisites

* Create Azure account and subscription.
  If you don't have an Azure subscription, [sign up for a free Azure account](https://azure.microsoft.com/free/).

* Create Service Bus namespace.
  If you don't have [a service bus namespace](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-create-namespace-portal),
  learn how to create your Service Bus namespace.

* Create messaging entity, such as a queue, topic or subscription.
  If you don't have these items, learn how to
    * [Create a queue in the Azure portal](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-portal#create-a-queue-in-the-azure-portal)
    * [Create a Topic using the Azure portal](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-topics-subscriptions-portal#create-a-topic-using-the-azure-portal)
    * [Create Subscriptions to the Topic](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-topics-subscriptions-portal#create-subscriptions-to-the-topic)

* Obtain tokens

    Shared Access Signature (SAS) Authentication Credentials are required to communicate with the Azure Service Bus.
    * Connection String
    * Entity Path

    Obtain the authorization credentials:
    * For Service Bus Queues

        1. Make sure you have an Azure subscription. If you don't have an Azure subscription, you can create a
        [free account](https://azure.microsoft.com/en-us/free/) before you begin.

        2. [Create a namespace in the Azure portal](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-portal#create-a-namespace-in-the-azure-portal)

        3. [Get the connection string](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-portal#get-the-connection-string)

        4. [Create a queue in the Azure portal & get Entity Path](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-portal#create-a-queue-in-the-azure-portal). 
        It is in the format ‘queueName’.

    * For Service Bus Topics and Subscriptions

        1. Make sure you have an Azure subscription. If you don't have an Azure subscription, you can create a
        [free account](https://azure.microsoft.com/en-us/free/) before you begin.

        2. [Create a namespace in the Azure portal](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-portal#create-a-namespace-in-the-azure-portal)

        3. [Get the connection string](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-portal#get-the-connection-string)

        4. [Create a topic in the Azure portal & get Entity Path](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-topics-subscriptions-portal#create-a-topic-using-the-azure-portal). 
        It's in the format ‘topicName‘.

        5. [Create a subscription in the Azure portal & get Entity Path](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-topics-subscriptions-portal#create-subscriptions-to-the-topic). 
        It’s in the format ‘topicName/subscriptions/subscriptionName’.

* Configure the connector with obtained tokens

> **NOTE:**
When configuring the listener, the entity path for a Queue is the entity name (Eg: "myQueueName") and the entity path 
for a subscription is in the following format `<topicName>/subscriptions/<subscriptionName>` 
(Eg: "myTopicName/subscriptions/mySubscriptionName").

## Quickstart

To use the Azure Service Bus connector in your Ballerina application, update the .bal file as follows:

### Send and Receive Messages from the Azure Service Bus Queue

This is the simplest scenario to send and receive messages from an Azure Service Bus queue. You need to obtain 
a connection string of the name space and an entity path name of the queue you want to send and receive messages from. 

#### Step 1: Import the Azure Service Bus Ballerina library
Import the ballerinax/asb module into the Ballerina project.
```ballerina
    import ballerinax/asb as asb;
```

#### Step 2: Initialize the Azure Service Bus Client configuration
You can now make the connection configuration using the connection string.
```ballerina
    asb:AsbConnectionConfiguration config = {
       connectionString: <CONNECTION_STRING>
    };
```

#### Step 3: Create an Azure Service Bus Client using the connection configuration
You can now make an Azure service bus client using the connection configuration.
```ballerina
    asb:AsbClient asbClient = new (config);
```

#### Step 4: Create a Queue Sender using the Azure Service Bus client
You can now make a sender connection using the Azure service bus client. Provide the `queueName` as a parameter. 
```ballerina
    handle queueSender = check asbClient->createQueueSender(queueName);
```

#### Step 5 : Create a Queue Receiver using the Azure Service Bus client
You can now make a receiver connection using the connection configuration. Provide the `queueName` as a parameter. 
Optionally you can provide the receive mode which is PEEKLOCK by default. You can find more information about the receive modes [here](https://docs.microsoft.com/en-us/java/api/com.microsoft.azure.servicebus.receivemode?view=azure-java-stable).
```ballerina
    handle queueReceiver = check asbClient->createQueueReceiver(queueName, asb:RECEIVEANDDELETE);
```

#### Step 6: Initialize the Input Values
Initialize the message to be sent with message body, optional parameters and properties.
```ballerina
    // Input values
    string stringContent = "This is My Message Body"; 
    byte[] byteContent = stringContent.toBytes();
    map<string> properties = {a: "propertyValue1", b: "propertyValue2"};
    int timeToLive = 60; // In seconds
    int serverWaitTime = 60; // In seconds

    asb:ApplicationProperties applicationProperties = {
        properties: {a: "propertyValue1", b: "propertyValue2"}
    };

    asb:Message message1 = {
        body: byteContent,
        contentType: asb:TEXT,
        timeToLive: timeToLive,
        applicationProperties: applicationProperties
    };
```

#### Step 7: Send a Message to Azure Service Bus
You can now send a message to the configured azure service bus entity with message body, optional parameters and 
properties. Here we have shown how to send a text message parsed to the byte array format.
```ballerina
    check asbClient->send(queueSender, message1);
```

#### Step 8: Receive a Message from Azure Service Bus
You can now receive a message from the configured azure service bus entity.
```ballerina
    asb:Message|asb:Error? messageReceived = asbClient->receive(queueReceiver, serverWaitTime);

    if (messageReceived is asb:Message) {
        log:printInfo("Reading Received Message : " + messageReceived.toString());
    } else if (messageReceived is ()) {
        log:printError("No message in the queue.");
    } else {
        log:printError("Receiving message via Asb receiver connection failed.");
    }
```

#### Step 9: Close Sender Connection
You can now close the sender connection.
```ballerina
    check asbClient->closeSender(queueSender);
```

#### Step 10: Close Receiver Connection
You can now close the receiver connection.
```ballerina
    check asbClient->closeReceiver(queueReceiver);
```

### Listen to Messages from the Azure Service Bus Queue

This is the simplest scenario to listen to messages from an Azure Service Bus queue. You need to obtain a connection 
string of the name space and an entity path name of the queue you want to listen messages from. 

#### Step 1: Import the Azure Service Bus Ballerina Library
Import the ballerinax/asb module into the Ballerina project.
```ballerina
    import ballerinax/asb as asb;
```

#### Step 2: Initialize the ASB Listener to Asynchronously listen to messages from the Azure Service Bus Queue
You can now initialize the ASB Listener.
```ballerina
    listener asb:Listener asbListener = new ();
```

#### Step 3: Create a service object with the service configuration and the service logic to execute based on the message received
You can now create a service object with the service configuration specified using the `@asb:ServiceConfig` annotation and attach it to the listener. We need to give the connection string and the entity path of the queue we are to listen messages from. We can optionally provide the receive mode. Default mode is the PEEKLOCK mode. You can find more information about the receive modes [here](https://docs.microsoft.com/en-us/java/api/com.microsoft.azure.servicebus.receivemode?view=azure-java-stable). Then we can give the service logic to execute when a message is received inside the onMessage remote function.
```ballerina
    @asb:ServiceConfig {
        entityConfig: {
            connectionString: <CONNECTION_STRING>,
            entityPath: <ENTITY_PATH>,
            receiveMode: <PEEKLOCK_OR_RECEIVEONDELETE>
        }
    }
    service asb:Service on asbListener {
        remote function onMessage(asb:Message message) returns error? {
            // Write your logic here
            log:printInfo("Azure service bus message as byte[] which is the standard according to the AMQP protocol" + 
            message.toString());
            string|xml|json|byte[] received = message.body;

            match message?.contentType {
                asb:JSON => {
                    string stringMessage = check string:fromBytes(<byte[]> received);
                    json jsonMessage = check value:fromJsonString(stringMessage);
                    log:printInfo("The message received: " + jsonMessage.toJsonString());
                }
                asb:XML => {
                    string stringMessage = check 'string:fromBytes(<byte[]> received);
                    xml xmlMessage = check 'xml:fromString(stringMessage);
                    log:printInfo("The message received: " + xmlMessage.toString());
                }
                asb:TEXT => {
                    string stringMessage = check 'string:fromBytes(<byte[]> received);
                    log:printInfo("The message received: " + stringMessage);
                }
            }
        }
    };
```
> **NOTE:**
Currently we are using the asb:Message record for both sender & receiver operations. When we use the ASB receiver connector instead of the ASB listener to receive messages we return the exact message converted (re-engineered) to the specific data type based on the content type of the message. But in the ASB listener we receive the message body as byte[] which is the standard according to the AMQP protocol. We haven't re-engineered the listener. Rather we provide the message body as a standard byte[]. So the user must do the conversion based on the content type of the message. We have provided a sample code segment above, where you can do the conversion easily.

## Quick reference
Code snippets of some frequently used functions: 

* Create Queue Sender

```ballerina
    handle queueSender = check asbClient->createQueueSender(queueName); 
```

* Create Queue Receiver

```ballerina
    handle queueReceiver = check asbClient->createQueueReceiver(queueName, asb:RECEIVEANDDELETE);
```

* Create Topic Sender

```ballerina
    handle topicSender = check asbClient->createTopicSender(topicName); 
```

* Create Subscription Receiver

```ballerina
    handle subscriptionReceiver = 
        check asbClient->createSubscriptionReceiver(topicName, subscriptionName1, asb:RECEIVEANDDELETE);
```

* Send Message to Queue
```ballerina
    check asbClient->send(queueSender, message);
```

* Receive Message from Queue
```ballerina
    asb:Message|asb:Error? message = asbClient->receive(queueReceiver, serverWaitTime);  
```

* Send Batch to Queue

```ballerina
    check asbClient->sendBatch(queueSender, messages);   
```

* Receive Batch from Queue
```ballerina
    asb:MessageBatch|asb:Error? messageBatch = asbClient->receiveBatch(queueReceiver, maxMessageCount, serverWaitTime);    
```

* Send Message to Topic
```ballerina
    check asbClient->send(topicSender, message);
```

* Receive Message from Subscription
```ballerina
    asb:Message|asb:Error? message = asbClient->receive(subscriptionReceiver, serverWaitTime);  
```

* Close Sender Connection
```ballerina
    check asbClient->closeSender(queueSender);
    check asbClient->closeSender(topicSender);
```

* Close Receiver Connection
```ballerina
    check asbClient->closeReceiver(queueReceiver);
    check asbClient->closeReceiver(subscriptionReceiver);
```

**[You can find a list of samples here](https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/tree/main/asb-ballerina/samples)**
