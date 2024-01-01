Ballerina Azure Service Bus Connector
===================

[![Build](https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/actions/workflows/ci.yml/badge.svg)](https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/actions/workflows/ci.yml)
[![Trivy](https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/actions/workflows/trivy-scan.yml/badge.svg)](https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/actions/workflows/trivy-scan.yml)
[![codecov](https://codecov.io/gh/ballerina-platform/module-ballerinax-azure-service-bus/branch/main/graph/badge.svg)](https://codecov.io/gh/ballerina-platform/module-ballerinax-azure-service-bus)
[![GraalVM Check](https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/actions/workflows/build-with-bal-test-native.yml/badge.svg)](https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/actions/workflows/build-with-bal-test-native.yml)
[![GitHub Last Commit](https://img.shields.io/github/last-commit/ballerina-platform/module-ballerinax-azure-service-bus.svg)](https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/commits/main)
[![GitHub Issues](https://img.shields.io/github/issues/ballerina-platform/ballerina-library/module/azure-servicebus.svg?label=Open%20Issues)](https://github.com/ballerina-platform/ballerina-library/labels/module/azure-servicebus)

## Overview

The [Ballerina](https://ballerina.io/) connector for Azure Service Bus allows you to connect to
an [Azure Service Bus](https://docs.microsoft.com/en-us/azure/service-bus-messaging/) via the Ballerina language.

The Azure Service Bus is a fully managed enterprise message broker with message queues and publish-subscribe topics. It
provides the capability to send and receive messages from Service Bus queues, topics, and subscriptions. The Azure
Service Bus handles messages that include data representing any kind of information, including structured data encoded
with common formats such as the following ones: JSON, XML, and Plain Text.

This connector supports the following operations:
- Manage (Get/Create/Update/Delete/list) a queue, topic, subscription or rule.
- Send messages to a queue, topic, or subscription.
- Receive messages from a queue, topic, or subscription.
- Listen to messages from a queue, topic, or subscription.

Service Bus provides a Microsoft-supported [native Java API](https://docs.microsoft.com/en-us/java/api/overview/azure/servicebus?view=azure-java-stable) (
SDK) and this module makes use of
this [public API](https://learn.microsoft.com/en-us/java/api/com.microsoft.azure.servicebus?view=azure-java-archive)
. As the public API
applies [SAS authentication](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-sas), this module
supports SAS authentication as well.

This module
supports [Service Bus SDK 7.13.1 version](https://learn.microsoft.com/en-us/java/api/overview/azure/service-bus?view=azure-java-stable#libraries-for-data-access)
. The source code on GitHub is
located [here](https://github.com/Azure/azure-service-bus-java). The
primary wire protocol for Service Bus is Advanced Messaging Queueing Protocol (AMQP) 1.0, an open ISO/IEC standard.

## Prerequisites

Before using this connector in your Ballerina application, complete the following:

### Create a namespace in the Azure portal
To begin using Service Bus messaging entities in Azure, you must first create a namespace with a name that is unique across Azure. A namespace provides a scoping container for Service Bus resources within your application.

To create a namespace:

#### Step 1: Sign in to the [Azure portal](https://portal.azure.com/).
If you don't have an Azure subscription, [sign up for a free Azure account](https://azure.microsoft.com/free/).

#### Step 2: Go to the Create Resource Service Bus menu

In the left navigation pane of the portal, select **All services**, select **Integration** from the list of categories, hover the mouse over **Service Bus**, and then select **Create** on the Service Bus tile.

![Create Resource Service Bus Menu](https://raw.githubusercontent.com/ballerina-platform/module-ballerinax-azure-service-bus/main/ballerina/resources/create-resource-service-bus-menu.png)

#### Step 3: In the Basics tag of the Create namespace page, follow these steps:

1. For **Subscription**, choose an Azure subscription in which to create the namespace.

2. For **Resource group**, choose an existing resource group in which the namespace will live, or create a new one.

3. Enter a **name for the namespace**. The namespace name should adhere to the following naming conventions:

* The name must be unique across Azure. The system immediately checks to see if the name is available.
* The name length is at least 6 and at most 50 characters.
* The name can contain only letters, numbers, and hyphens “-“.
* The name must start with a letter and end with a letter or number.
* The name doesn't end with “-sb“ or “-mgmt“.

4. For **Location**, choose the region in which your namespace should be hosted.

5. For **Pricing tier**, select the pricing tier (Basic, Standard, or Premium) for the namespace. For this quickstart, select Standard.

> **Notice:** If you want to use topics and subscriptions, choose either Standard or Premium. Topics/subscriptions aren't supported in the Basic pricing tier. If you selected the Premium pricing tier, specify the number of messaging units. The premium tier provides resource isolation at the CPU and memory level so that each workload runs in isolation. This resource container is called a messaging unit. A premium namespace has at least one messaging unit. You can select 1, 2, 4, 8, or 16 messaging units for each Service Bus Premium namespace. For more information, see [Service Bus Premium Messaging](https://learn.microsoft.com/en-us/azure/service-bus-messaging/service-bus-premium-messaging).`

6. Select **Review + create** at the bottom of the page.

![Create Namespace](https://raw.githubusercontent.com/ballerina-platform/module-ballerinax-azure-service-bus/main/ballerina/resources/create-namespace.png)

7. On the **Review + create** page, review settings, and select **Create**.


### Obtain tokens for authentication

To send and receive messages from a Service Bus queue or topic, clients must use a token that is signed by a shared access key, which is part of a shared access policy. A shared access policy defines a set of permissions that can be assigned to one or more Service Bus entities (queues, topics, event hubs, or relays). A shared access policy can be assigned to more than one entity, and a single entity can have more than one shared access policy assigned to it.

To obtain a token following steps should be followed:

1. In the left navigation pane of the portal, select *All services*, select *Integration* from the list of categories, hover the mouse over *Service Bus*, and then select your namespace.

2. In the left navigation pane of the namespace page, select *Shared access policies*.

3. Click on the *RootManageSharedAccessKey* policy.

4. Copy the *Primary Connection String* value and save it in a secure location. This is the connection string that you use to authenticate with the Service Bus service.

![Connection String](https://raw.githubusercontent.com/ballerina-platform/module-ballerinax-azure-service-bus/main/ballerina/resources/connection-string.png)

## ASB Admin Client
Azure Service Bus Admin Client is used to manage the Service Bus entities. This client can be used to create, update, delete, and list queues, topics, subscriptions, and rules.

The code snippet given below initializes an admin client with the basic configuration and creates a queue in the Azure Service Bus.

```ballerina
    import ballerinax/asb;

    configurable string connectionString = ?;

    public function main() returns error? {
        asb:Administrator adminClient = check new (connectionString);

        asb:QueueProperties? queue = check adminClient->createQueue("test-queue");

        if queue is asb:QueueProperties {
            log:printInfo(queue.toString());
        } else {
            log:printError(queue.toString());
        }
    }
```

## ASB Message Sender Client
Azure Service Bus Message Sender Client is used to send messages to a queue, topic, or subscription.

The code snippet given below initializes a message sender client with the basic configuration and sends a message to the Azure Service Bus.

```ballerina
    import ballerinax/asb;

    configurable string connectionString = ?;

    // Sender Configurations
    ASBServiceSenderConfig senderConfig = {
        connectionString: connectionString,
        entityType: QUEUE,
        topicOrQueueName: "myQueue"
    };

    public function main() returns error? {
        asb:MessageSender queueSender = check new (senderConfig);

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

## ASB Message Receiver Client
Azure Service Bus Message Receiver Client is used to receive messages from a queue, topic, or subscription.

The code snippet given below initializes a message receiver client with the basic configuration and receives a message from the Azure Service Bus.

```ballerina
    import ballerina/log;
    import ballerinax/asb;

    configurable string connectionString = ?;

    // Receiver Configurations
    ASBServiceReceiverConfig receiverConfig = {
        connectionString: connectionString,
        entityConfig: {
            queueName: "myQueue"
        },
        receiveMode: PEEK_LOCK
    };

    public function main() returns error? {
        asb:MessageReceiver queueReceiver = check new (receiverConfig);

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

## ASB Message Listener Client
Azure Service Bus Message Listener Client is used to listen to messages from a queue, topic, or subscription and process them asynchronously.

The code snippet given below initializes a message listener client with the basic configuration and listens to messages from the Azure Service Bus.

```ballerina
    import ballerina/log;
    import ballerinax/asb;

    configurable string connectionString = ?;

    // Listener Configurations
    asb:ListenerConfig configuration = {
        connectionString: connectionString
    };

    listener asb:Listener asbListener = new (configuration);

    @asb:ServiceConfig {
        queueName: "test-queue",
        peekLockModeEnabled: true,
        maxConcurrency: 1,
        prefetchCount: 10,
        maxAutoLockRenewDuration: 300
    }
    service asb:MessageService on asbListener {
        isolated remote function onMessage(asb:Message message, asb:Caller caller) returns asb:Error? {
            log:printInfo("Message received from queue: " + message.toBalString());
            _ = check caller.complete(message);
        }

        isolated remote function onError(asb:ErrorContext context, error 'error) returns asb:Error? {
            log:printInfo("Error received from queue: " + context.toBalString());
        }
    };
```

**[You can find a list of samples here](https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/tree/main/examples)**

## Issues and projects 

The **Issues** and **Projects** tabs are disabled for this repository as this is part of the Ballerina library. To report bugs, request new features, start new discussions, view project boards, etc., visit the Ballerina library [parent repository](https://github.com/ballerina-platform/ballerina-library). 

This repository only contains the source code for the package.

## Build from the source

### Prerequisites

1. Download and install Java SE Development Kit (JDK) version 17. You can download it from either of the following sources:

   * [Oracle JDK](https://www.oracle.com/java/technologies/downloads/)
   * [OpenJDK](https://adoptium.net/)

    > **Note:** After installation, remember to set the `JAVA_HOME` environment variable to the directory where JDK was installed.

2. Download and install [Ballerina Swan Lake](https://ballerina.io/).

3. Download and install [Docker](https://www.docker.com/get-started).

    > **Note**: Ensure that the Docker daemon is running before executing any tests.

### Build options

Execute the commands below to build from the source.

1. To build the package:
   ```
   ./gradlew clean build
   ```

2. To run the tests:
   ```
   ./gradlew clean test
   ```

3. To build the without the tests:
   ```
   ./gradlew clean build -x test
   ```

5. To debug the package with a remote debugger:
   ```
   ./gradlew clean build -Pdebug=<port>
   ```

6. To debug with the Ballerina language:
   ```
   ./gradlew clean build -PbalJavaDebug=<port>
   ```

7. Publish the generated artifacts to the local Ballerina Central repository:
    ```
    ./gradlew clean build -PpublishToLocalCentral=true
    ```

8. Publish the generated artifacts to the Ballerina Central repository:
   ```
   ./gradlew clean build -PpublishToCentral=true
   ```

## Contribute to Ballerina

As an open-source project, Ballerina welcomes contributions from the community.

For more information, go to the [contribution guidelines](https://github.com/ballerina-platform/ballerina-lang/blob/master/CONTRIBUTING.md).

## Code of conduct

All the contributors are encouraged to read the [Ballerina Code of Conduct](https://ballerina.io/code-of-conduct).

## Useful links

* For more information go to the [ASB package](https://central.ballerina.io/ballerinax/asb/latest).
* For example demonstrations of the usage, go to [Ballerina By Examples](https://ballerina.io/learn/by-example/).
* Chat live with us via our [Discord server](https://discord.gg/ballerinalang).
* Post all technical questions on Stack Overflow with the [#ballerina](https://stackoverflow.com/questions/tagged/ballerina) tag.