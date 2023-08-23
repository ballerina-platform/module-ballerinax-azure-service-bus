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
// check whether a subscription exists in a topic.
public function main() returns error? {
    log:printInfo("Initializing Asb admin client...");
    asb:Administrator adminClient = check new (connectionString);
    boolean? subscriptionExists = check adminClient->subscriptionExists("test-topic", "test-subscription");
    if subscriptionExists is boolean {
        log:printInfo("Sub exists: " + subscriptionExists.toString());
    } else {
        log:printError("Sub exists failed.");
    }
}
