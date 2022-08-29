// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

// Constants
const string SUBSCRIPTIONS = "/subscriptions/";

// Connection Configurations
configurable string connectionString = os:getEnv("CONNECTION_STRING");
configurable string queueName = os:getEnv("QUEUE_NAME");
configurable string topicName = os:getEnv("TOPIC_NAME");
configurable string subscriptionName1 = os:getEnv("SUBSCRIPTION_NAME1");
configurable string subscriptionName2 = os:getEnv("SUBSCRIPTION_NAME2");
configurable string subscriptionName3 = os:getEnv("SUBSCRIPTION_NAME3");
string subscriptionPath1 = topicName + SUBSCRIPTIONS + subscriptionName1;
string subscriptionPath2 = topicName + SUBSCRIPTIONS + subscriptionName2;
string subscriptionPath3 = topicName + SUBSCRIPTIONS + subscriptionName3;

// Input values
string stringContent = "This is My Message Body"; 
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
function testSendAndReceiveMessageFromQueueOperation() returns error? {
    log:printInfo("[[testSendAndReceiveMessageFromQueueOperation]]");

    log:printInfo("Creating Asb message sender.");
    MessageSender messageSender = check new (connectionString, queueName);

    log:printInfo("Creating Asb message receiver.");
    MessageReceiver messageReceiver = check new (connectionString, queueName);

    log:printInfo("Sending via Asb sender.");
    check messageSender->send(message1);

    log:printInfo("Receiving from Asb receiver client.");
    Message|Error? messageReceived = messageReceiver->receive(serverWaitTime);

    if (messageReceived is Message) {
        var result = check messageReceiver->complete(messageReceived);
        test:assertEquals(result, (), msg = "Complete message not succesful.");
    } else if (messageReceived is ()) {
        test:assertFail("No message in the queue.");
    } else {
        test:assertFail("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender client.");
    check messageSender->close();

    log:printInfo("Closing Asb receiver client.");
    check messageReceiver->close();
}

@test:Config { 
    groups: ["asb"],
    dependsOn: [testSendAndReceiveMessageFromQueueOperation],
    enable: true
}
function testSendAndReceiveBatchFromQueueOperation() returns error? {
    log:printInfo("[[testSendAndReceiveBatchFromQueueOperation]]");

    log:printInfo("Initializing Asb sender client.");
    MessageSender messageSender = check new (connectionString, queueName);

    log:printInfo("Initializing Asb receiver client.");
    MessageReceiver messageReceiver = check new (connectionString, queueName, RECEIVEANDDELETE);

    log:printInfo("Sending via Asb sender.");
    check messageSender->sendBatch(messages);

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
    check messageSender->close();

    log:printInfo("Closing Asb receiver.");
    check messageReceiver->close();
}

@test:Config { 
    groups: ["asb"],
    dependsOn: [testSendAndReceiveBatchFromQueueOperation],
    enable: true
}
function testCompleteMessageFromQueueOperation() returns error? {
    log:printInfo("[[testCompleteMessageFromQueueOperation]]");

    log:printInfo("Initializing Asb sender client.");
    MessageSender messageSender = check new (connectionString, queueName);

    log:printInfo("Initializing Asb receiver client.");
    MessageReceiver messageReceiver = check new (connectionString, queueName, PEEKLOCK);

    log:printInfo("Sending via Asb sender.");
    check messageSender->send(message1);

    log:printInfo("Receiving from Asb receiver client.");
    Message|Error? messageReceived = messageReceiver->receive(serverWaitTime);

    if (messageReceived is Message) {
        var result = check messageReceiver->complete(messageReceived);
        test:assertEquals(result, (), msg = "Complete message not succesful.");
    } else if (messageReceived is ()) {
        test:assertFail("No message in the queue.");
    } else {
        test:assertFail("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender client.");
    check messageSender->close();

    log:printInfo("Closing Asb receiver client.");
    check messageSender->close();
}

@test:Config { 
    groups: ["asb"],
    dependsOn: [testCompleteMessageFromQueueOperation],
    enable: true
}
function testAbandonMessageFromQueueOperation() returns error? {
    log:printInfo("[[testAbandonMessageFromQueueOperation]]");

    log:printInfo("Initializing Asb sender client.");
    MessageSender messageSender = check new (connectionString, queueName);

    log:printInfo("Initializing Asb receiver client.");
    MessageReceiver messageReceiver = check new (connectionString, queueName, PEEKLOCK);

    log:printInfo("Sending via Asb sender.");
    check messageSender->send(message1);

    log:printInfo("Receiving from Asb receiver.");
    Message|Error? messageReceived = messageReceiver->receive(serverWaitTime);

    if (messageReceived is Message) {
        var result = check messageReceiver->abandon(messageReceived);
        test:assertEquals(result, (), msg = "Abandon message not succesful.");
        Message|Error? messageReceivedAgain = messageReceiver->receive(serverWaitTime);
        if (messageReceivedAgain is Message) {
            test:assertEquals(messageReceivedAgain?.messageId, messageReceived?.messageId, 
                msg = "Abandon message not succesful.");
            check messageReceiver->complete(messageReceivedAgain);
        } else {
            test:assertFail("Abandon message not succesful.");
        }
    } else if (messageReceived is ()) {
        test:assertFail("No message in the queue.");
    } else {
        test:assertFail("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender.");
    check messageSender->close();

    log:printInfo("Closing Asb receiver.");
    check messageReceiver->close();
}

@test:Config { 
    groups: ["asb"],
    dependsOn: [testAbandonMessageFromQueueOperation],
    enable: true
}
function testDeadletterMessageFromQueueOperation() returns error? {
    log:printInfo("[[testDeadletterMessageFromQueueOperation]]");

    log:printInfo("Initializing Asb sender.");
    MessageSender messageSender = check new (connectionString, queueName);

    log:printInfo("Initializing Asb receiver.");
    MessageReceiver messageReceiver = check new (connectionString, queueName, PEEKLOCK);

    log:printInfo("Sending via Asb sender.");
    check messageSender->send(message1);

    log:printInfo("Receiving from Asb receiver.");
    Message|Error? messageReceived = messageReceiver->receive(serverWaitTime);

    if (messageReceived is Message) {
        var result = check messageReceiver->deadLetter(messageReceived);
        test:assertEquals(result, (), msg = "Deadletter message not succesful.");
    } else if (messageReceived is ()) {
        test:assertFail("No message in the queue.");
    } else {
        test:assertFail("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender.");
    check messageSender->close();

    log:printInfo("Closing Asb receiver.");
    check messageReceiver->close();
}

@test:Config { 
    groups: ["asb"],
    dependsOn: [testDeadletterMessageFromQueueOperation],
    enable: true
}
function testDeferMessageFromQueueOperation() returns error? {
    log:printInfo("[[testDeferMessageFromQueueOperation]]");

    log:printInfo("Initializing Asb sender.");
    MessageSender messageSender = check new (connectionString, queueName);

    log:printInfo("Initializing Asb receiver.");
    MessageReceiver messageReceiver = check new (connectionString, queueName, PEEKLOCK);

    log:printInfo("Sending via Asb sender.");
    check messageSender->send(message1);

    log:printInfo("Receiving from Asb receiver.");
    Message|Error? messageReceived = messageReceiver->receive(serverWaitTime);

    if (messageReceived is Message) {
        int result = check messageReceiver->defer(messageReceived);
        test:assertNotEquals(result, 0, msg = "Defer message not succesful.");
        Message|Error? messageReceivedAgain = check messageReceiver->receiveDeferred(result);
        if (messageReceivedAgain is Message) {
            test:assertEquals(messageReceivedAgain?.messageId, messageReceived?.messageId, 
                msg = "Receiving deferred message not succesful.");
            check messageReceiver->complete(messageReceivedAgain);
        }
    } else if (messageReceived is ()) {
        test:assertFail("No message in the queue.");
    } else {
        test:assertFail("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender client.");
    check messageSender->close();

    log:printInfo("Closing Asb receiver client.");
    check messageReceiver->close();
}

@test:Config { 
    groups: ["asb"],
    dependsOn: [testDeferMessageFromQueueOperation],
    enable: true
}
function testSendAndReceiveMessageFromSubscriptionOperation() returns error? {
    log:printInfo("[[testSendAndReceiveMessageFromSubscriptionOperation]]");

    log:printInfo("Initializing Asb sender client.");
    MessageSender topicSender = check new (connectionString, topicName);

    log:printInfo("Initializing Asb receiver client.");
    MessageReceiver subscriptionReceiver = check new (connectionString, subscriptionPath1, RECEIVEANDDELETE);

    log:printInfo("Sending via Asb sender client.");
    check topicSender->send(message1);

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
    check topicSender->close();

    log:printInfo("Closing Asb receiver client.");
    check subscriptionReceiver->close();
}

@test:Config { 
    groups: ["asb"],
    dependsOn: [testSendAndReceiveMessageFromSubscriptionOperation],
    enable: true
}
function testSendAndReceiveBatchFromSubscriptionOperation() returns error? {
    log:printInfo("[[testSendAndReceiveBatchFromSubscriptionOperation]]");

    log:printInfo("Initializing Asb sender client.");
    MessageSender topicSender = check new (connectionString, topicName);

    log:printInfo("Initializing Asb receiver client.");
    MessageReceiver subscriptionReceiver = check new (connectionString, subscriptionPath1, RECEIVEANDDELETE);

    log:printInfo("Sending via Asb sender client.");
    check topicSender->sendBatch(messages);

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
    check topicSender->close();

    log:printInfo("Closing Asb receiver client.");
    check subscriptionReceiver->close();
}

@test:Config { 
    groups: ["asb"],
    dependsOn: [testSendAndReceiveBatchFromSubscriptionOperation],
    enable: true
}
function testCompleteMessageFromSubscriptionOperation() returns error? {
    log:printInfo("[[testCompleteMessageFromSubscriptionOperation]]");

    log:printInfo("Initializing Asb sender client.");
    MessageSender topicSender = check new (connectionString, topicName);

    log:printInfo("Initializing Asb receiver client.");
    MessageReceiver subscriptionReceiver = check new (connectionString, subscriptionPath1, PEEKLOCK);

    log:printInfo("Sending via Asb sender client.");
    check topicSender->send(message1);

    log:printInfo("Receiving from Asb receiver client.");
    Message|Error? messageReceived = subscriptionReceiver->receive(serverWaitTime);

    if (messageReceived is Message) {
        var result = check subscriptionReceiver->complete(messageReceived);
        test:assertEquals(result, (), msg = "Complete message not succesful.");
    } else if (messageReceived is ()) {
        test:assertFail("No message in the subscription.");
    } else {
        test:assertFail("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender client.");
    check topicSender->close();

    log:printInfo("Closing Asb receiver client.");
    check subscriptionReceiver->close();
}

@test:Config { 
    groups: ["asb"],
    dependsOn: [testCompleteMessageFromSubscriptionOperation],
    enable: true
}
function testAbandonMessageFromSubscriptionOperation() returns error? {
    log:printInfo("[[testAbandonMessageFromSubscriptionOperation]]");

    log:printInfo("Initializing Asb sender client.");
    MessageSender topicSender = check new (connectionString, topicName);

    log:printInfo("Initializing Asb receiver client.");
    MessageReceiver subscriptionReceiver = check new (connectionString, subscriptionPath1, PEEKLOCK);

    log:printInfo("Sending via Asb sender client.");
    check topicSender->send(message1);

    log:printInfo("Receiving from Asb receiver client.");
    Message|Error? messageReceived = subscriptionReceiver->receive(serverWaitTime);

    if (messageReceived is Message) {
        var result = check subscriptionReceiver->abandon(messageReceived);
        test:assertEquals(result, (), msg = "Abandon message not succesful.");
        Message|Error? messageReceivedAgain = subscriptionReceiver->receive(serverWaitTime);
        if (messageReceivedAgain is Message) {
            check subscriptionReceiver->complete(messageReceivedAgain);
        } else {
            test:assertFail("Abandon message not succesful.");
        }
    } else if (messageReceived is ()) {
        test:assertFail("No message in the subscription.");
    } else {
        test:assertFail("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender client.");
    check topicSender->close();

    log:printInfo("Closing Asb receiver client.");
    check subscriptionReceiver->close();
}

@test:Config { 
    groups: ["asb"],
    dependsOn: [testAbandonMessageFromSubscriptionOperation],
    enable: true
}
function testDeadletterMessageFromSubscriptionOperation() returns error? {
    log:printInfo("[[testDeadletterMessageFromSubscriptionOperation]]");

    log:printInfo("Initializing Asb sender client.");
    MessageSender topicSender = check new (connectionString, topicName);

    log:printInfo("Initializing Asb receiver client.");
    MessageReceiver subscriptionReceiver = check new (connectionString, subscriptionPath1, PEEKLOCK);

    log:printInfo("Sending via Asb sender client.");
    check topicSender->send(message1);

    log:printInfo("Receiving from Asb receiver client.");
    Message|Error? messageReceived = subscriptionReceiver->receive(serverWaitTime);

    if (messageReceived is Message) {
        var result = check subscriptionReceiver->deadLetter(messageReceived);
        test:assertEquals(result, (), msg = "Deadletter message not succesful.");
    } else if (messageReceived is ()) {
        test:assertFail("No message in the subscription.");
    } else {
        test:assertFail("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender client.");
    check topicSender->close();

    log:printInfo("Closing Asb receiver client.");
    check subscriptionReceiver->close();
}

@test:Config { 
    groups: ["asb"],
    dependsOn: [testDeadletterMessageFromSubscriptionOperation],
    enable: true
}
function testDeferMessageFromSubscriptionOperation() returns error? {
    log:printInfo("[[testDeferMessageFromSubscriptionOperation]]");

    log:printInfo("Initializing Asb sender client.");
    MessageSender topicSender = check new (connectionString, topicName);

    log:printInfo("Initializing Asb receiver client.");
    MessageReceiver subscriptionReceiver = check new (connectionString, subscriptionPath1, PEEKLOCK);

    log:printInfo("Sending via Asb sender client.");
    check topicSender->send(message1);

    log:printInfo("Receiving from Asb receiver client.");
    Message|Error? messageReceived = subscriptionReceiver->receive(serverWaitTime);

    if (messageReceived is Message) {
        int result = check subscriptionReceiver->defer(messageReceived);
        test:assertNotEquals(result, 0, msg = "Defer message not succesful.");
        Message|Error? messageReceivedAgain = check subscriptionReceiver->receiveDeferred(result);
        if (messageReceivedAgain is Message) {
            test:assertEquals(messageReceivedAgain?.messageId, messageReceived?.messageId, 
                msg = "Receiving deferred message not succesful.");
            check subscriptionReceiver->complete(messageReceivedAgain);
        }
    } else if (messageReceived is ()) {
        test:assertFail("No message in the queue.");
    } else {
        test:assertFail("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender client.");
    check topicSender->close();

    log:printInfo("Closing Asb receiver client.");
    check subscriptionReceiver->close();
}
