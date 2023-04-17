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

    float|error? expectedPayload = messageReceiver->receivePayload(serverWaitTime);
    log:printInfo("Asserting received payloads.");
    test:assertTrue(expectedPayload is error, msg = "Unexpected payload received");
    test:assertEquals((<error>expectedPayload).message(), "{ballerina/lang.value}ConversionError");

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

    int|string|error? expectedPayload = messageReceiver->receivePayload(serverWaitTime);
    log:printInfo("Asserting received payloads.");
    test:assertTrue(expectedPayload is error, msg = "Unexpected payload received");
    test:assertEquals((<error>expectedPayload).message(), "Union types are not supported for the contextually expected type, except for nilable types");

    log:printInfo("Closing Asb sender client.");
    check messageSender->close();

    log:printInfo("Closing Asb receiver client.");
    check messageReceiver->close();
}
