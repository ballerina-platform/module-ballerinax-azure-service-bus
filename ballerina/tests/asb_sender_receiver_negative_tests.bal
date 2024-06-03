// Copyright (c) 2023 WSO2 LLC. (http://www.wso2.org).
//
// WSO2 LLC. licenses this file to you under the Apache License,
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
import ballerina/test;

@test:Config {
    groups: ["asb_sender_receiver_negative"],
    enable: true,
    dependsOn: [testCreateQueue, testCreateTopicOperation, testCreateSubscription]
}
function testReceivePayloadWithIncorrectExpectedType() returns error? {
    log:printInfo("[[testReceivePayloadWithIncorrectExpectedType]]");
    log:printInfo("Creating Asb message sender.");
    MessageSender messageSender = check new (senderConfig);

    log:printInfo("Sending anydata payloads via ASB sender");
    check messageSender->sendPayload(mapContent);

    log:printInfo("Creating Asb message receiver.");
    receiverConfig.receiveMode = RECEIVE_AND_DELETE;
    MessageReceiver messageReceiver = check new (receiverConfig);
    log:printInfo("Receiving from Asb receiver client.");

    float|Error? expectedPayload = messageReceiver->receivePayload(serverWaitTime);
    log:printInfo("Asserting received payloads.");
    test:assertTrue(expectedPayload is Error, msg = "Unexpected payload received");
    test:assertEquals((<Error>expectedPayload).message(), "Failed to deserialize the message payload " +
                            "into the contextually expected type 'float?'. Use a compatible Ballerina type or, " +
                            "use 'byte[]' type along with an appropriate deserialization logic afterwards.");

    log:printInfo("Closing Asb sender client.");
    check messageSender->close();

    log:printInfo("Closing Asb receiver client.");
    check messageReceiver->close();
}

@test:Config {
    groups: ["asb_sender_receiver_negative"],
    enable: true,
    dependsOn: [testReceivePayloadWithIncorrectExpectedType]
}
function testInvalidComplete() returns error? {
    log:printInfo("[[testInvalidComplete]");

    log:printInfo("Initializing Asb sender client.");
    MessageSender messageSender = check new (senderConfig);

    log:printInfo("Initializing Asb receiver client.");
    receiverConfig.receiveMode = RECEIVE_AND_DELETE;

    MessageReceiver messageReceiver = check new (receiverConfig);

    log:printInfo("Sending via Asb sender.");
    check messageSender->send(message1);

    log:printInfo("Receiving from Asb receiver client.");
    Message|error? receivedMessage = messageReceiver->receive(serverWaitTime);

    if receivedMessage is Message {
        log:printInfo("messgae" + receivedMessage.toString());
        Error? result = messageReceiver->complete(receivedMessage);
        test:assertTrue(result is error, msg = "Unexpected Complete for Messages in Receive and Delete Mode");
        string:RegExp completeFailedMsg = re `^Failed to complete message with ID:.*$`;
        test:assertTrue(completeFailedMsg.isFullMatch((<Error>result).message()), msg = "Invalid Complete for " +
        " Messages in Receive and Delete Mode");
    } else if receivedMessage is () {
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
    groups: ["asb_sender_receiver_negative"],
    dependsOn: [testInvalidComplete]
}
function testInvalidAbandon() returns error? {
    log:printInfo("[[testInvalidAbandon]");

    log:printInfo("Initializing Asb sender client.");
    MessageSender messageSender = check new (senderConfig);

    log:printInfo("Initializing Asb receiver client.");
    receiverConfig.receiveMode = RECEIVE_AND_DELETE;

    MessageReceiver messageReceiver = check new (receiverConfig);

    log:printInfo("Sending via Asb sender.");
    check messageSender->send(message1);

    log:printInfo("Receiving from Asb receiver client.");
    Message|error? receivedMessage = messageReceiver->receive(serverWaitTime);

    if receivedMessage is Message {
        log:printInfo("messgae" + receivedMessage.toString());
        Error? result = messageReceiver->abandon(receivedMessage);
        test:assertTrue(result is error, msg = "Unexpected Abandon for Messages in Receive and Delete Mode");
        string:RegExp abandonFailedMsg = re `^Failed to abandon message with ID:.*$`;
        test:assertTrue(abandonFailedMsg.isFullMatch((<Error>result).message()), msg = "Invalid Abandon for " +
        " Messages in Receive and Delete Mode");
    } else if receivedMessage is () {
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
    groups: ["asb_sender_receiver_negative"],
    dependsOn: [testInvalidAbandon]
}
function testReceivePayloadWithUnsupportedUnionExpectedType() returns error? {
    log:printInfo("[[testReceivePayloadWithUnsupportedUnionExpectedType]]");
    log:printInfo("Creating Asb message sender.");
    MessageSender messageSender = check new (senderConfig);

    log:printInfo("Sending payloads via ASB sender");
    check messageSender->sendPayload(mapContent);

    log:printInfo("Creating Asb message receiver.");
    receiverConfig.receiveMode = RECEIVE_AND_DELETE;
    MessageReceiver messageReceiver = check new (receiverConfig);
    log:printInfo("Receiving from Asb receiver client.");

    int|string|Error? expectedPayload = messageReceiver->receivePayload(serverWaitTime);
    log:printInfo("Asserting received payloads.");
    test:assertTrue(expectedPayload is error, msg = "Unexpected payload received");
    test:assertEquals((<Error>expectedPayload).message(), "Failed to deserialize the message payload " +
                            "into the contextually expected type '(int|string)?'. Use a compatible Ballerina type or, " +
                            "use 'byte[]' type along with an appropriate deserialization logic afterwards.");
    log:printInfo("Closing Asb sender client.");
    check messageSender->close();

    log:printInfo("Closing Asb receiver client.");
    check messageReceiver->close();
}

@test:Config {
    groups: ["asb_sender_receiver_negative"],
    enable: true,
    dependsOn: [testReceivePayloadWithUnsupportedUnionExpectedType]
}
function testSendToInvalidTopic() returns error? {
    log:printInfo("[[testSendToInvalidTopic]]");
    log:printInfo("Creating Asb message sender.");
    senderConfig.topicOrQueueName = "non-existing-topic";
    MessageSender messageSender = check new (senderConfig);

    log:printInfo("Sending payloads via ASB sender");
    Error? e = messageSender->sendPayload("message");
    test:assertTrue(e is error, msg = "Unexpected response received");
    test:assertEquals((<Error>e).message(), "ASB Error: MESSAGING_ENTITY_NOT_FOUND");

    log:printInfo("Closing Asb sender client.");
    check messageSender->close();
}

@test:Config {
    groups: ["asb_sender_receiver_negative"],
    enable: true,
    dependsOn: [testSendToInvalidTopic]
}
function testReceiveFromInvalidQueue() returns error? {
    log:printInfo("[[testReceiveFromInvalidQueue]]");
    log:printInfo("Creating Asb message receiver.");
    ASBServiceReceiverConfig invalidReceiverConfig = {
        connectionString: connectionString,
        entityConfig: {
            queueName: "non-existing-queue"
        },
        receiveMode: PEEK_LOCK
    };
    MessageReceiver messageReceiver = check new (invalidReceiverConfig);

    log:printInfo("Sending payloads via ASB sender");
    Message|error? e = messageReceiver->receive(5);
    test:assertTrue(e is error, msg = "Unexpected response received");
    test:assertEquals((<Error>e).message(), "ASB Error: MESSAGING_ENTITY_NOT_FOUND");

    log:printInfo("Closing Asb receiver client.");
    check messageReceiver->close();
}

@test:Config {
    groups: ["asb_sender_receiver_negative"],
    enable: true,
    dependsOn: [testReceiveFromInvalidQueue]
}
function testInvalidConnectionString() returns error? {
    log:printInfo("[[testInvalidConnectionString]]");
    log:printInfo("Creating Asb message sender.");
    senderConfig.connectionString = "invalid-connection-string";
    MessageSender|Error messageSender = new (senderConfig);

    test:assertTrue(messageSender is error, msg = "Client creation should have failed.");
    test:assertEquals((<Error>messageSender).message(), "Error occurred while processing request: " +
    "Connection string has invalid key value pair: invalid-connection-string");
}
