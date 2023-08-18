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
import ballerinax/asb;

// Connection Configurations
configurable string connectionString = ?;
configurable string queueName = ?;

// This sample demonstrates a scneario where azure service bus connecter is used to 
// send a message to a queue using message sender, receive a messsage using message receiver with RECEIVEANDDELETE mode
// so that the message will be deleted from the queue just after receiving.
public function main() returns error? {

    // Input values
    string stringContent = "This is My Message Body";
    byte[] byteContent = stringContent.toBytes();
    int timeToLive = 60; // In seconds
    int serverWaitTime = 60; // In seconds

    asb:ApplicationProperties applicationProperties = {
        properties: {a: "propertyValue1", b: "propertyValue2"}
    };

    asb:Message message1 = {
        body: byteContent,
        contentType: asb:TEXT,
        timeToLive: timeToLive,
        applicationProperties: applicationProperties
    };

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
        receiveMode: asb:RECEIVE_AND_DELETE
    };

    log:printInfo("Initializing Asb sender client.");
    asb:MessageSender queueSender = check new (senderConfig);

    log:printInfo("Initializing Asb receiver client.");
    asb:MessageReceiver queueReceiver = check new (receiverConfig);

    log:printInfo("Sending via Asb sender client.");
    check queueSender->send(message1);

    log:printInfo("Receiving from Asb receiver client.");
    asb:Message|asb:Error? messageReceived = queueReceiver->receive(serverWaitTime);

    if (messageReceived is asb:Message) {
        log:printInfo("Reading Received Message : " + messageReceived.toString());
    } else if (messageReceived is ()) {
        log:printError("No message in the queue.");
    } else {
        log:printError("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender client.");
    check queueSender->close();

    log:printInfo("Closing Asb receiver client.");
    check queueReceiver->close();
}
