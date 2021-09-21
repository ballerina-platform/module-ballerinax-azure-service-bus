# Ballerina Azure Service Bus Module

Connects to Microsoft Azure Service Bus using Ballerina.

# Module Overview
Microsoft Azure Service Bus is a fully managed enterprise message broker with message queues and publish-subscribe 
topics. The primary wire protocol for Service Bus is Advanced Messaging Queueing Protocol (AMQP) 1.0, an open ISO/IEC 
standard. Service Bus provides a Microsoft supported native API and this module make use of this public API. 
Service Bus service APIs access the Service Bus service directly, and perform various management operations at the 
entity level, rather than at the namespace level (such as sending a message to a queue). This module supports these 
basic operations. These APIs use SAS authentication and this module supports SAS authentication.  

# Compatibility
|                     |    Version                  |
|:-------------------:|:---------------------------:|
| Ballerina Language  | Swan-Lake-Beta3             |
| Service Bus API     | v3.5.1                      |

# Supported Operations

## Operations on Connecting to the ASB
The `ballerinax/asb` module contains operations related to connecting with the azure service bus. It includes operations 
to create SenderConnection, close SenderConnection, create ReceiverConnection, close ReceiverConnection. 

## Operations on Queue
The `ballerinax/asb` module contains operations on azure service bus Queue. It includes operations to send message to 
queue, receive message from queue, receive messages from queue, send batch of messages to queue, receive batch of 
messages from queue, complete messages from queue, complete message from queue, and abandon message from queue.  

## Operations on Topic/Subscription
The `ballerinax/asb` module contains operations on azure service bus Topic/Subscription. It includes operations to send 
message to topic, receive message from subscription, receive messages from subscription, send batch of messages to 
topic, receive batch of messages from subscription, complete messages from subscription, complete message from 
subscription, and abandon message from subscription.  

## Listener Capabilities
The `ballerinax/asb` module contains operations related to asynchronous message listening capabilities from the azure 
service bus. It includes operations to attach service, start listening, detach service, graceful stop listening, and 
immediate stop listening.

# Prerequisites:

* An Azure account and subscription.
  If you don't have an Azure subscription, [sign up for a free Azure account](https://azure.microsoft.com/free/).

* A Service Bus namespace.
  If you don't have [a service bus namespace](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-create-namespace-portal),
  learn how to create your Service Bus namespace.

* A messaging entity, such as a queue, topic or subscription.
  If you don't have these items, learn how to
    * [Create a queue in the Azure portal](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-portal#create-a-queue-in-the-azure-portal)
    * [Create a Topic using the Azure portal](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-topics-subscriptions-portal#create-a-topic-using-the-azure-portal)
    * [Create Subscriptions to the Topic](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-topics-subscriptions-portal#create-subscriptions-to-the-topic)

* Java 11 Installed
  Java Development Kit (JDK) with version 11 is required.

* Ballerina SLBeta3  Installed
  Ballerina Swan Lake Beta 3  is required.

* Shared Access Signature (SAS) Authentication Credentials
    * Connection String
    * Entity Path

## Configuration
Instantiate the connector by giving authorization credentials that a client application can use to send/receive messages
to/from the queue/topic/subscription.

### Getting the authorization credentials

#### For Service Bus Queues

1. Make sure you have an Azure subscription. If you don't have an Azure subscription, you can create a
   [free account](https://azure.microsoft.com/en-us/free/) before you begin.

2. [Create a namespace in the Azure portal](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-portal#create-a-namespace-in-the-azure-portal)

3. [Get the connection string](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-portal#get-the-connection-string)

4. [Create a queue in the Azure portal & get Entity Path](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-portal#create-a-queue-in-the-azure-portal). 
   It is in the format ‘queueName’.

#### For Service Bus Topics and Subscriptions

1. Make sure you have an Azure subscription. If you don't have an Azure subscription, you can create a
   [free account](https://azure.microsoft.com/en-us/free/) before you begin.

2. [Create a namespace in the Azure portal](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-portal#create-a-namespace-in-the-azure-portal)

3. [Get the connection string](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-portal#get-the-connection-string)

4. [Create a topic in the Azure portal & get Entity Path](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-topics-subscriptions-portal#create-a-topic-using-the-azure-portal). 
   It's in the format ‘topicName‘.

5. [Create a subscription in the Azure portal & get Entity Path](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-topics-subscriptions-portal#create-subscriptions-to-the-topic). 
   It’s in the format ‘topicName/subscriptions/subscriptionName’.

# Quickstart(s):

## Send and Receive Messages from the Azure Service Bus Queue

This is the simplest scenario to send and receive messages from an Azure Service Bus queue. You need to obtain 
a connection string of the name space and an entity path name of the queue you want to send and receive messages from. 

### Step 1: Import the Azure Service Bus Ballerina Library
First, import the ballerinax/asb module into the Ballerina project.
```ballerina
    import ballerinax/asb as asb;
```

### Step 2: Initialize the Azure Service Bus Client Configuration
You can now make the connection configuration using the connection string.
```ballerina
    asb:AsbConnectionConfiguration config = {
       connectionString: <CONNECTION_STRING>
    };
```

### Step 3: Create an Azure Service Bus Client using the connection configuration
You can now make an Azure service bus client using the connection configuration.
```ballerina
    asb:AsbClient asbClient = new (config);
```

### Step 4: Create a Queue Sender using the Azure service bus client
You can now make a sender connection using the Azure service bus client. Provide the `queueName` as a parameter.
```ballerina
    handle queueSender = checkpanic asbClient->createQueueSender(queueName);
```

### Step 5 : Create a Queue Receiver using the Azure service bus client
You can now make a receiver connection using the connection configuration. Provide the `queueName` as a parameter. 
Optionally you can provide the receive mode which is PEEKLOCK by default.
```ballerina
    handle queueReceiver = checkpanic asbClient->createQueueReceiver(queueName, asb:RECEIVEANDDELETE);
```

### Step 6: Initialize the Input Values
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

### Step 7: Send a Message to Azure Service Bus
You can now send a message to the configured azure service bus entity with message body, optional parameters and 
properties. Here we have shown how to send a text message parsed to the byte array format.
```ballerina
    checkpanic asbClient->send(queueSender, message1);
```

### Step 8: Receive a Message from Azure Service Bus
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

### Step 9: Close Sender Connection
You can now close the sender connection.
```ballerina
    checkpanic asbClient->closeSender(queueSender);
```

### Step 10: Close Receiver Connection
You can now close the receiver connection.
```ballerina
    checkpanic asbClient->closeReceiver(queueReceiver);
```

Sample is available at:
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/blob/slalpha5/asb-ballerina/samples/send_and_receive_message_from_queue.bal

## Send and Listen to Messages from the Azure Service Bus Queue

This is the simplest scenario to listen to messages from an Azure Service Bus queue. You need to obtain a connection 
string of the name space and an entity path name of the queue you want to listen messages from. 

### Step 1: Import the Azure Service Bus Ballerina Library
First, import the ballerinax/asb module into the Ballerina project.
```ballerina
    import ballerinax/asb as asb;
```

### Step 2: Initialize the Azure Service Bus Client Configuration
You can now make the connection configuration using the connection string.
```ballerina
    asb:ConnectionConfiguration config = {
       connectionString: <CONNECTION_STRING>
    };
```

### Step 3: Create an Azure Service Bus Client using the connection configuration
You can now make an Azure service bus client using the connection configuration.
```ballerina
    asb:AsbClient asbClient = new (config);
```

### Step 4: Create a Queue Sender using the Azure service bus client
You can now make a sender connection using the Azure service bus client. Provide the `queueName` as a parameter.
```ballerina
    handle queueSender = checkpanic asbClient->createQueueSender(queueName);
```

### Step 5: Initialize the Input Values
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

### Step 6: Send a Message to Azure Service Bus
You can now send a message to the configured azure service bus entity with message body, optional parameters and 
properties. Here we have shown how to send a text message parsed to the byte array format.
```ballerina
    checkpanic asbClient->send(queueSender, message1);
```

### Step 7: Create a service object with the service configuration and the service logic to execute based on the message received
You can now create a service object with the service configuration specified using the @asb:ServiceConfig annotation. 
We need to give the connection string and the entity path of the queue we are to listen messages from. We can optionally provide the receive mode. Default mode is the PEEKLOCK mode. Then we can give the service logic to execute when a message is received inside the onMessage remote function.
```ballerina
    asb:Service asyncTestService =
    @asb:ServiceConfig {
        entityConfig: {
            connectionString: <CONNECTION_STRING>,
            entityPath: <ENTITY_PATH>,
            receiveMode: <PEEKLOCK_OR_RECEIVEONDELETE>
        }
    }
    service object {
        remote function onMessage(asb:Message message) {
            log:printInfo("The message received: " + message.toString());
            // Write your logic here
        }
    };
```

### Step 8: Initialize the ASB Listener  and Asynchronously listen to messages from the Azure Service Bus Queue
You can now initialize the ASB Listener and attach the service object with the listener. Then the user can start the 
listener and asynchronously listen to messages from the azure service bus connection and execute the service logic 
based on the message received. Here we have sent the current worker to sleep for 20 seconds. You can detach the service 
from the listener endpoint at any instance and stop listening to messages. You can gracefully stop listening by 
detaching from the listener and by terminating the connection. You can also immediately stop listening by terminating 
the connection with the Azure Service Bus.
```ballerina
    asb:Listener? channelListener = new();
    if (channelListener is asb:Listener) {
        checkpanic channelListener.attach(asyncTestService);
        checkpanic channelListener.'start();
        log:printInfo("start listening");
        runtime:sleep(20);
        log:printInfo("end listening");
        checkpanic channelListener.detach(asyncTestService);
        checkpanic channelListener.gracefulStop();
        checkpanic channelListener.immediateStop();
    }
```

### Step 8: Close Sender Connection
You can now close the sender connection.
```ballerina
    checkpanic asbClient->closeSender(queueSender);
```

Sample is available at:
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/blob/slalpha5/asb-ballerina/samples/async_consumer.bal


# Samples: 

1. Send and Receive Batch from Queue

This is the basic scenario of sending and receiving a batch of messages from a queue. A user must create a sender 
connection and a receiver connection with the azure service bus to send and receive a message. The message is 
passed as a parameter with optional parameters and properties to the send operation. The user can 
receive the array of Message objects at the other receiver end.

Sample is available at:
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/blob/slalpha5/asb-ballerina/samples/send_and_receive_batch_from_queue.bal

```ballerina
import ballerina/log;
import ballerinax/asb;

// Connection Configurations
configurable string connectionString = ?;
configurable string queueName = ?;

public function main() {

    // Input values
    string stringContent = "This is My Message Body"; 
    byte[] byteContent = stringContent.toBytes();
    map<string> properties = {a: "propertyValue1", b: "propertyValue2"};
    int timeToLive = 60; // In seconds
    int serverWaitTime = 60; // In seconds
    int maxMessageCount = 2;

    asb:ApplicationProperties applicationProperties = {
        properties: properties
    };

    asb:Message message1 = {
        body: byteContent,
        contentType: asb:TEXT,
        timeToLive: timeToLive
    };

    asb:Message message2 = {
        body: byteContent,
        contentType: asb:TEXT,
        timeToLive: timeToLive
    };

    asb:MessageBatch messages = {
        messageCount: 2,
        messages: [message1, message2]
    };

    asb:AsbConnectionConfiguration config = {
        connectionString: connectionString
    };

    asb:AsbClient asbClient = new (config);

    log:printInfo("Creating Asb sender connection.");
    handle queueSender = checkpanic asbClient->createQueueSender(queueName);

    log:printInfo("Creating Asb receiver connection.");
    handle queueReceiver = checkpanic asbClient->createQueueReceiver(queueName, asb:RECEIVEANDDELETE);

    log:printInfo("Sending via Asb sender connection.");
    checkpanic asbClient->sendBatch(queueSender, messages);

    log:printInfo("Receiving from Asb receiver connection.");
    asb:MessageBatch|asb:Error? messageReceived = 
        asbClient->receiveBatch(queueReceiver, maxMessageCount, serverWaitTime);

    if (messageReceived is asb:MessageBatch) {
        foreach asb:Message message in messageReceived.messages {
            if (message.toString() != "") {
                log:printInfo("Reading Received Message : " + message.toString());
            }
        }
    } else if (messageReceived is ()) {
        log:printError("No message in the queue.");
    } else {
        log:printError("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender connection.");
    checkpanic asbClient->closeSender(queueSender);

    log:printInfo("Closing Asb receiver connection.");
    checkpanic asbClient->closeReceiver(queueReceiver);
}    
```

2. Send to Topic and Receive from Subscription

This is the basic scenario of sending a message to a topic and receiving a message from a subscription. A user must 
create a sender connection and a receiver connection with the azure service bus to send and receive a message. 
The message is passed as a parameter with optional parameters and properties to the send 
operation. The user can receive the Message object at the other receiver end.

Sample is available at:
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/blob/slalpha5/asb-ballerina/samples/send_to_topic_and_receive_from_subscription.bal

```ballerina
import ballerina/log;
import ballerinax/asb;

// Connection Configurations
configurable string connectionString = ?;
configurable string topicName = ?;
configurable string subscriptionName1 = ?;

public function main() {

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

    asb:AsbConnectionConfiguration config = {
        connectionString: connectionString
    };

    asb:AsbClient asbClient = new (config);

    log:printInfo("Creating Asb sender connection.");
    handle topicSender = checkpanic asbClient->createTopicSender(topicName);

    log:printInfo("Creating Asb receiver connection.");
    handle subscriptionReceiver = 
        checkpanic asbClient->createSubscriptionReceiver(topicName, subscriptionName1, asb:RECEIVEANDDELETE);

    log:printInfo("Sending via Asb sender connection.");
    checkpanic asbClient->send(topicSender, message1);

    log:printInfo("Receiving from Asb receiver connection.");
    asb:Message|asb:Error? messageReceived = asbClient->receive(subscriptionReceiver, serverWaitTime);

    if (messageReceived is asb:Message) {
        log:printInfo("Reading Received Message : " + messageReceived.toString());
    } else if (messageReceived is ()) {
        log:printError("No message in the subscription.");
    } else {
        log:printError("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender connection.");
    checkpanic asbClient->closeSender(topicSender);

    log:printInfo("Closing Asb receiver connection.");
    checkpanic asbClient->closeReceiver(subscriptionReceiver);
}    
```

More Samples are available at: 
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/tree/slalpha5/asb-ballerina/samples
