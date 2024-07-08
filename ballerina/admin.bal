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

import ballerina/jballerina.java as java;

# Ballerina Service Bus connector provides the capability to access Azure Service Bus SDK.
# Service Bus API provides data access to highly reliable queues and publish/subscribe topics of Azure Service Bus with deep feature capabilities.
@display {label: "Azure Service Bus Administrator", iconPath: "icon.png"}
public isolated client class Administrator {

    # Initialize the Azure Service Bus Admin client.
    # Create an [Azure account](https://docs.microsoft.com/en-us/learn/modules/create-an-azure-account/) and
    # obtain tokens following [this guide](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-portal#get-the-connection-string).
    # Configure the connection string to have the [required permission](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-sas).
    #
    # + connectionString - Azure Service Bus connection string
    public isolated function init(@display {label: "Azure Service Bus connection string"} string connectionString) returns Error? {
        check initializeAdministrator(self, java:fromString(connectionString));
    }

    # Create a topic with the given name or name and options.
    #
    # + topicName - Topic name
    # + topicOptions - Topic options to create the topic.This should be a record of type CreateTopicOptions
    # + return - Topic properties(Type of asb:TopicProperies) or error
    @display {label: "Create Topic"}
    isolated remote function createTopic(@display {label: "Topic"} string topicName, @display {label: "Topic Options"} *CreateTopicOptions topicOptions) returns TopicProperties|Error? = @java:Method {
        'class: "admin.io.ballerina.lib.asb.Administrator"
    } external;

    # Get the topic with the given name.
    #
    # + topicName - Topic name
    # + return - Topic properties(Type of asb:TopicProperies) or error
    @display {label: "Get Topic"}
    isolated remote function getTopic(@display {label: "Topic"} string topicName) returns TopicProperties|Error? = @java:Method {
        'class: "admin.io.ballerina.lib.asb.Administrator"
    } external;

    # Update the topic with the given options.
    #
    # + topicName - Topic name
    # + topicOptions - Topic options to update the topic.This should be a record of type UpdateTopicOptions
    # + return - Topic properties(Type of asb:TopicProperies) or error
    @display {label: "Update Topics"}
    isolated remote function updateTopic(@display {label: "Topic"} string topicName, @display {label: "Update Topic Options"} *UpdateTopicOptions topicOptions) returns TopicProperties|Error? = @java:Method {
        'class: "admin.io.ballerina.lib.asb.Administrator"
    } external;

    # List the topics.
    #
    # + return - Topic list(Type of asb:TopicList) or error
    @display {label: "List Topics"}
    isolated remote function listTopics() returns TopicList|Error? = @java:Method {
        'class: "admin.io.ballerina.lib.asb.Administrator"
    } external;

    # Delete the topic with the given name.
    #
    # + topicName - Topic name
    # + return - Error or nil
    @display {label: "Delete Topic"}
    isolated remote function deleteTopic(@display {label: "Topic"} string topicName) returns Error? = @java:Method {
        'class: "admin.io.ballerina.lib.asb.Administrator"
    } external;

    # Create a subscription with the given name or name and options.
    #
    # + topicName - Name of the topic associated with subscription
    # + subscriptionName - Name of the subscription
    # + subscriptionOptions - Subscription options to create the subscription.This should be a record of type CreateSubscriptionOptions.
    # + return - Subscription properties(Type of asb:SubscriptionProperies) or error
    @display {label: "Create Subscription"}
    isolated remote function createSubscription(@display {label: "Topic"} string topicName, @display {label: "Subscription"} string subscriptionName, @display {label: "Subscription Options"} *CreateSubscriptionOptions subscriptionOptions) returns SubscriptionProperties|Error? = @java:Method {
        'class: "admin.io.ballerina.lib.asb.Administrator"
    } external;

    # Get the subscription with the given name.
    #
    # + topicName - Name of the topic associated with subscription
    # + subscriptionName - Name of the subscription
    # + return - Subscription properties(Type of asb:SubscriptionProperies) or error
    @display {label: "Get Subscription"}
    isolated remote function getSubscription(@display {label: "Topic"} string topicName, @display {label: "Subscription"} string subscriptionName) returns SubscriptionProperties|Error? = @java:Method {
        'class: "admin.io.ballerina.lib.asb.Administrator"
    } external;

    # Update the subscription with the given options.
    #
    # + topicName - Name of the topic associated with subscription
    # + subscriptionName - Name of the subscription
    # + subscriptionOptions - Subscription options to update the subscription.This should be a record of type UpdateSubscriptionOptions
    # + return - Subscription properties(Type of asb:SubscriptionProperies) or error
    @display {label: "Update Subscription"}
    isolated remote function updateSubscription(@display {label: "Topic"} string topicName, @display {label: "Subscription"} string subscriptionName, @display {label: "Update Subscription Options"} *UpdateSubscriptionOptions subscriptionOptions) returns SubscriptionProperties|Error? = @java:Method {
        'class: "admin.io.ballerina.lib.asb.Administrator"
    } external;

    # List the subscriptions.
    #
    # + topicName - Name of the topic associated with subscription
    # + return - Subscription list(Type of asb:SubscriptionList) or error
    @display {label: "List Subscriptions"}
    isolated remote function listSubscriptions(@display {label: "Topic"} string topicName) returns SubscriptionList|Error? = @java:Method {
        'class: "admin.io.ballerina.lib.asb.Administrator"
    } external;

    # Delete the subscription with the given name.
    #
    # + topicName - Topic name
    # + subscriptionName - Subscription name
    # + return - Error or nil
    @display {label: "Delete Subscription"}
    isolated remote function deleteSubscription(@display {label: "Topic"} string topicName, @display {label: "Subscription"} string subscriptionName) returns Error? = @java:Method {
        'class: "admin.io.ballerina.lib.asb.Administrator"
    } external;

    # Get the status of existance of a topic with the given name.
    #
    # + topicName - Topic name
    # + return - Boolean or error
    @display {label: "is Topic Exists"}
    isolated remote function topicExists(@display {label: "Exists"} string topicName) returns boolean|Error? = @java:Method {
        'class: "admin.io.ballerina.lib.asb.Administrator"
    } external;

    # Get the status of existance of a subscription with the given name.
    #
    # + topicName - Topic name
    # + subscriptionName - Subscription name
    # + return - Boolean or error
    @display {label: "is Subscription Exists"}
    isolated remote function subscriptionExists(@display {label: "Topic"} string topicName, @display {label: "Subscription"} string subscriptionName) returns boolean|Error? = @java:Method {
        'class: "admin.io.ballerina.lib.asb.Administrator"
    } external;

    # Create a rule with the given name or name and options.
    #
    # + topicName - Name of the topic associated with subscription
    # + subscriptionName - Name of the subscription
    # + ruleName - Name of the rule
    # + ruleOptions - Rule options to create the rule.This should be a record of type CreateRuleOptions
    # + return - Rule properties(Type of asb:RuleProperies) or error
    @display {label: "Create Rule"}
    isolated remote function createRule(@display {label: "Topic"} string topicName, @display {label: "Subscription"} string subscriptionName, @display {label: "Rule"} string ruleName, @display {label: "Rule Options"} *CreateRuleOptions ruleOptions) returns RuleProperties|Error? = @java:Method {
        'class: "admin.io.ballerina.lib.asb.Administrator"
    } external;

    # Delete the rule with the given name.
    #
    # + topicName - Name of the topic associated with subscription
    # + subscriptionName - Name of the subscription associated with rule
    # + ruleName - Rule name
    # + return - Error or nil
    @display {label: "Get Rule"}
    isolated remote function getRule(@display {label: "Topic"} string topicName, @display {label: "Subscription"} string subscriptionName, @display {label: "Rule"} string ruleName) returns RuleProperties|Error? = @java:Method {
        'class: "admin.io.ballerina.lib.asb.Administrator"
    } external;

    # Update the rule with the options.
    #
    # + topicName - Name of the topic associated with subscription
    # + subscriptionName - Name of the subscription associated with rule
    # + ruleName - Rule name
    # + ruleOptions - Rule options to update the rule.This should be a record of type UpdateRuleOptions
    # + return - Rule properties(Type of asb:RuleProperies) or error
    @display {label: "Update Rule"}
    isolated remote function updateRule(@display {label: "Topic"} string topicName, @display {label: "Subscription"} string subscriptionName, @display {label: "Rule"} string ruleName, @display {label: "Update Rule Options"} *UpdateRuleOptions ruleOptions) returns RuleProperties|Error? = @java:Method {
        'class: "admin.io.ballerina.lib.asb.Administrator"
    } external;

    # List the rules.
    #
    # + topicName - Name of the topic associated with subscription
    # + subscriptionName - Name of the subscription
    # + return - Rule list(Type of asb:RuleList) or error
    @display {label: "List Rules"}
    isolated remote function listRules(@display {label: "Topic"} string topicName, @display {label: "Subscription"} string subscriptionName) returns RuleList|Error? = @java:Method {
        'class: "admin.io.ballerina.lib.asb.Administrator"
    } external;

    # Delete the rule with the given name.
    #
    # + topicName - Name of the topic associated with subscription
    # + subscriptionName - Name of the subscription associated with rule
    # + ruleName - Rule name
    # + return - Error or nil
    @display {label: "Delete Rule"}
    isolated remote function deleteRule(@display {label: "Topic"} string topicName, @display {label: "Subscription"} string subscriptionName, @display {label: "Rule"} string ruleName) returns Error? = @java:Method {
        'class: "admin.io.ballerina.lib.asb.Administrator"
    } external;

    # Create a queue with the given name or name and options.
    #
    # + queueName - Name of the queue
    # + queueOptions - Queue options to create the queue.This should be a record of type CreateQueueOptions
    # + return - Queue properties(Type of asb:QueueProperties) or error
    @display {label: "Create Queue"}
    isolated remote function createQueue(@display {label: "Queue"} string queueName, @display {label: "Queue Options"} *CreateQueueOptions queueOptions) returns QueueProperties|Error? = @java:Method {
        'class: "admin.io.ballerina.lib.asb.Administrator"
    } external;

    # Get the queue with the given name.
    #
    # + queueName - Name of the queue
    # + return - Queue properties(Type of asb:QueueProperties) or error
    @display {label: "Get Queue"}
    isolated remote function getQueue(@display {label: "Queue"} string queueName) returns QueueProperties|Error? = @java:Method {
        'class: "admin.io.ballerina.lib.asb.Administrator"
    } external;

    # Update the queue with the options.Q
    #
    # + queueName - Name of the queue
    # + queueOptions - Queue options to update the queue.This should be a record of type UpdateQueueOptions
    # + return - Queue properties(Type of asb:QueueProperties) or error
    @display {label: "Update Queue"}
    isolated remote function updateQueue(@display {label: "Queue"} string queueName, @display {label: "Update Queue Options"} *UpdateQueueOptions queueOptions) returns QueueProperties|Error? = @java:Method {
        'class: "admin.io.ballerina.lib.asb.Administrator"
    } external;

    # List the queues.
    #
    # + return - Queue list(Type of asb:QueueList) or error
    isolated remote function listQueues() returns QueueList|Error? = @java:Method {
        'class: "admin.io.ballerina.lib.asb.Administrator"
    } external;

    # Delete the queue with the given name.
    #
    # + queueName - Name of the queue
    # + return - Error or nil
    @display {label: "Delete Queue"}
    isolated remote function deleteQueue(@display {label: "Queue"} string queueName) returns Error? = @java:Method {
        'class: "admin.io.ballerina.lib.asb.Administrator"
    } external;

    # Check whether the queue exists.
    #
    # + queueName - Name of the queue
    # + return - Boolean or error
    @display {label: "is Queue Exists"}
    isolated remote function queueExists(@display {label: "Exists"} string queueName) returns boolean|Error? = @java:Method {
        'class: "admin.io.ballerina.lib.asb.Administrator"
    } external;
}

isolated function initializeAdministrator(Administrator adminClient, handle connectionString) returns Error? = @java:Method {
    'class: "admin.io.ballerina.lib.asb.Administrator"
} external;
