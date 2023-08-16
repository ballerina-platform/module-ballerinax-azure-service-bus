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

// This sample demonstrates a scenario where azure service bus connecter is used to 
// retrieve a queue from the service bus instance.
public function main() returns error? {
    log:printInfo("Initializing Asb admin client...");
    asb:Administrator Administrator = check new (connectionString);
    asb:QueueProperties? queue = check Administrator->getQueue("test-queue");
    if queue is asb:QueueProperties {
        log:printInfo(queue.toString());
    } else {
        log:printError(queue.toString());
    }
}
