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
configurable string topicName = ?;
configurable string subscriptionName = ?;

// This sample demonstrates a scneario where azure service bus connecter is used to 
// send a message to a topic using message sender, 
// receive a messsage from a subcription using subscription receiver with PEEK_LOCK mode
// then move the message in a dead letter subscription
// After moving to dead letter subscription,dead letter message will be received using receive(deadLettered = true)
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
        entityType: asb:TOPIC,
        topicOrQueueName: topicName
    };

    asb:ASBServiceReceiverConfig receiverConfig = {
        connectionString: connectionString,
        entityConfig: {
            topicName: topicName,
            subscriptionName: subscriptionName
        },
        receiveMode: asb:PEEK_LOCK
    };

    log:printInfo("Initializing Asb sender client.");
    asb:MessageSender topicSender = check new (senderConfig);

    log:printInfo("Initializing Asb receiver client.");
    asb:MessageReceiver subscriptionReceiver = check new (receiverConfig);

    log:printInfo("Sending via Asb sender client.");
    check topicSender->send(message1);

    log:printInfo("Receiving from Asb receiver client.");
    asb:Message|error? messageReceived = subscriptionReceiver->receive(serverWaitTime);

    if messageReceived is asb:Message {
        check subscriptionReceiver->deadLetter(messageReceived);
        asb:Message|error? messageReceivedFromDLQ = subscriptionReceiver->receive(serverWaitTime, deadLettered = true);

        if messageReceivedFromDLQ is asb:Message {
            log:printInfo("Message received from DLQ.");
            string message_str = check string:fromBytes(<byte[]>messageReceivedFromDLQ.body);
            log:printInfo("DLQ Message content: " + message_str);
        } else if messageReceivedFromDLQ is () {
            log:printError("No message in the queue.");
        } else {
            log:printError("Receiving message via Asb receiver connection failed.");
        }
    } else if messageReceived is () {
        log:printError("No message in the queue.");
    } else {
        log:printError("Receiving message via Asb receiver connection failed.");
    }

    log:printInfo("Closing Asb sender client.");
    check topicSender->close();

    log:printInfo("Closing Asb receiver client.");
    check subscriptionReceiver->close();
}
