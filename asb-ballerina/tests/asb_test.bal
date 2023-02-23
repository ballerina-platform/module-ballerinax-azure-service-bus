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
import ballerina/time;

// Connection Configurations
configurable string connectionString = os:getEnv("CONNECTION_STRING");
configurable string queueName = os:getEnv("QUEUE_NAME");
configurable string topicName = os:getEnv("TOPIC_NAME");
configurable string subscriptionName1 = os:getEnv("SUBSCRIPTION_NAME1");
string subscriptionPath1 = subscriptionName1;

// Input values
string stringContent = "This is ASB connector test-Message Body"; 
byte[] byteContent = stringContent.toBytes();
json jsonContent = {name: "wso2", color: "orange", price: 5.36};
byte[] byteContentFromJson = jsonContent.toJsonString().toBytes();
map<any> properties = {a: "propertyValue1", b: "propertyValue2", c: 1, d: "true", f: 1.345, s: false, k:1020202, g: jsonContent};
int timeToLive = 60; // In seconds
int serverWaitTime = 60; // In seconds
int maxMessageCount = 2;

ApplicationProperties applicationProperties = {
    properties: properties
};

Message message1 = {
    body: byteContent,
    contentType: TEXT,
    timeToLive: timeToLive,
    applicationProperties: applicationProperties
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

ASBServiceSenderConfig senderConfig = {
    connectionString: connectionString,
    entityType: QUEUE,
    topicOrQueueName: queueName
};

ASBServiceReceiverConfig receiverConfig = {
    connectionString: connectionString,
    entityConfig: {
        queueName: queueName
    },
    receiveMode: PEEK_LOCK
};

@test:Config { 
    groups: ["asb"],
    enable: true
}
function testSendAndReceiveMessageFromQueueOperation() returns error? {
    log:printInfo("[[testSendAndReceiveMessageFromQueueOperation]]");

    log:printInfo("Creating Asb message sender.");
    MessageSender messageSender = check new(senderConfig);

    log:printInfo("Creating Asb message receiver.");
    MessageReceiver messageReceiver = check new (receiverConfig);

    log:printInfo("Sending via Asb sender.");
    check messageSender->send(message1);

    log:printInfo("Receiving from Asb receiver client.");
    Message|error? messageReceived = messageReceiver->receive(serverWaitTime);

    if (messageReceived is Message) {
        var result = check messageReceiver->complete(messageReceived);
        test:assertEquals(result, (), msg = "Complete message not successful.");
        float mapValue = <float> check getApplicationPropertyByName(messageReceived, "f");
        test:assertEquals(mapValue, <float>properties["f"], "Retrieving application properties failed");
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
    MessageSender messageSender = check new(senderConfig);

    log:printInfo("Initializing Asb receiver client.");
    receiverConfig.receiveMode = RECEIVE_AND_DELETE;
    MessageReceiver messageReceiver = check new (receiverConfig);

    log:printInfo("Sending via Asb sender.");
    check messageSender->sendBatch(messages);

    log:printInfo("Receiving from Asb receiver.");
    MessageBatch|error? messageReceived = messageReceiver->receiveBatch(maxMessageCount);

    if (messageReceived is MessageBatch) {
        log:printInfo(messageReceived.toString());
        foreach Message message in messageReceived.messages {
            if (message.toString() != "") {
                string msg =  check string:fromBytes(<byte[]>message.body);
                test:assertEquals(msg, stringContent, msg = "Sent & received message are not equal.");
            }
        }
    } else if (messageReceived is ()) {
        test:assertFail("No message in the queue.");
    } else {
        test:assertFail("Receiving message via Asb receiver connection failed." + messageReceived.toString());
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
    MessageSender messageSender = check new(senderConfig);

    log:printInfo("Initializing Asb receiver client.");
    receiverConfig.receiveMode = PEEK_LOCK;
    log:printInfo(receiverConfig.toString());
    MessageReceiver messageReceiver = check new (receiverConfig);

    log:printInfo("Sending via Asb sender.");
    check messageSender->send(message1);

    log:printInfo("Receiving from Asb receiver client.");
    Message|error? messageReceived = messageReceiver->receive(serverWaitTime);

    if (messageReceived is Message) {
        log:printInfo("messgae" +  messageReceived.toString());
        var result = check messageReceiver->complete(messageReceived);
        test:assertEquals(result, (), msg = "Complete message not successful.");
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
    MessageSender messageSender = check new(senderConfig);

    log:printInfo("Initializing Asb receiver client.");
    MessageReceiver messageReceiver = check new (receiverConfig);

    log:printInfo("Sending via Asb sender.");
    check messageSender->send(message1);

    log:printInfo("Receiving from Asb receiver.");
    Message|error? messageReceived = messageReceiver->receive(serverWaitTime);

    if (messageReceived is Message) {
        var result = check messageReceiver->abandon(messageReceived);
        test:assertEquals(result, (), msg = "Abandon message not successful.");
        Message|error? messageReceivedAgain = messageReceiver->receive(serverWaitTime);
        if (messageReceivedAgain is Message) {
            test:assertEquals(messageReceivedAgain?.messageId, messageReceived?.messageId, 
                msg = "Abandon message not successful.");
            check messageReceiver->complete(messageReceivedAgain);
        } else {
            test:assertFail("Abandon message not successful.");
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
    MessageSender messageSender = check new(senderConfig);

    log:printInfo("Initializing Asb receiver.");
    MessageReceiver messageReceiver = check new (receiverConfig);

    log:printInfo("Sending via Asb sender.");
    check messageSender->send(message1);

    log:printInfo("Receiving from Asb receiver.");
    Message|error? messageReceived = messageReceiver->receive(serverWaitTime);

    if (messageReceived is Message) {
        var result = check messageReceiver->deadLetter(messageReceived);
        test:assertEquals(result, (), msg = "Deadletter message not successful.");
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
    MessageSender messageSender = check new(senderConfig);

    log:printInfo("Initializing Asb receiver.");
    MessageReceiver messageReceiver = check new (receiverConfig);

    log:printInfo("Sending via Asb sender.");
    check messageSender->send(message1);

    log:printInfo("Receiving from Asb receiver.");
    Message|error? messageReceived = messageReceiver->receive(serverWaitTime);

    if (messageReceived is Message) {
        int result = check messageReceiver->defer(messageReceived);
        test:assertNotEquals(result, 0, msg = "Defer message not successful.");
        Message|error? messageReceivedAgain = check messageReceiver->receiveDeferred(result);
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
    senderConfig.topicOrQueueName = topicName;
    MessageSender topicSender = check new (senderConfig);

    log:printInfo("Initializing Asb receiver client.");
    receiverConfig.entityConfig = {
        topicName: topicName,
        subscriptionName: subscriptionName1
    };
    receiverConfig.receiveMode = RECEIVE_AND_DELETE;
    MessageReceiver subscriptionReceiver = check new (receiverConfig);

    log:printInfo("Sending via Asb sender client.");
    check topicSender->send(message1);

    log:printInfo("Receiving from Asb receiver client.");
    Message|error? messageReceived = subscriptionReceiver->receive(serverWaitTime);

    if (messageReceived is Message) {
        log:printInfo(messageReceived.toString());
        string msg =  check string:fromBytes(<byte[]>messageReceived.body);
        test:assertEquals(msg, stringContent, msg = "Sent & received message not equal.");
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
    MessageSender topicSender = check new (senderConfig);

    log:printInfo("Initializing Asb receiver client.");
    MessageReceiver subscriptionReceiver = check new (receiverConfig);

    log:printInfo("Sending via Asb sender client.");
    check topicSender->sendBatch(messages);

    log:printInfo("Receiving from Asb receiver client.");
    MessageBatch|error? messageReceived = subscriptionReceiver->receiveBatch(maxMessageCount, serverWaitTime);

    if (messageReceived is MessageBatch) {
        foreach Message message in messageReceived.messages {
            if (message.toString() != "") {
                string msg =  check string:fromBytes(<byte[]>message.body);
                test:assertEquals(msg, stringContent, msg = "Sent & received message not equal.");
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
    MessageSender topicSender = check new (senderConfig);

    log:printInfo("Initializing Asb receiver client.");
    receiverConfig.receiveMode = PEEK_LOCK;
    MessageReceiver subscriptionReceiver = check new (receiverConfig);

    log:printInfo("Sending via Asb sender client.");
    check topicSender->send(message1);

    log:printInfo("Receiving from Asb receiver client.");
    Message|error? messageReceived = subscriptionReceiver->receive(serverWaitTime);

    if (messageReceived is Message) {
        var result = check subscriptionReceiver->complete(messageReceived);
        test:assertEquals(result, (), msg = "Complete message not successful.");
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
    MessageSender topicSender = check new (senderConfig);

    log:printInfo("Initializing Asb receiver client.");
    MessageReceiver subscriptionReceiver = check new (receiverConfig);

    log:printInfo("Sending via Asb sender client.");
    check topicSender->send(message1);

    log:printInfo("Receiving from Asb receiver client.");
    Message|error? messageReceived = subscriptionReceiver->receive(serverWaitTime);

    if (messageReceived is Message) {
        var result = check subscriptionReceiver->abandon(messageReceived);
        test:assertEquals(result, (), msg = "Abandon message not successful.");
        Message|error? messageReceivedAgain = subscriptionReceiver->receive(serverWaitTime);
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
    MessageSender topicSender = check new (senderConfig);

    log:printInfo("Initializing Asb receiver client.");
    MessageReceiver subscriptionReceiver = check new (receiverConfig);

    log:printInfo("Sending via Asb sender client.");
    check topicSender->send(message1);

    log:printInfo("Receiving from Asb receiver client.");
    Message|error? messageReceived = subscriptionReceiver->receive(serverWaitTime);

    if (messageReceived is Message) {
        var result = check subscriptionReceiver->deadLetter(messageReceived);
        test:assertEquals(result, (), msg = "Deadletter message not successful.");
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
    MessageSender topicSender = check new (senderConfig);

    log:printInfo("Initializing Asb receiver client.");
    MessageReceiver subscriptionReceiver = check new (receiverConfig);

    log:printInfo("Sending via Asb sender client.");
    check topicSender->send(message1);

    log:printInfo("Receiving from Asb receiver client.");
    Message|error? messageReceived = subscriptionReceiver->receive(serverWaitTime);

    if (messageReceived is Message) {
        int result = check subscriptionReceiver->defer(messageReceived);
        test:assertNotEquals(result, 0, msg = "Defer message not successful.");
        Message|error? messageReceivedAgain = check subscriptionReceiver->receiveDeferred(result);
        if (messageReceivedAgain is Message) {
            test:assertEquals(messageReceivedAgain?.messageId, messageReceived?.messageId, 
                msg = "Receiving deferred message not successful.");
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

@test:Config {
    groups: ["asb"],
    dependsOn: [testDeferMessageFromSubscriptionOperation],
    enable: true
}
function testMessageScheduling() returns error? {

    MessageSender topicSender;
    MessageReceiver subscriptionReceiver;

    do {

        log:printInfo("Initializing Asb sender client.");
        senderConfig.topicOrQueueName = topicName;
        topicSender = check new (senderConfig);

        log:printInfo("Initializing Asb receiver client.");
        receiverConfig.entityConfig = {
            topicName: topicName,
            subscriptionName: subscriptionName1
        };
        receiverConfig.receiveMode = RECEIVE_AND_DELETE;
        subscriptionReceiver = check new (receiverConfig);

        log:printInfo("Scheduling message via Asb sender client to be enqueued by 15 seconds");
        time:Utc utcScheduleTime = time:utcAddSeconds(time:utcNow(), 15);
        time:Civil civilScheduleTime = time:utcToCivil(utcScheduleTime);

        int sequenceNumber = check topicSender->schedule(message1, civilScheduleTime);

        log:printInfo("Scheduled message with sequence ID = " + sequenceNumber.toString());
        log:printInfo("Receiving from Asb receiver client. Max wait = " + serverWaitTime.toString());
        Message|error? messageReceived = subscriptionReceiver->receive(serverWaitTime);

        if (messageReceived is Message) {
            log:printInfo(messageReceived.toString());
            string msg = check string:fromBytes(<byte[]>messageReceived.body);
            test:assertEquals(msg, stringContent, msg = "Sent & received message not equal.");
        } else if (messageReceived is ()) {
            test:assertFail("Subscription did not receive message within " + serverWaitTime.toString());
        } else {
            test:assertFail("Receiving message via Asb receiver connection failed.");
        }
    } on fail error e {
        log:printInfo("Closing Asb sender client.");
        if (topicSender is MessageSender) {
            check topicSender->close();
        }
        log:printInfo("Closing Asb receiver client.");
        if (subscriptionReceiver is MessageReceiver) {
            check subscriptionReceiver->close();
        }
        return error("Error while executing test testMessageScheduling", e);
    }


}