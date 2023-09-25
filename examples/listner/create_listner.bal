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

// Listner Configurations
asb:ListenerConfig configuration = {
    connectionString: connectionString
};

listener asb:Listener asbListener = new (configuration);

@asb:ServiceConfig {
    queueName: "test-queue",
    peekLockModeEnabled: true,
    maxConcurrency: 1,
    prefetchCount: 10,
    maxAutoLockRenewDuration: 300
}
service asb:MessageService on asbListener {
    remote function onMessage(asb:Message message, asb:Caller caller) returns asb:Error? {
        log:printInfo("Message received from queue: " + message.toBalString());
        _ = check caller.complete(message);
    }

    remote function onError(asb:ErrorContext context, error 'error) returns asb:Error? {
        log:printInfo("Error received from queue: " + context.toBalString());
    }
};