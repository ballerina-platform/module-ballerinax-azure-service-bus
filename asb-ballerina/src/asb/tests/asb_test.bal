// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/test;
import ballerina/log;
import ballerina/system;
import ballerina/config;
import ballerina/runtime;
import ballerina/time;

// Connection Configuration
string connectionString = getConfigValue("CONNECTION_STRING");
string queuePath = getConfigValue("QUEUE_PATH");
string topicPath = getConfigValue("TOPIC_PATH");
string subscriptionPath1 = getConfigValue("SUBSCRIPTION_PATH1");
string subscriptionPath2 = getConfigValue("SUBSCRIPTION_PATH2");
string subscriptionPath3 = getConfigValue("SUBSCRIPTION_PATH3");

SenderConnection? senderConnection = ();
ReceiverConnection? receiverConnection = ();

// Input values
string stringContent = "This is My Message Body"; 
byte[] byteContent = stringContent.toBytes();
json jsonContent = {name: "apple", color: "red", price: 5.36};
byte[] byteContentFromJson = jsonContent.toJsonString().toBytes();
json[] jsonArrayContent = [{name: "apple", color: "red", price: 5.36}, {first: "John", last: "Pala"}];
string[] stringArrayContent = ["apple", "mango", "lemon", "orange"];
int[] integerArrayContent = [4, 5, 6];
map<string> parameters = {contentType: "application/json", messageId: "one", to: "sanju", replyTo: "carol", 
    label: "a1", sessionId: "b1", correlationId: "c1", timeToLive: "2"};
map<string> parameters1 = {contentType: "application/json", messageId: "one"};
map<string> parameters2 = {contentType: "application/json", messageId: "two", to: "sanju", replyTo: "carol", 
    label: "a1", sessionId: "b1", correlationId: "c1", timeToLive: "2"};
map<string> parameters3 = {contentType: "application/json"};
map<string> parameters4 = {contentType: "application/text", timeToLive: "8"};
map<string> properties = {a: "propertyValue1", b: "propertyValue2"};
string asyncConsumerMessage = "";
int maxMessageCount = 3;
int maxMessageCount1 = 2;
int serverWaitTime = 5;
int prefetchCountDisabled = 0;
int prefetchCountEnabled = 50;
int messageCount = 100;
int variableMessageCount = 1000;

# Before Suite Function
@test:BeforeSuite
function beforeSuiteFunc() {
    log:printInfo("Creating a ballerina Asb Sender connection.");
    SenderConnection? con = new ({connectionString: connectionString, entityPath: queuePath});
    senderConnection = con;

    log:printInfo("Creating a ballerina Asb Receiver connection.");
    ReceiverConnection? rec = new ({connectionString: connectionString, entityPath: queuePath});
    receiverConnection = rec;
}

# Test Sender Connection
@test:Config {
    enable: false
}
public function testSenderConnection() {
    boolean flag = false;
    SenderConnection? senderConnection = new ({connectionString: connectionString, entityPath: queuePath});
    if (senderConnection is SenderConnection) {
        flag = true;
    }
    test:assertTrue(flag, msg = "Asb Sender Connection creation failed.");
}

# Test Receiver Connection
@test:Config {
    enable: false
}
public function testReceieverConnection() {
    boolean flag = false;
    ReceiverConnection? receiverConnection = new ({connectionString: connectionString, entityPath: queuePath});
    if (receiverConnection is ReceiverConnection) {
        flag = true;
    }
    test:assertTrue(flag, msg = "Asb Receiver Connection creation failed.");
}

# Test send to queue operation
@test:Config {
    enable: false
}
function testSendToQueueOperation() {
    log:printInfo("Creating Asb sender connection.");
    SenderConnection? senderConnection = new ({connectionString: connectionString, entityPath: queuePath});

    if (senderConnection is SenderConnection) {
        log:printInfo("Sending via Asb sender connection.");
        checkpanic senderConnection.sendMessageWithConfigurableParameters(byteContent, parameters1, properties);
        checkpanic senderConnection.sendMessageWithConfigurableParameters(byteContentFromJson, parameters2, properties);
    } else {
        test:assertFail("Asb sender connection creation failed.");
    }

    if (senderConnection is SenderConnection) {
        log:printInfo("Closing Asb sender connection.");
        checkpanic senderConnection.closeSenderConnection();
    }
}

# Test receive one message from queue operation
@test:Config {
    dependsOn: ["testSendToQueueOperation"], 
    enable: false
}
function testReceiveFromQueueOperation() {
    log:printInfo("Creating Asb receiver connection.");
    ReceiverConnection? receiverConnection = new ({connectionString: connectionString, entityPath: queuePath});

    if (receiverConnection is ReceiverConnection) {
        log:printInfo("Receiving from Asb receiver connection.");
        Message|Error messageReceived = receiverConnection.receiveMessage(serverWaitTime);
        Message|Error jsonMessageReceived = receiverConnection.receiveMessage(serverWaitTime);
        if (messageReceived is Message && jsonMessageReceived is Message) {
            string messageRead = checkpanic messageReceived.getTextContent();
            log:printInfo("Reading Received Message : " + messageRead);
            json jsonMessageRead = checkpanic jsonMessageReceived.getJSONContent();
            log:printInfo("Reading Received Message : " + jsonMessageRead.toString());
        } else {
            test:assertFail("Receiving message via Asb receiver connection failed.");
        }
    } else {
        test:assertFail("Asb receiver connection creation failed.");
    }

    if (receiverConnection is ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection.");
        checkpanic receiverConnection.closeReceiverConnection();
    }
}

# Test receive messages from queue operation
@test:Config {
    dependsOn: ["testSendToQueueOperation"], 
    enable: false
}
function testReceiveMessagesFromQueueOperation() {
    log:printInfo("Creating Asb receiver connection.");
    ReceiverConnection? receiverConnection = new ({connectionString: connectionString, entityPath: queuePath});

    if (receiverConnection is ReceiverConnection) {
        log:printInfo("Receiving from Asb receiver connection.");
        var messageReceived = receiverConnection.receiveMessages(serverWaitTime, maxMessageCount);
        if(messageReceived is Messages) {
            int val = messageReceived.getMessageCount();
            log:printInfo("No. of messages received : " + val.toString());
            Message[] messages = messageReceived.getMessages();
            string messageReceived1 =  checkpanic messages[0].getTextContent();
            log:printInfo("Message1 content : " +messageReceived1);
            json messageReceived2 =  checkpanic messages[1].getJSONContent();
            log:printInfo("Message2 content : " +messageReceived2.toString());
        } else {
            test:assertFail(msg = messageReceived.message());
        }
    } else {
        test:assertFail("Asb receiver connection creation failed.");
    }

    if (receiverConnection is ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection.");
        checkpanic receiverConnection.closeReceiverConnection();
    }
}

# Test send batch to queue operation
@test:Config {
    enable: false
}
function testSendBatchToQueueOperation() {
    log:printInfo("Creating Asb sender connection.");
    SenderConnection? senderConnection = new ({connectionString: connectionString, entityPath: queuePath});

    if (senderConnection is SenderConnection) {
        log:printInfo("Sending via Asb sender connection.");
        checkpanic senderConnection.sendBatchMessage(stringArrayContent, parameters3, properties, maxMessageCount);
    } else {
        test:assertFail("Asb sender connection creation failed.");
    }

    if (senderConnection is SenderConnection) {
        log:printInfo("Closing Asb sender connection.");
        checkpanic senderConnection.closeSenderConnection();
    }
}

# Test receive batch from queue operation
@test:Config {
    dependsOn: ["testSendBatchToQueueOperation"], 
    enable: false
}
function testReceiveBatchFromQueueOperation() {
    log:printInfo("Creating Asb receiver connection.");
    ReceiverConnection? receiverConnection = new ({connectionString: connectionString, entityPath: queuePath});

    if (receiverConnection is ReceiverConnection) {
        log:printInfo("Receiving from Asb receiver connection.");
        var messageReceived = receiverConnection.receiveBatchMessage(maxMessageCount);
        if(messageReceived is Messages) {
            int val = messageReceived.getMessageCount();
            log:printInfo("No. of messages received : " + val.toString());
            Message[] messages = messageReceived.getMessages();
            string messageReceived1 =  checkpanic messages[0].getTextContent();
            log:printInfo("Message1 content : " + messageReceived1);
            string messageReceived2 =  checkpanic messages[1].getTextContent();
            log:printInfo("Message2 content : " + messageReceived2);
            string messageReceived3 =  checkpanic messages[2].getTextContent();
            log:printInfo("Message3 content : " + messageReceived3);
        } else {
            test:assertFail("Receiving message via Asb receiver connection failed.");
        }
    } else {
        test:assertFail("Asb receiver connection creation failed.");
    }

    if (receiverConnection is ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection.");
        checkpanic receiverConnection.closeReceiverConnection();
    }
}

# Test complete Messages from queue operation
@test:Config {
    dependsOn: ["testSendToQueueOperation"], 
    enable: false
}
function testCompleteMessagesFromQueueOperation() {
    log:printInfo("Creating Asb receiver connection.");
    ReceiverConnection? receiverConnection = new ({connectionString: connectionString, entityPath: queuePath});

    if (receiverConnection is ReceiverConnection) {
        log:printInfo("Completing messages from Asb receiver connection.");
        checkpanic receiverConnection.completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
        log:printInfo("Completing messages from Asb receiver connection.");
        checkpanic receiverConnection.completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
    } else {
        test:assertFail("Asb receiver connection creation failed.");
    }

    if (receiverConnection is ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection.");
        checkpanic receiverConnection.closeReceiverConnection();
    }
}

# Test complete single messages from queue operation
@test:Config {
    dependsOn: ["testSendToQueueOperation"], 
    enable: false
}
function testCompleteOneMessageFromQueueOperation() {
    log:printInfo("Creating Asb receiver connection.");
    ReceiverConnection? receiverConnection = new ({connectionString: connectionString, entityPath: queuePath});

    if (receiverConnection is ReceiverConnection) {
        log:printInfo("Completing message from Asb receiver connection.");
        checkpanic receiverConnection.completeOneMessage();
        checkpanic receiverConnection.completeOneMessage();
        checkpanic receiverConnection.completeOneMessage();
        log:printInfo("Done completing a message using its lock token.");
    } else {
        test:assertFail("Asb receiver connection creation failed.");
    }

    if (receiverConnection is ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection.");
        checkpanic receiverConnection.closeReceiverConnection();
    }
}

# Test abandon Message from queue operation
@test:Config {
    dependsOn: ["testSendToQueueOperation"], 
    enable: false
}
function testAbandonMessageFromQueueOperation() {
    log:printInfo("Creating Asb receiver connection.");
    ReceiverConnection? receiverConnection = new ({connectionString: connectionString, entityPath: queuePath});

    if (receiverConnection is ReceiverConnection) {
        log:printInfo("abandoning message from Asb receiver connection.");
        checkpanic receiverConnection.abandonMessage();
        log:printInfo("Done abandoning a message using its lock token.");
        log:printInfo("Completing messages from Asb receiver connection.");
        checkpanic receiverConnection.completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
    } else {
        test:assertFail("Asb receiver connection creation failed.");
    }

    if (receiverConnection is ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection.");
        checkpanic receiverConnection.closeReceiverConnection();
    }
}

# Test send to topic operation
@test:Config {
    enable: false
}
function testSendToTopicOperation() {
    log:printInfo("Creating Asb sender connection.");
    SenderConnection? senderConnection = new ({connectionString: connectionString, entityPath: topicPath});

    if (senderConnection is SenderConnection) {
        log:printInfo("Sending via Asb sender connection.");
        checkpanic senderConnection.sendMessageWithConfigurableParameters(byteContent, parameters1, properties);
        checkpanic senderConnection.sendMessageWithConfigurableParameters(byteContentFromJson, parameters2, properties);
    } else {
        test:assertFail("Asb sender connection creation failed.");
    }

    if (senderConnection is SenderConnection) {
        log:printInfo("Closing Asb sender connection.");
        checkpanic senderConnection.closeSenderConnection();
    }
}

# Test receive from subscription operation
@test:Config {
    dependsOn: ["testSendToTopicOperation"], 
    enable: false
}
function testReceiveFromSubscriptionOperation() {
    log:printInfo("Creating Asb receiver connection.");
    ReceiverConnection? receiverConnection1 = new ({connectionString: connectionString, entityPath: subscriptionPath1});
    ReceiverConnection? receiverConnection2 = new ({connectionString: connectionString, entityPath: subscriptionPath2});
    ReceiverConnection? receiverConnection3 = new ({connectionString: connectionString, entityPath: subscriptionPath3});

    if (receiverConnection1 is ReceiverConnection) {
        log:printInfo("Receiving from Asb receiver connection 1.");
        Message|error messageReceived = receiverConnection1.receiveMessage(serverWaitTime);
        Message|error jsonMessageReceived = receiverConnection1.receiveMessage(serverWaitTime);
        if (messageReceived is Message && jsonMessageReceived is Message) {
            string messageRead = checkpanic messageReceived.getTextContent();
            log:printInfo("Reading Received Message : " + messageRead);
            json jsonMessageRead = checkpanic jsonMessageReceived.getJSONContent();
            log:printInfo("Reading Received Message : " + jsonMessageRead.toString());
        } else {
            test:assertFail("Receiving message via Asb receiver connection failed.");
        }
    } else {
        test:assertFail("Asb receiver connection creation failed.");
    }


    if (receiverConnection2 is ReceiverConnection) {
        log:printInfo("Receiving from Asb receiver connection 2.");
        Message|error messageReceived = receiverConnection2.receiveMessage(serverWaitTime);
        Message|error jsonMessageReceived = receiverConnection2.receiveMessage(serverWaitTime);
        if (messageReceived is Message && jsonMessageReceived is Message) {
            string messageRead = checkpanic messageReceived.getTextContent();
            log:printInfo("Reading Received Message : " + messageRead);
            json jsonMessageRead = checkpanic jsonMessageReceived.getJSONContent();
            log:printInfo("Reading Received Message : " + jsonMessageRead.toString());
        } else {
            test:assertFail("Receiving message via Asb receiver connection failed.");
        }
    } else {
        test:assertFail("Asb receiver connection creation failed.");
    }

    if (receiverConnection3 is ReceiverConnection) {
        log:printInfo("Receiving from Asb receiver connection 3.");
        Message|error messageReceived = receiverConnection3.receiveMessage(serverWaitTime);
        Message|error jsonMessageReceived = receiverConnection3.receiveMessage(serverWaitTime);
        if (messageReceived is Message && jsonMessageReceived is Message) {
            string messageRead = checkpanic messageReceived.getTextContent();
            log:printInfo("Reading Received Message : " + messageRead);
            json jsonMessageRead = checkpanic jsonMessageReceived.getJSONContent();
            log:printInfo("Reading Received Message : " + jsonMessageRead.toString());
        } else {
            test:assertFail("Receiving message via Asb receiver connection failed.");
        }
    } else {
        test:assertFail("Asb receiver connection creation failed.");
    }

    if (receiverConnection1 is ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 1.");
        checkpanic receiverConnection1.closeReceiverConnection();
    }

    if (receiverConnection2 is ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 2.");
        checkpanic receiverConnection2.closeReceiverConnection();
    }

    if (receiverConnection3 is ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 3.");
        checkpanic receiverConnection3.closeReceiverConnection();
    }
}

# Test send batch to topic operation
@test:Config {
    enable: false
}
function testSendBatchToTopicOperation() {
    log:printInfo("Creating Asb sender connection.");
    SenderConnection? senderConnection = new ({connectionString: connectionString, entityPath: topicPath});

    if (senderConnection is SenderConnection) {
        log:printInfo("Sending via Asb sender connection.");
        checkpanic senderConnection.sendBatchMessage(stringArrayContent, parameters3, properties, maxMessageCount);
    } else {
        test:assertFail("Asb sender connection creation failed.");
    }

    if (senderConnection is SenderConnection) {
        log:printInfo("Closing Asb sender connection.");
        checkpanic senderConnection.closeSenderConnection();
    }
}

# Test receive batch from subscription operation
@test:Config {
    dependsOn: ["testSendBatchToTopicOperation"], 
    enable: false
}
function testReceiveBatchFromSubscriptionOperation() {
    log:printInfo("Creating Asb receiver connection.");
    ReceiverConnection? receiverConnection1 = new ({connectionString: connectionString, entityPath: subscriptionPath1});
    ReceiverConnection? receiverConnection2 = new ({connectionString: connectionString, entityPath: subscriptionPath2});
    ReceiverConnection? receiverConnection3 = new ({connectionString: connectionString, entityPath: subscriptionPath3});

    if (receiverConnection1 is ReceiverConnection) {
        log:printInfo("Receiving from Asb receiver connection 1.");
        var messagesReceived = receiverConnection1.receiveBatchMessage(maxMessageCount);
        if(messagesReceived is Messages) {
            int val = messagesReceived.getMessageCount();
            log:printInfo("No. of messages received : " + val.toString());
            Message[] messages = messagesReceived.getMessages();
            string messageReceived1 =  checkpanic messages[0].getTextContent();
            log:printInfo("Message1 content : " + messageReceived1);
            string messageReceived2 =  checkpanic messages[1].getTextContent();
            log:printInfo("Message2 content : " + messageReceived2);
            string messageReceived3 =  checkpanic messages[2].getTextContent();
            log:printInfo("Message3 content : " + messageReceived3);
        }else {
            test:assertFail("Receiving message via Asb receiver connection failed.");
        }
    } else {
        test:assertFail("Asb receiver connection creation failed.");
    }

    if (receiverConnection2 is ReceiverConnection) {
        log:printInfo("Receiving from Asb receiver connection 2.");
        var messagesReceived = receiverConnection2.receiveBatchMessage(maxMessageCount);
        if(messagesReceived is Messages) {
            int val = messagesReceived.getMessageCount();
            log:printInfo("No. of messages received : " + val.toString());
            Message[] messages = messagesReceived.getMessages();
            string messageReceived1 =  checkpanic messages[0].getTextContent();
            log:printInfo("Message1 content : " + messageReceived1);
            string messageReceived2 =  checkpanic messages[1].getTextContent();
            log:printInfo("Message2 content : " + messageReceived2);
            string messageReceived3 =  checkpanic messages[2].getTextContent();
            log:printInfo("Message3 content : " + messageReceived3);
        } else {
            test:assertFail("Receiving message via Asb receiver connection failed.");
        }
    } else {
        test:assertFail("Asb receiver connection creation failed.");
    }

    if (receiverConnection3 is ReceiverConnection) {
        log:printInfo("Receiving from Asb receiver connection 3.");
        var messagesReceived = receiverConnection3.receiveBatchMessage(maxMessageCount);
        if(messagesReceived is Messages) {
            int val = messagesReceived.getMessageCount();
            log:printInfo("No. of messages received : " + val.toString());
            Message[] messages = messagesReceived.getMessages();
            string messageReceived1 =  checkpanic messages[0].getTextContent();
            log:printInfo("Message1 content : " + messageReceived1);
            string messageReceived2 =  checkpanic messages[1].getTextContent();
            log:printInfo("Message2 content : " + messageReceived2);
            string messageReceived3 =  checkpanic messages[2].getTextContent();
            log:printInfo("Message3 content : " + messageReceived3);
        } else {
            test:assertFail("Receiving message via Asb receiver connection failed.");
        }
    } else {
        test:assertFail("Asb receiver connection creation failed.");
    }

    if (receiverConnection1 is ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 1.");
        checkpanic receiverConnection1.closeReceiverConnection();
    }

    if (receiverConnection2 is ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 2.");
        checkpanic receiverConnection2.closeReceiverConnection();
    }

    if (receiverConnection3 is ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 3.");
        checkpanic receiverConnection3.closeReceiverConnection();
    }
}

# Test complete Messages from subscription operation
@test:Config { 
    dependsOn: ["testSendToTopicOperation"], 
    enable: false
}
function testCompleteMessagesFromSubscriptionOperation() {
    log:printInfo("Creating Asb receiver connection.");
    ReceiverConnection? receiverConnection1 = new ({connectionString: connectionString, entityPath: subscriptionPath1});
    ReceiverConnection? receiverConnection2 = new ({connectionString: connectionString, entityPath: subscriptionPath2});
    ReceiverConnection? receiverConnection3 = new ({connectionString: connectionString, entityPath: subscriptionPath3});

    if (receiverConnection1 is ReceiverConnection) {
        log:printInfo("Completing messages from Asb receiver connection.");
        checkpanic receiverConnection1.completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
        log:printInfo("Completing messages from Asb receiver connection.");
        checkpanic receiverConnection1.completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
    } else {
        test:assertFail("Asb receiver connection creation failed.");
    }

    if (receiverConnection2 is ReceiverConnection) {
        log:printInfo("Completing messages from Asb receiver connection.");
        checkpanic receiverConnection2.completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
        log:printInfo("Completing messages from Asb receiver connection.");
        checkpanic receiverConnection2.completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
    } else {
        test:assertFail("Asb receiver connection creation failed.");
    }

    if (receiverConnection3 is ReceiverConnection) {
        log:printInfo("Completing messages from Asb receiver connection.");
        checkpanic receiverConnection3.completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
        log:printInfo("Completing messages from Asb receiver connection.");
        checkpanic receiverConnection3.completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
    } else {
        test:assertFail("Asb receiver connection creation failed.");
    }

    if (receiverConnection1 is ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 1.");
        checkpanic receiverConnection1.closeReceiverConnection();
    }

    if (receiverConnection2 is ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 2.");
        checkpanic receiverConnection2.closeReceiverConnection();
    }

    if (receiverConnection3 is ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 3.");
        checkpanic receiverConnection3.closeReceiverConnection();
    }
}

# Test complete single messages from subscription operation
@test:Config {
    dependsOn: ["testSendToTopicOperation"], 
    enable: false
}
function testCompleteOneMessageFromSubscriptionOperation() {
    log:printInfo("Creating Asb receiver connection.");
    ReceiverConnection? receiverConnection1 = new ({connectionString: connectionString, entityPath: subscriptionPath1});
    ReceiverConnection? receiverConnection2 = new ({connectionString: connectionString, entityPath: subscriptionPath2});
    ReceiverConnection? receiverConnection3 = new ({connectionString: connectionString, entityPath: subscriptionPath3});

    if (receiverConnection1 is ReceiverConnection) {
        log:printInfo("Completing message from Asb receiver connection.");
        checkpanic receiverConnection1.completeOneMessage();
        checkpanic receiverConnection1.completeOneMessage();
        checkpanic receiverConnection1.completeOneMessage();
        log:printInfo("Done completing a message using its lock token.");
    } else {
        test:assertFail("Asb receiver connection creation failed.");
    }

    if (receiverConnection2 is ReceiverConnection) {
        log:printInfo("Completing message from Asb receiver connection.");
        checkpanic receiverConnection2.completeOneMessage();
        checkpanic receiverConnection2.completeOneMessage();
        checkpanic receiverConnection2.completeOneMessage();
        log:printInfo("Done completing a message using its lock token.");
    } else {
        test:assertFail("Asb receiver connection creation failed.");
    }

    if (receiverConnection3 is ReceiverConnection) {
        log:printInfo("Completing message from Asb receiver connection.");
        checkpanic receiverConnection3.completeOneMessage();
        checkpanic receiverConnection3.completeOneMessage();
        checkpanic receiverConnection3.completeOneMessage();
        log:printInfo("Done completing a message using its lock token.");
    } else {
        test:assertFail("Asb receiver connection creation failed.");
    }

    if (receiverConnection1 is ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 1.");
        checkpanic receiverConnection1.closeReceiverConnection();
    }

    if (receiverConnection2 is ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 2.");
        checkpanic receiverConnection2.closeReceiverConnection();
    }

    if (receiverConnection3 is ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 3.");
        checkpanic receiverConnection3.closeReceiverConnection();
    }
}

# Test abandon Message from subscription operation
@test:Config {
    dependsOn: ["testSendToTopicOperation"], 
    enable: false
}
function testAbandonMessageFromSubscriptionOperation() {
    log:printInfo("Creating Asb receiver connection.");
    ReceiverConnection? receiverConnection1 = new ({connectionString: connectionString, entityPath: subscriptionPath1});
    ReceiverConnection? receiverConnection2 = new ({connectionString: connectionString, entityPath: subscriptionPath2});
    ReceiverConnection? receiverConnection3 = new ({connectionString: connectionString, entityPath: subscriptionPath3});

    if (receiverConnection1 is ReceiverConnection) {
        log:printInfo("abandoning message from Asb receiver connection.");
        checkpanic receiverConnection1.abandonMessage();
        log:printInfo("Done abandoning a message using its lock token.");
        log:printInfo("Completing messages from Asb receiver connection.");
        checkpanic receiverConnection1.completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
    } else {
        test:assertFail("Asb receiver connection creation failed.");
    }

    if (receiverConnection2 is ReceiverConnection) {
        log:printInfo("abandoning message from Asb receiver connection.");
        checkpanic receiverConnection2.abandonMessage();
        log:printInfo("Done abandoning a message using its lock token.");
        log:printInfo("Completing messages from Asb receiver connection.");
        checkpanic receiverConnection2.completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
    } else {
        test:assertFail("Asb receiver connection creation failed.");
    }

    if (receiverConnection3 is ReceiverConnection) {
        log:printInfo("abandoning message from Asb receiver connection.");
        checkpanic receiverConnection3.abandonMessage();
        log:printInfo("Done abandoning a message using its lock token.");
        log:printInfo("Completing messages from Asb receiver connection.");
        checkpanic receiverConnection3.completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
    } else {
        test:assertFail("Asb receiver connection creation failed.");
    }

    if (receiverConnection1 is ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 1.");
        checkpanic receiverConnection1.closeReceiverConnection();
    }

    if (receiverConnection2 is ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 2.");
        checkpanic receiverConnection2.closeReceiverConnection();
    }

    if (receiverConnection3 is ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 3.");
        checkpanic receiverConnection3.closeReceiverConnection();
    }
}

# Async test service used to attached to the listener
service asyncTestService = 
@ServiceConfig {
    queueConfig: {
        connectionString: connectionString,
        queueName: queuePath
    }
}
service {
    resource function onMessage(Message message) {
        var messageContent = message.getTextContent();
        if (messageContent is string) {
            asyncConsumerMessage = <@untainted> messageContent;
            log:printInfo("The message received: " + messageContent);
        } else {
            log:printError("Error occurred while retrieving the message content.");
        }
    }
};

# Test Listener capabilities
@test:Config {
    dependsOn: ["testSendToQueueOperation"], 
    enable: false
}
public function testAsyncConsumer() {

    ConnectionConfiguration config = {
        connectionString: connectionString,
        entityPath: queuePath
    };

    string message = string `{"name":"apple", "color":"red", "price":5.36}`;
    Listener? channelListener = new(config);
    if (channelListener is Listener) {
        checkpanic channelListener.__attach(asyncTestService);
        checkpanic channelListener.__start();
        log:printInfo("start");
        runtime:sleep(20000);
        log:printInfo("end");
        checkpanic channelListener.__detach(asyncTestService);
        checkpanic channelListener.__gracefulStop();
        checkpanic channelListener.__immediateStop();
        test:assertEquals(asyncConsumerMessage, message, msg = "Message received does not match.");
    }
}

# Test send duplicate to queue operation
@test:Config {
    enable: false
}
function testSendDuplicateToQueueOperation() {
    log:printInfo("Creating Asb sender connection.");
    SenderConnection? senderConnection = new ({connectionString: connectionString, entityPath: queuePath});

    if (senderConnection is SenderConnection) {
        log:printInfo("Sending via Asb sender connection.");
        checkpanic senderConnection.sendMessageWithConfigurableParameters(byteContent, parameters1, properties);
        checkpanic senderConnection.sendMessageWithConfigurableParameters(byteContent, parameters1, properties);
    } else {
        test:assertFail("Asb sender connection creation failed.");
    }

    if (senderConnection is SenderConnection) {
        log:printInfo("Closing Asb sender connection.");
        checkpanic senderConnection.closeSenderConnection();
    }
}

# Test receive duplicate messages from queue operation
@test:Config {
    dependsOn: ["testSendDuplicateToQueueOperation"], 
    enable: false
}
function testReceiveDuplicateMessagesFromQueueOperation() {
    log:printInfo("Creating Asb receiver connection.");
    ReceiverConnection? receiverConnection = new ({connectionString: connectionString, entityPath: queuePath});

    if (receiverConnection is ReceiverConnection) {
        log:printInfo("Receiving from Asb receiver connection.");
        var messageReceived = receiverConnection.receiveMessages(serverWaitTime, maxMessageCount1);
        if(messageReceived is Messages) {
            int val = messageReceived.getMessageCount();
            log:printInfo("No. of messages received : " + val.toString());
            Message[] messages = messageReceived.getMessages();
            string messageReceived1 =  checkpanic messages[0].getTextContent();
            log:printInfo("Message1 content : " +messageReceived1);
            string messageReceived2 =  checkpanic messages[1].getTextContent();
            log:printInfo("Message2 content : " +messageReceived2.toString());
        } else {
            test:assertEquals(messageReceived.message(), "Received a duplicate message!", 
                msg = "Error message does not match");
        }
    } else {
        test:assertFail("Asb receiver connection creation failed.");
    }

    if (receiverConnection is ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection.");
        checkpanic receiverConnection.closeReceiverConnection();
    }
}

# Test Dead-Letter Message from queue operation
@test:Config {
    dependsOn: ["testSendToQueueOperation"], 
    enable: false
}
function testDeadLetterFromQueueOperation() {
    log:printInfo("Creating Asb receiver connection.");
    ReceiverConnection? receiverConnection = new ({connectionString: connectionString, entityPath: queuePath});

    if (receiverConnection is ReceiverConnection) {
        log:printInfo("Dead-Letter message from Asb receiver connection.");
        checkpanic receiverConnection.deadLetterMessage("deadLetterReason", "deadLetterErrorDescription");
        log:printInfo("Done Dead-Letter a message using its lock token.");
        log:printInfo("Completing messages from Asb receiver connection.");
        checkpanic receiverConnection.completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
    } else {
        test:assertFail("Asb receiver connection creation failed.");
    }

    if (receiverConnection is ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection.");
        checkpanic receiverConnection.closeReceiverConnection();
    }
}

# Test Dead-Letter Message from subscription operation
@test:Config {
    dependsOn: ["testSendToTopicOperation"], 
    enable: false
}
function testDeadLetterFromSubscriptionOperation() {
    log:printInfo("Creating Asb receiver connection.");
    ReceiverConnection? receiverConnection1 = new ({connectionString: connectionString, entityPath: subscriptionPath1});
    ReceiverConnection? receiverConnection2 = new ({connectionString: connectionString, entityPath: subscriptionPath2});
    ReceiverConnection? receiverConnection3 = new ({connectionString: connectionString, entityPath: subscriptionPath3});

    if (receiverConnection1 is ReceiverConnection) {
        log:printInfo("Dead-Letter message from Asb receiver connection.");
        checkpanic receiverConnection1.deadLetterMessage("deadLetterReason", "deadLetterErrorDescription");
        log:printInfo("Done Dead-Letter a message using its lock token.");
        log:printInfo("Completing messages from Asb receiver connection.");
        checkpanic receiverConnection1.completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
    } else {
        test:assertFail("Asb receiver connection creation failed.");
    }

    if (receiverConnection2 is ReceiverConnection) {
        log:printInfo("Dead-Letter message from Asb receiver connection.");
        checkpanic receiverConnection2.deadLetterMessage("deadLetterReason", "deadLetterErrorDescription");
        log:printInfo("Done Dead-Letter a message using its lock token.");
        log:printInfo("Completing messages from Asb receiver connection.");
        checkpanic receiverConnection2.completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
    } else {
        test:assertFail("Asb receiver connection creation failed.");
    }

    if (receiverConnection3 is ReceiverConnection) {
        log:printInfo("Dead-Letter message from Asb receiver connection.");
        checkpanic receiverConnection3.deadLetterMessage("deadLetterReason", "deadLetterErrorDescription");
        log:printInfo("Done Dead-Letter a message using its lock token.");
        log:printInfo("Completing messages from Asb receiver connection.");
        checkpanic receiverConnection3.completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
    } else {
        test:assertFail("Asb receiver connection creation failed.");
    }

    if (receiverConnection1 is ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 1.");
        checkpanic receiverConnection1.closeReceiverConnection();
    }

    if (receiverConnection2 is ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 2.");
        checkpanic receiverConnection2.closeReceiverConnection();
    }

    if (receiverConnection3 is ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 3.");
        checkpanic receiverConnection3.closeReceiverConnection();
    }
}

# Test Defer Message from queue operation
@test:Config {
    dependsOn: ["testSendToQueueOperation"], 
    enable: false
}
function testDeferFromQueueOperation() {
    log:printInfo("Creating Asb receiver connection.");
    ReceiverConnection? receiverConnection = new ({connectionString: connectionString, entityPath: queuePath});

    if (receiverConnection is ReceiverConnection) {
        log:printInfo("Defer message from Asb receiver connection.");
        var sequenceNumber = receiverConnection.deferMessage();
        log:printInfo("Done Deferring a message using its lock token.");
        log:printInfo("Receiving from Asb receiver connection.");
        Message|Error jsonMessageReceived = receiverConnection.receiveMessage(serverWaitTime);
        if (jsonMessageReceived is Message) {
            json jsonMessageRead = checkpanic jsonMessageReceived.getJSONContent();
            log:printInfo("Reading Received Message : " + jsonMessageRead.toString());
        } else {
            test:assertFail("Receiving message via Asb receiver connection failed.");
        }
        log:printInfo("Receiving Deferred Message from Asb receiver connection.");
        if(sequenceNumber is int) {
            if(sequenceNumber == 0) {
                test:assertFail("No message in the queue");
            }
            Message|Error messageReceived = receiverConnection.receiveDeferredMessage(sequenceNumber);
            if (messageReceived is Message) {
                string messageRead = checkpanic messageReceived.getTextContent();
                log:printInfo("Reading Received Message : " + messageRead);
            } else {
                test:assertFail(msg = messageReceived.message());
            }
        } else {
            test:assertFail(msg = sequenceNumber.message());
        }
    } else {
        test:assertFail("Asb receiver connection creation failed.");
    }

    if (receiverConnection is ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection.");
        checkpanic receiverConnection.closeReceiverConnection();
    }
}

# Test Defer Message from subscription operation
@test:Config {
    dependsOn: ["testSendToTopicOperation"], 
    enable: false
}
function testDeferFromSubscriptionOperation() {
    log:printInfo("Creating Asb receiver connection.");
    ReceiverConnection? receiverConnection1 = new ({connectionString: connectionString, entityPath: subscriptionPath1});
    ReceiverConnection? receiverConnection2 = new ({connectionString: connectionString, entityPath: subscriptionPath2});
    ReceiverConnection? receiverConnection3 = new ({connectionString: connectionString, entityPath: subscriptionPath3});

    if (receiverConnection1 is ReceiverConnection) {
        log:printInfo("Defer message from Asb receiver connection.");
        var sequenceNumber = receiverConnection1.deferMessage();
        log:printInfo("Done Deferring a message using its lock token.");
        log:printInfo("Receiving from Asb receiver connection.");
        Message|Error jsonMessageReceived = receiverConnection1.receiveMessage(serverWaitTime);
        if (jsonMessageReceived is Message) {
            json jsonMessageRead = checkpanic jsonMessageReceived.getJSONContent();
            log:printInfo("Reading Received Message : " + jsonMessageRead.toString());
        } else {
            test:assertFail("Receiving message via Asb receiver connection failed.");
        }
        log:printInfo("Receiving Deferred Message from Asb receiver connection.");
        if(sequenceNumber is int) {
            if(sequenceNumber == 0) {
                test:assertFail("No message in the queue");
            }
            Message|Error messageReceived = receiverConnection1.receiveDeferredMessage(sequenceNumber);
            if (messageReceived is Message) {
                string messageRead = checkpanic messageReceived.getTextContent();
                log:printInfo("Reading Received Message : " + messageRead);
            } else {
                test:assertFail(msg = messageReceived.message());
            }
        } else {
            test:assertFail(msg = sequenceNumber.message());
        }
    } else {
        test:assertFail("Asb receiver connection creation failed.");
    }

    if (receiverConnection2 is ReceiverConnection) {
        log:printInfo("Defer message from Asb receiver connection.");
        var sequenceNumber = receiverConnection2.deferMessage();
        log:printInfo("Done Deferring a message using its lock token.");
        log:printInfo("Receiving from Asb receiver connection.");
        Message|Error jsonMessageReceived = receiverConnection2.receiveMessage(serverWaitTime);
        if (jsonMessageReceived is Message) {
            json jsonMessageRead = checkpanic jsonMessageReceived.getJSONContent();
            log:printInfo("Reading Received Message : " + jsonMessageRead.toString());
        } else {
            test:assertFail("Receiving message via Asb receiver connection failed.");
        }
        log:printInfo("Receiving Deferred Message from Asb receiver connection.");
        if(sequenceNumber is int) {
            if(sequenceNumber == 0) {
                test:assertFail("No message in the queue");
            }
            Message|Error messageReceived = receiverConnection2.receiveDeferredMessage(sequenceNumber);
            if (messageReceived is Message) {
                string messageRead = checkpanic messageReceived.getTextContent();
                log:printInfo("Reading Received Message : " + messageRead);
            } else {
                test:assertFail(msg = messageReceived.message());
            }
        } else {
            test:assertFail(msg = sequenceNumber.message());
        }
    } else {
        test:assertFail("Asb receiver connection creation failed.");
    }

    if (receiverConnection3 is ReceiverConnection) {
        log:printInfo("Defer message from Asb receiver connection.");
        var sequenceNumber = receiverConnection3.deferMessage();
        log:printInfo("Done Deferring a message using its lock token.");
        log:printInfo("Receiving from Asb receiver connection.");
        Message|Error jsonMessageReceived = receiverConnection3.receiveMessage(serverWaitTime);
        if (jsonMessageReceived is Message) {
            json jsonMessageRead = checkpanic jsonMessageReceived.getJSONContent();
            log:printInfo("Reading Received Message : " + jsonMessageRead.toString());
        } else {
            test:assertFail("Receiving message via Asb receiver connection failed.");
        }
        log:printInfo("Receiving Deferred Message from Asb receiver connection.");
        if(sequenceNumber is int) {
            if(sequenceNumber == 0) {
                test:assertFail("No message in the queue");
            }
            Message|Error messageReceived = receiverConnection3.receiveDeferredMessage(sequenceNumber);
            if (messageReceived is Message) {
                string messageRead = checkpanic messageReceived.getTextContent();
                log:printInfo("Reading Received Message : " + messageRead);
            } else {
                test:assertFail(msg = messageReceived.message());
            }
        } else {
            test:assertFail(msg = sequenceNumber.message());
        }
    } else {
        test:assertFail("Asb receiver connection creation failed.");
    }

    if (receiverConnection1 is ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 1.");
        checkpanic receiverConnection1.closeReceiverConnection();
    }

    if (receiverConnection2 is ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 2.");
        checkpanic receiverConnection2.closeReceiverConnection();
    }

    if (receiverConnection3 is ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 3.");
        checkpanic receiverConnection3.closeReceiverConnection();
    }
}

# Test Renew Lock on Message from queue operation
@test:Config {
    dependsOn: ["testSendToQueueOperation"], 
    enable: false
}
function testRenewLockOnMessageFromQueueOperation() {
    log:printInfo("Creating Asb receiver connection.");
    ReceiverConnection? receiverConnection = new ({connectionString: connectionString, entityPath: queuePath});

    if (receiverConnection is ReceiverConnection) {
        log:printInfo("Renew lock on message from Asb receiver connection.");
        checkpanic receiverConnection.renewLockOnMessage();
        log:printInfo("Done renewing a message.");
        log:printInfo("Completing messages from Asb receiver connection.");
        checkpanic receiverConnection.completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
    } else {
        test:assertFail("Asb receiver connection creation failed.");
    }

    if (receiverConnection is ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection.");
        checkpanic receiverConnection.closeReceiverConnection();
    }
}

# Test Renew Lock on Message from subscription operation
@test:Config {
    dependsOn: ["testSendToTopicOperation"], 
    enable: false
}
function testRenewLockOnMessageFromSubscriptionOperation() {
    log:printInfo("Creating Asb receiver connection.");
    ReceiverConnection? receiverConnection1 = new ({connectionString: connectionString, entityPath: subscriptionPath1});
    ReceiverConnection? receiverConnection2 = new ({connectionString: connectionString, entityPath: subscriptionPath2});
    ReceiverConnection? receiverConnection3 = new ({connectionString: connectionString, entityPath: subscriptionPath3});

    if (receiverConnection1 is ReceiverConnection) {
        log:printInfo("Renew lock on message from Asb receiver connection 1.");
        checkpanic receiverConnection1.renewLockOnMessage();
        log:printInfo("Done renewing a message.");
        log:printInfo("Completing messages from Asb receiver connection 1.");
        checkpanic receiverConnection1.completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
    } else {
        test:assertFail("Asb receiver connection creation failed.");
    }

    if (receiverConnection2 is ReceiverConnection) {
        log:printInfo("Renew lock on message from Asb receiver connection 2.");
        checkpanic receiverConnection2.renewLockOnMessage();
        log:printInfo("Done renewing a message.");
        log:printInfo("Completing messages from Asb receiver connection 2.");
        checkpanic receiverConnection2.completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
    } else {
        test:assertFail("Asb receiver connection creation failed.");
    }

    if (receiverConnection3 is ReceiverConnection) {
        log:printInfo("Renew lock on message from Asb receiver connection 3.");
        checkpanic receiverConnection3.renewLockOnMessage();
        log:printInfo("Done renewing a message.");
        log:printInfo("Completing messages from Asb receiver connection 3.");
        checkpanic receiverConnection3.completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
    } else {
        test:assertFail("Asb receiver connection creation failed.");
    }

    if (receiverConnection1 is ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 1.");
        checkpanic receiverConnection1.closeReceiverConnection();
    }

    if (receiverConnection2 is ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 2.");
        checkpanic receiverConnection2.closeReceiverConnection();
    }

    if (receiverConnection3 is ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 3.");
        checkpanic receiverConnection3.closeReceiverConnection();
    }
}

# Test prefetch count operation with prefetch disabled
@test:Config {
    enable: false
}
function testPrefetchCountWithPrefetchDisabled() {
    log:printInfo("Creating Asb sender connection.");
    SenderConnection? senderConnection = new ({connectionString: connectionString, entityPath: queuePath});

    if (senderConnection is SenderConnection) {
        int i = 1;
        while (i <= messageCount) {
            log:printInfo("Sending message " + i.toString() + " via Asb sender connection.");
            checkpanic senderConnection.sendMessageWithConfigurableParameters(byteContent, parameters1, properties);
            i = i + 1;
        }
    } else {
        test:assertFail("Asb sender connection creation failed.");
    }

    if (senderConnection is SenderConnection) {
        log:printInfo("Closing Asb sender connection.");
        checkpanic senderConnection.closeSenderConnection();
    }

    log:printInfo("Creating Asb receiver connection.");
    ReceiverConnection? receiverConnection = new ({connectionString: connectionString, entityPath: queuePath});

    if (receiverConnection is ReceiverConnection) {
        log:printInfo("Setting the prefetch count for the Asb receiver connection as : " 
            + prefetchCountDisabled.toString());
        checkpanic receiverConnection.setPrefetchCount(prefetchCountDisabled);

        time:Time time1 = time:currentTime();
        int startTimeMills = time1.time;
        int i = 1;
        while (i <= messageCount) {
            log:printInfo("Receiving message " + i.toString() + " from Asb receiver connection.");
            Message|Error messageReceived = receiverConnection.receiveMessage(serverWaitTime);
            if (messageReceived is Message) {
                string messageRead = checkpanic messageReceived.getTextContent();
                log:printInfo("Reading Received Message " + i.toString() + " : " + messageRead);
            } else {
                test:assertFail("Receiving message via Asb receiver connection failed.");
            }
            i = i + 1;
        }
        time:Time time2 = time:currentTime();
        int endTimeMills = time2.time;
        int timeElapsed = endTimeMills - startTimeMills;
        log:printInfo("Time elapsed : " + timeElapsed.toString() + " milliseconds");
    } else {
        test:assertFail("Asb receiver connection creation failed.");
    }

    if (receiverConnection is ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection.");
        checkpanic receiverConnection.closeReceiverConnection();
    }
}

# Test prefetch count operation with prefetch enabled
@test:Config {
    enable: false
}
function testPrefetchCountWithPrefetchEnabled() {
    log:printInfo("Creating Asb sender connection.");
    SenderConnection? senderConnection = new ({connectionString: connectionString, entityPath: queuePath});

    if (senderConnection is SenderConnection) {
        int i = 1;
        while (i <= messageCount) {
            log:printInfo("Sending message " + i.toString() + " via Asb sender connection.");
            checkpanic senderConnection.sendMessageWithConfigurableParameters(byteContent, parameters1, properties);
            i = i + 1;
        }
    } else {
        test:assertFail("Asb sender connection creation failed.");
    }

    if (senderConnection is SenderConnection) {
        log:printInfo("Closing Asb sender connection.");
        checkpanic senderConnection.closeSenderConnection();
    }

    log:printInfo("Creating Asb receiver connection.");
    ReceiverConnection? receiverConnection = new ({connectionString: connectionString, entityPath: queuePath});

    if (receiverConnection is ReceiverConnection) {
        log:printInfo("Setting the prefetch count for the Asb receiver connection as : " 
            + prefetchCountEnabled.toString());
        checkpanic receiverConnection.setPrefetchCount(prefetchCountEnabled);

        time:Time time1 = time:currentTime();
        int startTimeMills = time1.time;
        int i = 1;
        while (i <= messageCount) {
            log:printInfo("Receiving message " + i.toString() + " from Asb receiver connection.");
            Message|Error messageReceived = receiverConnection.receiveMessage(serverWaitTime);
            if (messageReceived is Message) {
                string messageRead = checkpanic messageReceived.getTextContent();
                log:printInfo("Reading Received Message " + i.toString() + " : " + messageRead);
            } else {
                test:assertFail("Receiving message via Asb receiver connection failed.");
            }
            i = i + 1;
        }
        time:Time time2 = time:currentTime();
        int endTimeMills = time2.time;
        int timeElapsed = endTimeMills - startTimeMills;
        log:printInfo("Time elapsed : " + timeElapsed.toString() + " milliseconds");
    } else {
        test:assertFail("Asb receiver connection creation failed.");
    }

    if (receiverConnection is ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection.");
        checkpanic receiverConnection.closeReceiverConnection();
    }
}

# Test prefetch count operation with variable loads
@test:Config {
    enable: false
}
function testSendAndReceiveMessagesWithVariableLoad() {
    log:printInfo("Creating Asb sender connection.");
    SenderConnection? senderConnection = new ({connectionString: connectionString, entityPath: queuePath});

    if (senderConnection is SenderConnection) {
        int i = 1;
        while (i <= variableMessageCount) {
            string stringContent = "This is My Message Body " + i.toString(); 
            byte[] byteContent = stringContent.toBytes();
            log:printInfo("Sending message " + i.toString() + " via Asb sender connection.");
            checkpanic senderConnection.sendMessageWithConfigurableParameters(byteContent, parameters4, properties);
            i = i + 1;
        }
    } else {
        test:assertFail("Asb sender connection creation failed.");
    }

    if (senderConnection is SenderConnection) {
        log:printInfo("Closing Asb sender connection.");
        checkpanic senderConnection.closeSenderConnection();
    }

    log:printInfo("Creating Asb receiver connection.");
    ReceiverConnection? receiverConnection = new ({connectionString: connectionString, entityPath: queuePath});

    if (receiverConnection is ReceiverConnection) {
        log:printInfo("Setting the prefetch count for the Asb receiver connection as : " 
            + prefetchCountDisabled.toString());
        checkpanic receiverConnection.setPrefetchCount(prefetchCountDisabled);

        time:Time time1 = time:currentTime();
        int startTimeMills = time1.time;
        int i = 1;
        while (i <= variableMessageCount) {
            log:printInfo("Receiving message " + i.toString() + " from Asb receiver connection.");
            Message|Error messageReceived = receiverConnection.receiveMessage(serverWaitTime);
            if (messageReceived is Message) {
                string messageRead = checkpanic messageReceived.getTextContent();
                log:printInfo("Reading Received Message " + i.toString() + " : " + messageRead);
            } else {
                test:assertFail("Receiving message via Asb receiver connection failed.");
            }
            i = i + 1;
        }
        time:Time time2 = time:currentTime();
        int endTimeMills = time2.time;
        int timeElapsed = endTimeMills - startTimeMills;
        log:printInfo("Time elapsed : " + timeElapsed.toString() + " milliseconds");
    } else {
        test:assertFail("Asb receiver connection creation failed.");
    }

    if (receiverConnection is ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection.");
        checkpanic receiverConnection.closeReceiverConnection();
    }
}

# Test prefetch count operation with variable loads using different workers
@test:Config {
    enable: true
}
function testSendAndReceiveMessagesWithVariableLoadUsingWorkers() {
    int variableMessageCount = 5;
    BasicProperties properties = {replyTo: "propertyValue1", contentType: "propertyValue2", 
        contentEncoding: "propertyValue3", correlationId: "propertyValue4"};
    log:printInfo("Worker execution started");
    worker w1 {
        log:printInfo("Creating Asb sender connection.");
        SenderConnection? senderConnection = new ({connectionString: connectionString, entityPath: queuePath});

        if (senderConnection is SenderConnection) {
            int i = 1;
            while (i <= variableMessageCount) {
                runtime:sleep(5000);
                string stringContent = "This is My Message Body " + i.toString(); 
                byte[] byteContent = stringContent.toBytes();
                log:printInfo("Sending message " + i.toString() + " via Asb sender connection.");
                checkpanic senderConnection.sendMessageWithConfigurableParameters(byteContent, parameters4, properties);
                i = i + 1;
            }
        } else {
            test:assertFail("Asb sender connection creation failed.");
        }

        if (senderConnection is SenderConnection) {
            log:printInfo("Closing Asb sender connection.");
            checkpanic senderConnection.closeSenderConnection();
        }
    }

    worker w2 {
        log:printInfo("Creating Asb receiver connection.");
        ReceiverConnection? receiverConnection = new ({connectionString: connectionString, entityPath: queuePath});

        if (receiverConnection is ReceiverConnection) {
            log:printInfo("Setting the prefetch count for the Asb receiver connection as : " 
                + prefetchCountDisabled.toString());
            checkpanic receiverConnection.setPrefetchCount(prefetchCountDisabled);

            time:Time time1 = time:currentTime();
            int startTimeMills = time1.time;
            int i = 1;
            while (i <= variableMessageCount) {
                runtime:sleep(10000);
                log:printInfo("Receiving message " + i.toString() + " from Asb receiver connection.");
                Message|Error messageReceived = receiverConnection.receiveMessage(serverWaitTime);
                if (messageReceived is Message && messageReceived.getMessageContentType() == "application/text") {
                    string messageRead = checkpanic messageReceived.getTextContent();
                    log:printInfo("Reading Received Message " + i.toString() + " : " + messageRead);
                    var messageProperties = messageReceived.getProperties();
                    if(messageProperties is BasicProperties) {
                        log:printInfo("Reading Message Properties " + i.toString() + " : " 
                            + messageProperties.toString());
                    }
                } else {
                    test:assertFail("Receiving message via Asb receiver connection failed.");
                }
                i = i + 1;
            }
            time:Time time2 = time:currentTime();
            int endTimeMills = time2.time;
            int timeElapsed = endTimeMills - startTimeMills;
            log:printInfo("Time elapsed : " + timeElapsed.toString() + " milliseconds");
        } else {
            test:assertFail("Asb receiver connection creation failed.");
        }

        if (receiverConnection is ReceiverConnection) {
            log:printInfo("Closing Asb receiver connection.");
            checkpanic receiverConnection.closeReceiverConnection();
        }
    }

    _ = wait {w1, w2};
    log:printInfo("Worker execution finished");
}

# After Suite Function
@test:AfterSuite {}
function afterSuiteFunc() {
    SenderConnection? con = senderConnection;
    if (con is SenderConnection) {
        log:printInfo("Closing the Sender Connection");
        checkpanic con.closeSenderConnection();
    }

    ReceiverConnection? rec = receiverConnection;
    if (rec is ReceiverConnection) {
        log:printInfo("Closing the Receiver Connection");
        checkpanic rec.closeReceiverConnection();
    }
}

# Get configuration value for the given key from ballerina.conf file.
# 
# + return - configuration value of the given key as a string
function getConfigValue(string key) returns string {
    return (system:getEnv(key) != "") ? system:getEnv(key) : config:getAsString(key);
}
