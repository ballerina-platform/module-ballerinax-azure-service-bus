// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 import ballerina/os;
import ballerina/test;

// Connection Configurations
 configurable string connectionString = os:getEnv("CONNECTION_STRING");
configurable string testTopic1 = "topic1";
configurable string testTopic2 = "topic2";
configurable string testSubscription1 = "subscription1";
configurable string testSubscription2 = "subscription2";
configurable string testRule1 = "rule1";
configurable string testQueue1 = "queue1";
configurable string testQueue2 = "queue2";
configurable string testRule2 = "rule2";
string subscriptionPath1 = testSubscription1;
string userMetaData="Test User Meta Data";

SqlRule rule = {
    filter: "1=1",
    action: "SET a = 'b'"
};

Duration deletion = {
    seconds: 80000,
    nanoseconds: 101
};
Duration dupdue = {
    seconds: 80000,
    nanoseconds: 101
};
Duration ttl = {
    seconds: 70000,
    nanoseconds: 200
};
Duration lockDue = {
    seconds: 90,
    nanoseconds: 200
};
CreateTopicOptions topicConfig = {
    autoDeleteOnIdle: deletion,
    defaultMessageTimeToLive: ttl,
    duplicateDetectionHistoryTimeWindow: dupdue,
    lockDuration: lockDue,
    maxDeliveryCount: 10,
    maxMessageSizeInKilobytes: 256,
    maxSizeInMegabytes: 1024,
    status: ACTIVE,
    userMetadata: userMetaData,
    enableBatchedOperations: true,
    deadLetteringOnMessageExpiration: true,
    requiresDuplicateDetection: false,
    enablePartitioning: false,
    requiresSession: false,
    supportOrdering: false
};
UpdateTopicOptions updateTopicConfig = {
    autoDeleteOnIdle: deletion,
    defaultMessageTimeToLive: ttl,
    duplicateDetectionHistoryTimeWindow: dupdue,
    maxDeliveryCount: 10,
    maxMessageSizeInKilobytes: 256,
    maxSizeInMegabytes: 1024,
    status: ACTIVE,
    userMetadata: userMetaData,
    enableBatchedOperations: true,
    deadLetteringOnMessageExpiration: true,
    requiresDuplicateDetection: false,
    enablePartitioning: false,
    requiresSession: false,
    supportOrdering: false
};
CreateSubscriptionOptions subConfig = {
    autoDeleteOnIdle: deletion,
    enableBatchedOperations: true,
    deadLetteringOnMessageExpiration: true,
    defaultMessageTimeToLive: ttl,
    deadLetteringOnFilterEvaluationExceptions: true,
    forwardDeadLetteredMessagesTo: testQueue1,
    forwardTo: testQueue1,
    requiresSession: false,
    lockDuration: lockDue,
    maxDeliveryCount: 10,
    status:ACTIVE,
    userMetadata: userMetaData
};
UpdateSubscriptionOptions updateSubConfig={
    autoDeleteOnIdle: deletion,
    defaultMessageTimeToLive: ttl,
    deadLetteringOnMessageExpiration: true,
    deadLetteringOnFilterEvaluationExceptions: true,
    enableBatchedOperations: true,
    forwardDeadLetteredMessagesTo: testQueue1,
    forwardTo: testQueue1,
    lockDuration: lockDue,
    maxDeliveryCount: 10,
    status: ACTIVE,
    userMetadata: userMetaData
};
CreateQueueOptions queueConfig = {
    autoDeleteOnIdle: deletion,
    defaultMessageTimeToLive: ttl,
    duplicateDetectionHistoryTimeWindow: dupdue,
    forwardDeadLetteredMessagesTo: testQueue1,
    forwardTo: testQueue1,
    lockDuration: lockDue,
    maxDeliveryCount: 10,
    maxMessageSizeInKilobytes: 0,
    maxSizeInMegabytes: 1024,
    status: ACTIVE,
    userMetadata: userMetaData,
    enableBatchedOperations: true,
    deadLetteringOnMessageExpiration: true,
    requiresDuplicateDetection: false,
    enablePartitioning: false,
    requiresSession: false
};
UpdateQueueOptions updateQueueConfig = {
    autoDeleteOnIdle: deletion,
    defaultMessageTimeToLive: ttl,
    duplicateDetectionHistoryTimeWindow: dupdue,
    forwardDeadLetteredMessagesTo: testQueue2,
    lockDuration: lockDue,
    maxDeliveryCount: 10,
    maxMessageSizeInKilobytes: 0,
    maxSizeInMegabytes: 1024,
    status: ACTIVE,
    userMetadata: userMetaData,
    enableBatchedOperations: true,
    deadLetteringOnMessageExpiration: true,
    requiresDuplicateDetection: false,
    enablePartitioning: false,
    requiresSession: false
};
CreateRuleOptions ruleConfig = {
    rule: rule
};
UpdateRuleOptions updateRuleConfig = {
    rule: rule
};


@test:Config {
    groups: ["asb_admin"],
    enable: true
}
function testCreateQueue() returns error? {
    log:printInfo("[[testCreateQueue]]");
    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);
    QueueProperties? queueProp = check adminClient->createQueue(testQueue1);
    if (queueProp is QueueProperties) {
        log:printInfo("Queue created successfully.");
    } else {
        test:assertFail("Queue creation failed.");
    }
}
@test:Config {
    groups: ["asb_admin"],
    dependsOn: [testCreateQueue],
    enable: true
}
function testQueueExists() returns error? {
    log:printInfo("[[testQueueExists]]");
    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);
    boolean? queueExists = check adminClient->queueExists(testQueue1);
    if (queueExists is boolean) {
        test:assertEquals(queueExists, true, msg = "Queue exists failed.");
    } else {
        test:assertFail("Queue exists failed.");
    }
}
@test:Config {
    groups: ["asb_admin"],
    enable: true
}
function testCreateTopicOperation() returns error? {
    log:printInfo("[[testCreateTopicOperation]]");

    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);
    TopicProperties? topicProp = check adminClient->createTopic(testTopic1);
    if (topicProp is TopicProperties) {
        log:printInfo("Topic created successfully.");
    } else {
        test:assertFail("Topic creation failed.");
    }
}
@test:Config {
    groups: ["asb_admin"],
    dependsOn: [testCreateTopicOperation],
    enable: true
}
function testTopicExists() returns error? {
    log:printInfo("[[testTopicExists]]");
    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);
    boolean? topicExists = check adminClient->topicExists(testTopic1);
    if (topicExists is boolean) {
        test:assertEquals(topicExists, true, msg = "Topic creation failed.");
    } else {
        test:assertFail("Topic exists failed.");
    }
}
@test:Config {
    groups: ["asb_admin"],
    dependsOn: [testCreateTopicOperation],
    enable: true
}
function testCreateSubscription() returns error? {
    log:printInfo("[[testCreateSubscription]]");
    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);
    SubscriptionProperties? subscriptionProp = check adminClient->createSubscription(testTopic1, testSubscription1);
    if (subscriptionProp is SubscriptionProperties) {
        log:printInfo("Subscription created successfully.");
        test:assertEquals(subscriptionProp.status, "Active", msg = "Subscription creation failed.");
    } else {
        test:assertFail("Subscription creation failed.");
    }
}
@test:Config {
    groups: ["asb_admin"],
    dependsOn: [testCreateSubscription],
    enable: true
}
function testSubscriptionExists() returns error? {
    log:printInfo("[[testSubscriptionExists]]");
    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);
    boolean? subscriptionExists = check adminClient->subscriptionExists(testTopic1, testSubscription1);
    if (subscriptionExists is boolean) {
        test:assertEquals(subscriptionExists, true, msg = "Subscription exists failed.");
    } else {
        test:assertFail("Subscription exists failed.");
    }
}

@test:Config {
    groups: ["asb_admin"],
    dependsOn: [testCreateSubscription],
    enable: true
}
function testCreateRule() returns error? {
    log:printInfo("[[testCreateRule]]");
    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);
    RuleProperties? ruleProp = check adminClient->createRule(testTopic1, testSubscription1, testRule1);
    if (ruleProp is RuleProperties) {
        log:printInfo("Rule created successfully.");
        test:assertEquals(ruleProp.name, testRule1, msg = "Rule creation failed.");
    } else {
        test:assertFail("Rule creation failed.");
    }
}
@test:Config {
    groups: ["asb_admin"],
    dependsOn: [testQueueExists],
    enable: true
}
function testCreateWithOptionQueue() returns error? {
    log:printInfo("[[testCreateWithOptionQueue]]");
    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);
    QueueProperties? queueProp = check adminClient->createQueue(testQueue2, queueConfig);
    if (queueProp is QueueProperties) {
        log:printInfo(queueProp.toString());
        test:assertEquals(queueProp.name, testQueue2, msg = "Queue creation failed. wrong name");
        //test:assertEquals(queueProp.autoDeleteOnIdle, queueConfig.autoDeleteOnIdle, msg = "Queue creation failed. wrong autoDeleteOnIdle");
        test:assertEquals(queueProp.deadLetteringOnMessageExpiration, queueConfig.deadLetteringOnMessageExpiration,
            msg = "Queue creation failed. wrong deadLetteringOnMessageExpiration");
        test:assertEquals(queueProp.defaultMessageTimeToLive.seconds, queueConfig.defaultMessageTimeToLive?.seconds, 
            msg = "Queue creation failed. wrong defaultMessageTimeToLive");
        test:assertEquals(queueProp.duplicateDetectionHistoryTimeWindow.seconds,    
            queueConfig.duplicateDetectionHistoryTimeWindow?.seconds, msg = "Queue creation failed. wrong duplicateDetectionHistoryTimeWindow");
        test:assertEquals(queueProp.enableBatchedOperations, queueConfig.enableBatchedOperations,
            msg = "Queue creation failed. wrong enableBatchedOperations");
        test:assertEquals(queueProp.enablePartitioning, queueConfig.enablePartitioning, msg = "Queue creation failed.");
        test:assertEquals(queueProp.forwardDeadLetteredMessagesTo, "https://testballerina.servicebus.windows.net/" + testQueue1 ,
            msg = "Queue creation failed. wrong forwardDeadLetteredMessagesTo");
        test:assertEquals(queueProp.forwardTo, "https://testballerina.servicebus.windows.net/" + testQueue1 , msg = "Queue creation failed. wrong forwardTo");
        test:assertEquals(queueProp.lockDuration.seconds, queueConfig.lockDuration?.seconds, msg = "Queue creation failed. wrong lockDuration");
        test:assertEquals(queueProp.maxDeliveryCount, queueConfig.maxDeliveryCount, msg = "Queue creation failed. wrong maxDeliveryCount");
        test:assertEquals(queueProp.requiresDuplicateDetection, queueConfig.requiresDuplicateDetection,
             msg = "Queue creation failed. wrong requiresDuplicateDetection");
        test:assertEquals(queueProp.requiresSession, queueConfig.requiresSession, msg = "Queue creation failed. wrong requiresSession");
        test:assertEquals(queueProp.status, queueConfig.status, msg = "Queue creation failed. wrong status");
        test:assertEquals(queueProp.userMetadata, queueConfig.userMetadata, msg = "Queue creation failed. wrong userMetadata");
        log:printInfo("Queue created successfully.");
    } else {
        test:assertFail("Queue creation failed.");
    }
}
@test:Config {
    groups: ["asb_admin"],
    dependsOn: [testCreateWithOptionQueue],
    enable: true
}
function testGetQueue() returns error? {
    log:printInfo("[[testCreateWithOptionQueue]]");
    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);
    QueueProperties? queueProp = check adminClient->getQueue(testQueue2);
    if (queueProp is QueueProperties) {
        log:printInfo(queueProp.toString());
        test:assertEquals(queueProp.name, testQueue2, msg = "Queue creation failed. wrong name");
        //test:assertEquals(queueProp.autoDeleteOnIdle, queueConfig.autoDeleteOnIdle, msg = "Queue creation failed. wrong autoDeleteOnIdle");
        test:assertEquals(queueProp.deadLetteringOnMessageExpiration, queueConfig.deadLetteringOnMessageExpiration,
            msg = "Queue creation failed. wrong deadLetteringOnMessageExpiration");
        test:assertEquals(queueProp.defaultMessageTimeToLive.seconds, queueConfig.defaultMessageTimeToLive?.seconds, 
            msg = "Queue creation failed. wrong defaultMessageTimeToLive");
        test:assertEquals(queueProp.duplicateDetectionHistoryTimeWindow.seconds,    
            queueConfig.duplicateDetectionHistoryTimeWindow?.seconds, msg = "Queue creation failed. wrong duplicateDetectionHistoryTimeWindow");
        test:assertEquals(queueProp.enableBatchedOperations, queueConfig.enableBatchedOperations,
            msg = "Queue creation failed. wrong enableBatchedOperations");
        test:assertEquals(queueProp.enablePartitioning, queueConfig.enablePartitioning, msg = "Queue creation failed.");
        test:assertEquals(queueProp.forwardDeadLetteredMessagesTo, "https://testballerina.servicebus.windows.net/" + testQueue1 ,
            msg = "Queue creation failed. wrong forwardDeadLetteredMessagesTo");
        test:assertEquals(queueProp.forwardTo, "https://testballerina.servicebus.windows.net/" + testQueue1 , msg = "Queue creation failed. wrong forwardTo");
        test:assertEquals(queueProp.lockDuration.seconds, queueConfig.lockDuration?.seconds, msg = "Queue creation failed. wrong lockDuration");
        test:assertEquals(queueProp.maxDeliveryCount, queueConfig.maxDeliveryCount, msg = "Queue creation failed. wrong maxDeliveryCount");
        test:assertEquals(queueProp.requiresDuplicateDetection, queueConfig.requiresDuplicateDetection,
             msg = "Queue creation failed. wrong requiresDuplicateDetection");
        test:assertEquals(queueProp.requiresSession, queueConfig.requiresSession, msg = "Queue creation failed. wrong requiresSession");
        test:assertEquals(queueProp.status, queueConfig.status, msg = "Queue creation failed. wrong status");
        test:assertEquals(queueProp.userMetadata, queueConfig.userMetadata, msg = "Queue creation failed. wrong userMetadata");
        log:printInfo("Queue created successfully.");
    } else {
        test:assertFail("Queue creation failed.");
    }
}
@test:Config {
    groups: ["asb_admin"],
    dependsOn: [testCreateQueue],
    enable: true
}
function testUpdateQueue() returns error? {
    log:printInfo("[[testUpdateQueue]]");
    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);
    QueueProperties? queueProp = check adminClient->updateQueue(testQueue1, updateQueueConfig);
    if (queueProp is QueueProperties) {
        log:printInfo(queueProp.toString());
        test:assertEquals(queueProp.name, testQueue1, msg = "Queue creation failed. wrong name");
        //test:assertEquals(queueProp.autoDeleteOnIdle, queueConfig.autoDeleteOnIdle, msg = "Queue creation failed. wrong autoDeleteOnIdle");
        test:assertEquals(queueProp.deadLetteringOnMessageExpiration, updateQueueConfig.deadLetteringOnMessageExpiration,
            msg = "Queue creation failed. wrong deadLetteringOnMessageExpiration");
        test:assertEquals(queueProp.defaultMessageTimeToLive.seconds, updateQueueConfig.defaultMessageTimeToLive?.seconds, 
            msg = "Queue creation failed. wrong defaultMessageTimeToLive");
        test:assertEquals(queueProp.duplicateDetectionHistoryTimeWindow.seconds,    
            updateQueueConfig.duplicateDetectionHistoryTimeWindow?.seconds, msg = "Queue creation failed. wrong duplicateDetectionHistoryTimeWindow");
        test:assertEquals(queueProp.enableBatchedOperations, queueConfig.enableBatchedOperations,
            msg = "Queue creation failed. wrong enableBatchedOperations");
        test:assertEquals(queueProp.enablePartitioning, updateQueueConfig.enablePartitioning, msg = "Queue creation failed.");
        test:assertEquals(queueProp.forwardDeadLetteredMessagesTo, "https://testballerina.servicebus.windows.net/" + testQueue2 ,
            msg = "Queue creation failed. wrong forwardDeadLetteredMessagesTo");
        //test:assertEquals(queueProp.forwardTo, "https://testballerina.servicebus.windows.net/" + testQueue2 , msg = "Queue creation failed. wrong forwardTo");
        test:assertEquals(queueProp.lockDuration.seconds, updateQueueConfig.lockDuration?.seconds, msg = "Queue creation failed. wrong lockDuration");
        test:assertEquals(queueProp.maxDeliveryCount, updateQueueConfig.maxDeliveryCount, msg = "Queue creation failed. wrong maxDeliveryCount");
        test:assertEquals(queueProp.requiresDuplicateDetection, updateQueueConfig.requiresDuplicateDetection,
             msg = "Queue creation failed. wrong requiresDuplicateDetection");
        test:assertEquals(queueProp.requiresSession, updateQueueConfig.requiresSession, msg = "Queue creation failed. wrong requiresSession");
        test:assertEquals(queueProp.status, updateQueueConfig.status, msg = "Queue creation failed. wrong status");
        test:assertEquals(queueProp.userMetadata, updateQueueConfig.userMetadata, msg = "Queue creation failed. wrong userMetadata");
        log:printInfo("Queue created successfully.");
    } else {
        test:assertFail("Queue creation failed.");
    }
}
@test:Config {
    groups: ["asb_admin"],
    dependsOn: [testCreateQueue],
    enable: true
}
function testCreateWithOptionTopic() returns error? {
    log:printInfo("[[testCreateWithOptionTopic]]");
    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);
    TopicProperties? topicProp = check adminClient->createTopic(testTopic2, topicConfig);
    if (topicProp is TopicProperties) {
        log:printInfo(topicProp.toString());
        test:assertEquals(topicProp.name, testTopic2, msg = "Topic creation failed. wrong name");
        test:assertEquals(topicProp.autoDeleteOnIdle.seconds, topicConfig.autoDeleteOnIdle?.seconds, msg = "Topic creation failed. wrong autoDeleteOnIdle");
        test:assertEquals(topicProp.enableBatchedOperations, topicConfig.enableBatchedOperations,
            msg = "Topic creation failed. wrong enableBatchedOperations");
        test:assertEquals(topicProp.enablePartitioning, topicConfig.enablePartitioning, msg = "Topic creation failed.");
        test:assertEquals(topicProp.maxSizeInMegabytes, topicConfig.maxSizeInMegabytes, msg = "Topic creation failed. wrong maxSizeInMegabytes");
        test:assertEquals(topicProp.requiresDuplicateDetection, topicConfig.requiresDuplicateDetection,
             msg = "Topic creation failed. wrong requiresDuplicateDetection");
        test:assertEquals(topicProp.status, topicConfig.status, msg = "Topic creation failed. wrong status");
        test:assertEquals(topicProp.userMetadata, topicConfig.userMetadata, msg = "Topic creation failed. wrong userMetadata");
        test:assertEquals(topicProp.duplicateDetectionHistoryTimeWindow?.seconds, topicConfig.duplicateDetectionHistoryTimeWindow?.seconds, 
            msg = "Topic creation failed. wrong duplicateDetectionHistoryTimeWindow");
        test:assertEquals(topicProp.defaultMessageTimeToLive?.seconds, topicConfig.defaultMessageTimeToLive?.seconds,
            msg = "Topic creation failed. wrong defaultMessageTimeToLive");
        test:assertEquals(topicProp.supportOrdering, topicConfig.supportOrdering, msg = "Topic creation failed. wrong supportOrdering");
        test:assertEquals(topicProp.maxMessageSizeInKilobytes, topicConfig.maxMessageSizeInKilobytes, msg = "Topic creation failed. wrong maxMessageSizeInKilobytes");
        log:printInfo("Topic created successfully.");
    } else {
        test:assertFail("Topic creation failed.");
    }
}
@test:Config {
    groups: ["asb_admin"],
    dependsOn: [testCreateWithOptionTopic],
    enable: true
}
function testGetTopic() returns error? {
    log:printInfo("[[testGetTopic]]");
    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);
    TopicProperties? topicProp = check adminClient->getTopic(testTopic2);
    if (topicProp is TopicProperties) {
        log:printInfo(topicProp.toString());
        test:assertEquals(topicProp.name, testTopic2, msg = "Topic creation failed. wrong name");
        test:assertEquals(topicProp.autoDeleteOnIdle.seconds, topicConfig.autoDeleteOnIdle?.seconds, msg = "Topic creation failed. wrong autoDeleteOnIdle");
        test:assertEquals(topicProp.enableBatchedOperations, topicConfig.enableBatchedOperations,
            msg = "Topic creation failed. wrong enableBatchedOperations");
        test:assertEquals(topicProp.enablePartitioning, topicConfig.enablePartitioning, msg = "Topic creation failed.");
        test:assertEquals(topicProp.maxSizeInMegabytes, topicConfig.maxSizeInMegabytes, msg = "Topic creation failed. wrong maxSizeInMegabytes");
        test:assertEquals(topicProp.requiresDuplicateDetection, topicConfig.requiresDuplicateDetection,
             msg = "Topic creation failed. wrong requiresDuplicateDetection");
        test:assertEquals(topicProp.status, topicConfig.status, msg = "Topic creation failed. wrong status");
        test:assertEquals(topicProp.userMetadata, topicConfig.userMetadata, msg = "Topic creation failed. wrong userMetadata");
        test:assertEquals(topicProp.duplicateDetectionHistoryTimeWindow?.seconds, topicConfig.duplicateDetectionHistoryTimeWindow?.seconds, 
            msg = "Topic creation failed. wrong duplicateDetectionHistoryTimeWindow");
        test:assertEquals(topicProp.defaultMessageTimeToLive?.seconds, topicConfig.defaultMessageTimeToLive?.seconds,
            msg = "Topic creation failed. wrong defaultMessageTimeToLive");
        test:assertEquals(topicProp.supportOrdering, topicConfig.supportOrdering, msg = "Topic creation failed. wrong supportOrdering");
        test:assertEquals(topicProp.maxMessageSizeInKilobytes, topicConfig.maxMessageSizeInKilobytes, msg = "Topic creation failed. wrong maxMessageSizeInKilobytes");
        log:printInfo("Topic created successfully.");
    } else {
        test:assertFail("Topic creation failed.");
    }
}
@test:Config {
    groups: ["asb_admin"],
    dependsOn: [testGetTopic],
    enable: true
}
function testUpdateTopic() returns error? {
    log:printInfo("[[testUpdateTopic]]");
    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);
    TopicProperties? topicProp = check adminClient->updateTopic(testTopic2, updateTopicConfig);
    if (topicProp is TopicProperties) {
        log:printInfo(topicProp.toString());
        test:assertEquals(topicProp.name, testTopic2, msg = "Topic creation failed. wrong name");
        test:assertEquals(topicProp.autoDeleteOnIdle.seconds, topicConfig.autoDeleteOnIdle?.seconds, msg = "Topic creation failed. wrong autoDeleteOnIdle");
        test:assertEquals(topicProp.enableBatchedOperations, topicConfig.enableBatchedOperations,
            msg = "Topic creation failed. wrong enableBatchedOperations");
        test:assertEquals(topicProp.enablePartitioning, topicConfig.enablePartitioning, msg = "Topic creation failed.");
        test:assertEquals(topicProp.maxSizeInMegabytes, topicConfig.maxSizeInMegabytes, msg = "Topic creation failed. wrong maxSizeInMegabytes");
        test:assertEquals(topicProp.requiresDuplicateDetection, topicConfig.requiresDuplicateDetection,
             msg = "Topic creation failed. wrong requiresDuplicateDetection");
        test:assertEquals(topicProp.status, topicConfig.status, msg = "Topic creation failed. wrong status");
        test:assertEquals(topicProp.userMetadata, topicConfig.userMetadata, msg = "Topic creation failed. wrong userMetadata");
        test:assertEquals(topicProp.duplicateDetectionHistoryTimeWindow?.seconds, topicConfig.duplicateDetectionHistoryTimeWindow?.seconds, 
            msg = "Topic creation failed. wrong duplicateDetectionHistoryTimeWindow");
        test:assertEquals(topicProp.defaultMessageTimeToLive?.seconds, topicConfig.defaultMessageTimeToLive?.seconds,
            msg = "Topic creation failed. wrong defaultMessageTimeToLive");
        test:assertEquals(topicProp.supportOrdering, topicConfig.supportOrdering, msg = "Topic creation failed. wrong supportOrdering");
        test:assertEquals(topicProp.maxMessageSizeInKilobytes, topicConfig.maxMessageSizeInKilobytes, msg = "Topic creation failed. wrong maxMessageSizeInKilobytes");
        log:printInfo("Topic created successfully.");
    } else {
        test:assertFail("Topic creation failed.");
    }
}
@test:Config {
    groups: ["asb_admin"],
    dependsOn: [testUpdateTopic],
    enable: false
}
function testTopicList() returns error? {
    log:printInfo("[[testTopicList]]");
    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);
    TopicList? topicProp = check adminClient->listTopics();
    if (topicProp is TopicList) {
        if (topicProp.list.length() >= 1) {
            log:printInfo(topicProp.toString());
            log:printInfo("Topic list successfully.");
        } else {
            test:assertFail("Topic list failed.");
        }
    } else {
        test:assertFail("Topic list creation failed.");
    }
}
@test:Config {
    groups: ["asb_admin"],
    dependsOn: [testCreateSubscription],
    enable: true
}
function testCreateSubscriptionWithOption() returns error? 
{
    log:printInfo("[[testCreateSubscriptionWithOption]]");
    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);
    SubscriptionProperties? subscriptionProp = check adminClient->createSubscription(testTopic1, testSubscription2, subConfig);
    if (subscriptionProp is SubscriptionProperties) {
        log:printInfo(subscriptionProp.toString());
        test:assertEquals(subscriptionProp.subscriptionName, testSubscription2, msg = "Subscription creation failed. wrong name");
        test:assertEquals(subscriptionProp.topicName,testTopic1, msg = "Subscription creation failed. wrong topic");
        //test:assertEquals(subscriptionProp.autoDeleteOnIdle.seconds, subConfig.autoDeleteOnIdle?.seconds, msg = "Subscription creation failed. wrong autoDeleteOnIdle");
        test:assertEquals(subscriptionProp.deadLetteringOnMessageExpiration, subConfig.deadLetteringOnMessageExpiration,
            msg = "Subscription creation failed. wrong deadLetteringOnMessageExpiration");
        test:assertEquals(subscriptionProp.defaultMessageTimeToLive.seconds, subConfig.defaultMessageTimeToLive?.seconds, 
            msg = "Subscription creation failed. wrong defaultMessageTimeToLive");
        test:assertEquals(subscriptionProp.enableBatchedOperations, subConfig.enableBatchedOperations,
            msg = "Subscription creation failed. wrong enableBatchedOperations");
        test:assertEquals(subscriptionProp.forwardDeadLetteredMessagesTo, "https://testballerina.servicebus.windows.net/" + testQueue1 ,
            msg = "Subscription creation failed. wrong forwardDeadLetteredMessagesTo");
        test:assertEquals(subscriptionProp.forwardTo, "https://testballerina.servicebus.windows.net/" + testQueue1 , msg = "Subscription creation failed. wrong forwardTo");
        test:assertEquals(subscriptionProp.lockDuration.seconds, subConfig.lockDuration?.seconds, msg = "Subscription creation failed. wrong lockDuration");
        test:assertEquals(subscriptionProp.maxDeliveryCount, subConfig.maxDeliveryCount, msg = "Subscription creation failed. wrong maxDeliveryCount");
        test:assertEquals(subscriptionProp.status, subConfig.status, msg = "Subscription creation failed. wrong status");
        test:assertEquals(subscriptionProp.userMetadata, subConfig.userMetadata, msg = "Subscription creation failed. wrong userMetadata");
        test:assertEquals(subscriptionProp.requiresSession, subConfig.requiresSession, msg = "Subscription creation failed. wrong requiresSession");
        log:printInfo("Subscription created successfully.");
    } else {
        test:assertFail("Subscription creation failed.");
    }
}
@test:Config {
    groups: ["asb_admin"],
    dependsOn: [testCreateSubscriptionWithOption],
    enable: true
}
function testGetSubscription() returns error? {
    log:printInfo("[[testGetSubscription]]");
    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);
    SubscriptionProperties? subscriptionProp = check adminClient->getSubscription(testTopic1, testSubscription2);
    if (subscriptionProp is SubscriptionProperties) {
        log:printInfo(subscriptionProp.toString());
        test:assertEquals(subscriptionProp.subscriptionName, testSubscription2, msg = "Subscription creation failed. wrong name");
        test:assertEquals(subscriptionProp.topicName,testTopic1, msg = "Subscription creation failed. wrong topic");
        //test:assertEquals(subscriptionProp.autoDeleteOnIdle.seconds, subConfig.autoDeleteOnIdle?.seconds, msg = "Subscription creation failed. wrong autoDeleteOnIdle");
        test:assertEquals(subscriptionProp.deadLetteringOnMessageExpiration, subConfig.deadLetteringOnMessageExpiration,
            msg = "Subscription creation failed. wrong deadLetteringOnMessageExpiration");
        test:assertEquals(subscriptionProp.defaultMessageTimeToLive.seconds, subConfig.defaultMessageTimeToLive?.seconds, 
            msg = "Subscription creation failed. wrong defaultMessageTimeToLive");
        test:assertEquals(subscriptionProp.enableBatchedOperations, subConfig.enableBatchedOperations,
            msg = "Subscription creation failed. wrong enableBatchedOperations");
        test:assertEquals(subscriptionProp.forwardDeadLetteredMessagesTo, "https://testballerina.servicebus.windows.net/" + testQueue1 ,
            msg = "Subscription creation failed. wrong forwardDeadLetteredMessagesTo");
        test:assertEquals(subscriptionProp.forwardTo, "https://testballerina.servicebus.windows.net/" + testQueue1 , msg = "Subscription creation failed. wrong forwardTo");
        test:assertEquals(subscriptionProp.lockDuration.seconds, subConfig.lockDuration?.seconds, msg = "Subscription creation failed. wrong lockDuration");
        test:assertEquals(subscriptionProp.maxDeliveryCount, subConfig.maxDeliveryCount, msg = "Subscription creation failed. wrong maxDeliveryCount");
        test:assertEquals(subscriptionProp.status, subConfig.status, msg = "Subscription creation failed. wrong status");
        test:assertEquals(subscriptionProp.userMetadata, subConfig.userMetadata, msg = "Subscription creation failed. wrong userMetadata");
        test:assertEquals(subscriptionProp.requiresSession, subConfig.requiresSession, msg = "Subscription creation failed. wrong requiresSession");
        log:printInfo("Subscription created successfully.");
    } else {
        test:assertFail("Subscription creation failed.");
    }
}
@test:Config {
    groups: ["asb_admin"],
    dependsOn: [testGetSubscription],
    enable: true
}
function testUpdateSubscription() returns error? {
    log:printInfo("[[testUpdateSubscription]]");
    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);
    SubscriptionProperties? subscriptionProp = check adminClient->updateSubscription(testTopic1, testSubscription1, updateSubConfig);
    if (subscriptionProp is SubscriptionProperties) {
        test:assertEquals(subscriptionProp.subscriptionName, testSubscription1, msg = "Subscription creation failed. wrong name");
        test:assertEquals(subscriptionProp.topicName,testTopic1, msg = "Subscription creation failed. wrong topic");
        //test:assertEquals(subscriptionProp.autoDeleteOnIdle.seconds, subConfig.autoDeleteOnIdle?.seconds, msg = "Subscription creation failed. wrong autoDeleteOnIdle");
        test:assertEquals(subscriptionProp.deadLetteringOnMessageExpiration, updateSubConfig.deadLetteringOnMessageExpiration,
            msg = "Subscription creation failed. wrong deadLetteringOnMessageExpiration");
        test:assertEquals(subscriptionProp.defaultMessageTimeToLive.seconds, updateSubConfig.defaultMessageTimeToLive?.seconds,
            msg = "Subscription creation failed. wrong defaultMessageTimeToLive");
        test:assertEquals(subscriptionProp.enableBatchedOperations, updateSubConfig.enableBatchedOperations,
            msg = "Subscription creation failed. wrong enableBatchedOperations");
        test:assertEquals(subscriptionProp.forwardDeadLetteredMessagesTo, "https://testballerina.servicebus.windows.net/" + testQueue1 ,
            msg = "Subscription creation failed. wrong forwardDeadLetteredMessagesTo");
        test:assertEquals(subscriptionProp.forwardTo, "https://testballerina.servicebus.windows.net/" + testQueue1 , msg = "Subscription creation failed. wrong forwardTo");
        test:assertEquals(subscriptionProp.lockDuration.seconds, updateSubConfig.lockDuration?.seconds, msg = "Subscription creation failed. wrong lockDuration");
        test:assertEquals(subscriptionProp.maxDeliveryCount, updateSubConfig.maxDeliveryCount, msg = "Subscription creation failed. wrong maxDeliveryCount");
        test:assertEquals(subscriptionProp.status, updateSubConfig.status, msg = "Subscription creation failed. wrong status");
        //test:assertEquals(subscriptionProp.userMetadata, updateSubConfig.userMetadata, msg = "Subscription creation failed. wrong userMetadata");
        log:printInfo("Subscription created successfully.");
    } else {
        test:assertFail("Subscription creation failed.");
    }
}
@test:Config {
    groups: ["asb_admin"],
    dependsOn: [testCreateSubscriptionWithOption],
    enable: true
}
function testSubscriptionList() returns error? {
    log:printInfo("[[testSubscriptionList]]");
    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);
    SubscriptionList? subscriptionProp = check adminClient->listSubscriptions(testTopic1);
    if (subscriptionProp is SubscriptionList) {
        if (subscriptionProp.list.length() >= 2) {
            log:printInfo(subscriptionProp.toString());
            log:printInfo("Subscription list successfully.");
        } else {
            test:assertFail("Subscription list failed.");
        }
    } else {
        test:assertFail("Subscription creation failed.");
    }
}
@test:Config {
    groups: ["asb_admin"],
    dependsOn: [testCreateSubscriptionWithOption],
    enable: true
}
function testCreateRuleWithOptions() returns error? {
    log:printInfo("[[testCreateRuleWithOptions]]");
    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);
    RuleProperties? ruleProp = check adminClient->createRule(testTopic1, testSubscription1, testRule2, ruleConfig);
    if (ruleProp is RuleProperties) {
        log:printInfo("Rule created successfully.");
        test:assertEquals(ruleProp.name, testRule2, msg = "Rule creation failed.");
        test:assertEquals(ruleProp.rule.filter, "1=1", msg = "Rule creation failed.");
    } else {
        test:assertFail("Rule creation failed.");
    }
}
@test:Config {
    groups: ["asb_admin"],
    dependsOn: [testCreateRuleWithOptions],
    enable: true
}
function testGetRule() returns error? {
    log:printInfo("[[testGetRule]]");
    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);
    RuleProperties? ruleProp = check adminClient->getRule(testTopic1, testSubscription1, testRule2);
    if (ruleProp is RuleProperties) {
        log:printInfo("Rule created successfully.");
        test:assertEquals(ruleProp.name, testRule2, msg = "Rule creation failed.");
        test:assertEquals(ruleProp.rule.filter, "1=1", msg = "Rule creation failed.");
    } else {
        test:assertFail("Rule creation failed.");
    }
}
@test:Config {
    groups: ["asb_admin"],
    dependsOn: [testGetRule],
    enable: true
}
function testUpdateRule() returns error? {
    log:printInfo("[[testUpdateRule]]");
    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);
    RuleProperties? ruleProp = check adminClient->updateRule(testTopic1, testSubscription1, testRule1, updateRuleConfig);
    if (ruleProp is RuleProperties) {
        log:printInfo("Rule created successfully.");
        test:assertEquals(ruleProp.name, testRule1, msg = "Rule creation failed.");
        test:assertEquals(ruleProp.rule.filter, "1=1", msg = "Rule creation failed.");
    } else {
        test:assertFail("Rule creation failed.");
    }
}
@test:Config {
    groups: ["asb_admin"],
    dependsOn: [testUpdateRule],
    enable: true
}
function testRuleList() returns error? {
    log:printInfo("[[testRuleList]]");
    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);
    RuleList? ruleProp = check adminClient->listRules(testTopic1, testSubscription1);
    if (ruleProp is RuleList) {
        if (ruleProp.list.length() >= 2) {
            log:printInfo(ruleProp.toString());
            log:printInfo("Rule list successfully.");
        } else {
            test:assertFail("Rule list failed.");
        }
    } else {
        test:assertFail("Rule creation failed.");
    }
}
@test:Config {
    groups: ["asb_admin"],
    dependsOn: [testCreateRuleWithOptions],
    enable: true
}
function testDeleteRule() returns error? {
    log:printInfo("[[testDeleteRule]]");
    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);
    Error? result= check adminClient->deleteRule(testTopic1, testSubscription1, testRule1);
    if (result is Error) {
        test:assertFail("Rule deletion failed.");
    }
    result = check adminClient->deleteRule(testTopic1, testSubscription1, testRule2);
    if (result is Error) {
        test:assertFail("Rule deletion failed.");
    }
}
@test:Config {
    groups: ["asb_admin"],
    dependsOn: [testDeleteRule],
    enable: true
}
function testDeleteSubscriptionWithOption() returns error? {
    log:printInfo("[[testDeleteSubscriptionWithOption]]");
    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);
    Error? result = check adminClient->deleteSubscription(testTopic1, testSubscription1);
    if (result is Error) {
        test:assertFail("Subscription deletion failed.");
    }
    result = check adminClient->deleteSubscription(testTopic1, testSubscription2);
    if (result is Error) {
        test:assertFail("Subscription deletion failed.");
    }
}
@test:Config {
    groups: ["asb_admin"],
    dependsOn: [testDeleteSubscriptionWithOption,testDeferMessageFromSubscriptionOperation,testInvalidConnectionString],
    enable: true
}
function testDeleteQueue() returns error? {
    log:printInfo("[[testDeleteQueue]]");
    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);
    Error? result = check adminClient->deleteQueue(testQueue2);
    if (result is Error) {
        test:assertFail("Queue deletion failed.");
    }
    result = check adminClient->deleteQueue(testQueue1);
    if (result is Error) {
        test:assertFail("Queue deletion failed.");
    }
}

@test:Config {
    groups: ["asb_admin"],
    dependsOn: [testDeleteQueue,testDeferMessageFromSubscriptionOperation,testInvalidConnectionString],
    enable: true
}
function testDeleteTopic() returns error? {
    log:printInfo("[[testDeleteTopic]]");
    log:printInfo("Initializing Asb admin client.");
    ASBAdministration adminClient = check new (connectionString);
    Error? result = check adminClient->deleteTopic(testTopic1);
    if (result is Error) {
        test:assertFail("Topic deletion failed.");
    }
    result = check adminClient->deleteTopic(testTopic2);
    if (result is Error) {
        test:assertFail("Topic deletion failed.");
    }
}