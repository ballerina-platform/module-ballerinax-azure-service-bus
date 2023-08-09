// Copyright (c) 2023 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/log;
import ballerina/test;

configurable string testTopic3 = "topic3";
configurable string nonExistingName = "nonExistingTopicName";
configurable string invalidName = "#TEST";
configurable string testSubscription3 = "subscription3";
configurable string testRule3 = "rule3";
configurable string testQueue3 = "queue3";
configurable string duplicateTopicQueueErrorPrefix = string `Error occurred while processing request: Status code 409, "<Error><Code>409</Code><Detail>SubCode=40900. Conflict. You're requesting an operation that isn't allowed in the resource's current state. To know more visit https://aka.ms/sbResourceMgrExceptions.`;
configurable string duplicateSubscriptionErrorPrefix = string `Error occurred while processing request: Status code 409, "<Error><Code>409</Code><Detail>The messaging entity '`;
configurable string duplicateRuleErrorPrefix = string `Error occurred while processing request: Status code 409, "<Error><Code>409</Code><Detail>The messaging entity `;
configurable string nonExistingQueueError = string `error("Error occurred while processing request: Queue '${nonExistingName}' does not exist.",error("com.azure.core.exception.ResourceNotFoundException: Queue '${nonExistingName}' does not exist."))`;
configurable string nonExistingTopicError = string `error("Error occurred while processing request, Status Code:200",error("com.azure.core.exception.ResourceNotFoundException: Topic '${nonExistingName}' does not exist."))`;
configurable string nonExistingSubscriptionErrorPrefix = string `Error occurred while processing request: Status code 404, "<Error><Code>404</Code><Detail>Entity`;
configurable string nonExistingRuleErrorPrefix = string `error("Error occurred while processing request, Status Code:404",error("com.azure.messaging.servicebus.administration.implementation.models.ServiceBusManagementErrorException: Status code 404, "<Error><Code>404</Code><Detail>Entity`;
configurable string invalidNameErrorPrefix = string `Error occurred while processing request: Status code 400, "<Error><Code>400</Code><Detail>SubCode=40000. 'https://testballerina.servicebus.windows.net/${invalidName}?api-version=2021-05' contains character(s) that is not allowed by Service Bus. Entity segments can contain only letters, numbers, periods (.), hyphens (-), and underscores (_). To know more visit https://aka.ms/sbResourceMgrExceptions.`;
configurable string invalidSubscriptionNameErrorPrefix = string `Error occurred while processing request: Status code 400, "<Error><Code>400</Code><Detail>'sb://testballerina.servicebus.windows.net/${testTopic3}/subscriptions/${invalidName}?api-version=2021-05' contains character(s) that is not allowed by Service Bus. Entity segments can contain only letters, numbers, periods (.), hyphens (-), and underscores (_). To know more visit https://aka.ms/sbResourceMgrExceptions.`;
configurable string invalidRuleNameErrorPrefix = string `Error occurred while processing request: Status code 400, "<Error><Code>400</Code><Detail>'sb://testballerina.servicebus.windows.net/${testTopic3}/subscriptions/${testSubscription3}/rules/${invalidName}?api-version=2021-05' contains character(s) that is not allowed by Service Bus. Entity segments can contain only letters, numbers, periods (.), hyphens (-), and underscores (_). To know more visit https://aka.ms/sbResourceMgrExceptions.`;
int duplicateTopicQueueErrorPrefixLength = duplicateTopicQueueErrorPrefix.length();
int duplicateSubscriptionErrorPrefixLength = duplicateSubscriptionErrorPrefix.length();
int duplicateRuleErrorPrefixLength = duplicateRuleErrorPrefix.length();
int nonExistingSubscriptionErrorPrefixLength = nonExistingSubscriptionErrorPrefix.length();
int nonExistingRuleErrorPrefixLength = nonExistingRuleErrorPrefix.length();
int invalidNameErrorPrefixLength = invalidNameErrorPrefix.length();
int invalidSubscriptionNameErrorPrefixLength = invalidSubscriptionNameErrorPrefix.length();
int invalidRuleNameErrorPrefixLength = invalidRuleNameErrorPrefix.length();

@test:Config {
    groups: ["asb_admin_negative"],
    enable: true
}
function testCreateQTSR() returns error? {
    log:printInfo("[[testCreateQueue/Topic/Subscription/Rule]]");
    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);

    TopicProperties? topicProp = check adminClient->createTopic(testTopic3);
    QueueProperties? queueProp = check adminClient->createQueue(testQueue3);
    SubscriptionProperties? subProp = check adminClient->createSubscription(testTopic3, testSubscription3);
    RuleProperties? ruleProp = check adminClient->createRule(testTopic3, testSubscription3, testRule3);
    if (topicProp is TopicProperties && queueProp is QueueProperties && subProp is SubscriptionProperties && ruleProp is RuleProperties) {
        log:printInfo("Topic, Queue, Subscription and Rule created successfully.");
    } else {
        test:assertFail("Topic, Queue, Subscription and Rule creation failed.");
    }
}

@test:Config {
    groups: ["asb_admin_negative"],
    dependsOn: [testCreateQTSR],
    enable: true
}
function testDuplicateTopicCreation() returns error? {
    log:printInfo("[[testDuplicateTopicCreation]]");

    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);
    TopicProperties|Error? failedReq = adminClient->createTopic(testTopic3);
    test:assertTrue(failedReq is Error, msg = "Duplicate creation failed.");
    test:assertEquals((<Error>failedReq).message().substring(0, duplicateTopicQueueErrorPrefixLength), duplicateTopicQueueErrorPrefix);
}

@test:Config {
    groups: ["asb_admin_negative"],
    dependsOn: [testCreateQTSR],
    enable: true
}
function testDuplicateQueueCreation() returns error? {
    log:printInfo("[[testDuplicateQueueCreation]]");

    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);

    QueueProperties|Error? failedReq = adminClient->createQueue(testQueue3);
    test:assertTrue(failedReq is Error, msg = "Duplicate creation failed.");
    test:assertEquals((<Error>failedReq).message().substring(0, duplicateTopicQueueErrorPrefixLength), duplicateTopicQueueErrorPrefix);
}

@test:Config {
    groups: ["asb_admin_negative"],
    dependsOn: [testCreateQTSR],
    enable: true
}
function testDuplicateSubscriptionCreation() returns error? {
    log:printInfo("[[testDuplicateSubscriptionCreation]]");

    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);

    SubscriptionProperties|Error? failedReq = adminClient->createSubscription(testTopic3, testSubscription3);
    test:assertTrue(failedReq is Error, msg = "Duplicate creation failed.");
    test:assertEquals((<Error>failedReq).message().substring(0, duplicateSubscriptionErrorPrefixLength), duplicateSubscriptionErrorPrefix);

}

@test:Config {
    groups: ["asb_admin_negative"],
    dependsOn: [testCreateQTSR],
    enable: true
}
function testDuplicateRuleCreation() returns error? {
    log:printInfo("[[testDuplicateRuleCreation]]");

    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);

    RuleProperties|Error? failedReq = adminClient->createRule(testTopic3, testSubscription3, testRule3);
    test:assertTrue(failedReq is Error, msg = "Duplicate creation failed.");
    test:assertEquals((<Error>failedReq).message().substring(0, duplicateRuleErrorPrefixLength), duplicateRuleErrorPrefix);

}

@test:Config {
    groups: ["asb_admin_negative"],
    enable: true
}
function testGetNonExistingTopic() returns error? {
    log:printInfo("[[testGetNonExistingTopic]]");

    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);

    TopicProperties|Error? failedReq = adminClient->getTopic(nonExistingName);
    test:assertTrue(failedReq is Error, msg = "Get non existing topic failed.");
    test:assertEquals((<Error>failedReq).toString(), nonExistingTopicError);
}

@test:Config {
    groups: ["asb_admin_negative"],
    enable: true
}
function testGetNonExistingQueue() returns error? {
    log:printInfo("[[testGetNonExistingQueue]]");

    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);

    QueueProperties|Error? failedReq = adminClient->getQueue(nonExistingName);
    test:assertTrue(failedReq is Error, msg = "Get non existing queue failed.");
    test:assertEquals((<Error>failedReq).toString(), nonExistingQueueError);
}

@test:Config {
    groups: ["asb_admin_negative"],
    dependsOn: [testCreateQTSR],
    enable: true
}
function testGetNonExistingSubscription() returns error? {
    log:printInfo("[[testGetNonExistingSubscription]]");

    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);

    SubscriptionProperties|Error? failedReq = adminClient->getSubscription(testTopic3, nonExistingName);
    test:assertTrue(failedReq is Error, msg = "Get non existing subscription failed.");
    test:assertEquals((<Error>failedReq).message().substring(0, nonExistingSubscriptionErrorPrefixLength), nonExistingSubscriptionErrorPrefix);
}

@test:Config {
    groups: ["asb_admin_negative"],
    dependsOn: [testCreateQTSR],
    enable: true
}
function testGetNonExistingRule() returns error? {
    log:printInfo("[[testGetNonExistingRule]]");

    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);

    RuleProperties|Error? failedReq = adminClient->getRule(testTopic3, testSubscription3, nonExistingName);
    test:assertTrue(failedReq is Error, msg = "Get non existing rule failed.");
    test:assertEquals((<Error>failedReq).toString().substring(0, nonExistingRuleErrorPrefixLength), nonExistingRuleErrorPrefix);
}

@test:Config {
    groups: ["asb_admin_negative"],
    enable: true
}
function testCreateInvalidTopic() returns error? {
    log:printInfo("[[testCreateInvalidTopic]]");

    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);

    TopicProperties|Error? failedReq = adminClient->createTopic(invalidName);
    test:assertTrue(failedReq is Error, msg = "Create invalid topic failed.");
    test:assertEquals((<Error>failedReq).message().substring(0, invalidNameErrorPrefixLength), invalidNameErrorPrefix);
}

@test:Config {
    groups: ["asb_admin_negative"],
    enable: true
}
function testCreateInvalidQueue() returns error? {
    log:printInfo("[[testCreateInvalidQueue]]");

    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);

    QueueProperties|Error? failedReq = adminClient->createQueue(invalidName);
    test:assertTrue(failedReq is Error, msg = "Create invalid queue failed.");
    test:assertEquals((<Error>failedReq).message().substring(0, invalidNameErrorPrefixLength), invalidNameErrorPrefix);
}

@test:Config {
    groups: ["asb_admin_negative"],
    dependsOn: [testCreateQTSR],
    enable: true
}
function testCreateInvalidSubscription() returns error? {
    log:printInfo("[[testCreateInvalidSubscription]]");

    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);

    SubscriptionProperties|Error? failedReq = adminClient->createSubscription(testTopic3, invalidName);
    test:assertTrue(failedReq is Error, msg = "Create invalid subscription failed.");
    test:assertEquals((<Error>failedReq).message().substring(0, invalidSubscriptionNameErrorPrefixLength), invalidSubscriptionNameErrorPrefix);
}

@test:Config {
    groups: ["asb_admin_negative"],
    dependsOn: [testCreateQTSR],
    enable: true
}
function testCreateInvalidRule() returns error? {
    log:printInfo("[[testCreateInvalidRule]]");

    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);

    RuleProperties|Error? failedReq = adminClient->createRule(testTopic3, testSubscription3, invalidName);
    test:assertTrue(failedReq is Error, msg = "Create invalid rule failed.");
    test:assertEquals((<Error>failedReq).message().substring(0, invalidRuleNameErrorPrefixLength), invalidRuleNameErrorPrefix);
}

@test:Config {
    groups: ["asb_admin_negative"],
    dependsOn: [testCreateQTSR],
    enable: true
}
function testDeleteQTSR() returns error? {
    log:printInfo("[[testDeleteQueue/Topic/Subscription/Rule]]");

    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);

    check adminClient->deleteTopic(testTopic3);
    check adminClient->deleteQueue(testQueue3);
    log:printInfo("Topic, Queue, Subscription and Rule deleted successfully.");
}
