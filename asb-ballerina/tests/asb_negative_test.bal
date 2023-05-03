// Copyright (c) 2023 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
    groups: ["asb_negative"]
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
    groups: ["asb_negative"]
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
    groups: ["asb_negative"]
}
function testSendToInvalidTopic() returns error? {
    log:printInfo("[[testSendToInvalidTopic]]");
    log:printInfo("Creating Asb message sender.");
    senderConfig.topicOrQueueName = "non-existing-topic";
    MessageSender messageSender = check new (senderConfig);

    log:printInfo("Sending payloads via ASB sender");
    Error? e = messageSender->sendPayload("message");
    test:assertTrue(e is error, msg = "Unexpected response received");
    test:assertEquals((<Error>e).message(), "ASB request failed due to: MESSAGING_ENTITY_NOT_FOUND");

    log:printInfo("Closing Asb sender client.");
    check messageSender->close();
}

@test:Config {
    groups: ["asb_negative"]
}
function testReceiveFromInvalidQueue() returns error? {
    log:printInfo("[[testReceiveFromInvalidQueue]]");
    log:printInfo("Creating Asb message receiver.");
    receiverConfig.entityConfig = {queueName: "non-existing-queue"};
    MessageReceiver messageReceiver = check new (receiverConfig);

    log:printInfo("Sending payloads via ASB sender");
    Message|error? e = messageReceiver->receive(5);
    test:assertTrue(e is error, msg = "Unexpected response received");
    test:assertEquals((<Error>e).message(), "ASB request failed due to: MESSAGING_ENTITY_NOT_FOUND");

    log:printInfo("Closing Asb receiver client.");
    check messageReceiver->close();
}

@test:Config {
    groups: ["asb_negative"]
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
