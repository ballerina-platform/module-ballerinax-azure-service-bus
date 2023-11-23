Ballerina Azure Service Bus Connector
===================

[![Build](https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/workflows/CI/badge.svg)](https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/actions?query=workflow%3ACI)
[![codecov](https://codecov.io/gh/ballerina-platform/module-ballerinax-azure-service-bus/branch/main/graph/badge.svg)](https://codecov.io/gh/ballerina-platform/module-ballerinax-azure-service-bus)
[![Trivy](https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/actions/workflows/trivy-scan.yml/badge.svg)](https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/actions/workflows/trivy-scan.yml)
[![GitHub Last Commit](https://img.shields.io/github/last-commit/ballerina-platform/module-ballerinax-azure-service-bus.svg)](https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/commits/master)
[![GraalVM Check](https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/actions/workflows/build-with-bal-test-native.yml/badge.svg)](https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/actions/workflows/build-with-bal-test-native.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## Overview

The [Ballerina](https://ballerina.io/) connector for Azure Service Bus allows you to connect to
an [Azure Service Bus](https://docs.microsoft.com/en-us/azure/service-bus-messaging/) via the Ballerina language.

The Azure Service Bus is a fully managed enterprise message broker with message queues and publish-subscribe topics. It
provides the capability to send and receive messages from Service Bus queues, topics, and subscriptions. The Azure
Service Bus handles messages that include data representing any kind of information, including structured data encoded
with common formats such as the following ones: JSON, XML, and Plain Text.

This connector supports the following operations:
- Send messages to a queue, topic, or subscription.
- Receive messages from a queue, topic, or subscription.
- Manage (Get/Create/Update/Delete/list) a queue, topic,subscription or rule.
- Listen to messages from a queue, topic, or subscription.

Service Bus provides a Microsoft
supported [native Java API](https://docs.microsoft.com/en-us/java/api/overview/azure/servicebus?view=azure-java-stable) (
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
![Create Resource Service Bus Menu](https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/tree/main/ballerina/resources/create-resource-service-bus-menu.png?raw=true)

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

`Important: If you want to use topics and subscriptions, choose either Standard or Premium. Topics/subscriptions aren't supported in the Basic pricing tier.`

If you selected the Premium pricing tier, specify the number of messaging units. The premium tier provides resource isolation at the CPU and memory level so that each workload runs in isolation. This resource container is called a messaging unit. A premium namespace has at least one messaging unit. You can select 1, 2, 4, 8, or 16 messaging units for each Service Bus Premium namespace. For more information, see Service Bus Premium Messaging.

6. Select **Review + create** at the bottom of the page.

![Create Namespace](https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/tree/main/ballerina/resources/create-namespace.png?raw=true)

7. On the **Review + create** page, review settings, and select **Create**.


### Obtain tokens for authentication

To send and receive messages from a Service Bus queue or topic, clients must use a token that is signed by a shared access key, which is part of a shared access policy. A shared access policy defines a set of permissions that can be assigned to one or more Service Bus entities (queues, topics, event hubs, or relays). A shared access policy can be assigned to more than one entity, and a single entity can have more than one shared access policy assigned to it.

To obtain a token following steps should be followed:

1. In the left navigation pane of the portal, select *All services*, select *Integration* from the list of categories, hover the mouse over *Service Bus*, and then select your namespace.

2. In the left navigation pane of the namespace page, select *Shared access policies*.

3. Click on the *RootManageSharedAccessKey* policy.

4. Copy the *Primary Connection String* value and save it in a secure location. This is the connection string that you use to authenticate with the Service Bus service.
![Connection String](https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/tree/main/ballerina/resources/connection-string.png?raw=true)

## Sample

To use the Azure Service Bus connector in your Ballerina application, update the .bal file as follows:
### Enabling Azure SDK Logs
To enable Azure logs in a Ballerina module, you need to set the environment variable ASB_CLOUD_LOGS to ACTIVE. You can do this by adding the following line to your shell script or using the export command in your terminal(to deactivate, remove the variable value):

`export ASB_CLOUD_LOGS=ACTIVE`

### Enabling Internal Connector Logs
To enable internal connector logs in a Ballerina module, you need to set the log level in the Config.toml file using the  custom configuration record Where <log_level> is the desired log level (e.g. DEBUG, INFO, WARN, ERROR, FATAL, (Default)OFF)

```
[ballerinax.asb.customConfiguration]
logLevel="OFF"
```


### Step 1: Import connector

Import the `ballerinax/asb` module into the Ballerina project.

```ballerina
import ballerinax/asb as asb;
```

### Step 2: Create a new connector instance

#### Initialize an Admin Client

This can be done by providing a connection string.

````ballerina
    configurable string connectionString = ?;
    asb:AdminClient admin = check new (connectionString);
````

#### Initialize a Message Sender client

This can be done by providing a connection string with a queue or topic name.

```ballerina
    configurable string connectionString = ?;

    ASBServiceSenderConfig senderConfig = {
        connectionString: connectionString,
        entityType: QUEUE,
        topicOrQueueName: "myQueue"
    };
    asb:MessageSender sender = check new (senderConfig);
```

#### Initialize a Message Receiver client

This can be done by providing a connection string with a queue name, topic name, or subscription path. Here, the Receive mode is
optional. (Default: PEEKLOCK)

```ballerina
    configurable string connectionString = ?;

    ASBServiceReceiverConfig receiverConfig = {
        connectionString: connectionString,
        entityConfig: {
            queueName: "myQueue"
        },
        receiveMode: PEEK_LOCK
    };
    asb:MessageReceiver receiver = check new (receiverConfig);
```

#### Initialize a Message Listener client

This can be done by providing a connection string.

```ballerina
    configurable string connectionString = ?;
    asb:Listener listener = check new (connectionString);
```

### Step 3: Invoke connector operation

1. Now you can use the operations available within the connector. Note that they are in the form of remote operations.

   Following is an example of how to create a queue in the Azure Service Bus using the connector.

    **Create a queue in the Azure Service Bus**
    
     ```ballerina
    public function main() returns error? {
        asb:AdminClient admin = check new (adminConfig);

        check admin->createQueue("myQueue");
    
        check admin->close();
    }
     ```

   Following is an example on how to send messages to the Azure Service Bus using the connector.

   **Send a message to the Azure Service Bus**

    ```ballerina
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

   Following is an example of how to receive messages from the Azure Service Bus using the client connector. Optionally
   you can provide the receive mode which is PEEKLOCK by default. You can find more information about the receive
   modes [here](https://docs.microsoft.com/en-us/java/api/com.microsoft.azure.servicebus.receivemode?view=azure-java-stable).

   **Receive a message from the Azure Service Bus**

    ```ballerina
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
    Following is an example on how to receive messages from the Azure Service Bus using the listner in connector.
    
    **Listen to Messages from the Azure Service Bus**
    ```ballerina
    @ServiceConfig {
        queueName: "myqueue",
        peekLockModeEnabled: true,
        maxConcurrency: 1,
        prefetchCount: 20,
        maxAutoLockRenewDuration: 300
    }
    service MessageService on asbListener {
        isolated remote function onMessage(Message message, Caller caller) returns error? {
            log:printInfo("Message received:" + message.body.toString());
            _ = check caller.complete(message);
        }
        isolated remote function onError(ErrorContext context, error 'error) returns error? {
            // Write your error handling logic here
        }
    };
    ```
    **!!! NOTE: You can complete, abandon, deadLetter, defer, renewLock using the asb:Caller instance. If you want to handle errors that come when processing messages, use MessageServiceErrorHandling service type.**

2. Use `bal run` command to compile and run the Ballerina program.

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

* For more information go to the [`asb` package](https://central.ballerina.io/ballerinax/asb/latest).
* For example demonstrations of the usage, go to [Ballerina By Examples](https://ballerina.io/learn/by-example/).
* Chat live with us via our [Discord server](https://discord.gg/ballerinalang).
* Post all technical questions on Stack Overflow with the [#ballerina](https://stackoverflow.com/questions/tagged/ballerina) tag.