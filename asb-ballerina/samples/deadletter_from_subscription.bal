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
configurable string topicPath = ?;
configurable string subscriptionPath1 = ?;
configurable string subscriptionPath2 = ?;
configurable string subscriptionPath3 = ?;

public function main() {

    // Input values
    string stringContent = "This is My Message Body"; 
    byte[] byteContent = stringContent.toBytes();
    json jsonContent = {name: "apple", color: "red", price: 5.36};
    byte[] byteContentFromJson = jsonContent.toJsonString().toBytes();
    map<string> parameters1 = {contentType: "plain/text", messageId: "one"};
    map<string> parameters2 = {contentType: "application/json", messageId: "two", to: "user1", replyTo: "user2", 
        label: "a1", sessionId: "b1", correlationId: "c1", timeToLive: "2"};
    map<string> properties = {a: "propertyValue1", b: "propertyValue2"};

    asb:ConnectionConfiguration senderConfig = {
        connectionString: connectionString,
        entityPath: topicPath
    };

    asb:ConnectionConfiguration receiverConfig1 = {
        connectionString: connectionString,
        entityPath: subscriptionPath1
    };

    asb:ConnectionConfiguration receiverConfig2 = {
        connectionString: connectionString,
        entityPath: subscriptionPath2
    };

    asb:ConnectionConfiguration receiverConfig3 = {
        connectionString: connectionString,
        entityPath: subscriptionPath3
    };

    log:printInfo("Creating Asb sender connection.");
    asb:SenderConnection? senderConnection = checkpanic new (senderConfig);

    log:printInfo("Creating Asb receiver connection.");
    asb:ReceiverConnection? receiverConnection1 = checkpanic new (receiverConfig1);
    asb:ReceiverConnection? receiverConnection2 = checkpanic new (receiverConfig2);
    asb:ReceiverConnection? receiverConnection3 = checkpanic new (receiverConfig3);

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Sending via Asb sender connection.");
        checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContent, parameters1, properties);
        checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContentFromJson, parameters2, properties);
    } else {
        log:printError("Asb sender connection creation failed.");
    }

    if (receiverConnection1 is asb:ReceiverConnection) {
        log:printInfo("Dead-Letter message from Asb receiver connection 1.");
        checkpanic receiverConnection1->deadLetterMessage("deadLetterReason", "deadLetterErrorDescription");
        log:printInfo("Done Dead-Letter a message using its lock token.");
        log:printInfo("Completing messages from Asb receiver connection 1.");
        checkpanic receiverConnection1->completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (receiverConnection2 is asb:ReceiverConnection) {
        log:printInfo("Dead-Letter message from Asb receiver connection 2.");
        checkpanic receiverConnection2->deadLetterMessage("deadLetterReason", "deadLetterErrorDescription");
        log:printInfo("Done Dead-Letter a message using its lock token.");
        log:printInfo("Completing messages from Asb receiver connection 2.");
        checkpanic receiverConnection2->completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (receiverConnection3 is asb:ReceiverConnection) {
        log:printInfo("Dead-Letter message from Asb receiver connection 3.");
        checkpanic receiverConnection3->deadLetterMessage("deadLetterReason", "deadLetterErrorDescription");
        log:printInfo("Done Dead-Letter a message using its lock token.");
        log:printInfo("Completing messages from Asb receiver connection 3.");
        checkpanic receiverConnection3->completeMessages();
        log:printInfo("Done completing messages using their lock tokens.");
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (senderConnection is asb:SenderConnection) {
        log:printInfo("Closing Asb sender connection.");
        checkpanic senderConnection.closeSenderConnection();
    }

    if (receiverConnection1 is asb:ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 1.");
        checkpanic receiverConnection1.closeReceiverConnection();
    }

    if (receiverConnection2 is asb:ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 2.");
        checkpanic receiverConnection2.closeReceiverConnection();
    }

    if (receiverConnection3 is asb:ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection 3.");
        checkpanic receiverConnection3.closeReceiverConnection();
    }
}    
