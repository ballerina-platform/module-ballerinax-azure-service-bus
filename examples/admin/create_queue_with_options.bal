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

// Connection Configurations
configurable string connectionString = ?;

asb:Duration deletion = {
    seconds: 80000,
    nanoseconds: 101
};
asb:Duration dupdue = {
    seconds: 80000,
    nanoseconds: 101
};
asb:Duration ttl = {
    seconds: 70000,
    nanoseconds: 200
};
asb:Duration lockDue = {
    seconds: 90,
    nanoseconds: 200
};
string userMetaData = "Test User Meta Data";
asb:CreateQueueOptions queueConfig = {
    autoDeleteOnIdle: deletion,
    defaultMessageTimeToLive: ttl,
    duplicateDetectionHistoryTimeWindow: dupdue,
    forwardDeadLetteredMessagesTo: "forwardedQueue", //Should be a valid queue name
    forwardTo: "forwardedQueue", //Should be a valid queue name
    lockDuration: lockDue,
    maxDeliveryCount: 10,
    maxMessageSizeInKilobytes: 0,
    maxSizeInMegabytes: 1024,
    enableBatchedOperations: true,
    deadLetteringOnMessageExpiration: true,
    requiresDuplicateDetection: false,
    enablePartitioning: false,
    requiresSession: false,
    userMetadata: userMetaData
};

// This sample demonstrates a scenario where azure service bus connecter is used to 
// create a queue in azure service bus. 

public function main() returns error? {
    log:printInfo("Initializing Asb admin client...");
    asb:Administrator Administrator = check new (connectionString);
    asb:QueueProperties? queue = check Administrator->createQueue("test-queue", queueConfig);
    if (queue is asb:QueueProperties) {
        log:printInfo(queue.toString());
    } else {
        log:printError(queue.toString());
    }
}
