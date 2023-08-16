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

// Connection Configurations
configurable string connectionString = ?;

// This sample demonstrates a scenario where azure service bus connecter is used to 
// create a subscription in a topic.
public function main() returns error? {
    log:printInfo("Initializing Asb admin client...");
    asb:Administrator Administrator = check new (connectionString);
    asb:TopicProperties? topic = check Administrator->createTopic("test-topic",
        autoDeleteOnIdle = deletion,
        defaultMessageTimeToLive = ttl,
        duplicateDetectionHistoryTimeWindow = dupdue,
        enableBatchedOperations = true,
        enablePartitioning = false,
        maxSizeInMegabytes = 1024,
        requiresDuplicateDetection = false,
        status = asb:ACTIVE,
        userMetadata = userMetaData,
        lockDuration = lockDue,
        maxDeliveryCount = 10,
        maxMessageSizeInKilobytes = 0,
        requiresSession = false,
        deadLetteringOnMessageExpiration = true
    );
    if (topic is asb:TopicProperties) {
        log:printInfo(topic.toString());
    } else {
        log:printError(topic.toString());
    }
}
