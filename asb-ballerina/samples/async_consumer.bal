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
configurable string connectionString = ?;
configurable string queueName = ?;

public function main() {

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

    asb:AsbConnectionConfiguration config = {
        connectionString: connectionString
    };

    asb:AsbClient asbClient = new (config);

    log:printInfo("Creating Asb sender connection.");
    handle queueSender = checkpanic asbClient->createQueueSender(queueName);

    log:printInfo("Sending via Asb sender connection.");
    checkpanic asbClient->send(queueSender, message1);

    asb:Service asyncTestService =
    @asb:ServiceConfig {
        entityConfig: {
            connectionString: connectionString,
            entityPath: queueName
        }
    }
    service object {
        remote function onMessage(asb:Message message) {
            log:printInfo("The message received: " + message.toString());
            // Write your logic here
        }
    };

    asb:Listener? channelListener = new();
    if (channelListener is asb:Listener) {
        checkpanic channelListener.attach(asyncTestService);
        checkpanic channelListener.'start();
        log:printInfo("start listening");
        runtime:sleep(20);
        log:printInfo("end listening");
        checkpanic channelListener.detach(asyncTestService);
        checkpanic channelListener.gracefulStop();
        checkpanic channelListener.immediateStop();
    }

    log:printInfo("Closing Asb sender connection.");
    checkpanic asbClient->closeSender(queueSender);
}    
