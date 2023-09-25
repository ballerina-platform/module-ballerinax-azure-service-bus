// Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org).
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

import ballerina/test;
import ballerina/log;
import ballerina/lang.runtime;

boolean testSuccessfullyCompleted = false;
string lisnterTestQueueName = "pre-created-test-queue";

ListenerConfig configuration = {
    connectionString: connectionString
};

listener Listener asbListener = new (configuration);

@ServiceConfig {
    queueName: "pre-created-test-queue",
    peekLockModeEnabled: true,
    maxConcurrency: 1,
    prefetchCount: 20,
    maxAutoLockRenewDuration: 300
}

service MessageService on asbListener {
    remote function onMessage(Message message, Caller caller) returns error? {
        Error? result = caller.complete(message);
        log:printInfo("Message received"+message.body.toString());
        if (message.body == byteContent && result is ()) {
            testSuccessfullyCompleted = true;
        }
        return;
    }
    remote function onError(ErrorContext context, error 'error) returns error? {
        return;
    }
};

@test:Config {
    enable: true,
    groups: ["asb_listner"]
}
function testListner() returns error? {
    log:printInfo("[[testListner]]");
    ASBServiceSenderConfig listnerTestSenderConfig = {
        connectionString: connectionString,
        entityType: QUEUE,
        topicOrQueueName: lisnterTestQueueName
    };
    log:printInfo("Initializing Asb sender client.");
    MessageSender queueSender = check new (listnerTestSenderConfig);

    log:printInfo("Sending via Asb sender client.");
    check queueSender->send(message1);
 
    log:printInfo("Closing Asb sender client.");
    check queueSender->close();

    int counter = 20;
    while (!testSuccessfullyCompleted && counter >= 0) {
        runtime:sleep(1);
        log:printInfo("Waiting for the message to be received");
        counter -= 1;
    }
    test:assertTrue(testSuccessfullyCompleted, msg = "ASB listener did not receive the message");
}
