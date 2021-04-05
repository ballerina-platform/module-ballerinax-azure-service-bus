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
import ballerina/http;
import ballerinax/asb;

// Connection Configurations
configurable string connectionString = ?;
configurable string queuePath = ?;

service /asb on new http:Listener(9090) {

    resource function get sendAndReceive(http:Caller caller, http:Request req)
            returns error? {

        string stringContent = "This is My Message Body"; 
        byte[] byteContent = stringContent.toBytes();
        json jsonContent = {name: "apple", color: "red", price: 5.36};
        byte[] byteContentFromJson = jsonContent.toJsonString().toBytes();
        map<string> parameters1 = {contentType: "application/json", messageId: "one"};
        map<string> parameters2 = {contentType: "application/json", messageId: "two", to: "sanju", replyTo: "carol", 
        label: "a1", sessionId: "b1", correlationId: "c1", timeToLive: "2"};
        map<string> parameters = {contentType: "application/json", messageId: "one", to: "sanju", replyTo: "carol", 
        label: "a1", sessionId: "b1", correlationId: "c1", timeToLive: "2"};
        map<string> properties = {a: "propertyValue1", b: "propertyValue2"};
        int serverWaitTime = 5;

        asb:ConnectionConfiguration config = {
        connectionString: connectionString,
        entityPath: queuePath
    };

        log:printInfo("Creating Asb sender connection.");
        asb:SenderConnection? senderConnection = checkpanic new (config);

        log:printInfo("Creating Asb receiver connection.");
        asb:ReceiverConnection? receiverConnection = checkpanic new (config);

        if (senderConnection is asb:SenderConnection) {
            log:printInfo("Sending via Asb sender connection.");
            checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContent, parameters1, properties);
            checkpanic senderConnection->sendMessageWithConfigurableParameters(byteContentFromJson, parameters2, properties);
        }

        if (receiverConnection is asb:ReceiverConnection) {
            log:printInfo("Receiving from Asb receiver connection.");
            asb:Message|asb:Error? messageReceived = receiverConnection->receiveMessage(serverWaitTime);
            asb:Message|asb:Error? jsonMessageReceived = receiverConnection->receiveMessage(serverWaitTime);
            if (messageReceived is asb:Message && jsonMessageReceived is asb:Message) {
                string messageRead = checkpanic messageReceived.getTextContent();
                log:printInfo("Reading Received Message : " + messageRead);
                json jsonMessageRead = checkpanic jsonMessageReceived.getJSONContent();
                log:printInfo("Reading Received Message : " + jsonMessageRead.toString());
            } 
        }

        if (senderConnection is asb:SenderConnection) {
            log:printInfo("Closing Asb sender connection.");
            checkpanic senderConnection.closeSenderConnection();
        }

        if (receiverConnection is asb:ReceiverConnection) {
            log:printInfo("Closing Asb receiver connection.");
            checkpanic receiverConnection.closeReceiverConnection();
        }

        check caller->respond("Successful!");
    }
}
