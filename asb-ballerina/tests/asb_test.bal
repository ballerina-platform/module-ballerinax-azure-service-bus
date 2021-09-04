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

import ballerina/log;
import ballerina/os;
import ballerina/test;

// Connection Configurations
configurable string connectionString = os:getEnv("CONNECTION_STRING");
configurable string queueName = os:getEnv("QUEUE_NAME");
configurable string topicName = os:getEnv("TOPIC_NAME");
configurable string subscriptionPath1 = os:getEnv("SUBSCRIPTION_PATH1");
configurable string subscriptionPath2 = os:getEnv("SUBSCRIPTION_PATH2");
configurable string subscriptionPath3 = os:getEnv("SUBSCRIPTION_PATH3");

// Input values
string stringContent = "[ASB] This is My Message Body"; 
byte[] byteContent = stringContent.toBytes();
json jsonContent = {name: "wso2", color: "orange", price: 5.36};
byte[] byteContentFromJson = jsonContent.toJsonString().toBytes();
map<string> properties = {a: "propertyValue1", b: "propertyValue2"};
int timeToLive = 60; // In seconds
int serverWaitTime = 60; // In seconds
int maxMessageCount = 2;

ApplicationProperties applicationProperties = {
    properties: properties
};

Message message1 = {
    body: byteContent,
    contentType: TEXT,
    timeToLive: timeToLive
};

Message message2 = {
    body: byteContent,
    contentType: TEXT,
    timeToLive: timeToLive
};

MessageBatch messages = {
    messageCount: 2,
    messages: [message1, message2]
};

AsbConnectionConfiguration config = {
    connectionString: connectionString
};

@test:Config { 
    groups: ["asb"],
    enable: true
}
function testSendAndReceiveMessageFromQueueOperation() {
    log:printInfo("[[testSendAndReceiveMessageFromQueueOperation]]");

    log:printInfo("Creating Asb message sender.");
    MessageSender messageSender = checkpanic new (connectionString, queueName);
    
    log:printInfo("Creating Asb message receiver.");
    MessageReceiver messageReceiver = checkpanic new (connectionString, queueName);

    log:printInfo("Sending via Asb sender.");
    checkpanic messageSender->send(message1);

    log:printInfo("Receiving from Asb receiver client.");
    Message|Error? messageReceived = messageReceiver->receive(serverWaitTime);

    if (messageReceived is Message) {
        var result = checkpanic messageReceiver->complete(messageReceived);
        test:assertEquals(result, (), msg = "Complete message not succesful.");
    } else if (messageReceived is ()) {
        test:assertFail("No message in the queue.");
    } else {
        test:assertFail("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender client.");
    checkpanic messageSender->close();

    log:printInfo("Closing Asb receiver client.");
    checkpanic messageReceiver->close();
}

@test:Config { 
    groups: ["asb"],
    dependsOn: [testSendAndReceiveMessageFromQueueOperation],
    enable: true
}
function testSendAndReceiveBatchFromQueueOperation() {
    log:printInfo("[[testSendAndReceiveBatchFromQueueOperation]]");

    log:printInfo("Initializing Asb sender client.");
    MessageSender messageSender = checkpanic new (connectionString, queueName);

    log:printInfo("Initializing Asb receiver client.");
    MessageReceiver messageReceiver = checkpanic new (connectionString, queueName, RECEIVEANDDELETE);

    log:printInfo("Sending via Asb sender.");
    checkpanic messageSender->sendBatch(messages);

    log:printInfo("Receiving from Asb receiver.");
    MessageBatch|Error? messageReceived = messageReceiver->receiveBatch(maxMessageCount);

    if (messageReceived is MessageBatch) {
        log:printInfo(messageReceived.toString());
        foreach Message message in messageReceived.messages {
            if (message.toString() != "") {
                test:assertEquals(message.body, stringContent, msg = "Sent & recieved message not equal.");
            }
        }
    } else if (messageReceived is ()) {
        test:assertFail("No message in the queue.");
    } else {
        test:assertFail("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender. ");
    checkpanic messageSender->close();

    log:printInfo("Closing Asb receiver.");
    checkpanic messageReceiver->close();
}

@test:Config { 
    groups: ["asb"],
    dependsOn: [testSendAndReceiveBatchFromQueueOperation],
    enable: true
}
function testCompleteMessageFromQueueOperation() { 
    log:printInfo("[[testCompleteMessageFromQueueOperation]]");

    log:printInfo("Initializing Asb sender client.");
    MessageSender messageSender = checkpanic new (connectionString, queueName);

    log:printInfo("Initializing Asb receiver client.");
    MessageReceiver messageReceiver = checkpanic new (connectionString, queueName, PEEKLOCK);

    log:printInfo("Sending via Asb sender.");
    checkpanic messageSender->send(message1);

    log:printInfo("Receiving from Asb receiver client.");
    Message|Error? messageReceived = messageReceiver->receive(serverWaitTime);

    if (messageReceived is Message) {
        var result = checkpanic messageReceiver->complete(messageReceived);
        test:assertEquals(result, (), msg = "Complete message not succesful.");
    } else if (messageReceived is ()) {
        test:assertFail("No message in the queue.");
    } else {
        test:assertFail("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender client.");
    checkpanic messageSender->close();

    log:printInfo("Closing Asb receiver client.");
    checkpanic messageSender->close();
}

@test:Config { 
    groups: ["asb"],
    dependsOn: [testCompleteMessageFromQueueOperation],
    enable: true
}
function testAbandonMessageFromQueueOperation() {
    log:printInfo("[[testAbandonMessageFromQueueOperation]]");

    log:printInfo("Initializing Asb sender client.");
    MessageSender messageSender = checkpanic new (connectionString, queueName);

    log:printInfo("Initializing Asb receiver client.");
    MessageReceiver messageReceiver = checkpanic new (connectionString, queueName, PEEKLOCK);

    log:printInfo("Sending via Asb sender.");
    checkpanic messageSender->send(message1);

    log:printInfo("Receiving from Asb receiver.");
    Message|Error? messageReceived = messageReceiver->receive(serverWaitTime);

    if (messageReceived is Message) {
        var result = checkpanic messageReceiver->abandon(messageReceived);
        test:assertEquals(result, (), msg = "Abandon message not succesful.");
        Message|Error? messageReceivedAgain = messageReceiver->receive(serverWaitTime);
        if (messageReceivedAgain is Message) {
            test:assertEquals(messageReceivedAgain?.messageId, messageReceived?.messageId, 
                msg = "Abandon message not succesful.");
            checkpanic messageReceiver->complete(messageReceivedAgain);
        } else {
            test:assertFail("Abandon message not succesful.");
        }
    } else if (messageReceived is ()) {
        test:assertFail("No message in the queue.");
    } else {
        test:assertFail("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender.");
    checkpanic messageSender->close();

    log:printInfo("Closing Asb receiver.");
    checkpanic messageReceiver->close();
}

@test:Config { 
    groups: ["asb"],
    dependsOn: [testAbandonMessageFromQueueOperation],
    enable: true
}
function testDeadletterMessageFromQueueOperation() {
    log:printInfo("[[testDeadletterMessageFromQueueOperation]]");

    log:printInfo("Initializing Asb sender.");
    MessageSender messageSender = checkpanic new (connectionString, queueName);

    log:printInfo("Initializing Asb receiver.");
    MessageReceiver messageReceiver = checkpanic new (connectionString, queueName, PEEKLOCK);

    log:printInfo("Sending via Asb sender.");
    checkpanic messageSender->send(message1);

    log:printInfo("Receiving from Asb receiver.");
    Message|Error? messageReceived = messageReceiver->receive(serverWaitTime);

    if (messageReceived is Message) {
        var result = checkpanic messageReceiver->deadLetter(messageReceived);
        test:assertEquals(result, (), msg = "Deadletter message not succesful.");
    } else if (messageReceived is ()) {
        test:assertFail("No message in the queue.");
    } else {
        test:assertFail("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender.");
    checkpanic messageSender->close();

    log:printInfo("Closing Asb receiver.");
    checkpanic messageReceiver->close();
}

@test:Config { 
    groups: ["asb"],
    dependsOn: [testDeadletterMessageFromQueueOperation],
    enable: true
}
function testDeferMessageFromQueueOperation() {
    log:printInfo("[[testDeferMessageFromQueueOperation]]");

    log:printInfo("Initializing Asb sender.");
    MessageSender messageSender = checkpanic new (connectionString, queueName);

    log:printInfo("Initializing Asb receiver.");
    MessageReceiver messageReceiver = checkpanic new (connectionString, queueName, PEEKLOCK);

    log:printInfo("Sending via Asb sender.");
    checkpanic messageSender->send(message1);

    log:printInfo("Receiving from Asb receiver.");
    Message|Error? messageReceived = messageReceiver->receive(serverWaitTime);

    if (messageReceived is Message) {
        int result = checkpanic messageReceiver->defer(messageReceived);
        test:assertNotEquals(result, 0, msg = "Defer message not succesful.");
        Message|Error? messageReceivedAgain = checkpanic messageReceiver->receiveDeferred(result);
        if (messageReceivedAgain is Message) {
            test:assertEquals(messageReceivedAgain?.messageId, messageReceived?.messageId, 
                msg = "Receiving deferred message not succesful.");
            checkpanic messageReceiver->complete(messageReceivedAgain);
        }
    } else if (messageReceived is ()) {
        test:assertFail("No message in the queue.");
    } else {
        test:assertFail("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender client.");
    checkpanic messageSender->close();

    log:printInfo("Closing Asb receiver client.");
    checkpanic messageReceiver->close();
}

@test:Config { 
    groups: ["asb"],
    dependsOn: [testDeferMessageFromQueueOperation],
    enable: true
}
function testSendAndReceiveMessageFromSubscriptionOperation() {
    log:printInfo("[[testSendAndReceiveMessageFromSubscriptionOperation]]");

    log:printInfo("Initializing Asb sender client.");
    MessageSender topicSender = checkpanic new (connectionString, topicName);

    log:printInfo("Initializing Asb receiver client.");
    MessageReceiver subscriptionReceiver = checkpanic new (connectionString, subscriptionPath1, RECEIVEANDDELETE);

    log:printInfo("Sending via Asb sender client.");
    checkpanic topicSender->send(message1);

    log:printInfo("Receiving from Asb receiver client.");
    Message|Error? messageReceived = subscriptionReceiver->receive(serverWaitTime);

    if (messageReceived is Message) {
        test:assertEquals(messageReceived.body, stringContent, msg = "Sent & recieved message not equal.");
    } else if (messageReceived is ()) {
        test:assertFail("No message in the subscription.");
    } else {
        test:assertFail("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender client.");
    checkpanic topicSender->close();

    log:printInfo("Closing Asb receiver client.");
    checkpanic subscriptionReceiver->close();
}

@test:Config { 
    groups: ["asb"],
    dependsOn: [testSendAndReceiveMessageFromSubscriptionOperation],
    enable: true
}
function testSendAndReceiveBatchFromSubscriptionOperation() {
    log:printInfo("[[testSendAndReceiveBatchFromSubscriptionOperation]]");

    log:printInfo("Initializing Asb sender client.");
    MessageSender topicSender = checkpanic new(connectionString, topicName);

    log:printInfo("Initializing Asb receiver client.");
    MessageReceiver subscriptionReceiver = checkpanic new(connectionString, subscriptionPath1, RECEIVEANDDELETE);

    log:printInfo("Sending via Asb sender client.");
    checkpanic topicSender->sendBatch(messages);

    log:printInfo("Receiving from Asb receiver client.");
    MessageBatch|Error? messageReceived = subscriptionReceiver->receiveBatch(maxMessageCount, serverWaitTime);

    if (messageReceived is MessageBatch) {
        foreach Message message in messageReceived.messages {
            if (message.toString() != "") {
                test:assertEquals(message.body, stringContent, msg = "Sent & recieved message not equal.");
            }
        }
    } else if (messageReceived is ()) {
        test:assertFail("No message in the subscription.");
    } else {
        test:assertFail("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender client.");
    checkpanic topicSender->close();

    log:printInfo("Closing Asb receiver client.");
    checkpanic subscriptionReceiver->close();
}

@test:Config { 
    groups: ["asb"],
    dependsOn: [testSendAndReceiveBatchFromSubscriptionOperation],
    enable: true
}
function testCompleteMessageFromSubscriptionOperation() {
    log:printInfo("[[testCompleteMessageFromSubscriptionOperation]]");

    log:printInfo("Initializing Asb sender client.");
    MessageSender topicSender = checkpanic new(connectionString, topicName);

    log:printInfo("Initializing Asb receiver client.");
    MessageReceiver subscriptionReceiver = checkpanic new(connectionString, subscriptionPath1, PEEKLOCK);

    log:printInfo("Sending via Asb sender client.");
    checkpanic topicSender->send(message1);

    log:printInfo("Receiving from Asb receiver client.");
    Message|Error? messageReceived = subscriptionReceiver->receive(serverWaitTime);

    if (messageReceived is Message) {
        var result = checkpanic subscriptionReceiver->complete(messageReceived);
        test:assertEquals(result, (), msg = "Complete message not succesful.");
    } else if (messageReceived is ()) {
        test:assertFail("No message in the subscription.");
    } else {
        test:assertFail("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender client.");
    checkpanic topicSender->close();

    log:printInfo("Closing Asb receiver client.");
    checkpanic subscriptionReceiver->close();
}

@test:Config { 
    groups: ["asb"],
    dependsOn: [testCompleteMessageFromSubscriptionOperation],
    enable: true
}
function testAbandonMessageFromSubscriptionOperation() {
    log:printInfo("[[testAbandonMessageFromSubscriptionOperation]]");

    log:printInfo("Initializing Asb sender client.");
    MessageSender topicSender = checkpanic new(connectionString, topicName);

    log:printInfo("Initializing Asb receiver client.");
    MessageReceiver subscriptionReceiver = checkpanic new(connectionString, subscriptionPath1, PEEKLOCK);

    log:printInfo("Sending via Asb sender client.");
    checkpanic topicSender->send(message1);

    log:printInfo("Receiving from Asb receiver client.");
    Message|Error? messageReceived = subscriptionReceiver->receive(serverWaitTime);

    if (messageReceived is Message) {
        var result = checkpanic subscriptionReceiver->abandon(messageReceived);
        test:assertEquals(result, (), msg = "Abandon message not succesful.");
        Message|Error? messageReceivedAgain = subscriptionReceiver->receive(serverWaitTime);
        if (messageReceivedAgain is Message) {
            checkpanic subscriptionReceiver->complete(messageReceivedAgain);
        } else {
            test:assertFail("Abandon message not succesful.");
        }
    } else if (messageReceived is ()) {
        test:assertFail("No message in the subscription.");
    } else {
        test:assertFail("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender client.");
    checkpanic topicSender->close();

    log:printInfo("Closing Asb receiver client.");
    checkpanic subscriptionReceiver->close();
}

@test:Config { 
    groups: ["asb"],
    dependsOn: [testAbandonMessageFromSubscriptionOperation],
    enable: true
}
function testDeadletterMessageFromSubscriptionOperation() {
    log:printInfo("[[testDeadletterMessageFromSubscriptionOperation]]");

    log:printInfo("Initializing Asb sender client.");
    MessageSender topicSender = checkpanic new(connectionString, topicName);

    log:printInfo("Initializing Asb receiver client.");
    MessageReceiver subscriptionReceiver = checkpanic new(connectionString, subscriptionPath1, PEEKLOCK);

    log:printInfo("Sending via Asb sender client.");
    checkpanic topicSender->send(message1);

    log:printInfo("Receiving from Asb receiver client.");
    Message|Error? messageReceived = subscriptionReceiver->receive(serverWaitTime);

    if (messageReceived is Message) {
        var result = checkpanic subscriptionReceiver->deadLetter(messageReceived);
        test:assertEquals(result, (), msg = "Deadletter message not succesful.");
    } else if (messageReceived is ()) {
        test:assertFail("No message in the subscription.");
    } else {
        test:assertFail("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender client.");
    checkpanic topicSender->close();

    log:printInfo("Closing Asb receiver client.");
    checkpanic subscriptionReceiver->close();
}

@test:Config { 
    groups: ["asb"],
    dependsOn: [testDeadletterMessageFromSubscriptionOperation],
    enable: true
}
function testDeferMessageFromSubscriptionOperation() {
    log:printInfo("[[testDeferMessageFromSubscriptionOperation]]");

    log:printInfo("Initializing Asb sender client.");
    MessageSender topicSender = checkpanic new(connectionString, topicName);

    log:printInfo("Initializing Asb receiver client.");
    MessageReceiver subscriptionReceiver = checkpanic new(connectionString, subscriptionPath1, PEEKLOCK);

    log:printInfo("Sending via Asb sender client.");
    checkpanic topicSender->send(message1);

    log:printInfo("Receiving from Asb receiver client.");
    Message|Error? messageReceived = subscriptionReceiver->receive(serverWaitTime);

    if (messageReceived is Message) {
        int result = checkpanic subscriptionReceiver->defer(messageReceived);
        test:assertNotEquals(result, 0, msg = "Defer message not succesful.");
        Message|Error? messageReceivedAgain = checkpanic subscriptionReceiver->receiveDeferred(result);
        if (messageReceivedAgain is Message) {
            test:assertEquals(messageReceivedAgain?.messageId, messageReceived?.messageId, 
                msg = "Receiving deferred message not succesful.");
            checkpanic subscriptionReceiver->complete(messageReceivedAgain);
        }
    } else if (messageReceived is ()) {
        test:assertFail("No message in the queue.");
    } else {
        test:assertFail("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender client.");
    checkpanic topicSender->close();

    log:printInfo("Closing Asb receiver client.");
    checkpanic subscriptionReceiver->close();
}
