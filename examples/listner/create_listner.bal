// Copyright (c) 2023 WSO2 LLC. (http://www.wso2.org).
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

import ballerina/log;
import ballerinax/asb;

configurable string connectionString = ?;

listener asb:Listener asbListener = check new ({
    connectionString,
    entityConfig: {
        queueName: "test-queue"
    },
    prefetchCount: 10,
    autoComplete: false
});

service asb:Service on asbListener {

    isolated remote function onMessage(asb:Message message, asb:Caller caller) returns error? {
        log:printInfo("Message received from queue: " + message.toBalString());
        return caller->complete();
    }

    isolated remote function onError(asb:MessageRetrievalError 'error) returns error? {
        log:printError("Error occurred while retrieving messages from the queue", 'error);
    }
}
