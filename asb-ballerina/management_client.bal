// Copyright (c) 2023 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
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

import ballerina/jballerina.java as java;

# Ballerina Service Bus connector provides the capability to access Azure Service Bus SDK.
# Service Bus API provides data access to highly reliable queues and publish/subscribe topics of Azure Service Bus with deep feature capabilities.
@display {label: "Azure Service Bus Message Manager", iconPath: "resources/asb.svg"}
public isolated client class ManagementClient {

    final string connectionString;
    final handle managementClientHandle;

    # Initializes the connector. During initialization you can pass the [Shared Access Signature (SAS) authentication credentials](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-sas)
    # Create an [Azure account](https://docs.microsoft.com/en-us/learn/modules/create-an-azure-account/) and 
    # obtain tokens following [this guide](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-portal#get-the-connection-string). 
    # Configure the connection string to have the [required permission](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-sas).
    # 
    # + connectionString - Connection String of Azure service bus
    public isolated function init(@display {label: "Connection String"} string connectionString) returns Error? {
        self.connectionString = connectionString;
        self.managementClientHandle = initManagementClient(java:fromString(self.connectionString));
    }

    // isolated remote function getNamespaceInfo() returns NamespaceInfo|error {
    //     return check getNamespaceInfo(self.managementClientHandle); 
    // }

    // isolated remote function getMessageCountDetailsOfQueue(string entityPath) returns MessageCountDetails|error {
    //     return check getMessageCountDetailsOfQueue(self.managementClientHandle, java:fromString(entityPath)); 
    // }
}

isolated function initManagementClient(handle connectionString) returns handle = @java:Constructor {
    'class: "org.ballerinax.asb.management.ManagementSource",
    paramTypes: ["java.lang.String"]
} external;

isolated function getNamespaceInfo(handle managementClientHandle) returns NamespaceInfo|error = @java:Method {
    'class: "org.ballerinax.asb.management.ManagementSource"
} external;

isolated function getMessageCountDetailsOfQueue(handle managementClientHandle, handle entityPath) returns MessageCountDetails|error = @java:Method {
    'class: "org.ballerinax.asb.management.ManagementSource"
} external;

isolated function createQueue(handle managementClientHandle, handle queueName) returns QueueProperties|error = @java:Method {
    'class: "org.ballerinax.asb.management.ManagementSource"
} external;

isolated function deleteQueue(handle managementClientHandle, handle queueName) returns error? = @java:Method {
    'class: "org.ballerinax.asb.management.ManagementSource"
} external;

isolated function createTopic(handle managementClientHandle, handle topicName) returns TopicProperties|error = @java:Method {
    'class: "org.ballerinax.asb.management.ManagementSource"
} external;

isolated function deleteTopic(handle managementClientHandle, handle topicName) returns error? = @java:Method {
    'class: "org.ballerinax.asb.management.ManagementSource"
} external;

isolated function createSubscription(handle managementClientHandle, handle topicName, handle subscriptionName) returns SubscriptionProperties|error = @java:Method {
    'class: "org.ballerinax.asb.management.ManagementSource"
} external;

isolated function deleteSubscription(handle managementClientHandle, handle topicName, handle subscriptionName) returns error? = @java:Method {
    'class: "org.ballerinax.asb.management.ManagementSource"
} external;
