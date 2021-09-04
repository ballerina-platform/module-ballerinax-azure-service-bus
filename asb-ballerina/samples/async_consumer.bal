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
import ballerina/lang.runtime;
import ballerinax/asb;

// Connection Configurations
configurable string connectionString;
configurable string queueName;

public function main() returns error? {

    // Input values
    string stringContent = "This is My Message Body"; 
    byte[] byteContent = stringContent.toBytes();
    map<string> properties = {a: "propertyValue1", b: "propertyValue2"};
    int timeToLive = 60; // In seconds

    asb:ApplicationProperties applicationProperties = {
        properties: {a: "propertyValue1", b: "propertyValue2"}
    };

    asb:Message message1 = {
        body: byteContent,
        contentType: asb:TEXT,
        timeToLive: timeToLive,
        applicationProperties: applicationProperties
    };

    log:printInfo("Initializing Asb sender client.");
    asb:MessageSender queueSender = check new (connectionString, queueName);

    log:printInfo("Sending via Asb sender client.");
    check queueSender->send(message1);

    asb:Service asyncTestService =
    service object {
        remote function onMessage(asb:Message message, asb:Caller caller) {
            log:printInfo("The message received: " + message.toString());
            // Write your logic here
        }
    };

    asb:Listener? channelListener = check new (connectionString, queueName);
    if (channelListener is asb:Listener) {
        check channelListener.attach(asyncTestService);
        check channelListener.'start();
        log:printInfo("start listening");
        runtime:sleep(20);
        log:printInfo("end listening");
        check channelListener.detach(asyncTestService);
        check channelListener.gracefulStop();
        check channelListener.immediateStop();
    }

    log:printInfo("Closing Asb sender client.");
    check queueSender->close();
}    
