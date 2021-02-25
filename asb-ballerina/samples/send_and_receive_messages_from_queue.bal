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
configurable string queuePath = ?;

public function main() {

    // Input values
    string stringContent = "This is My Message Body"; 
    byte[] byteContent = stringContent.toBytes();
    json jsonContent = {name: "apple", color: "red", price: 5.36};
    byte[] byteContentFromJson = jsonContent.toJsonString().toBytes();
    map<string> parameters1 = {contentType: "text/plain", messageId: "one"};
    map<string> parameters2 = {contentType: "application/json", messageId: "two", to: "user1", replyTo: "user2", 
        label: "a1", sessionId: "b1", correlationId: "c1", timeToLive: "2"};
    map<string> properties = {a: "propertyValue1", b: "propertyValue2"};
    int maxMessageCount = 3;
    int serverWaitTime = 5;

    asb:ConnectionConfiguration config = {
        connectionString: connectionString,
        entityPath: queuePath
    };

    log:print("Creating Asb sender connection.");
    asb:SenderConnection? senderConnection = checkpanic new (config);

    log:print("Creating Asb receiver connection.");
    asb:ReceiverConnection? receiverConnection = checkpanic new (config);

    if (senderConnection is asb:SenderConnection) {
        log:print("Sending via Asb sender connection.");
        checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContent, parameters1, properties);
        checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContentFromJson, parameters2, properties);
    } else {
        log:printError("Asb sender connection creation failed.");
    }

    if (receiverConnection is asb:ReceiverConnection) {
        log:print("Receiving from Asb receiver connection.");
        var messageReceived = receiverConnection->receiveMessages(serverWaitTime, maxMessageCount);
        if(messageReceived is asb:Messages) {
            int val = messageReceived.getMessageCount();
            log:print("No. of messages received : " + val.toString());
            asb:Message[] messages = messageReceived.getMessages();
            string messageReceived1 =  checkpanic messages[0].getTextContent();
            log:print("Message1 content : " +messageReceived1);
            json messageReceived2 =  checkpanic messages[1].getJSONContent();
            log:print("Message2 content : " +messageReceived2.toString());
        } else {
            log:printError(messageReceived.message());
        }
    } else {
        log:printError("Asb receiver connection creation failed.");
    }

    if (senderConnection is asb:SenderConnection) {
        log:print("Closing Asb sender connection.");
        checkpanic senderConnection.closeSenderConnection();
    }

    if (receiverConnection is asb:ReceiverConnection) {
        log:print("Closing Asb receiver connection.");
        checkpanic receiverConnection.closeReceiverConnection();
    }
}    
