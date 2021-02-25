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
| Ballerina Language  | Swan-Lake-Alpha2            |
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

* Ballerina SLAlpha2 Installed
  Ballerina Swan Lake Alpha 2 is required.

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
You can now make the connection configuration using the connection string and entity path.
```ballerina
    asb:ConnectionConfiguration config = {
       connectionString: <CONNECTION_STRING>,
       entityPath: <QUEUE_PATH>
    };
```

### Step 3: Create a Sender Connection using the connection configuration
You can now make a sender connection using the connection configuration.
```ballerina
    asb:SenderConnection? senderConnection = checkpanic new (config);
```

### Step 4 : Create a Receiver Connection using the connection configuration
You can now make a receiver connection using the connection configuration.
```ballerina
    asb:ReceiverConnection? receiverConnection = checkpanic new (config);
```

### Step 5: Initialize the Input Values
Initialize the message to be sent with optional parameters and properties.
```ballerina
    string stringContent = "This is My Message Body";
    byte[] byteContent = stringContent.toBytes();
    json jsonContent = {name: "apple", color: "red", price: 5.36};
    byte[] byteContentFromJson = jsonContent.toJsonString().toBytes();
    map<string> parameters1 = {contentType: "text/plain", messageId: "one"};
    map<string> parameters2 = {contentType: "application/json", messageId: "two", to: "user1", replyTo: "user2",
       label: "a1", sessionId: "b1", correlationId: "c1", timeToLive: "2"};
    map<string> properties = {a: "propertyValue1", b: "propertyValue2"};
```

### Step 6: Send a Message to Azure Service Bus
You can now send a message to the configured azure service bus entity with message content, optional parameters and 
properties. Here we have shown how to send a text message and a json message parsed to the byte array format.
```ballerina
    if (senderConnection is asb:SenderConnection) {
       log:print("Sending via Asb sender connection.");
       Checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContent, parameters1, properties);
       checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContentFromJson, parameters2, properties);
    } else {
       log:printError("Asb sender connection creation failed.");
    }
```

### Step 7: Receive a Message from Azure Service Bus
You can now receive a message from the configured azure service bus entity.
```ballerina
    if (receiverConnection is asb:ReceiverConnection) {
       log:print("Receiving from Asb receiver connection.");
       asb:Message|asb:Error? messageReceived = receiverConnection->receiveMessage(serverWaitTime);
       asb:Message|asb:Error? jsonMessageReceived = receiverConnection->receiveMessage(serverWaitTime);
       if (messageReceived is asb:Message && jsonMessageReceived is asb:Message) {
           string messageRead = checkpanic messageReceived.getTextContent();
           log:print("Reading Received Message : " + messageRead);
           json jsonMessageRead = checkpanic jsonMessageReceived.getJSONContent();
           log:print("Reading Received Message : " + jsonMessageRead.toString());
       }
    }
```

### Step 8: Close Sender Connection
You can now close the sender connection.
```ballerina
    if (senderConnection is asb:SenderConnection) {
       log:print("Closing Asb sender connection.");
       checkpanic senderConnection.closeSenderConnection();
    }
```

### Step 9: Close Receiver Connection
You can now close the receiver connection.
```ballerina
    if (receiverConnection is asb:ReceiverConnection) {
       log:print("Closing Asb receiver connection.");
       checkpanic receiverConnection.closeReceiverConnection();
    }
```

## Send and Listen to Messages from the Azure Service Bus Queue

This is the simplest scenario to listen to messages from an Azure Service Bus queue. You need to obtain a connection 
string of the name space and an entity path name of the queue you want to listen messages from. 

### Step 1: Import the Azure Service Bus Ballerina Library
First, import the ballerinax/asb module into the Ballerina project.
```ballerina
    import ballerinax/asb as asb;
```

### Step 2: Initialize the Azure Service Bus Client Configuration
You can now make the connection configuration using the connection string and entity path.
```ballerina
    asb:ConnectionConfiguration config = {
       connectionString: <CONNECTION_STRING>,
       entityPath: <QUEUE_PATH>
    };
```

### Step 3: Create a Sender Connection using the connection configuration
You can now make a sender connection using the connection configuration.
```ballerina
    asb:SenderConnection? senderConnection = checkpanic new (config);
```

### Step 4: Initialize the Input Values
Initialize the message to be sent with optional parameters and properties.
```ballerina
    string stringContent = "This is My Message Body";
    byte[] byteContent = stringContent.toBytes();
    json jsonContent = {name: "apple", color: "red", price: 5.36};
    byte[] byteContentFromJson = jsonContent.toJsonString().toBytes();
    map<string> parameters1 = {contentType: "text/plain", messageId: "one"};
    map<string> parameters2 = {contentType: "application/json", messageId: "two", to: "user1", replyTo: "user2",
       label: "a1", sessionId: "b1", correlationId: "c1", timeToLive: "2"};
    map<string> properties = {a: "propertyValue1", b: "propertyValue2"};
```

### Step 5: Send a Message to Azure Service Bus
You can now send a message to the configured azure service bus entity with message content, optional parameters and
properties. Here we have shown how to send a text message and a json message parsed to the byte array format.
```ballerina
    if (senderConnection is asb:SenderConnection) {
       log:print("Sending via Asb sender connection.");
       Checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContent, parameters1, properties);
       checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContentFromJson, parameters2, properties);
    } else {
       log:printError("Asb sender connection creation failed.");
    }
```

### Step 6: Create a service object with the service configuration and the service logic to execute based on the message received
You can now create a service object with the service configuration specified using the @asb:ServiceConfig annotation. 
We need to give the connection string and the entity path of the queue we are to listen messages from. Then we can give 
the service logic to execute when a message is received inside the onMessage remote function.
```ballerina
    asb:Service asyncTestService =
    @asb:ServiceConfig {
        queueConfig: {
            connectionString: <CONNECTION_STRING>,
            queueName: <QUEUE_PATH>
        }
    }
    service object {
        remote function onMessage(asb:Message message) {
            var messageContent = message.getTextContent();
            if (messageContent is string) {
                log:print("The message received: " + messageContent);
            } else {
                log:printError("Error occurred while retrieving the message content.");
            }
        }
    };
```

### Step 7: Initialize the ASB Listener  and Asynchronously listen to messages from the Azure Service Bus Queue
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
        log:print("start listening");
        runtime:sleep(20);
        log:print("end listening");
        checkpanic channelListener.detach(asyncTestService);
        checkpanic channelListener.gracefulStop();
        checkpanic channelListener.immediateStop();
    }
```

### Step 8: Close Sender Connection
You can now close the sender connection.
```ballerina
    if (senderConnection is asb:SenderConnection) {
       log:print("Closing Asb sender connection.");
       checkpanic senderConnection.closeSenderConnection();
    }
```

# Samples: 

1. Send and Receive Message from Queue
This is the basic scenario of sending and receiving a message from a queue. A user must create a sender connection and 
a receiver connection with the azure service bus to send and receive a message. The message body is passed as 
a parameter in byte array format with optional parameters and properties to the send operation. The user can receive 
the Message object at the other receiver end.

Sample is available at:
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/blob/main/asb-ballerina/samples/send_and_receive_message_from_queue.bal

```ballerina
import ballerina/config;
import ballerina/log;
import ballerinax/asb;

public function main() {

    // Input values
    string stringContent = "This is My Message Body"; 
    byte[] byteContent = stringContent.toBytes();
    json jsonContent = {name: "apple", color: "red", price: 5.36};
    byte[] byteContentFromJson = jsonContent.toJsonString().toBytes();
    map<string> parameters1 = {contentType: "text/plain", messageId: "one"};
    map<string> parameters2 = {contentType: "application/json", messageId: "two", to: "user1", replyTo: "user2", 
        label: "a1", sessionId: "b1", correlationId: "c1", timeToLive: "2"};
    map<string> properties = {a: "propertyValue1", b: "propertyValue2"};
    int serverWaitTime = 5;

    asb:ConnectionConfiguration config = {
        connectionString: <CONNECTION_STRING>,
        entityPath: <QUEUE_PATH>
    };

    log:print("Creating Asb sender connection.");
    asb:SenderConnection? senderConnection = checkpanic new (config);

    log:print("Creating Asb receiver connection.");
    asb:ReceiverConnection? receiverConnection = checkpanic new (config);

    if (senderConnection is asb:SenderConnection) {
        log:print("Sending via Asb sender connection.");
        checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContent, parameters1, properties);
        checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContentFromJson, parameters2, properties);
    } else {
        log:printError("Asb sender connection creation failed.");
    }

    if (receiverConnection is asb:ReceiverConnection) {
        log:print("Receiving from Asb receiver connection.");
        asb:Message|asb:Error? messageReceived = receiverConnection->receiveMessage(serverWaitTime);
        asb:Message|asb:Error? jsonMessageReceived = receiverConnection->receiveMessage(serverWaitTime);
        if (messageReceived is asb:Message && jsonMessageReceived is asb:Message) {
            string messageRead = checkpanic messageReceived.getTextContent();
            log:print("Reading Received Message : " + messageRead);
            json jsonMessageRead = checkpanic jsonMessageReceived.getJSONContent();
            log:print("Reading Received Message : " + jsonMessageRead.toString());
        } 
    }

    if (senderConnection is asb:SenderConnection) {
        log:print("Closing Asb sender connection.");
        checkpanic senderConnection.closeSenderConnection();
    }

    if (receiverConnection is asb:ReceiverConnection) {
        log:print("Closing Asb receiver connection.");
        checkpanic receiverConnection.closeReceiverConnection();
    }
}    
```

2. Send and Receive Messages from Queue
This is the basic scenario of sending and receiving one or more messages from a queue. A user must create a 
sender connection and a receiver connection with the azure service bus to send and receive a message. The message body 
is passed as a parameter in byte array format with optional parameters and properties to the send operation. The user 
can receive the Message object at the other receiver end.

Sample is available at:
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/blob/main/asb-ballerina/samples/send_and_receive_messages_from_queue.bal

```ballerina
import ballerina/config;
import ballerina/log;
import ballerinax/asb;

public function main() {

    // Input values
    string stringContent = "This is My Message Body"; 
    byte[] byteContent = stringContent.toBytes();
    json jsonContent = {name: "apple", color: "red", price: 5.36};
    byte[] byteContentFromJson = jsonContent.toJsonString().toBytes();
    map<string> parameters1 = {contentType: "text/plain", messageId: "one"};
    map<string> parameters2 = {contentType: "application/json", messageId: "two", to: "user1", replyTo: "user2", 
        label: "a1", sessionId: "b1", correlationId: "c1", timeToLive: "2"};
    map<string> properties = {a: "propertyValue1", b: "propertyValue2"};
    int maxMessageCount = 3;
    int serverWaitTime = 5;

    asb:ConnectionConfiguration config = {
        connectionString: <CONNECTION_STRING>,
        entityPath: <QUEUE_PATH>
    };

    log:print("Creating Asb sender connection.");
    asb:SenderConnection? senderConnection = checkpanic new (config);

    log:print("Creating Asb receiver connection.");
    asb:ReceiverConnection? receiverConnection = checkpanic new (config);

    if (senderConnection is asb:SenderConnection) {
        log:print("Sending via Asb sender connection.");
        checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContent, parameters1, properties);
        checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContentFromJson, parameters2, properties);
    } else {
        log:printError("Asb sender connection creation failed.");
    }

    if (receiverConnection is asb:ReceiverConnection) {
        log:print("Receiving from Asb receiver connection.");
        var messageReceived = receiverConnection->receiveMessages(serverWaitTime, maxMessageCount);
        if(messageReceived is asb:Messages) {
            int val = messageReceived.getMessageCount();
            log:print("No. of messages received : " + val.toString());
            asb:Message[] messages = messageReceived.getMessages();
            string messageReceived1 =  checkpanic messages[0].getTextContent();
            log:print("Message1 content : " +messageReceived1);
            json messageReceived2 =  checkpanic messages[1].getJSONContent();
            log:print("Message2 content : " +messageReceived2.toString());
        } else {
            log:printError(messageReceived.message());
        }
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (senderConnection is asb:SenderConnection) {
        log:print("Closing Asb sender connection.");
        checkpanic senderConnection.closeSenderConnection();
    }

    if (receiverConnection is asb:ReceiverConnection) {
        log:print("Closing Asb receiver connection.");
        checkpanic receiverConnection.closeReceiverConnection();
    }
}  
```

3. Send and Receive Batch from Queue
This is the basic scenario of sending and receiving a batch of messages from a queue. A user must create a sender 
connection and a receiver connection with the azure service bus to send and receive a message. The message body is 
passed as a parameter in byte array format with optional parameters and properties to the send operation. The user can 
receive the array of Message objects at the other receiver end.

Sample is available at:
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/blob/main/asb-ballerina/samples/send_and_receive_batch_from_queue.bal

```ballerina
import ballerina/config;
import ballerina/log;
import ballerinax/asb;

public function main() {

    // Input values
    string[] stringArrayContent = ["apple", "mango", "lemon", "orange"];
    map<string> parameters = {contentType: "text/plain", timeToLive: "2"};
    map<string> properties = {a: "propertyValue1", b: "propertyValue2"};
    int maxMessageCount = 3;
    int serverWaitTime = 5;

    asb:ConnectionConfiguration config = {
        connectionString: <CONNECTION_STRING>,
        entityPath: <QUEUE_PATH>
    };

    log:print("Creating Asb sender connection.");
    asb:SenderConnection? senderConnection = checkpanic new (config);

    log:print("Creating Asb receiver connection.");
    asb:ReceiverConnection? receiverConnection = checkpanic new (config);

    if (senderConnection is asb:SenderConnection) {
        log:print("Sending via Asb sender connection.");
        checkpanic senderConnection->sendBatchMessage(stringArrayContent, parameters, properties, maxMessageCount);
    } else {
        log:printError("Asb sender connection creation failed.");
    }

    if (receiverConnection is asb:ReceiverConnection) {
        log:print("Receiving from Asb receiver connection.");
        var messageReceived = receiverConnection->receiveBatchMessage(maxMessageCount);
        if(messageReceived is asb:Messages) {
            int val = messageReceived.getMessageCount();
            log:print("No. of messages received : " + val.toString());
            asb:Message[] messages = messageReceived.getMessages();
            string messageReceived1 =  checkpanic messages[0].getTextContent();
            log:print("Message1 content : " + messageReceived1);
            string messageReceived2 =  checkpanic messages[1].getTextContent();
            log:print("Message2 content : " + messageReceived2);
            string messageReceived3 =  checkpanic messages[2].getTextContent();
            log:print("Message3 content : " + messageReceived3);
        } else {
            log:printError("Receiving message via Asb receiver connection failed.");
        }
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (senderConnection is asb:SenderConnection) {
        log:print("Closing Asb sender connection.");
        checkpanic senderConnection.closeSenderConnection();
    }

    if (receiverConnection is asb:ReceiverConnection) {
        log:print("Closing Asb receiver connection.");
        checkpanic receiverConnection.closeReceiverConnection();
    }
}    
```

4. Send to Topic and Receive from Subscription
This is the basic scenario of sending a message to a topic and receiving a message from a subscription. A user must 
create a sender connection and a receiver connection with the azure service bus to send and receive a message. 
The message body is passed as a parameter in byte array format with optional parameters and properties to the send 
operation. The user can receive the Message object at the other receiver end.

Sample is available at:
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/blob/main/asb-ballerina/samples/send_to_topic_and_receive_from_subscription.bal

```ballerina
import ballerina/config;
import ballerina/log;
import ballerinax/asb;

public function main() {

    // Input values
    string stringContent = "This is My Message Body"; 
    byte[] byteContent = stringContent.toBytes();
    json jsonContent = {name: "apple", color: "red", price: 5.36};
    byte[] byteContentFromJson = jsonContent.toJsonString().toBytes();
    map<string> parameters1 = {contentType: "text/plain", messageId: "one"};
    map<string> parameters2 = {contentType: "application/json", messageId: "two", to: "user1", replyTo: "user2", 
        label: "a1", sessionId: "b1", correlationId: "c1", timeToLive: "2"};
    map<string> properties = {a: "propertyValue1", b: "propertyValue2"};
    int serverWaitTime = 5;

    asb:ConnectionConfiguration senderConfig = {
        connectionString: <CONNECTION_STRING>,
        entityPath: <TOPIC_PATH>
    };

    asb:ConnectionConfiguration receiverConfig1 = {
        connectionString: <CONNECTION_STRING>,
        entityPath: <SUBSCRIPTION_PATH1>
    };

    asb:ConnectionConfiguration receiverConfig2 = {
        connectionString: <CONNECTION_STRING>,
        entityPath: <SUBSCRIPTION_PATH2>
    };

    asb:ConnectionConfiguration receiverConfig3 = {
        connectionString: <CONNECTION_STRING>,
        entityPath: <SUBSCRIPTION_PATH3>
    };

    log:print("Creating Asb sender connection.");
    asb:SenderConnection? senderConnection = checkpanic new (senderConfig);

    log:print("Creating Asb receiver connection.");
    asb:ReceiverConnection? receiverConnection1 = checkpanic new (receiverConfig1);
    asb:ReceiverConnection? receiverConnection2 = checkpanic new (receiverConfig2);
    asb:ReceiverConnection? receiverConnection3 = checkpanic new (receiverConfig3);

    if (senderConnection is asb:SenderConnection) {
        log:print("Sending via Asb sender connection.");
        checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContent, parameters1, properties);
        checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContentFromJson, parameters2, properties);
    } else {
        log:printError("Asb sender connection creation failed.");
    }

    if (receiverConnection1 is asb:ReceiverConnection) {
        log:print("Receiving from Asb receiver connection 1.");
        asb:Message|asb:Error? messageReceived = receiverConnection1->receiveMessage(serverWaitTime);
        asb:Message|asb:Error? jsonMessageReceived = receiverConnection1->receiveMessage(serverWaitTime);
        if (messageReceived is asb:Message && jsonMessageReceived is asb:Message) {
            string messageRead = checkpanic messageReceived.getTextContent();
            log:print("Reading Received Message : " + messageRead);
            json jsonMessageRead = checkpanic jsonMessageReceived.getJSONContent();
            log:print("Reading Received Message : " + jsonMessageRead.toString());
        } else {
            log:printError("Receiving message via Asb receiver connection failed.");
        }
    } else {
        log:printError("Asb receiver connection creation failed.");
    }


    if (receiverConnection2 is asb:ReceiverConnection) {
        log:print("Receiving from Asb receiver connection 2.");
        asb:Message|asb:Error? messageReceived = receiverConnection2->receiveMessage(serverWaitTime);
        asb:Message|asb:Error? jsonMessageReceived = receiverConnection2->receiveMessage(serverWaitTime);
        if (messageReceived is asb:Message && jsonMessageReceived is asb:Message) {
            string messageRead = checkpanic messageReceived.getTextContent();
            log:print("Reading Received Message : " + messageRead);
            json jsonMessageRead = checkpanic jsonMessageReceived.getJSONContent();
            log:print("Reading Received Message : " + jsonMessageRead.toString());
        } else {
            log:printError("Receiving message via Asb receiver connection failed.");
        }
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (receiverConnection3 is asb:ReceiverConnection) {
        log:print("Receiving from Asb receiver connection 3.");
        asb:Message|asb:Error? messageReceived = receiverConnection3->receiveMessage(serverWaitTime);
        asb:Message|asb:Error? jsonMessageReceived = receiverConnection3->receiveMessage(serverWaitTime);
        if (messageReceived is asb:Message && jsonMessageReceived is asb:Message) {
            string messageRead = checkpanic messageReceived.getTextContent();
            log:print("Reading Received Message : " + messageRead);
            json jsonMessageRead = checkpanic jsonMessageReceived.getJSONContent();
            log:print("Reading Received Message : " + jsonMessageRead.toString());
        } else {
            log:printError("Receiving message via Asb receiver connection failed.");
        }
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (senderConnection is asb:SenderConnection) {
        log:print("Closing Asb sender connection.");
        checkpanic senderConnection.closeSenderConnection();
    }

    if (receiverConnection1 is asb:ReceiverConnection) {
        log:print("Closing Asb receiver connection 1.");
        checkpanic receiverConnection1.closeReceiverConnection();
    }

    if (receiverConnection2 is asb:ReceiverConnection) {
        log:print("Closing Asb receiver connection 2.");
        checkpanic receiverConnection2.closeReceiverConnection();
    }

    if (receiverConnection3 is asb:ReceiverConnection) {
        log:print("Closing Asb receiver connection 3.");
        checkpanic receiverConnection3.closeReceiverConnection();
    }
}    
```

More Samples are available at: 
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/tree/main/asb-ballerina/samples
