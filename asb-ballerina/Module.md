## Overview

The [Ballerina](https://ballerina.io/) connector for Azure Service Bus allows you to connect to
an [Azure Service Bus](https://docs.microsoft.com/en-us/azure/service-bus-messaging/) via the Ballerina language.

The Azure Service Bus is a fully managed enterprise message broker with message queues and publish-subscribe topics.It
provides the capability to send and receive messages from Service Bus queues, topics, and subscriptions. The Azure
Service Bus handles messages that include data representing any kind of information, including structured data encoded
with the common formats such as the following ones: JSON, XML, Plain Text.

This module also supports asynchronous message listening capabilities from the azure service bus. Service Bus provides a
Microsoft
supported [native Java API](https://docs.microsoft.com/en-us/java/api/overview/azure/servicebus?view=azure-java-stable) (
SDK) and this module make use of
this [public API](https://docs.microsoft.com/en-us/java/api/overview/azure/servicebus/client?view=azure-java-stable&preserve-view=true)
. As the public API
applies [SAS authentication](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-sas), this module
supports SAS authentication as well.

This module
supports [Service Bus SDK 3.5.1 version](https://docs.microsoft.com/en-us/java/api/overview/azure/servicebus/client?view=azure-java-stable&preserve-view=true)
. The source code on GitHub is
located [here](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/servicebus/microsoft-azure-servicebus). The
primary wire protocol for Service Bus is Advanced Messaging Queueing Protocol (AMQP) 1.0, an open ISO/IEC standard.

## Prerequisites

Before using this connector in your Ballerina application, complete the following:

* Create an Azure account and a subscription. If you don't have an Azure
  subscription, [sign up for a free Azure account](https://azure.microsoft.com/free/).

* Create a Service Bus namespace. If you don't
  have [a service bus namespace](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-create-namespace-portal)
  , learn how to create your Service Bus namespace.

* Create a messaging entity, such as a queue, topic or subscription. If you don't have these items, learn how to
    * [Create a queue in the Azure portal](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-portal#create-a-queue-in-the-azure-portal)
    * [Create a topic using the Azure portal](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-topics-subscriptions-portal#create-a-topic-using-the-azure-portal)
    * [Create subscriptions to the topic](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-topics-subscriptions-portal#create-subscriptions-to-the-topic)

* Obtain tokens

  Shared Access Signature (SAS) Authentication Credentials are required to communicate with the Azure Service Bus.
    * Connection String
    * Entity Path

  Obtain the authorization credentials:
    * For Service Bus Queues

        1. [Create a namespace in the Azure portal](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-portal#create-a-namespace-in-the-azure-portal)

        2. [Get the connection string](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-portal#get-the-connection-string)

        3. [Create a queue in the Azure portal & get Entity Path](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-portal#create-a-queue-in-the-azure-portal)
           . It is in the format ‘queueName’.

    * For Service Bus Topics and Subscriptions

        1. [Create a namespace in the Azure portal](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-portal#create-a-namespace-in-the-azure-portal)

        2. [Get the connection string](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-portal#get-the-connection-string)

        3. [Create a topic in the Azure portal & get Entity Path](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-topics-subscriptions-portal#create-a-topic-using-the-azure-portal)
           . It's in the format ‘topicName‘.

        4. [Create a subscription in the Azure portal & get Entity Path](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-topics-subscriptions-portal#create-subscriptions-to-the-topic)
           . It’s in the format ‘topicName/subscriptions/subscriptionName’.

## Quickstart

To use the Azure Service Bus connector in your Ballerina application, update the .bal file as follows:

### Step 1: Import connector

Import the `ballerinax/asb` module into the Ballerina project.

```ballerina
import ballerinax/asb as asb;
```

### Step 2: Create a new connector instance

#### Initialize a Message Sender client

This can be done providing connection string with queue or topic name.

```ballerina
asb:MessageSender queueSender = check new (connectionString, queueName);
asb:MessageSender topicSender = check new (connectionString, TopicName);
```

#### Initialize a Message Receiver client

This can be done providing connection string with queue name, topic name or subscription path. Here, Receive mode is
optional. (Default : PEEKLOCK)

```ballerina
asb:MessageReceiver queueReceiver = check new (connectionString, queueName);
asb:MessageReceiver subscriptionReceiver = check new (connectionString, subscriptionPath);
```

### Step 3: Invoke connector operation

1. Now you can use the operations available within the connector. Note that they are in the form of remote operations.

   Following is an example on how to send messages to the Azure Service Bus using the connector.

   Send a message to the Azure Service Bus

    ```ballerina
    public function main() returns error? {
        asb:MessageSender queueSender = check new (connectionString, queueName);

        string stringContent = "This is My Message Body"; 
        byte[] byteContent = stringContent.toBytes();
        int timeToLive = 60; // In seconds

        asb:ApplicationProperties applicationProperties = {
            properties: {a: "propertyValue1", b: "propertyValue2"}
        };

        asb:Message message = {
            body: byteContent,
            contentType: asb:TEXT,
            timeToLive: timeToLive,
            applicationProperties: applicationProperties
        };

        check queueSender->send(message);

        check queueSender->close();
    }
    ```

   Following is an example on how to receive messages from the Azure Service Bus using the client connector.Optionally
   you can provide the receive mode which is PEEKLOCK by default. You can find more information about the receive
   modes [here](https://docs.microsoft.com/en-us/java/api/com.microsoft.azure.servicebus.receivemode?view=azure-java-stable)
   .

   Receive a message from the Azure Service Bus

    ```ballerina
        public function main() returns error? {
            asb:MessageReceiver queueReceiver = check new (connectionString, queueName, asb:RECEIVEANDDELETE);

            int serverWaitTime = 60; // In seconds

            asb:Message|asb:Error? messageReceived = queueReceiver->receive(serverWaitTime);

            if (messageReceived is asb:Message) {
                log:printInfo("Reading Received Message : " + messageReceived.toString());
            } else if (messageReceived is ()) {
                log:printError("No message in the queue.");
            } else {
                log:printError("Receiving message via Asb receiver connection failed.");
            }

            check queueReceiver->close();
        }
    ```

   Following is an example on how to asynchronously listen to messages from the Azure Service Bus using the listener.
   You need to create a new listener instance before listening. Then, you need to create a service object with the
   service configuration specified using the `@asb:ServiceConfig` annotation and attach it to the listener. You need to
   give the connection string and the entity path of the queue we are to listen messages from. We can optionally provide
   the receive mode. Default mode is the PEEKLOCK mode. You can find more information about the receive modes
   [here](https://docs.microsoft.com/en-us/java/api/com.microsoft.azure.servicebus.receivemode?view=azure-java-stable).
   Finally, you can provide the service logic to execute when a message is received inside the onMessage remote
   function.

   Listen to Messages from the Azure Service Bus

   **!!! NOTE:**
   When configuring the listener, the entity path for a Queue is the entity name (Eg: "myQueueName") and the entity path
   for a subscription is in the following format `<topicName>/subscriptions/<subscriptionName>`
   (Eg: "myTopicName/subscriptions/mySubscriptionName").

    ```ballerina
    listener asb:Listener asbListener = new (connectionString, queueName, asb:PEEKLOCK);

    service asb:Service on asbListener {
        remote function onMessage(asb:Message message, asb:Caller caller) returns error? {
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

   **!!! NOTE:**
   Currently we are using the asb:Message record for both sender & receiver operations. When we use the ASB receiver
   connector instead of the ASB listener to receive messages we return the exact message converted (re-engineered) to
   the specific data type based on the content type of the message. But in the ASB listener we receive the message body
   as byte[] which is the standard according to the AMQP protocol. We haven't re-engineered the listener. Rather we
   provide the message body as a standard byte[]. So the user must do the conversion based on the content type of the
   message. We have provided a sample code segment above, where you can do the conversion easily.


2. Use `bal run` command to compile and run the Ballerina program.

**[You can find a list of samples here](https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/tree/main/asb-ballerina/samples)**
