# Ballerina Azure Service Bus Connector Samples: 

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

    log:printInfo("Creating Asb sender connection.");
    asb:SenderConnection? senderConnection = checkpanic new (config);

    log:printInfo("Creating Asb receiver connection.");
    asb:ReceiverConnection? receiverConnection = checkpanic new (config);

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Sending via Asb sender connection.");
        checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContent, parameters1, properties);
        checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContentFromJson, parameters2, properties);
    } else {
        log:printError("Asb sender connection creation failed.");
    }

    if (receiverConnection is asb:ReceiverConnection) {
        log:printInfo("Receiving from Asb receiver connection.");
        asb:Message|asb:Error? messageReceived = receiverConnection->receiveMessage(serverWaitTime);
        asb:Message|asb:Error? jsonMessageReceived = receiverConnection->receiveMessage(serverWaitTime);
        if (messageReceived is asb:Message && jsonMessageReceived is asb:Message) {
            string messageRead = checkpanic messageReceived.getTextContent();
            log:printInfo("Reading Received Message : " + messageRead);
            json jsonMessageRead = checkpanic jsonMessageReceived.getJSONContent();
            log:printInfo("Reading Received Message : " + jsonMessageRead.toString());
        } 
    }

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Closing Asb sender connection.");
        checkpanic senderConnection.closeSenderConnection();
    }

    if (receiverConnection is asb:ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection.");
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

    log:printInfo("Creating Asb sender connection.");
    asb:SenderConnection? senderConnection = checkpanic new (config);

    log:printInfo("Creating Asb receiver connection.");
    asb:ReceiverConnection? receiverConnection = checkpanic new (config);

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Sending via Asb sender connection.");
        checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContent, parameters1, properties);
        checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContentFromJson, parameters2, properties);
    } else {
        log:printError("Asb sender connection creation failed.");
    }

    if (receiverConnection is asb:ReceiverConnection) {
        log:printInfo("Receiving from Asb receiver connection.");
        var messageReceived = receiverConnection->receiveMessages(serverWaitTime, maxMessageCount);
        if(messageReceived is asb:Messages) {
            int val = messageReceived.getMessageCount();
            log:printInfo("No. of messages received : " + val.toString());
            asb:Message[] messages = messageReceived.getMessages();
            string messageReceived1 =  checkpanic messages[0].getTextContent();
            log:printInfo("Message1 content : " +messageReceived1);
            json messageReceived2 =  checkpanic messages[1].getJSONContent();
            log:printInfo("Message2 content : " +messageReceived2.toString());
        } else {
            log:printError(messageReceived.message());
        }
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Closing Asb sender connection.");
        checkpanic senderConnection.closeSenderConnection();
    }

    if (receiverConnection is asb:ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection.");
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

    log:printInfo("Creating Asb sender connection.");
    asb:SenderConnection? senderConnection = checkpanic new (config);

    log:printInfo("Creating Asb receiver connection.");
    asb:ReceiverConnection? receiverConnection = checkpanic new (config);

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Sending via Asb sender connection.");
        checkpanic senderConnection->sendBatchMessage(stringArrayContent, parameters, properties, maxMessageCount);
    } else {
        log:printError("Asb sender connection creation failed.");
    }

    if (receiverConnection is asb:ReceiverConnection) {
        log:printInfo("Receiving from Asb receiver connection.");
        var messageReceived = receiverConnection->receiveBatchMessage(maxMessageCount);
        if(messageReceived is asb:Messages) {
            int val = messageReceived.getMessageCount();
            log:printInfo("No. of messages received : " + val.toString());
            asb:Message[] messages = messageReceived.getMessages();
            string messageReceived1 =  checkpanic messages[0].getTextContent();
            log:printInfo("Message1 content : " + messageReceived1);
            string messageReceived2 =  checkpanic messages[1].getTextContent();
            log:printInfo("Message2 content : " + messageReceived2);
            string messageReceived3 =  checkpanic messages[2].getTextContent();
            log:printInfo("Message3 content : " + messageReceived3);
        } else {
            log:printError("Receiving message via Asb receiver connection failed.");
        }
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Closing Asb sender connection.");
        checkpanic senderConnection.closeSenderConnection();
    }

    if (receiverConnection is asb:ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection.");
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

    log:printInfo("Creating Asb sender connection.");
    asb:SenderConnection? senderConnection = checkpanic new (senderConfig);

    log:printInfo("Creating Asb receiver connection.");
    asb:ReceiverConnection? receiverConnection1 = checkpanic new (receiverConfig1);
    asb:ReceiverConnection? receiverConnection2 = checkpanic new (receiverConfig2);
    asb:ReceiverConnection? receiverConnection3 = checkpanic new (receiverConfig3);

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Sending via Asb sender connection.");
        checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContent, parameters1, properties);
        checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContentFromJson, parameters2, properties);
    } else {
        log:printError("Asb sender connection creation failed.");
    }

    if (receiverConnection1 is asb:ReceiverConnection) {
        log:printInfo("Receiving from Asb receiver connection 1.");
        asb:Message|asb:Error? messageReceived = receiverConnection1->receiveMessage(serverWaitTime);
        asb:Message|asb:Error? jsonMessageReceived = receiverConnection1->receiveMessage(serverWaitTime);
        if (messageReceived is asb:Message && jsonMessageReceived is asb:Message) {
            string messageRead = checkpanic messageReceived.getTextContent();
            log:printInfo("Reading Received Message : " + messageRead);
            json jsonMessageRead = checkpanic jsonMessageReceived.getJSONContent();
            log:printInfo("Reading Received Message : " + jsonMessageRead.toString());
        } else {
            log:printError("Receiving message via Asb receiver connection failed.");
        }
    } else {
        log:printError("Asb receiver connection creation failed.");
    }


    if (receiverConnection2 is asb:ReceiverConnection) {
        log:printInfo("Receiving from Asb receiver connection 2.");
        asb:Message|asb:Error? messageReceived = receiverConnection2->receiveMessage(serverWaitTime);
        asb:Message|asb:Error? jsonMessageReceived = receiverConnection2->receiveMessage(serverWaitTime);
        if (messageReceived is asb:Message && jsonMessageReceived is asb:Message) {
            string messageRead = checkpanic messageReceived.getTextContent();
            log:printInfo("Reading Received Message : " + messageRead);
            json jsonMessageRead = checkpanic jsonMessageReceived.getJSONContent();
            log:printInfo("Reading Received Message : " + jsonMessageRead.toString());
        } else {
            log:printError("Receiving message via Asb receiver connection failed.");
        }
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (receiverConnection3 is asb:ReceiverConnection) {
        log:printInfo("Receiving from Asb receiver connection 3.");
        asb:Message|asb:Error? messageReceived = receiverConnection3->receiveMessage(serverWaitTime);
        asb:Message|asb:Error? jsonMessageReceived = receiverConnection3->receiveMessage(serverWaitTime);
        if (messageReceived is asb:Message && jsonMessageReceived is asb:Message) {
            string messageRead = checkpanic messageReceived.getTextContent();
            log:printInfo("Reading Received Message : " + messageRead);
            json jsonMessageRead = checkpanic jsonMessageReceived.getJSONContent();
            log:printInfo("Reading Received Message : " + jsonMessageRead.toString());
        } else {
            log:printError("Receiving message via Asb receiver connection failed.");
        }
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Closing Asb sender connection.");
        checkpanic senderConnection.closeSenderConnection();
    }

    if (receiverConnection1 is asb:ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 1.");
        checkpanic receiverConnection1.closeReceiverConnection();
    }

    if (receiverConnection2 is asb:ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 2.");
        checkpanic receiverConnection2.closeReceiverConnection();
    }

    if (receiverConnection3 is asb:ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 3.");
        checkpanic receiverConnection3.closeReceiverConnection();
    }
}    
```

5. Send Batch to Topic and Receive from Subscription
This is the basic scenario of sending a batch of messages to a topic and receiving a batch of messages from 
a subscription. A user must create a sender connection and a receiver connection with the azure service bus to send and 
receive a message. The message body is passed as a parameter in byte array format with optional parameters and 
properties to the send operation. The user can receive the array of Message objects at the other receiver end.

Sample is available at:
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/blob/main/asb-ballerina/samples/send_batch_to_topic_and_receive_from_subscription.bal

```ballerina
import ballerina/config;
import ballerina/log;
import ballerinax/asb;

public function main() {

    // Input values
    string[] stringArrayContent = ["apple", "mango", "lemon", "orange"];
    map<string> parameters3 = {contentType: "text/plain", timeToLive: "2"};
    map<string> properties = {a: "propertyValue1", b: "propertyValue2"};
    int maxMessageCount = 3;

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

    log:printInfo("Creating Asb sender connection.");
    asb:SenderConnection? senderConnection = checkpanic new (senderConfig);

    log:printInfo("Creating Asb receiver connection.");
    asb:ReceiverConnection? receiverConnection1 = checkpanic new (receiverConfig1);
    asb:ReceiverConnection? receiverConnection2 = checkpanic new (receiverConfig2);
    asb:ReceiverConnection? receiverConnection3 = checkpanic new (receiverConfig3);

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Sending via Asb sender connection.");
        checkpanic senderConnection->sendBatchMessage(stringArrayContent, parameters3, properties, maxMessageCount);
    } else {
        log:printError("Asb sender connection creation failed.");
    }

    if (receiverConnection1 is asb:ReceiverConnection) {
        log:printInfo("Receiving from Asb receiver connection 1.");
        var messagesReceived = receiverConnection1->receiveBatchMessage(maxMessageCount);
        if(messagesReceived is asb:Messages) {
            int val = messagesReceived.getMessageCount();
            log:printInfo("No. of messages received : " + val.toString());
            asb:Message[] messages = messagesReceived.getMessages();
            string messageReceived1 =  checkpanic messages[0].getTextContent();
            log:printInfo("Message1 content : " + messageReceived1);
            string messageReceived2 =  checkpanic messages[1].getTextContent();
            log:printInfo("Message2 content : " + messageReceived2);
            string messageReceived3 =  checkpanic messages[2].getTextContent();
            log:printInfo("Message3 content : " + messageReceived3);
        }else {
            log:printError("Receiving message via Asb receiver connection failed.");
        }
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (receiverConnection2 is asb:ReceiverConnection) {
        log:printInfo("Receiving from Asb receiver connection 2.");
        var messagesReceived = receiverConnection2->receiveBatchMessage(maxMessageCount);
        if(messagesReceived is asb:Messages) {
            int val = messagesReceived.getMessageCount();
            log:printInfo("No. of messages received : " + val.toString());
            asb:Message[] messages = messagesReceived.getMessages();
            string messageReceived1 =  checkpanic messages[0].getTextContent();
            log:printInfo("Message1 content : " + messageReceived1);
            string messageReceived2 =  checkpanic messages[1].getTextContent();
            log:printInfo("Message2 content : " + messageReceived2);
            string messageReceived3 =  checkpanic messages[2].getTextContent();
            log:printInfo("Message3 content : " + messageReceived3);
        } else {
            log:printError("Receiving message via Asb receiver connection failed.");
        }
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (receiverConnection3 is asb:ReceiverConnection) {
        log:printInfo("Receiving from Asb receiver connection 3.");
        var messagesReceived = receiverConnection3->receiveBatchMessage(maxMessageCount);
        if(messagesReceived is asb:Messages) {
            int val = messagesReceived.getMessageCount();
            log:printInfo("No. of messages received : " + val.toString());
            asb:Message[] messages = messagesReceived.getMessages();
            string messageReceived1 =  checkpanic messages[0].getTextContent();
            log:printInfo("Message1 content : " + messageReceived1);
            string messageReceived2 =  checkpanic messages[1].getTextContent();
            log:printInfo("Message2 content : " + messageReceived2);
            string messageReceived3 =  checkpanic messages[2].getTextContent();
            log:printInfo("Message3 content : " + messageReceived3);
        } else {
            log:printError("Receiving message via Asb receiver connection failed.");
        }
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Closing Asb sender connection.");
        checkpanic senderConnection.closeSenderConnection();
    }

    if (receiverConnection1 is asb:ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 1.");
        checkpanic receiverConnection1.closeReceiverConnection();
    }

    if (receiverConnection2 is asb:ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 2.");
        checkpanic receiverConnection2.closeReceiverConnection();
    }

    if (receiverConnection3 is asb:ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 3.");
        checkpanic receiverConnection3.closeReceiverConnection();
    }
}    
```

6. Complete Messages from Queue
This is the basic scenario of sending and completing messages from a queue. A user must create a sender connection and 
a receiver connection with the azure service bus to send and receive a message. The message body is passed as 
a parameter in byte array format with optional parameters and properties to the send operation. The user can complete 
all the Messages from the queue.

Sample is available at:
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/blob/main/asb-ballerina/samples/complete_messages_from_queue.bal

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

    asb:ConnectionConfiguration config = {
        connectionString: <CONNECTION_STRING>,
        entityPath: <QUEUE_PATH>
    };

    log:printInfo("Creating Asb sender connection.");
    asb:SenderConnection? senderConnection = checkpanic new (config);

    log:printInfo("Creating Asb receiver connection.");
    asb:ReceiverConnection? receiverConnection = checkpanic new (config);

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Sending via Asb sender connection.");
        checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContent, parameters1, properties);
        checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContentFromJson, parameters2, properties);
    } else {
        log:printError("Asb sender connection creation failed.");
    }

    if (receiverConnection is asb:ReceiverConnection) {
        log:printInfo("Completing messages from Asb receiver connection.");
        checkpanic receiverConnection->completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
        log:printInfo("Completing messages from Asb receiver connection.");
        checkpanic receiverConnection->completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Closing Asb sender connection.");
        checkpanic senderConnection.closeSenderConnection();
    }

    if (receiverConnection is asb:ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection.");
        checkpanic receiverConnection.closeReceiverConnection();
    }
}  
```

7. Complete Message from Queue
This is the basic scenario of sending and completing a message from a queue. A user must create a sender connection and 
a receiver connection with the azure service bus to send and receive a message. The message body is passed as 
a parameter in byte array format with optional parameters and properties to the send operation. The user can complete 
single Messages from the queue.

Sample is available at:
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/blob/main/asb-ballerina/samples/complete_message_from_queue.bal

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
    map<string> parameters2 = {contentType: "application/json", messageId: "two", to: "sanju", replyTo: "carol", 
        label: "a1", sessionId: "b1", correlationId: "c1", timeToLive: "2"};
    map<string> properties = {a: "propertyValue1", b: "propertyValue2"};

    asb:ConnectionConfiguration config = {
        connectionString: <CONNECTION_STRING>,
        entityPath: <QUEUE_PATH>
    };

    log:printInfo("Creating Asb sender connection.");
    asb:SenderConnection? senderConnection = checkpanic new (config);

    log:printInfo("Creating Asb receiver connection.");
    asb:ReceiverConnection? receiverConnection = checkpanic new (config);

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Sending via Asb sender connection.");
        checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContent, parameters1, properties);
        checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContentFromJson, parameters2, properties);
    } else {
        log:printError("Asb sender connection creation failed.");
    }

    if (receiverConnection is asb:ReceiverConnection) {
        log:printInfo("Completing message from Asb receiver connection.");
        checkpanic receiverConnection->completeOneMessage();
        checkpanic receiverConnection->completeOneMessage();
        checkpanic receiverConnection->completeOneMessage();
        log:printInfo("Done completing a message using its lock token.");
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Closing Asb sender connection.");
        checkpanic senderConnection.closeSenderConnection();
    }

    if (receiverConnection is asb:ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection.");
        checkpanic receiverConnection.closeReceiverConnection();
    }
}    
```

8. Complete Messages from Subscription
This is the basic scenario of sending and completing messages from a subscription. A user must create a sender 
connection and a receiver connection with the azure service bus to send and receive a message. The message body is 
passed as a parameter in byte array format with optional parameters and properties to the send operation. The user can 
complete all the Messages from the subscription.

Sample is available at:
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/blob/main/asb-ballerina/samples/complete_messages_from_subscription.bal

```ballerina
import ballerina/config;
import ballerina/log;
import ballerinax/asb;

public function main() {

    // Input values
    string[] stringArrayContent = ["apple", "mango", "lemon", "orange"];
    map<string> parameters = {contentType: "plain/text", timeToLive: "2"};
    map<string> properties = {a: "propertyValue1", b: "propertyValue2"};
    int maxMessageCount = 3;

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

    log:printInfo("Creating Asb sender connection.");
    asb:SenderConnection? senderConnection = checkpanic new (senderConfig);

    log:printInfo("Creating Asb receiver connection.");
    asb:ReceiverConnection? receiverConnection1 = checkpanic new (receiverConfig1);
    asb:ReceiverConnection? receiverConnection2 = checkpanic new (receiverConfig2);
    asb:ReceiverConnection? receiverConnection3 = checkpanic new (receiverConfig3);

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Sending via Asb sender connection.");
        checkpanic senderConnection->sendBatchMessage(stringArrayContent, parameters, properties, maxMessageCount);
    } else {
        log:printError("Asb sender connection creation failed.");
    }

    if (receiverConnection1 is asb:ReceiverConnection) {
        log:printInfo("Completing messages from Asb receiver connection 1.");
        checkpanic receiverConnection1->completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
        log:printInfo("Completing messages from Asb receiver connection 1.");
        checkpanic receiverConnection1->completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (receiverConnection2 is asb:ReceiverConnection) {
        log:printInfo("Completing messages from Asb receiver connection 2.");
        checkpanic receiverConnection2->completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
        log:printInfo("Completing messages from Asb receiver connection 2.");
        checkpanic receiverConnection2->completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (receiverConnection3 is asb:ReceiverConnection) {
        log:printInfo("Completing messages from Asb receiver connection 3.");
        checkpanic receiverConnection3->completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
        log:printInfo("Completing messages from Asb receiver connection 3.");
        checkpanic receiverConnection3->completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Closing Asb sender connection.");
        checkpanic senderConnection.closeSenderConnection();
    }

    if (receiverConnection1 is asb:ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 1.");
        checkpanic receiverConnection1.closeReceiverConnection();
    }

    if (receiverConnection2 is asb:ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 2.");
        checkpanic receiverConnection2.closeReceiverConnection();
    }

    if (receiverConnection3 is asb:ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 3.");
        checkpanic receiverConnection3.closeReceiverConnection();
    }
}    
```

9. Complete Message from Subscription
This is the basic scenario of sending and completing a message from a subscription. A user must create a sender 
connection and a receiver connection with the azure service bus to send and receive a message. The message body is 
passed as a parameter in byte array format with optional parameters and properties to the send operation. The user can 
complete single Messages from the subscription.

Sample is available at:
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/blob/main/asb-ballerina/samples/complete_message_from_subscription.bal

```ballerina
import ballerina/config;
import ballerina/log;
import ballerinax/asb;

public function main() {

    // Input values
    string[] stringArrayContent = ["apple", "mango", "lemon", "orange"];
    map<string> parameters = {contentType: "application/text", timeToLive: "2"};
    map<string> properties = {a: "propertyValue1", b: "propertyValue2"};
    int maxMessageCount = 3;

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

    log:printInfo("Creating Asb sender connection.");
    asb:SenderConnection? senderConnection = checkpanic new (senderConfig);

    log:printInfo("Creating Asb receiver connection.");
    asb:ReceiverConnection? receiverConnection1 = checkpanic new (receiverConfig1);
    asb:ReceiverConnection? receiverConnection2 = checkpanic new (receiverConfig2);
    asb:ReceiverConnection? receiverConnection3 = checkpanic new (receiverConfig3);

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Sending via Asb sender connection.");
        checkpanic senderConnection->sendBatchMessage(stringArrayContent, parameters, properties, maxMessageCount);
    } else {
        log:printError("Asb sender connection creation failed.");
    }

    if (receiverConnection1 is asb:ReceiverConnection) {
        log:printInfo("Completing message from Asb receiver connection 1.");
        checkpanic receiverConnection1->completeOneMessage();
        checkpanic receiverConnection1->completeOneMessage();
        checkpanic receiverConnection1->completeOneMessage();
        log:printInfo("Done completing a message using its lock token.");
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (receiverConnection2 is asb:ReceiverConnection) {
        log:printInfo("Completing message from Asb receiver connection 2.");
        checkpanic receiverConnection2->completeOneMessage();
        checkpanic receiverConnection2->completeOneMessage();
        checkpanic receiverConnection2->completeOneMessage();
        log:printInfo("Done completing a message using its lock token.");
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (receiverConnection3 is asb:ReceiverConnection) {
        log:printInfo("Completing message from Asb receiver connection 3.");
        checkpanic receiverConnection3->completeOneMessage();
        checkpanic receiverConnection3->completeOneMessage();
        checkpanic receiverConnection3->completeOneMessage();
        log:printInfo("Done completing a message using its lock token.");
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Closing Asb sender connection.");
        checkpanic senderConnection.closeSenderConnection();
    }

    if (receiverConnection1 is asb:ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 1.");
        checkpanic receiverConnection1.closeReceiverConnection();
    }

    if (receiverConnection2 is asb:ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 2.");
        checkpanic receiverConnection2.closeReceiverConnection();
    }

    if (receiverConnection3 is asb:ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 3.");
        checkpanic receiverConnection3.closeReceiverConnection();
    }
}    
```

10. Abandon Message from Queue
This is the basic scenario of sending and abandoning a message  from a queue. A user must create a sender connection 
and a receiver connection with the azure service bus to send and receive a message. The message body is passed as 
a parameter in byte array format with optional parameters and properties to the send operation. The user can abandon 
a single Messages & make available again for processing from Queue.

Sample is available at:
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/blob/main/asb-ballerina/samples/abandon_message_from_queue.bal

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
    map<string> parameters1 = {contentType: "plain/text", messageId: "one"};
    map<string> parameters2 = {contentType: "application/json", messageId: "two", to: "sanju", replyTo: "carol", 
        label: "a1", sessionId: "b1", correlationId: "c1", timeToLive: "2"};
    map<string> properties = {a: "propertyValue1", b: "propertyValue2"};

    asb:ConnectionConfiguration config = {
        connectionString: <CONNECTION_STRING>,
        entityPath: <QUEUE_PATH>
    };

    log:printInfo("Creating Asb sender connection.");
    asb:SenderConnection? senderConnection = checkpanic new (config);

    log:printInfo("Creating Asb receiver connection.");
    asb:ReceiverConnection? receiverConnection = checkpanic new (config);

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Sending via Asb sender connection.");
        checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContent, parameters1, properties);
        checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContentFromJson, parameters2, properties);
    } else {
        log:printError("Asb sender connection creation failed.");
    }

    if (receiverConnection is asb:ReceiverConnection) {
        log:printInfo("abandoning message from Asb receiver connection.");
        checkpanic receiverConnection->abandonMessage();
        log:printInfo("Done abandoning a message using its lock token.");
        log:printInfo("Completing messages from Asb receiver connection.");
        checkpanic receiverConnection->completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Closing Asb sender connection.");
        checkpanic senderConnection.closeSenderConnection();
    }

    if (receiverConnection is asb:ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection.");
        checkpanic receiverConnection.closeReceiverConnection();
    }
}    
```

11. Abandon Message from Subscription
This is the basic scenario of sending and abandoning a message  from a subscription. A user must create a sender 
connection and a receiver connection with the azure service bus to send and receive a message. The message body is 
passed as a parameter in byte array format with optional parameters and properties to the send operation. The user can 
abandon a single Messages & make available again for processing from subscription.

Sample is available at:
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/blob/main/asb-ballerina/samples/abandon_message_from_subscription.bal

```ballerina
import ballerina/config;
import ballerina/log;
import ballerinax/asb;

public function main() {

    // Input values
    string[] stringArrayContent = ["apple", "mango", "lemon", "orange"];
    map<string> parameters = {contentType: "application/text", timeToLive: "2"};
    map<string> properties = {a: "propertyValue1", b: "propertyValue2"};
    int maxMessageCount = 3;

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

    log:printInfo("Creating Asb sender connection.");
    asb:SenderConnection? senderConnection = checkpanic new (senderConfig);

    log:printInfo("Creating Asb receiver connection.");
    asb:ReceiverConnection? receiverConnection1 = checkpanic new (receiverConfig1);
    asb:ReceiverConnection? receiverConnection2 = checkpanic new (receiverConfig2);
    asb:ReceiverConnection? receiverConnection3 = checkpanic new (receiverConfig3);

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Sending via Asb sender connection.");
        checkpanic senderConnection->sendBatchMessage(stringArrayContent, parameters, properties, maxMessageCount);
    } else {
        log:printError("Asb sender connection creation failed.");
    }

    if (receiverConnection1 is asb:ReceiverConnection) {
        log:printInfo("abandoning message from Asb receiver connection 1.");
        checkpanic receiverConnection1->abandonMessage();
        log:printInfo("Done abandoning a message using its lock token.");
        log:printInfo("Completing messages from Asb receiver connection 1.");
        checkpanic receiverConnection1->completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (receiverConnection2 is asb:ReceiverConnection) {
        log:printInfo("abandoning message from Asb receiver connection 2.");
        checkpanic receiverConnection2->abandonMessage();
        log:printInfo("Done abandoning a message using its lock token.");
        log:printInfo("Completing messages from Asb receiver connection 2.");
        checkpanic receiverConnection2->completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (receiverConnection3 is asb:ReceiverConnection) {
        log:printInfo("abandoning message from Asb receiver connection 3.");
        checkpanic receiverConnection3->abandonMessage();
        log:printInfo("Done abandoning a message using its lock token.");
        log:printInfo("Completing messages from Asb receiver connection 3.");
        checkpanic receiverConnection3->completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Closing Asb sender connection.");
        checkpanic senderConnection.closeSenderConnection();
    }

    if (receiverConnection1 is asb:ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 1.");
        checkpanic receiverConnection1.closeReceiverConnection();
    }

    if (receiverConnection2 is asb:ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 2.");
        checkpanic receiverConnection2.closeReceiverConnection();
    }

    if (receiverConnection3 is asb:ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 3.");
        checkpanic receiverConnection3.closeReceiverConnection();
    }
}    
```

12. Asynchronous Consumer
This is the basic scenario of sending and listening to messages from a queue. A user must create a sender connection 
and a receiver connection with the azure service bus to send and receive a message. The message body is passed as 
a parameter in byte array format with optional parameters and properties to the send operation. The user can 
asynchronously listen to messages from the azure service bus connection and execute the service logic based on the 
message received.

Sample is available at:
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/blob/main/asb-ballerina/samples/async_consumer.bal

```ballerina
import ballerina/config;
import ballerina/log;
import ballerina/runtime;
import ballerinax/asb;

public function main() {

    // Input values
    string stringContent = "This is My Message Body"; 
    byte[] byteContent = stringContent.toBytes();
    json jsonContent = {name: "apple", color: "red", price: 5.36};
    byte[] byteContentFromJson = jsonContent.toJsonString().toBytes();
    map<string> parameters1 = {contentType: "plain/text", messageId: "one"};
    map<string> parameters2 = {contentType: "application/json", messageId: "two", to: "user1", replyTo: "user2", 
        label: "a1", sessionId: "b1", correlationId: "c1", timeToLive: "2"};
    map<string> properties = {a: "propertyValue1", b: "propertyValue2"};

    asb:ConnectionConfiguration config = {
        connectionString: <CONNECTION_STRING>,
        entityPath: <QUEUE_PATH>
    };

    log:printInfo("Creating Asb sender connection.");
    asb:SenderConnection? senderConnection = checkpanic new (config);

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Sending via Asb sender connection.");
        checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContent, parameters1, properties);
        checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContentFromJson, parameters2, properties);
    } else {
        log:printError("Asb sender connection creation failed.");
    }

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
                log:printInfo("The message received: " + messageContent);
            } else {
                log:printError("Error occurred while retrieving the message content.");
            }
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

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Closing Asb sender connection.");
        checkpanic senderConnection.closeSenderConnection();
    }
}    
```

13. Duplicate Messages Detection
This is the basic scenario of detecting duplicate messages from a queue. A user must create a sender connection and 
a receiver connection with the azure service bus to send and receive a message. The message body is passed as 
a parameter in byte array format with optional parameters and properties to the send operation. The user can receive 
the Message object at the other receiver end. If a duplicate message is received the receive operation fails.

Sample is available at:
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/blob/main/asb-ballerina/samples/duplicate_messages_from_queue.bal

```ballerina
import ballerina/config;
import ballerina/log;
import ballerinax/asb;

public function main() {

    // Input values
    string stringContent = "This is My Message Body"; 
    byte[] byteContent = stringContent.toBytes();
    map<string> parameters = {contentType: "plain/text", messageId: "one"};
    map<string> properties = {a: "propertyValue1", b: "propertyValue2"};
    int maxMessageCount = 2;
    int serverWaitTime = 5;

    asb:ConnectionConfiguration config = {
        connectionString: <CONNECTION_STRING>,
        entityPath: <QUEUE_PATH>
    };

    log:printInfo("Creating Asb sender connection.");
    asb:SenderConnection? senderConnection = checkpanic new (config);

    log:printInfo("Creating Asb receiver connection.");
    asb:ReceiverConnection? receiverConnection = checkpanic new (config);

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Sending via Asb sender connection.");
        checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContent, parameters, properties);
        checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContent, parameters, properties);
    } else {
        log:printError("Asb sender connection creation failed.");
    }

    if (receiverConnection is asb:ReceiverConnection) {
        log:printInfo("Receiving from Asb receiver connection.");
        var messageReceived = receiverConnection->receiveMessages(serverWaitTime, maxMessageCount);
        if(messageReceived is asb:Messages) {
            int val = messageReceived.getMessageCount();
            log:printInfo("No. of messages received : " + val.toString());
            asb:Message[] messages = messageReceived.getMessages();
            string messageReceived1 =  checkpanic messages[0].getTextContent();
            log:printInfo("Message1 content : " +messageReceived1);
            string messageReceived2 =  checkpanic messages[1].getTextContent();
            log:printInfo("Message2 content : " +messageReceived2.toString());
        } else {
            log:printError(messageReceived.message());
        }
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Closing Asb sender connection.");
        checkpanic senderConnection.closeSenderConnection();
    }

    if (receiverConnection is asb:ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection.");
        checkpanic receiverConnection.closeReceiverConnection();
    }
}    
```

14. Deadletter from Queue
This is the basic scenario of sending and dead lettering a message  from a queue. A user must create a sender 
connection and a receiver connection with the azure service bus to send and receive a message. The message body is 
passed as a parameter in byte array format with optional parameters and properties to the send operation. The user can 
abandon a single Message & moves the message to the Dead-Letter Queue.

Sample is available at:
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/blob/main/asb-ballerina/samples/deadletter_from_queue.bal

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
    map<string> parameters1 = {contentType: "plain/text", messageId: "one"};
    map<string> parameters2 = {contentType: "application/json", messageId: "two", to: "user1", replyTo: "user2", 
        label: "a1", sessionId: "b1", correlationId: "c1", timeToLive: "2"};
    map<string> properties = {a: "propertyValue1", b: "propertyValue2"};

    asb:ConnectionConfiguration config = {
        connectionString: <CONNECTION_STRING>,
        entityPath: <QUEUE_PATH>
    };

    log:printInfo("Creating Asb sender connection.");
    asb:SenderConnection? senderConnection = checkpanic new (config);

    log:printInfo("Creating Asb receiver connection.");
    asb:ReceiverConnection? receiverConnection = checkpanic new (config);

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Sending via Asb sender connection.");
        checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContent, parameters1, properties);
        checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContentFromJson, parameters2, properties);
    } else {
        log:printError("Asb sender connection creation failed.");
    }

    if (receiverConnection is asb:ReceiverConnection) {
        log:printInfo("Dead-Letter message from Asb receiver connection.");
        checkpanic receiverConnection->deadLetterMessage("deadLetterReason", "deadLetterErrorDescription");
        log:printInfo("Done Dead-Letter a message using its lock token.");
        log:printInfo("Completing messages from Asb receiver connection.");
        checkpanic receiverConnection->completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Closing Asb sender connection.");
        checkpanic senderConnection.closeSenderConnection();
    }

    if (receiverConnection is asb:ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection.");
        checkpanic receiverConnection.closeReceiverConnection();
    }
}    
```

15. Deadletter from Subscription
This is the basic scenario of sending and dead lettering a message  from a subscription. A user must create a sender 
connection and a receiver connection with the azure service bus to send and receive a message. The message body is 
passed as a parameter in byte array format with optional parameters and properties to the send operation. The user can 
abandon a single Message & move the message to the Dead-Letter subscription.

Sample is available at:
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/blob/main/asb-ballerina/samples/deadletter_from_subscription.bal

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
    map<string> parameters1 = {contentType: "plain/text", messageId: "one"};
    map<string> parameters2 = {contentType: "application/json", messageId: "two", to: "user1", replyTo: "user2", 
        label: "a1", sessionId: "b1", correlationId: "c1", timeToLive: "2"};
    map<string> properties = {a: "propertyValue1", b: "propertyValue2"};

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

    log:printInfo("Creating Asb sender connection.");
    asb:SenderConnection? senderConnection = checkpanic new (senderConfig);

    log:printInfo("Creating Asb receiver connection.");
    asb:ReceiverConnection? receiverConnection1 = checkpanic new (receiverConfig1);
    asb:ReceiverConnection? receiverConnection2 = checkpanic new (receiverConfig2);
    asb:ReceiverConnection? receiverConnection3 = checkpanic new (receiverConfig3);

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Sending via Asb sender connection.");
        checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContent, parameters1, properties);
        checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContentFromJson, parameters2, properties);
    } else {
        log:printError("Asb sender connection creation failed.");
    }

    if (receiverConnection1 is asb:ReceiverConnection) {
        log:printInfo("Dead-Letter message from Asb receiver connection 1.");
        checkpanic receiverConnection1->deadLetterMessage("deadLetterReason", "deadLetterErrorDescription");
        log:printInfo("Done Dead-Letter a message using its lock token.");
        log:printInfo("Completing messages from Asb receiver connection 1.");
        checkpanic receiverConnection1->completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (receiverConnection2 is asb:ReceiverConnection) {
        log:printInfo("Dead-Letter message from Asb receiver connection 2.");
        checkpanic receiverConnection2->deadLetterMessage("deadLetterReason", "deadLetterErrorDescription");
        log:printInfo("Done Dead-Letter a message using its lock token.");
        log:printInfo("Completing messages from Asb receiver connection 2.");
        checkpanic receiverConnection2->completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (receiverConnection3 is asb:ReceiverConnection) {
        log:printInfo("Dead-Letter message from Asb receiver connection 3.");
        checkpanic receiverConnection3->deadLetterMessage("deadLetterReason", "deadLetterErrorDescription");
        log:printInfo("Done Dead-Letter a message using its lock token.");
        log:printInfo("Completing messages from Asb receiver connection 3.");
        checkpanic receiverConnection3->completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Closing Asb sender connection.");
        checkpanic senderConnection.closeSenderConnection();
    }

    if (receiverConnection1 is asb:ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 1.");
        checkpanic receiverConnection1.closeReceiverConnection();
    }

    if (receiverConnection2 is asb:ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 2.");
        checkpanic receiverConnection2.closeReceiverConnection();
    }

    if (receiverConnection3 is asb:ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 3.");
        checkpanic receiverConnection3.closeReceiverConnection();
    }
}    
```

16. Defer from Queue
This is the basic scenario of sending and deferring a message from a queue. A user must create a sender connection and 
a receiver connection with the azure service bus to send and receive a message. The message body is passed as 
a parameter in byte array format with optional parameters and properties to the send operation. The user can defer 
a single Message. Deferred messages can only be received by using sequence number and return Message object.

Sample is available at:
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/blob/main/asb-ballerina/samples/defer_from_queue.bal

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
    map<string> parameters1 = {contentType: "plain/text", messageId: "one"};
    map<string> parameters2 = {contentType: "application/json", messageId: "two", to: "user1", replyTo: "user2", 
        label: "a1", sessionId: "b1", correlationId: "c1", timeToLive: "2"};
    map<string> properties = {a: "propertyValue1", b: "propertyValue2"};
    int serverWaitTime = 5;

    asb:ConnectionConfiguration config = {
        connectionString: <CONNECTION_STRING>,
        entityPath: <QUEUE_PATH>
    };

    log:printInfo("Creating Asb sender connection.");
    asb:SenderConnection? senderConnection = checkpanic new (config);

    log:printInfo("Creating Asb receiver connection.");
    asb:ReceiverConnection? receiverConnection = checkpanic new (config);

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Sending via Asb sender connection.");
        checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContent, parameters1, properties);
        checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContentFromJson, parameters2, properties);
    } else {
        log:printError("Asb sender connection creation failed.");
    }

    if (receiverConnection is asb:ReceiverConnection) {
        log:printInfo("Defer message from Asb receiver connection.");
        var sequenceNumber = receiverConnection->deferMessage();
        log:printInfo("Done Deferring a message using its lock token.");
        log:printInfo("Receiving from Asb receiver connection.");
        asb:Message|asb:Error? jsonMessageReceived = receiverConnection->receiveMessage(serverWaitTime);
        if (jsonMessageReceived is asb:Message) {
            json jsonMessageRead = checkpanic jsonMessageReceived.getJSONContent();
            log:printInfo("Reading Received Message : " + jsonMessageRead.toString());
        } else {
            log:printError("Receiving message via Asb receiver connection failed.");
        }
        log:printInfo("Receiving Deferred Message from Asb receiver connection.");
        if(sequenceNumber is int) {
            if(sequenceNumber == 0) {
                log:printError("No message in the queue");
            }
            asb:Message|asb:Error? messageReceived = receiverConnection->receiveDeferredMessage(sequenceNumber);
            if (messageReceived is asb:Message) {
                string messageRead = checkpanic messageReceived.getTextContent();
                log:printInfo("Reading Received Message : " + messageRead);
            } else if (messageReceived is ()) {
                log:printError("No deferred message received with given sequence number");
            } else {
                log:printError(msg = messageReceived.message());
            }
        } else {
            log:printError(msg = sequenceNumber.message());
        }
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Closing Asb sender connection.");
        checkpanic senderConnection.closeSenderConnection();
    }

    if (receiverConnection is asb:ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection.");
        checkpanic receiverConnection.closeReceiverConnection();
    }
}    
```

17. Defer from Subscription
This is the basic scenario of sending and deferring a message  from a subscription. A user must create a sender 
connection and a receiver connection with the azure service bus to send and receive a message. The message body is 
passed as a parameter in byte array format with optional parameters and properties to the send operation. The user can 
defer a single Message. Deferred messages can only be received by using sequence number and return Message object.

Sample is available at:
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/blob/main/asb-ballerina/samples/defer_from_subscription.bal

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
    map<string> parameters1 = {contentType: "plain/text", messageId: "one"};
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

    log:printInfo("Creating Asb sender connection.");
    asb:SenderConnection? senderConnection = checkpanic new (senderConfig);

    log:printInfo("Creating Asb receiver connection.");
    asb:ReceiverConnection? receiverConnection1 = checkpanic new (receiverConfig1);
    asb:ReceiverConnection? receiverConnection2 = checkpanic new (receiverConfig2);
    asb:ReceiverConnection? receiverConnection3 = checkpanic new (receiverConfig3);

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Sending via Asb sender connection.");
        checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContent, parameters1, properties);
        checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContentFromJson, parameters2, properties);
    } else {
        log:printError("Asb sender connection creation failed.");
    }

    if (receiverConnection1 is asb:ReceiverConnection) {
        log:printInfo("Defer message from Asb receiver connection 1.");
        var sequenceNumber = receiverConnection1->deferMessage();
        log:printInfo("Done Deferring a message using its lock token.");
        log:printInfo("Receiving from Asb receiver connection 1.");
        asb:Message|asb:Error? jsonMessageReceived = receiverConnection1->receiveMessage(serverWaitTime);
        if (jsonMessageReceived is asb:Message) {
            json jsonMessageRead = checkpanic jsonMessageReceived.getJSONContent();
            log:printInfo("Reading Received Message : " + jsonMessageRead.toString());
        } else {
            log:printError("Receiving message via Asb receiver connection failed.");
        }
        log:printInfo("Receiving Deferred Message from Asb receiver connection 1.");
        if(sequenceNumber is int) {
            if(sequenceNumber == 0) {
                log:printError("No message in the queue");
            }
            asb:Message|asb:Error? messageReceived = receiverConnection1->receiveDeferredMessage(sequenceNumber);
            if (messageReceived is asb:Message) {
                string messageRead = checkpanic messageReceived.getTextContent();
                log:printInfo("Reading Received Message : " + messageRead);
            } else if (messageReceived is ()) {
                log:printError("No deferred message received with given sequence number");
            } else {
                log:printError(msg = messageReceived.message());
            }
        } else {
            log:printError(msg = sequenceNumber.message());
        }
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (receiverConnection2 is asb:ReceiverConnection) {
        log:printInfo("Defer message from Asb receiver connection 2.");
        var sequenceNumber = receiverConnection2->deferMessage();
        log:printInfo("Done Deferring a message using its lock token.");
        log:printInfo("Receiving from Asb receiver connection 2.");
        asb:Message|asb:Error? jsonMessageReceived = receiverConnection2->receiveMessage(serverWaitTime);
        if (jsonMessageReceived is asb:Message) {
            json jsonMessageRead = checkpanic jsonMessageReceived.getJSONContent();
            log:printInfo("Reading Received Message : " + jsonMessageRead.toString());
        } else {
            log:printError("Receiving message via Asb receiver connection failed.");
        }
        log:printInfo("Receiving Deferred Message from Asb receiver connection 2.");
        if(sequenceNumber is int) {
            if(sequenceNumber == 0) {
                log:printError("No message in the queue");
            }
            asb:Message|asb:Error? messageReceived = receiverConnection2->receiveDeferredMessage(sequenceNumber);
            if (messageReceived is asb:Message) {
                string messageRead = checkpanic messageReceived.getTextContent();
                log:printInfo("Reading Received Message : " + messageRead);
            } else if (messageReceived is ()) {
                log:printError("No deferred message received with given sequence number");
            } else {
                log:printError(msg = messageReceived.message());
            }
        } else {
            log:printError(msg = sequenceNumber.message());
        }
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (receiverConnection3 is asb:ReceiverConnection) {
        log:printInfo("Defer message from Asb receiver connection 3.");
        var sequenceNumber = receiverConnection3->deferMessage();
        log:printInfo("Done Deferring a message using its lock token.");
        log:printInfo("Receiving from Asb receiver connection 3.");
        asb:Message|asb:Error? jsonMessageReceived = receiverConnection3->receiveMessage(serverWaitTime);
        if (jsonMessageReceived is asb:Message) {
            json jsonMessageRead = checkpanic jsonMessageReceived.getJSONContent();
            log:printInfo("Reading Received Message : " + jsonMessageRead.toString());
        } else {
            log:printError("Receiving message via Asb receiver connection failed.");
        }
        log:printInfo("Receiving Deferred Message from Asb receiver connection 3.");
        if(sequenceNumber is int) {
            if(sequenceNumber == 0) {
                log:printError("No message in the queue");
            }
            asb:Message|asb:Error? messageReceived = receiverConnection3->receiveDeferredMessage(sequenceNumber);
            if (messageReceived is asb:Message) {
                string messageRead = checkpanic messageReceived.getTextContent();
                log:printInfo("Reading Received Message : " + messageRead);
            } else if (messageReceived is ()) {
                log:printError("No deferred message received with given sequence number");
            } else {
                log:printError(msg = messageReceived.message());
            }
        } else {
            log:printError(msg = sequenceNumber.message());
        }
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Closing Asb sender connection.");
        checkpanic senderConnection.closeSenderConnection();
    }

    if (receiverConnection1 is asb:ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 1.");
        checkpanic receiverConnection1.closeReceiverConnection();
    }

    if (receiverConnection2 is asb:ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 2.");
        checkpanic receiverConnection2.closeReceiverConnection();
    }

    if (receiverConnection3 is asb:ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 3.");
        checkpanic receiverConnection3.closeReceiverConnection();
    }
}    
```

18. Renew Lock on Message from Queue
This is the basic scenario of renewing a lock on a message  from a queue. A user must create a sender connection and 
a receiver connection with the azure service bus to send and receive a message. The message body is passed as 
a parameter in byte array format with optional parameters and properties to the send operation.  The user can renew 
a lock on a single Message from the queue.

Sample is available at:
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/blob/main/asb-ballerina/samples/renew_lock_on_message_from_queue.bal

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
    map<string> parameters1 = {contentType: "plain/text", messageId: "one"};
    map<string> parameters2 = {contentType: "application/json", messageId: "two", to: "user1", replyTo: "user2", 
        label: "a1", sessionId: "b1", correlationId: "c1", timeToLive: "2"};
    map<string> properties = {a: "propertyValue1", b: "propertyValue2"};

    asb:ConnectionConfiguration config = {
        connectionString: <CONNECTION_STRING>,
        entityPath: <QUEUE_PATH>
    };

    log:printInfo("Creating Asb sender connection.");
    asb:SenderConnection? senderConnection = checkpanic new (config);

    log:printInfo("Creating Asb receiver connection.");
    asb:ReceiverConnection? receiverConnection = checkpanic new (config);

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Sending via Asb sender connection.");
        checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContent, parameters1, properties);
        checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContentFromJson, parameters2, properties);
    } else {
        log:printError("Asb sender connection creation failed.");
    }

    if (receiverConnection is asb:ReceiverConnection) {
        log:printInfo("Renew lock on message from Asb receiver connection.");
        checkpanic receiverConnection->renewLockOnMessage();
        log:printInfo("Done renewing a message.");
        log:printInfo("Completing messages from Asb receiver connection.");
        checkpanic receiverConnection->completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Closing Asb sender connection.");
        checkpanic senderConnection.closeSenderConnection();
    }

    if (receiverConnection is asb:ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection.");
        checkpanic receiverConnection.closeReceiverConnection();
    }
}    
```

19. Renew Lock on Message from Subscription
This is the basic scenario of renewing a lock on a message from a subscription. A user must create a sender connection 
and a receiver connection with the azure service bus to send and receive a message. The message body is passed as 
a parameter in byte array format with optional parameters and properties to the send operation. The user can renew 
a lock on a single Message from the subscription.

Sample is available at:
https://github.com/ballerina-platform/module-ballerinax-azure-service-bus/blob/main/asb-ballerina/samples/renew_lock_on_message_from_subscription.bal

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
    map<string> parameters1 = {contentType: "plain/text", messageId: "one"};
    map<string> parameters2 = {contentType: "application/json", messageId: "two", to: "user1", replyTo: "user2", 
        label: "a1", sessionId: "b1", correlationId: "c1", timeToLive: "2"};
    map<string> properties = {a: "propertyValue1", b: "propertyValue2"};

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

    log:printInfo("Creating Asb sender connection.");
    asb:SenderConnection? senderConnection = checkpanic new (senderConfig);

    log:printInfo("Creating Asb receiver connection.");
    asb:ReceiverConnection? receiverConnection1 = checkpanic new (receiverConfig1);
    asb:ReceiverConnection? receiverConnection2 = checkpanic new (receiverConfig2);
    asb:ReceiverConnection? receiverConnection3 = checkpanic new (receiverConfig3);

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Sending via Asb sender connection.");
        checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContent, parameters1, properties);
        checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContentFromJson, parameters2, properties);
    } else {
        log:printError("Asb sender connection creation failed.");
    }

    if (receiverConnection1 is asb:ReceiverConnection) {
        log:printInfo("Renew lock on message from Asb receiver connection 1.");
        checkpanic receiverConnection1->renewLockOnMessage();
        log:printInfo("Done renewing a message.");
        log:printInfo("Completing messages from Asb receiver connection 1.");
        checkpanic receiverConnection1->completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (receiverConnection2 is asb:ReceiverConnection) {
        log:printInfo("Renew lock on message from Asb receiver connection 2.");
        checkpanic receiverConnection2->renewLockOnMessage();
        log:printInfo("Done renewing a message.");
        log:printInfo("Completing messages from Asb receiver connection 2.");
        checkpanic receiverConnection2->completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (receiverConnection3 is asb:ReceiverConnection) {
        log:printInfo("Renew lock on message from Asb receiver connection 3.");
        checkpanic receiverConnection3->renewLockOnMessage();
        log:printInfo("Done renewing a message.");
        log:printInfo("Completing messages from Asb receiver connection 3.");
        checkpanic receiverConnection3->completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Closing Asb sender connection.");
        checkpanic senderConnection.closeSenderConnection();
    }

    if (receiverConnection1 is asb:ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 1.");
        checkpanic receiverConnection1.closeReceiverConnection();
    }

    if (receiverConnection2 is asb:ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 2.");
        checkpanic receiverConnection2.closeReceiverConnection();
    }

    if (receiverConnection3 is asb:ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 3.");
        checkpanic receiverConnection3.closeReceiverConnection();
    }
}    
```
