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

import ballerina/lang.runtime;
import ballerina/log;
import ballerina/test;

// Connection Configurations
string listenerTestQueueName = "pre-created-test-queue";

ASBServiceSenderConfig listnerTestSenderConfig = {
    connectionString: connectionString,
    entityType: QUEUE,
    topicOrQueueName: listenerTestQueueName
};

ListenerConfiguration configuration = {
    connectionString: connectionString,
    entityConfig: {
        queueName: listenerTestQueueName
    },
    autoComplete: false,
    maxConcurrency: 1,
    prefetchCount: 20,
    maxAutoLockRenewDuration: 300
};

listener Listener asbListener = new (configuration);

enum State {
    NONE,
    RECIEVED,
    RECIEVED_AND_COMPLETED,
    RECIEVED_AND_ABANDON,
    RECIEVED_AND_DLQ,
    RECIEVED_AND_DEFER
}

isolated State listnerState = NONE;

isolated function getListnerState() returns State
{
    lock {
        return listnerState;
    }
}

isolated function setListnerState(State state)
{
    lock {
        listnerState = state;
    }
}

isolated int testCaseCounter = 0;

isolated function getTestCaseCount() returns int
{
    lock {
        return testCaseCounter;
    }
}

isolated function increamentTestCaseCount()
{
    lock {
        testCaseCounter += 1;
    }
}

service on asbListener {
    isolated remote function onMessage(Message message, Caller caller) returns error? {
        if message.body == "This is ASB connector test-Message Body".toBytes() {
            if getTestCaseCount() == 0 {
                setListnerState(RECIEVED);
            } else if getTestCaseCount() == 1 {
                Error? result = caller->complete();
                if result is () {
                    setListnerState(RECIEVED_AND_COMPLETED);
                }
            }
            else if getTestCaseCount() == 2 {
                Error? result = caller->defer();
                if result is () {
                    setListnerState(RECIEVED_AND_DEFER);
                }
            }
            else if getTestCaseCount() == 3 {
                Error? result = caller->deadLetter(
                    deadLetterReason = "Testing Purpose", 
                    deadLetterErrorDescription = "Manual DLQ : Testing Purpose");
                if result is () {
                    setListnerState(RECIEVED_AND_DLQ);
                }
            }
            else if getTestCaseCount() == 4 {
                Error? result = caller->abandon();
                if result is () {
                    setListnerState(RECIEVED_AND_ABANDON);
                }
            }
            increamentTestCaseCount();
        }
    }

    isolated remote function onError(ErrorContext context, error 'error) returns error? {}
}

function sendMessage() returns error? {
    MessageSender queueSender = check new (listnerTestSenderConfig);
    check queueSender->send(message);
    check queueSender->close();
}

@test:Config {
    enable: false,
    groups: ["asb_listner"]
}
function testListnerReceive() returns error? {
    log:printInfo("[[testListnerReceive]]");
    log:printInfo("Sending via Asb sender client.");
    check sendMessage();
    int counter = 10;
    while (getListnerState() != RECIEVED && counter >= 0) {
        runtime:sleep(1);
        log:printInfo("Waiting for the message to be received");
        counter -= 1;
    }
    test:assertTrue(getListnerState() == RECIEVED, msg = "ASB listener did not receive the message");
}

@test:Config {
    enable: false,
    groups: ["asb_listner"],
    dependsOn: [testListnerReceive]
}
function testListnerReceiveAndCompleted() returns error? {
    log:printInfo("[[testListnerReceiveAndCompleted]]");
    log:printInfo("Sending via Asb sender client.");
    check sendMessage();
    int counter = 10;
    while (getListnerState() != RECIEVED_AND_COMPLETED && counter >= 0) {
        runtime:sleep(1);
        log:printInfo("Waiting for the message to be received");
        counter -= 1;
    }
    test:assertTrue(getListnerState() == RECIEVED_AND_COMPLETED, msg = "ASB listener did not receive and completed the message");
}

@test:Config {
    enable: false,
    groups: ["asb_listner"],
    dependsOn: [testListnerReceiveAndCompleted]
}
function testListnerReceiveAndDefer() returns error? {
    log:printInfo("[[testListnerReceiveAndDefer]]");
    log:printInfo("Sending via Asb sender client.");
    check sendMessage();
    int counter = 10;
    while (getListnerState() != RECIEVED_AND_DEFER && counter >= 0) {
        runtime:sleep(1);
        log:printInfo("Waiting for the message to be received");
        counter -= 1;
    }
    test:assertTrue(getListnerState() == RECIEVED_AND_DEFER, msg = "ASB listener did not receive and defer the message");
}

@test:Config {
    enable: false,
    groups: ["asb_listner"],
    dependsOn: [testListnerReceiveAndDefer]
}
function testListnerReceiveAndDLQ() returns error? {
    log:printInfo("[[testListnerReceiveAndDLQ]]");
    log:printInfo("Sending via Asb sender client.");
    check sendMessage();
    int counter = 10;
    while (getListnerState() != RECIEVED_AND_DLQ && counter >= 0) {
        runtime:sleep(1);
        log:printInfo("Waiting for the message to be received");
        counter -= 1;
    }
    test:assertTrue(getListnerState() == RECIEVED_AND_DLQ, msg = "ASB listener did not receive and DLQ the message");
}

@test:Config {
    enable: false,
    groups: ["asb_listner"],
    dependsOn: [testListnerReceiveAndDLQ]
}
function testListnerReceiveAndAbandon() returns error? {
    log:printInfo("[[testListnerReceiveAndAbandon]]");
    log:printInfo("Sending via Asb sender client.");
    check sendMessage();
    int counter = 10;
    while (getListnerState() != RECIEVED_AND_ABANDON && counter >= 0) {
        runtime:sleep(1);
        log:printInfo("Waiting for the message to be received");
        counter -= 1;
    }
    test:assertTrue(getListnerState() == RECIEVED_AND_ABANDON, msg = "ASB listener did not receive and abandon the message");
}
