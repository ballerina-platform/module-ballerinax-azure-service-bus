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
configurable string subscriptionName1 = os:getEnv("SUBSCRIPTION_NAME1");
configurable string subscriptionName2 = os:getEnv("SUBSCRIPTION_NAME2");
configurable string subscriptionName3 = os:getEnv("SUBSCRIPTION_NAME3");

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
function testSendAndReceiveMessageFromQueueOperation() {
    log:printInfo("[[testSendAndReceiveMessageFromQueueOperation]]");
    AsbClient asbClient = new (config);

    log:printInfo("Creating Asb sender connection.");
    handle queueSender = checkpanic asbClient->createQueueSender(queueName);

    log:printInfo("Creating Asb receiver connection.");
    handle queueReceiver = checkpanic asbClient->createQueueReceiver(queueName, RECEIVEANDDELETE);

    log:printInfo("Sending via Asb sender connection.");
    checkpanic asbClient->send(queueSender, message1);

    log:printInfo("Receiving from Asb receiver connection.");
    Message|Error? messageReceived = asbClient->receive(queueReceiver, serverWaitTime);

    if (messageReceived is Message) {
        log:printInfo(messageReceived.toString());
        test:assertEquals(messageReceived.body, stringContent, msg = "Sent & recieved message not equal.");
    } else if (messageReceived is ()) {
        test:assertFail("No message in the queue.");
    } else {
        test:assertFail("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender connection.");
    checkpanic asbClient->closeSender(queueSender);

    log:printInfo("Closing Asb receiver connection.");
    checkpanic asbClient->closeReceiver(queueReceiver);
}

@test:Config { 
    groups: ["asb"],
    dependsOn: [testSendAndReceiveMessageFromQueueOperation],
    enable: true
}
function testSendAndReceiveBatchFromQueueOperation() {
    log:printInfo("[[testSendAndReceiveBatchFromQueueOperation]]");
    AsbClient asbClient = new (config);

    log:printInfo("Creating Asb sender connection.");
    handle queueSender = checkpanic asbClient->createQueueSender(queueName);

    log:printInfo("Creating Asb receiver connection.");
    handle queueReceiver = checkpanic asbClient->createQueueReceiver(queueName, RECEIVEANDDELETE);

    log:printInfo("Sending via Asb sender connection.");
    checkpanic asbClient->sendBatch(queueSender, messages);

    log:printInfo("Receiving from Asb receiver connection.");
    MessageBatch|Error? messageReceived = asbClient->receiveBatch(queueReceiver, maxMessageCount);

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

    log:printInfo("Closing Asb sender connection.");
    checkpanic asbClient->closeSender(queueSender);

    log:printInfo("Closing Asb receiver connection.");
    checkpanic asbClient->closeReceiver(queueReceiver);
}

@test:Config { 
    groups: ["asb"],
    dependsOn: [testSendAndReceiveBatchFromQueueOperation],
    enable: true
}
function testCompleteMessageFromQueueOperation() {
    log:printInfo("[[testCompleteMessageFromQueueOperation]]");
    AsbClient asbClient = new (config);

    log:printInfo("Creating Asb sender connection.");
    handle queueSender = checkpanic asbClient->createQueueSender(queueName);

    log:printInfo("Creating Asb receiver connection.");
    handle queueReceiver = checkpanic asbClient->createQueueReceiver(queueName, PEEKLOCK);

    log:printInfo("Sending via Asb sender connection.");
    checkpanic asbClient->send(queueSender, message1);

    log:printInfo("Receiving from Asb receiver connection.");
    Message|Error? messageReceived = asbClient->receive(queueReceiver, serverWaitTime);

    if (messageReceived is Message) {
        var result = checkpanic asbClient->complete(queueReceiver, messageReceived);
        test:assertEquals(result, (), msg = "Complete message not succesful.");
    } else if (messageReceived is ()) {
        test:assertFail("No message in the queue.");
    } else {
        test:assertFail("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender connection.");
    checkpanic asbClient->closeSender(queueSender);

    log:printInfo("Closing Asb receiver connection.");
    checkpanic asbClient->closeReceiver(queueReceiver);
}

@test:Config { 
    groups: ["asb"],
    dependsOn: [testCompleteMessageFromQueueOperation],
    enable: true
}
function testAbandonMessageFromQueueOperation() {
    log:printInfo("[[testAbandonMessageFromQueueOperation]]");
    AsbClient asbClient = new (config);

    log:printInfo("Creating Asb sender connection.");
    handle queueSender = checkpanic asbClient->createQueueSender(queueName);

    log:printInfo("Creating Asb receiver connection.");
    handle queueReceiver = checkpanic asbClient->createQueueReceiver(queueName, PEEKLOCK);

    log:printInfo("Sending via Asb sender connection.");
    checkpanic asbClient->send(queueSender, message1);

    log:printInfo("Receiving from Asb receiver connection.");
    Message|Error? messageReceived = asbClient->receive(queueReceiver, serverWaitTime);

    if (messageReceived is Message) {
        var result = checkpanic asbClient->abandon(queueReceiver, messageReceived);
        test:assertEquals(result, (), msg = "Abandon message not succesful.");
        Message|Error? messageReceivedAgain = asbClient->receive(queueReceiver, serverWaitTime);
        if (messageReceivedAgain is Message) {
            test:assertEquals(messageReceivedAgain?.messageId, messageReceived?.messageId, 
                msg = "Abandon message not succesful.");
            checkpanic asbClient->complete(queueReceiver, messageReceivedAgain);
        } else {
            test:assertFail("Abandon message not succesful.");
        }
    } else if (messageReceived is ()) {
        test:assertFail("No message in the queue.");
    } else {
        test:assertFail("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender connection.");
    checkpanic asbClient->closeSender(queueSender);

    log:printInfo("Closing Asb receiver connection.");
    checkpanic asbClient->closeReceiver(queueReceiver);
}

@test:Config { 
    groups: ["asb"],
    dependsOn: [testAbandonMessageFromQueueOperation],
    enable: true
}
function testDeadletterMessageFromQueueOperation() {
    log:printInfo("[[testDeadletterMessageFromQueueOperation]]");
    AsbClient asbClient = new (config);

    log:printInfo("Creating Asb sender connection.");
    handle queueSender = checkpanic asbClient->createQueueSender(queueName);

    log:printInfo("Creating Asb receiver connection.");
    handle queueReceiver = checkpanic asbClient->createQueueReceiver(queueName, PEEKLOCK);

    log:printInfo("Sending via Asb sender connection.");
    checkpanic asbClient->send(queueSender, message1);

    log:printInfo("Receiving from Asb receiver connection.");
    Message|Error? messageReceived = asbClient->receive(queueReceiver, serverWaitTime);

    if (messageReceived is Message) {
        var result = checkpanic asbClient->deadLetter(queueReceiver, messageReceived);
        test:assertEquals(result, (), msg = "Deadletter message not succesful.");
    } else if (messageReceived is ()) {
        test:assertFail("No message in the queue.");
    } else {
        test:assertFail("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender connection.");
    checkpanic asbClient->closeSender(queueSender);

    log:printInfo("Closing Asb receiver connection.");
    checkpanic asbClient->closeReceiver(queueReceiver);
}

@test:Config { 
    groups: ["asb"],
    dependsOn: [testDeadletterMessageFromQueueOperation],
    enable: true
}
function testDeferMessageFromQueueOperation() {
    log:printInfo("[[testDeferMessageFromQueueOperation]]");
    AsbClient asbClient = new (config);

    log:printInfo("Creating Asb sender connection.");
    handle queueSender = checkpanic asbClient->createQueueSender(queueName);

    log:printInfo("Creating Asb receiver connection.");
    handle queueReceiver = checkpanic asbClient->createQueueReceiver(queueName, PEEKLOCK);

    log:printInfo("Sending via Asb sender connection.");
    checkpanic asbClient->send(queueSender, message1);

    log:printInfo("Receiving from Asb receiver connection.");
    Message|Error? messageReceived = asbClient->receive(queueReceiver, serverWaitTime);

    if (messageReceived is Message) {
        int result = checkpanic asbClient->defer(queueReceiver, messageReceived);
        test:assertNotEquals(result, 0, msg = "Defer message not succesful.");
        Message|Error? messageReceivedAgain = checkpanic asbClient->receiveDeferred(queueReceiver, result);
        if (messageReceivedAgain is Message) {
            test:assertEquals(messageReceivedAgain?.messageId, messageReceived?.messageId, 
                msg = "Receiving deferred message not succesful.");
            checkpanic asbClient->complete(queueReceiver, messageReceivedAgain);
        }
    } else if (messageReceived is ()) {
        test:assertFail("No message in the queue.");
    } else {
        test:assertFail("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender connection.");
    checkpanic asbClient->closeSender(queueSender);

    log:printInfo("Closing Asb receiver connection.");
    checkpanic asbClient->closeReceiver(queueReceiver);
}

@test:Config { 
    groups: ["asb"],
    dependsOn: [testDeferMessageFromQueueOperation],
    enable: true
}
function testSendAndReceiveMessageFromSubscriptionOperation() {
    log:printInfo("[[testSendAndReceiveMessageFromSubscriptionOperation]]");
    AsbClient asbClient = new (config);

    log:printInfo("Creating Asb sender connection.");
    handle topicSender = checkpanic asbClient->createTopicSender(topicName);

    log:printInfo("Creating Asb receiver connection.");
    handle subscriptionReceiver = 
        checkpanic asbClient->createSubscriptionReceiver(topicName, subscriptionName1, RECEIVEANDDELETE);

    log:printInfo("Sending via Asb sender connection.");
    checkpanic asbClient->send(topicSender, message1);

    log:printInfo("Receiving from Asb receiver connection.");
    Message|Error? messageReceived = asbClient->receive(subscriptionReceiver, serverWaitTime);

    if (messageReceived is Message) {
        test:assertEquals(messageReceived.body, stringContent, msg = "Sent & recieved message not equal.");
    } else if (messageReceived is ()) {
        test:assertFail("No message in the subscription.");
    } else {
        test:assertFail("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender connection.");
    checkpanic asbClient->closeSender(topicSender);

    log:printInfo("Closing Asb receiver connection.");
    checkpanic asbClient->closeReceiver(subscriptionReceiver);
}

@test:Config { 
    groups: ["asb"],
    dependsOn: [testSendAndReceiveMessageFromSubscriptionOperation],
    enable: true
}
function testSendAndReceiveBatchFromSubscriptionOperation() {
    log:printInfo("[[testSendAndReceiveBatchFromSubscriptionOperation]]");
    AsbClient asbClient = new (config);

    log:printInfo("Creating Asb sender connection.");
    handle topicSender = checkpanic asbClient->createTopicSender(topicName);

    log:printInfo("Creating Asb receiver connection.");
    handle subscriptionReceiver = 
        checkpanic asbClient->createSubscriptionReceiver(topicName, subscriptionName1, RECEIVEANDDELETE);

    log:printInfo("Sending via Asb sender connection.");
    checkpanic asbClient->sendBatch(topicSender, messages);

    log:printInfo("Receiving from Asb receiver connection.");
    MessageBatch|Error? messageReceived = 
        asbClient->receiveBatch(subscriptionReceiver, maxMessageCount, serverWaitTime);

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

    log:printInfo("Closing Asb sender connection.");
    checkpanic asbClient->closeSender(topicSender);

    log:printInfo("Closing Asb receiver connection.");
    checkpanic asbClient->closeReceiver(subscriptionReceiver);
}

@test:Config { 
    groups: ["asb"],
    dependsOn: [testSendAndReceiveBatchFromSubscriptionOperation],
    enable: true
}
function testCompleteMessageFromSubscriptionOperation() {
    log:printInfo("[[testCompleteMessageFromSubscriptionOperation]]");
    AsbClient asbClient = new (config);

    log:printInfo("Creating Asb sender connection.");
    handle topicSender = checkpanic asbClient->createTopicSender(topicName);

    log:printInfo("Creating Asb receiver connection.");
    handle subscriptionReceiver = 
        checkpanic asbClient->createSubscriptionReceiver(topicName, subscriptionName1, PEEKLOCK);

    log:printInfo("Sending via Asb sender connection.");
    checkpanic asbClient->send(topicSender, message1);

    log:printInfo("Receiving from Asb receiver connection.");
    Message|Error? messageReceived = asbClient->receive(subscriptionReceiver, serverWaitTime);

    if (messageReceived is Message) {
        var result = checkpanic asbClient->complete(subscriptionReceiver, messageReceived);
        test:assertEquals(result, (), msg = "Complete message not succesful.");
    } else if (messageReceived is ()) {
        test:assertFail("No message in the subscription.");
    } else {
        test:assertFail("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender connection.");
    checkpanic asbClient->closeSender(topicSender);

    log:printInfo("Closing Asb receiver connection.");
    checkpanic asbClient->closeReceiver(subscriptionReceiver);
}

@test:Config { 
    groups: ["asb"],
    dependsOn: [testCompleteMessageFromSubscriptionOperation],
    enable: true
}
function testAbandonMessageFromSubscriptionOperation() {
    log:printInfo("[[testAbandonMessageFromSubscriptionOperation]]");
    AsbClient asbClient = new (config);

    log:printInfo("Creating Asb sender connection.");
    handle topicSender = checkpanic asbClient->createTopicSender(topicName);

    log:printInfo("Creating Asb receiver connection.");
    handle subscriptionReceiver = 
        checkpanic asbClient->createSubscriptionReceiver(topicName, subscriptionName1, PEEKLOCK);

    log:printInfo("Sending via Asb sender connection.");
    checkpanic asbClient->send(topicSender, message1);

    log:printInfo("Receiving from Asb receiver connection.");
    Message|Error? messageReceived = asbClient->receive(subscriptionReceiver, serverWaitTime);

    if (messageReceived is Message) {
        var result = checkpanic asbClient->abandon(subscriptionReceiver, messageReceived);
        test:assertEquals(result, (), msg = "Abandon message not succesful.");
        Message|Error? messageReceivedAgain = asbClient->receive(subscriptionReceiver, serverWaitTime);
        if (messageReceivedAgain is Message) {
            checkpanic asbClient->complete(subscriptionReceiver, messageReceivedAgain);
        } else {
            test:assertFail("Abandon message not succesful.");
        }
    } else if (messageReceived is ()) {
        test:assertFail("No message in the subscription.");
    } else {
        test:assertFail("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender connection.");
    checkpanic asbClient->closeSender(topicSender);

    log:printInfo("Closing Asb receiver connection.");
    checkpanic asbClient->closeReceiver(subscriptionReceiver);
}

@test:Config { 
    groups: ["asb"],
    dependsOn: [testAbandonMessageFromSubscriptionOperation],
    enable: true
}
function testDeadletterMessageFromSubscriptionOperation() {
    log:printInfo("[[testDeadletterMessageFromSubscriptionOperation]]");
    AsbClient asbClient = new (config);

    log:printInfo("Creating Asb sender connection.");
    handle topicSender = checkpanic asbClient->createTopicSender(topicName);

    log:printInfo("Creating Asb receiver connection.");
    handle subscriptionReceiver = 
        checkpanic asbClient->createSubscriptionReceiver(topicName, subscriptionName1, PEEKLOCK);

    log:printInfo("Sending via Asb sender connection.");
    checkpanic asbClient->send(topicSender, message1);

    log:printInfo("Receiving from Asb receiver connection.");
    Message|Error? messageReceived = asbClient->receive(subscriptionReceiver, serverWaitTime);

    if (messageReceived is Message) {
        var result = checkpanic asbClient->deadLetter(subscriptionReceiver, messageReceived);
        test:assertEquals(result, (), msg = "Deadletter message not succesful.");
    } else if (messageReceived is ()) {
        test:assertFail("No message in the subscription.");
    } else {
        test:assertFail("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender connection.");
    checkpanic asbClient->closeSender(topicSender);

    log:printInfo("Closing Asb receiver connection.");
    checkpanic asbClient->closeReceiver(subscriptionReceiver);
}

@test:Config { 
    groups: ["asb"],
    dependsOn: [testDeadletterMessageFromSubscriptionOperation],
    enable: true
}
function testDeferMessageFromSubscriptionOperation() {
    log:printInfo("[[testDeferMessageFromSubscriptionOperation]]");
    AsbClient asbClient = new (config);

    log:printInfo("Creating Asb sender connection.");
    handle topicSender = checkpanic asbClient->createTopicSender(topicName);

    log:printInfo("Creating Asb receiver connection.");
    handle subscriptionReceiver = 
        checkpanic asbClient->createSubscriptionReceiver(topicName, subscriptionName1, PEEKLOCK);

    log:printInfo("Sending via Asb sender connection.");
    checkpanic asbClient->send(topicSender, message1);

    log:printInfo("Receiving from Asb receiver connection.");
    Message|Error? messageReceived = asbClient->receive(subscriptionReceiver, serverWaitTime);

    if (messageReceived is Message) {
        int result = checkpanic asbClient->defer(subscriptionReceiver, messageReceived);
        test:assertNotEquals(result, 0, msg = "Defer message not succesful.");
        Message|Error? messageReceivedAgain = checkpanic asbClient->receiveDeferred(subscriptionReceiver, result);
        if (messageReceivedAgain is Message) {
            test:assertEquals(messageReceivedAgain?.messageId, messageReceived?.messageId, 
                msg = "Receiving deferred message not succesful.");
            checkpanic asbClient->complete(subscriptionReceiver, messageReceivedAgain);
        }
    } else if (messageReceived is ()) {
        test:assertFail("No message in the queue.");
    } else {
        test:assertFail("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender connection.");
    checkpanic asbClient->closeSender(topicSender);

    log:printInfo("Closing Asb receiver connection.");
    checkpanic asbClient->closeReceiver(subscriptionReceiver);
}
