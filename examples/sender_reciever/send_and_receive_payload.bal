// Copyright (c) 2023 WSO2 LLC. (http://www.wso2.org).
//
// WSO2 LLS. licenses this file to you under the Apache License,
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
import ballerinax/asb;

// Connection Configurations
configurable string connectionString = ?;
configurable string queueName = ?;

// This sample demonstrates a scneario where azure service bus connecter is used to 
// send a payload to a queue and receive the same payload from the queue.
public function main() returns error? {

    // Input values
    string stringContent = "This is My Message Body";
    byte[] byteContent = stringContent.toBytes();
    int serverWaitTime = 60; // In seconds

    asb:ASBServiceSenderConfig senderConfig = {
        connectionString: connectionString,
        entityType: asb:QUEUE,
        topicOrQueueName: queueName
    };

    asb:ASBServiceReceiverConfig receiverConfig = {
        connectionString: connectionString,
        entityConfig: {
            queueName: queueName
        },
        receiveMode: asb:PEEK_LOCK
    };

    log:printInfo("Initializing Asb sender client.");
    asb:MessageSender messageSender = check new (senderConfig);

    log:printInfo("Initializing Asb receiver client.");
    asb:MessageReceiver messageReceiver = check new (receiverConfig);

    //Sending payload. payload can be either nil, boolean, int, float, string, json, byte[], xml, map<anydata>, record
    log:printInfo("Sending via Asb sender client.");
    check messageSender->sendPayload(byteContent);

    //Receiving payload. payload can be either nil, boolean, int, float, string, json, byte[], xml, map<anydata>, record
    log:printInfo("Receiving from Asb receiver client.");
    byte[]|error? bytePayload = messageReceiver->receivePayload(serverWaitTime);

    log:printInfo("Asserting received payloads.");
    if bytePayload is byte[] {
        string stringPayload = check string:fromBytes(bytePayload);
        log:printInfo("Received message: " + stringPayload);
    } else {
        log:printError("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender client.");
    check messageSender->close();

    log:printInfo("Closing Asb receiver client.");
    check messageReceiver->close();
}
