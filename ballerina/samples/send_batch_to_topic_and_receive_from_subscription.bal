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
import ballerinax/azure.sb;

// Connection Configurations
configurable string connectionString = ?;
configurable string topicName = ?;
configurable string subscriptionName = ?;

// This sample demonstrates a scneario where azure service bus connecter is used to 
// send a batch of messages to a topic using topic sender, 
// receive batch of messsages from a subscription using subscription receiver with RECEIVEANDDELETE mode
// (message will be deleted from the queue just after received).
public function main() returns error? {

    // Input values
    string stringContent = "This is My Message Body"; 
    byte[] byteContent = stringContent.toBytes();
    map<string> properties = {a: "propertyValue1", b: "propertyValue2"};
    int timeToLive = 60; // In seconds
    int serverWaitTime = 60; // In seconds
    int maxMessageCount = 2;

    sb:Message message1 = {
        body: byteContent,
        contentType: sb:TEXT,
        timeToLive: timeToLive
    };

    sb:Message message2 = {
        body: byteContent,
        contentType: sb:TEXT,
        timeToLive: timeToLive
    };

    sb:MessageBatch messages = {
        messageCount: 2,
        messages: [message1, message2]
    };

    sb:ASBServiceSenderConfig senderConfig = {
        connectionString: connectionString,
        entityType: sb:TOPIC,
        topicOrQueueName: topicName
    };

    sb:ASBServiceReceiverConfig receiverConfig = {
        connectionString: connectionString,
        entityConfig: {
            topicName: topicName,
            subscriptionName: subscriptionName
        },
        receiveMode: sb:RECEIVE_AND_DELETE
    };

    log:printInfo("Initializing Asb sender client.");
    sb:MessageSender topicSender = check new (senderConfig);

    log:printInfo("Initializing Asb receiver client.");
    sb:MessageReceiver subscriptionReceiver = check new (receiverConfig);

    log:printInfo("Sending via Asb sender client.");
    check topicSender->sendBatch(messages);

    log:printInfo("Receiving from Asb receiver client.");
    sb:MessageBatch|error? messageReceived = subscriptionReceiver->receiveBatch(maxMessageCount, serverWaitTime);

    if (messageReceived is sb:MessageBatch) {
        foreach sb:Message message in messageReceived.messages {
            if (message.toString() != "") {
                log:printInfo("Reading Received Message : " + message.toString());
            }
        }
    } else if (messageReceived is ()) {
        log:printError("No message in the subscription.");
    } else {
        log:printError("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender client.");
    check topicSender->close();

    log:printInfo("Closing Asb receiver client.");
    check subscriptionReceiver->close();
}    
