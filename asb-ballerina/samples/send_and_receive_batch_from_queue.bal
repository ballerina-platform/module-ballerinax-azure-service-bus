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
import ballerinax/asb;

// Connection Configurations
configurable string connectionString = ?;
configurable string queueName = ?;

public function main() returns error? {

    // Input values
    string stringContent = "This is My Message Body"; 
    byte[] byteContent = stringContent.toBytes();
    map<string> properties = {a: "propertyValue1", b: "propertyValue2"};
    int timeToLive = 60; // In seconds
    int serverWaitTime = 60; // In seconds
    int maxMessageCount = 2;

    asb:ApplicationProperties applicationProperties = {
        properties: properties
    };

    asb:Message message1 = {
        body: byteContent,
        contentType: asb:TEXT,
        timeToLive: timeToLive
    };

    asb:Message message2 = {
        body: byteContent,
        contentType: asb:TEXT,
        timeToLive: timeToLive
    };

    asb:MessageBatch messages = {
        messageCount: 2,
        messages: [message1, message2]
    };

    asb:AsbConnectionConfiguration config = {
        connectionString: connectionString
    };

    log:printInfo("Initializing Asb sender client.");
    asb:MessageSender queueSender = check new (connectionString, queueName);

    log:printInfo("Initializing Asb receiver client.");
    asb:MessageReceiver queueReceiver = check new (connectionString, queueName, asb:RECEIVEANDDELETE);

    log:printInfo("Sending via Asb sender client.");
    check queueSender->sendBatch(messages);

    log:printInfo("Receiving from Asb receiver client.");
    asb:MessageBatch|asb:Error? messageReceived = queueReceiver->receiveBatch(maxMessageCount, serverWaitTime);

    if (messageReceived is asb:MessageBatch) {
        foreach asb:Message message in messageReceived.messages {
            if (message.toString() != "") {
                log:printInfo("Reading Received Message : " + message.toString());
            }
        }
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
