# Ballerina Azure Service Bus Connector Samples: 

1. Send and Receive Message from Queue
This is the basic scenario of sending and receiving a message from a queue. A user must create a sender connection and 
a receiver connection with the azure service bus to send and receive a message. The message is passed as 
a parameter to the send operation. The user can receive the Message object at the other receiver end.

Sample is available at:
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/blob/main/asb-ballerina/samples/send_and_receive_message_from_queue.bal

```ballerina
import ballerina/log;
import ballerinax/asb;

// Connection Configurations
configurable string connectionString = ?;
configurable string queuePath = ?;

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
    checkpanic asbClient->createQueueSender(queuePath);

    log:printInfo("Creating Asb receiver connection.");
    checkpanic asbClient->createQueueReceiver(queuePath, asb:RECEIVEANDDELETE);

    log:printInfo("Sending via Asb sender connection.");
    checkpanic asbClient->send(message1);

    log:printInfo("Receiving from Asb receiver connection.");
    asb:Message|asb:Error? messageReceived = asbClient->receive(serverWaitTime);

    if (messageReceived is asb:Message) {
        log:printInfo("Reading Received Message : " + messageReceived.toString());
    } else if (messageReceived is ()) {
        log:printError("No message in the queue.");
    } else {
        log:printError("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender connection.");
    checkpanic asbClient->closeSender();

    log:printInfo("Closing Asb receiver connection.");
    checkpanic asbClient->closeReceiver();
}    
```

2. Send and Receive Batch from Queue
This is the basic scenario of sending and receiving a batch of messages from a queue. A user must create a sender 
connection and a receiver connection with the azure service bus to send and receive a message. The batch message is 
passed as a parameter to the send operation. The user can receive the batch message at the other receiver end.

Sample is available at:
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/blob/main/asb-ballerina/samples/send_and_receive_batch_from_queue.bal

```ballerina
import ballerina/log;
import ballerinax/asb;

// Connection Configurations
configurable string connectionString = ?;
configurable string queuePath = ?;

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
    checkpanic asbClient->createQueueSender(queuePath);

    log:printInfo("Creating Asb receiver connection.");
    checkpanic asbClient->createQueueReceiver(queuePath, asb:RECEIVEANDDELETE);

    log:printInfo("Sending via Asb sender connection.");
    checkpanic asbClient->sendBatch(messages);

    log:printInfo("Receiving from Asb receiver connection.");
    asb:MessageBatch|asb:Error? messageReceived = asbClient->receiveBatch(maxMessageCount, serverWaitTime);

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
    checkpanic asbClient->closeSender();

    log:printInfo("Closing Asb receiver connection.");
    checkpanic asbClient->closeReceiver();
}    
```

3. Send to Topic and Receive from Subscription
This is the basic scenario of sending a message to a topic and receiving a message from a subscription. A user must 
create a sender connection and a receiver connection with the azure service bus to send and receive a message. 
The message is passed as a parameter to the send operation. The user can receive the Message at the other receiver end.

Sample is available at:
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/blob/main/asb-ballerina/samples/send_to_topic_and_receive_from_subscription.bal

```ballerina
import ballerina/log;
import ballerinax/asb;

// Connection Configurations
configurable string connectionString = ?;
configurable string topicPath = ?;
configurable string subscriptionPath1 = ?;

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
    checkpanic asbClient->createTopicSender(topicPath);

    log:printInfo("Creating Asb receiver connection.");
    checkpanic asbClient->createSubscriptionReceiver(subscriptionPath1, asb:RECEIVEANDDELETE);

    log:printInfo("Sending via Asb sender connection.");
    checkpanic asbClient->send(message1);

    log:printInfo("Receiving from Asb receiver connection.");
    asb:Message|asb:Error? messageReceived = asbClient->receive(serverWaitTime);

    if (messageReceived is asb:Message) {
        log:printInfo("Reading Received Message : " + messageReceived.toString());
    } else if (messageReceived is ()) {
        log:printError("No message in the subscription.");
    } else {
        log:printError("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender connection.");
    checkpanic asbClient->closeSender();

    log:printInfo("Closing Asb receiver connection.");
    checkpanic asbClient->closeReceiver();
}    
```

4. Send Batch to Topic and Receive from Subscription
This is the basic scenario of sending a batch of messages to a topic and receiving a batch of messages from 
a subscription. A user must create a sender connection and a receiver connection with the azure service bus to send and 
receive a message. The batch message is passed as a parameter with optional parameters and properties to the send operation. 
The user can receive the batch message at the other receiver end.

Sample is available at:
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/blob/main/asb-ballerina/samples/send_batch_to_topic_and_receive_from_subscription.bal

```ballerina
import ballerina/log;
import ballerinax/asb;

// Connection Configurations
configurable string connectionString = ?;
configurable string topicPath = ?;
configurable string subscriptionPath1 = ?;

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
    checkpanic asbClient->createTopicSender(topicPath);

    log:printInfo("Creating Asb receiver connection.");
    checkpanic asbClient->createSubscriptionReceiver(subscriptionPath1, asb:RECEIVEANDDELETE);

    log:printInfo("Sending via Asb sender connection.");
    checkpanic asbClient->sendBatch(messages);

    log:printInfo("Receiving from Asb receiver connection.");
    asb:MessageBatch|asb:Error? messageReceived = asbClient->receiveBatch(maxMessageCount, serverWaitTime);

    if (messageReceived is asb:MessageBatch) {
        foreach asb:Message message in messageReceived.messages {
            if (message.toString() != "") {
                log:printInfo("Reading Received Message : " + message.toString());
            }
        }
    } else if (messageReceived is ()) {
        log:printError("No message in the subscription.");
    } else {
        log:printError("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender connection.");
    checkpanic asbClient->closeSender();

    log:printInfo("Closing Asb receiver connection.");
    checkpanic asbClient->closeReceiver();
}    
```

5. Complete Message from Queue
This is the basic scenario of sending and completing a message from a queue. A user must create a sender connection and 
a receiver connection with the azure service bus to send and receive a message. The message is passed as 
a parameter with optional parameters and properties to the send operation. The user can complete a message from the queue.

Sample is available at:
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/blob/main/asb-ballerina/samples/complete_message_from_queue.bal

```ballerina
import ballerina/log;
import ballerinax/asb;

// Connection Configurations
configurable string connectionString = ?;
configurable string queuePath = ?;

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
    checkpanic asbClient->createQueueSender(queuePath);

    log:printInfo("Creating Asb receiver connection.");
    checkpanic asbClient->createQueueReceiver(queuePath, asb:PEEKLOCK);

    log:printInfo("Sending via Asb sender connection.");
    checkpanic asbClient->send(message1);

    log:printInfo("Receiving from Asb receiver connection.");
    asb:Message|asb:Error? messageReceived = asbClient->receive(serverWaitTime);

    if (messageReceived is asb:Message) {
        checkpanic asbClient->complete(messageReceived);
        log:printInfo("Complete message successful");
    } else if (messageReceived is ()) {
        log:printError("No message in the queue.");
    } else {
        log:printError("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender connection.");
    checkpanic asbClient->closeSender();

    log:printInfo("Closing Asb receiver connection.");
    checkpanic asbClient->closeReceiver();
}    
```

6. Complete Message from Subscription
This is the basic scenario of sending and completing a message from a subscription. A user must create a sender 
connection and a receiver connection with the azure service bus to send and receive a message. The message is 
passed as a parameter with optional parameters and properties to the send operation. The user can complete a messages from the subscription.

Sample is available at:
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/blob/main/asb-ballerina/samples/complete_message_from_subscription.bal

```ballerina
import ballerina/log;
import ballerinax/asb;

// Connection Configurations
configurable string connectionString = ?;
configurable string topicPath = ?;
configurable string subscriptionPath1 = ?;

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
    checkpanic asbClient->createTopicSender(topicPath);

    log:printInfo("Creating Asb receiver connection.");
    checkpanic asbClient->createSubscriptionReceiver(subscriptionPath1, asb:PEEKLOCK);

    log:printInfo("Sending via Asb sender connection.");
    checkpanic asbClient->send(message1);

    log:printInfo("Receiving from Asb receiver connection.");
    asb:Message|asb:Error? messageReceived = asbClient->receive(serverWaitTime);

    if (messageReceived is asb:Message) {
        checkpanic asbClient->complete(messageReceived);
        log:printInfo("Complete message successful");
    } else if (messageReceived is ()) {
        log:printError("No message in the subscription.");
    } else {
        log:printError("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender connection.");
    checkpanic asbClient->closeSender();

    log:printInfo("Closing Asb receiver connection.");
    checkpanic asbClient->closeReceiver();
}    
```

7. Abandon Message from Queue
This is the basic scenario of sending and abandoning a message from a queue. A user must create a sender connection 
and a receiver connection with the azure service bus to send and receive a message. The message is passed as 
a parameter with optional parameters and properties to the send operation. The user can abandon 
a single Messages & make available again for processing from Queue.

Sample is available at:
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/blob/main/asb-ballerina/samples/abandon_message_from_queue.bal

```ballerina
import ballerina/log;
import ballerinax/asb;

// Connection Configurations
configurable string connectionString = ?;
configurable string queuePath = ?;

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
    checkpanic asbClient->createQueueSender(queuePath);

    log:printInfo("Creating Asb receiver connection.");
    checkpanic asbClient->createQueueReceiver(queuePath, asb:PEEKLOCK);

    log:printInfo("Sending via Asb sender connection.");
    checkpanic asbClient->send(message1);

    log:printInfo("Receiving from Asb receiver connection.");
    asb:Message|asb:Error? messageReceived = asbClient->receive(serverWaitTime);

    if (messageReceived is asb:Message) {
        checkpanic asbClient->abandon(messageReceived);
        asb:Message|asb:Error? messageReceivedAgain = asbClient->receive(serverWaitTime);
        if (messageReceivedAgain is asb:Message) {
            checkpanic asbClient->complete(messageReceivedAgain);
            log:printInfo("Abandon message successful");
        } else {
            log:printError("Abandon message not succesful.");
        }
    } else if (messageReceived is ()) {
        log:printError("No message in the queue.");
    } else {
        log:printError("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender connection.");
    checkpanic asbClient->closeSender();

    log:printInfo("Closing Asb receiver connection.");
    checkpanic asbClient->closeReceiver();
}    
```

8. Abandon Message from Subscription
This is the basic scenario of sending and abandoning a message from a subscription. A user must create a sender 
connection and a receiver connection with the azure service bus to send and receive a message. The message is 
passed as a parameter with optional parameters and properties to the send operation. The user can 
abandon a single Messages & make available again for processing from subscription.

Sample is available at:
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/blob/main/asb-ballerina/samples/abandon_message_from_subscription.bal

```ballerina
import ballerina/log;
import ballerinax/asb;

// Connection Configurations
configurable string connectionString = ?;
configurable string topicPath = ?;
configurable string subscriptionPath1 = ?;

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
    checkpanic asbClient->createTopicSender(topicPath);

    log:printInfo("Creating Asb receiver connection.");
    checkpanic asbClient->createSubscriptionReceiver(subscriptionPath1, asb:PEEKLOCK);

    log:printInfo("Sending via Asb sender connection.");
    checkpanic asbClient->send(message1);

    log:printInfo("Receiving from Asb receiver connection.");
    asb:Message|asb:Error? messageReceived = asbClient->receive(serverWaitTime);

    if (messageReceived is asb:Message) {
        checkpanic asbClient->abandon(messageReceived);
        asb:Message|asb:Error? messageReceivedAgain = asbClient->receive(serverWaitTime);
        if (messageReceivedAgain is asb:Message) {
            checkpanic asbClient->complete(messageReceivedAgain);
            log:printInfo("Abandon message successful");
        } else {
            log:printError("Abandon message not succesful.");
        }
    } else if (messageReceived is ()) {
        log:printError("No message in the subscription.");
    } else {
        log:printError("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender connection.");
    checkpanic asbClient->closeSender();

    log:printInfo("Closing Asb receiver connection.");
    checkpanic asbClient->closeReceiver();
}    
```

9. Asynchronous Consumer
This is the basic scenario of sending and listening to messages from a queue. A user must create a sender connection 
and a receiver connection with the azure service bus to send and receive a message. The message body is passed as 
a parameter in byte array format with optional parameters and properties to the send operation. The user can 
asynchronously listen to messages from the azure service bus connection and execute the service logic based on the 
message received.

Sample is available at:
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/blob/main/asb-ballerina/samples/async_consumer.bal

```ballerina
import ballerina/log;
import ballerina/lang.runtime;
import ballerinax/asb;

// Connection Configurations
configurable string connectionString = ?;
configurable string queuePath = ?;

public function main() {

    // Input values
    string stringContent = "This is My Message Body"; 
    byte[] byteContent = stringContent.toBytes();
    map<string> properties = {a: "propertyValue1", b: "propertyValue2"};
    int timeToLive = 60; // In seconds

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
    checkpanic asbClient->createQueueSender(queuePath);

    log:printInfo("Sending via Asb sender connection.");
    checkpanic asbClient->send(message1);

    asb:Service asyncTestService =
    @asb:ServiceConfig {
        entityConfig: {
            connectionString: connectionString,
            entityPath: queuePath
        }
    }
    service object {
        remote function onMessage(asb:Message message) {
            log:printInfo("The message received: " + message.toString());
            // Write your logic here
        }
    };

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

    log:printInfo("Closing Asb sender connection.");
    checkpanic asbClient->closeSender();
}    
```

10. Deadletter from Queue
This is the basic scenario of sending and dead lettering a message  from a queue. A user must create a sender 
connection and a receiver connection with the azure service bus to send and receive a message. The message is 
passed as a parameter with optional parameters and properties to the send operation. The user can 
abandon a single Message & moves the message to the Dead-Letter Queue.

Sample is available at:
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/blob/main/asb-ballerina/samples/deadletter_from_queue.bal

```ballerina
import ballerina/log;
import ballerinax/asb;

// Connection Configurations
configurable string connectionString = ?;
configurable string queuePath = ?;

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
    checkpanic asbClient->createQueueSender(queuePath);

    log:printInfo("Creating Asb receiver connection.");
    checkpanic asbClient->createQueueReceiver(queuePath, asb:PEEKLOCK);

    log:printInfo("Sending via Asb sender connection.");
    checkpanic asbClient->send(message1);

    log:printInfo("Receiving from Asb receiver connection.");
    asb:Message|asb:Error? messageReceived = asbClient->receive(serverWaitTime);

    if (messageReceived is asb:Message) {
        checkpanic asbClient->deadLetter(messageReceived);
        asb:Message|asb:Error? messageReceivedAgain = asbClient->receive(serverWaitTime);
        if (messageReceivedAgain is ()) {
            log:printInfo("Deadletter message successful");
        } else {
            log:printError("Deadletter message not succesful.");
        }
    } else if (messageReceived is ()) {
        log:printError("No message in the queue.");
    } else {
        log:printError("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender connection.");
    checkpanic asbClient->closeSender();

    log:printInfo("Closing Asb receiver connection.");
    checkpanic asbClient->closeReceiver();
}    
```

11. Deadletter from Subscription
This is the basic scenario of sending and dead lettering a message  from a subscription. A user must create a sender 
connection and a receiver connection with the azure service bus to send and receive a message. The message body is 
passed as a parameter with optional parameters and properties to the send operation. The user can 
abandon a single Message & move the message to the Dead-Letter subscription.

Sample is available at:
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/blob/main/asb-ballerina/samples/deadletter_from_subscription.bal

```ballerina
import ballerina/log;
import ballerinax/asb;

// Connection Configurations
configurable string connectionString = ?;
configurable string topicPath = ?;
configurable string subscriptionPath1 = ?;

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
    checkpanic asbClient->createTopicSender(topicPath);

    log:printInfo("Creating Asb receiver connection.");
    checkpanic asbClient->createSubscriptionReceiver(subscriptionPath1, asb:PEEKLOCK);

    log:printInfo("Sending via Asb sender connection.");
    checkpanic asbClient->send(message1);

    log:printInfo("Receiving from Asb receiver connection.");
    asb:Message|asb:Error? messageReceived = asbClient->receive(serverWaitTime);

    if (messageReceived is asb:Message) {
        checkpanic asbClient->deadLetter(messageReceived);
        asb:Message|asb:Error? messageReceivedAgain = asbClient->receive(serverWaitTime);
        if (messageReceivedAgain is ()) {
            log:printInfo("Deadletter message successful");
        } else {
            log:printError("Deadletter message not succesful.");
        }
    } else if (messageReceived is ()) {
        log:printError("No message in the subscription.");
    } else {
        log:printError("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender connection.");
    checkpanic asbClient->closeSender();

    log:printInfo("Closing Asb receiver connection.");
    checkpanic asbClient->closeReceiver();
}    
```

12. Defer from Queue
This is the basic scenario of sending and deferring a message from a queue. A user must create a sender connection and 
a receiver connection with the azure service bus to send and receive a message. The message body is passed as 
a parameter \with optional parameters and properties to the send operation. The user can defer 
a single Message. Deferred messages can only be received by using sequence number and return Message record.

Sample is available at:
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/blob/main/asb-ballerina/samples/defer_from_queue.bal

```ballerina
import ballerina/log;
import ballerinax/asb;

// Connection Configurations
configurable string connectionString = ?;
configurable string queuePath = ?;

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
    checkpanic asbClient->createQueueSender(queuePath);

    log:printInfo("Creating Asb receiver connection.");
    checkpanic asbClient->createQueueReceiver(queuePath, asb:PEEKLOCK);

    log:printInfo("Sending via Asb sender connection.");
    checkpanic asbClient->send(message1);

    log:printInfo("Receiving from Asb receiver connection.");
    asb:Message|asb:Error? messageReceived = asbClient->receive(serverWaitTime);

    if (messageReceived is asb:Message) {
        int sequenceNumber = checkpanic asbClient->defer(messageReceived);
        log:printInfo("Defer message successful");
        asb:Message|asb:Error? messageReceivedAgain = checkpanic asbClient->receiveDeferred(sequenceNumber);
        if (messageReceivedAgain is asb:Message) {
            log:printInfo("Reading Deferred Message : " + messageReceivedAgain.toString());
        }
    } else if (messageReceived is ()) {
        log:printError("No message in the queue.");
    } else {
        log:printError("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender connection.");
    checkpanic asbClient->closeSender();

    log:printInfo("Closing Asb receiver connection.");
    checkpanic asbClient->closeReceiver();
}    
```

13. Defer from Subscription
This is the basic scenario of sending and deferring a message from a subscription. A user must create a sender 
connection and a receiver connection with the azure service bus to send and receive a message. The message body is 
passed as a parameter with optional parameters and properties to the send operation. The user can 
defer a single Message. Deferred messages can only be received by using sequence number and return Message record.

Sample is available at:
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/blob/main/asb-ballerina/samples/defer_from_subscription.bal

```ballerina
import ballerina/log;
import ballerinax/asb;

// Connection Configurations
configurable string connectionString = ?;
configurable string topicPath = ?;
configurable string subscriptionPath1 = ?;

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
    checkpanic asbClient->createTopicSender(topicPath);

    log:printInfo("Creating Asb receiver connection.");
    checkpanic asbClient->createSubscriptionReceiver(subscriptionPath1, asb:PEEKLOCK);

    log:printInfo("Sending via Asb sender connection.");
    checkpanic asbClient->send(message1);

    log:printInfo("Receiving from Asb receiver connection.");
    asb:Message|asb:Error? messageReceived = asbClient->receive(serverWaitTime);

    if (messageReceived is asb:Message) {
        int sequenceNumber = checkpanic asbClient->defer(messageReceived);
        log:printInfo("Defer message successful");
        asb:Message|asb:Error? messageReceivedAgain = checkpanic asbClient->receiveDeferred(sequenceNumber);
        if (messageReceivedAgain is asb:Message) {
            log:printInfo("Reading Deferred Message : " + messageReceivedAgain.toString());
        }
    } else if (messageReceived is ()) {
        log:printError("No message in the subscription.");
    } else {
        log:printError("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender connection.");
    checkpanic asbClient->closeSender();

    log:printInfo("Closing Asb receiver connection.");
    checkpanic asbClient->closeReceiver();
}    
```

14. Renew Lock on Message from Queue
This is the basic scenario of renewing a lock on a message from a queue. A user must create a sender connection and 
a receiver connection with the azure service bus to send and receive a message. The message body is passed as 
a parameter with optional parameters and properties to the send operation.  The user can renew 
a lock on a single Message from the queue.

Sample is available at:
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/blob/main/asb-ballerina/samples/renew_lock_on_message_from_queue.bal

```ballerina
import ballerina/log;
import ballerinax/asb;

// Connection Configurations
configurable string connectionString = ?;
configurable string queuePath = ?;

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
    checkpanic asbClient->createQueueSender(queuePath);

    log:printInfo("Creating Asb receiver connection.");
    checkpanic asbClient->createQueueReceiver(queuePath, asb:PEEKLOCK);

    log:printInfo("Sending via Asb sender connection.");
    checkpanic asbClient->send(message1);

    log:printInfo("Receiving from Asb receiver connection.");
    asb:Message|asb:Error? messageReceived = asbClient->receive(serverWaitTime);

    if (messageReceived is asb:Message) {
        checkpanic asbClient->renewLock(messageReceived);
        asb:Message|asb:Error? messageReceivedAgain = asbClient->receive(serverWaitTime);
        if (messageReceivedAgain is ()) {
            log:printInfo("Renew lock message successful");
        } else {
            log:printError("Renew lock on message not succesful.");
        }
    } else if (messageReceived is ()) {
        log:printError("No message in the queue.");
    } else {
        log:printError("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender connection.");
    checkpanic asbClient->closeSender();

    log:printInfo("Closing Asb receiver connection.");
    checkpanic asbClient->closeReceiver();
}    
```

15. Renew Lock on Message from Subscription
This is the basic scenario of renewing a lock on a message from a subscription. A user must create a sender connection 
and a receiver connection with the azure service bus to send and receive a message. The message body is passed as 
a parameter in byte array format with optional parameters and properties to the send operation. The user can renew 
a lock on a single Message from the subscription.

Sample is available at:
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/blob/main/asb-ballerina/samples/renew_lock_on_message_from_subscription.bal

```ballerina
import ballerina/log;
import ballerinax/asb;

// Connection Configurations
configurable string connectionString = ?;
configurable string topicPath = ?;
configurable string subscriptionPath1 = ?;

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
    checkpanic asbClient->createTopicSender(topicPath);

    log:printInfo("Creating Asb receiver connection.");
    checkpanic asbClient->createSubscriptionReceiver(subscriptionPath1, asb:PEEKLOCK);
    
    log:printInfo("Sending via Asb sender connection.");
    checkpanic asbClient->send(message1);

    log:printInfo("Receiving from Asb receiver connection.");
    asb:Message|asb:Error? messageReceived = asbClient->receive(serverWaitTime);

    if (messageReceived is asb:Message) {
        checkpanic asbClient->renewLock(messageReceived);
        asb:Message|asb:Error? messageReceivedAgain = asbClient->receive(serverWaitTime);
        if (messageReceivedAgain is ()) {
            log:printInfo("Renew lock message successful");
        } else {
            log:printError("Renew lock on message not succesful.");
        }
    } else if (messageReceived is ()) {
        log:printError("No message in the queue.");
    } else {
        log:printError("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender connection.");
    checkpanic asbClient->closeSender();

    log:printInfo("Closing Asb receiver connection.");
    checkpanic asbClient->closeReceiver();
}    
```
