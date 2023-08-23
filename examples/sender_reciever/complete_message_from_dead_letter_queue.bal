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
// send a message to a queue using message sender, receive that message using message receiver with PEEKLOCK mode, 
// then move the message in a DLQ (dead letter queue)
// After moving to DLQ, recieve the DQL message using receive(deadLettered = true)
// Then complete the DLQ message and receive the dlq messages again to check whether the message is settled from DLQ
public function main() returns error? {

    // Input values
    string stringContent = "This is message goes to DLQ.";
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
        receiveMode: asb:PEEK_LOCK
    };

    log:printInfo("Initializing Asb sender client.");
    asb:MessageSender queueSender = check new (senderConfig);

    log:printInfo("Initializing Asb receiver client.");
    asb:MessageReceiver queueReceiver = check new (receiverConfig);

    log:printInfo("Sending via Asb sender client.");
    check queueSender->send(message1);

    log:printInfo("Receiving from Asb receiver client.");
    asb:Message|error? messageReceived = queueReceiver->receive(serverWaitTime);

    if (messageReceived is asb:Message) {

        //deadLetter the message
        check queueReceiver->deadLetter(messageReceived);

        //get message from DLQ
        log:printInfo("Receiving from DLQ via Asb receiver client.");
        asb:Message|error? messageReceivedFromDLQ = queueReceiver->receive(serverWaitTime, deadLettered = true);
        if (messageReceivedFromDLQ is asb:Message) {

            //Check whether the message received from DLQ
            log:printInfo("Message received from DLQ.");
            string? message_id = messageReceivedFromDLQ.messageId;
            if (message_id is string) {
                log:printInfo("DLQ Top Message ID: " + message_id);
            }

            //Complete the DLQ Message
            log:printInfo("Completing the DLQ message.");
            check queueReceiver->complete(messageReceivedFromDLQ, deadLettered = true);

            //Receive the message from DLQ after complete
            log:printInfo("Receiving from DLQ via Asb receiver client after complete.");
            asb:Message|error? checkReceivingDLQAfterComplete = queueReceiver->receive(serverWaitTime, deadLettered = true);
            if (checkReceivingDLQAfterComplete is asb:Message) { //if there are any messages in the DLQ
                log:printInfo("Message received from DLQ.");
                message_id = checkReceivingDLQAfterComplete.messageId;
                string source = checkReceivingDLQAfterComplete.deadLetterSource;
                if (message_id is string) {
                    log:printInfo("DLQ Top Message ID: " + message_id + source);
                }
            } else if (checkReceivingDLQAfterComplete is ()) { //if there are no messages in the DLQ
                log:printError("No message in the deadletter queue.");
            } else {
                log:printError("Receiving message via Asb receiver connection failed.");
            }
        } else if (messageReceivedFromDLQ is ()) {
            log:printError("No message in the DLQ.");
        } else {
            log:printError("Receiving message via ASBReceiver:DLQ connection failed.");
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
