// Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/io;
import ballerina/os;
import ballerina/test;

configurable string connectionString1 = os:getEnv("CONNECTION_STRING");

const string QUEUE_NAME = "queue1";

final ManagementClient clientEp = check new(connectionString1);

@test:Config {
    groups: ["adminClient"]
}
isolated function testCreateQueue() returns error? {
    QueueProperties queueProperties = check clientEp->createQueue(QUEUE_NAME);
    io:println(queueProperties);
    test:assertEquals(queueProperties.topicName, QUEUE_NAME);
    test:assertEquals(queueProperties.status, "Active");
}

@test:Config {
    groups: ["adminClient"],
    dependsOn: [testCreateQueue]
}
isolated function testTopicDeletion() returns error? {
    check clientEp->deleteQueue(QUEUE_NAME);
}