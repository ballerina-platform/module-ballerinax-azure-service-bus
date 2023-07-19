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

import ballerina/jballerina.java as java;
import ballerina/time;

# Ballerina Service Bus connector provides the capability to access Azure Service Bus SDK.
# Service Bus API provides data access to highly reliable queues and publish/subscribe topics of Azure Service Bus with deep feature capabilities.
@display {label: "Azure Service Bus Message Sender", iconPath: "icon.png"}
public isolated client class MessageSender {

    private string connectionString;
    final handle senderHandle;
    private string topicOrQueueName;
    private string entityType;
    private LogLevel logLevel;

    # Initializes the connector. During initialization you can pass the [Shared Access Signature (SAS) authentication credentials](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-sas)
    # Create an [Azure account](https://docs.microsoft.com/en-us/learn/modules/create-an-azure-account/) and 
    # obtain tokens following [this guide](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-portal#get-the-connection-string). 
    # Configure the connection string to have the [required permission](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-sas).
    #
    # + config - Azure service bus sender configuration
    public isolated function init(ASBServiceSenderConfig config) returns Error? {

        self.connectionString = config.connectionString;
        self.topicOrQueueName = config.topicOrQueueName;
        self.entityType = config.entityType;
        self.logLevel = customConfiguration.logLevel;
        handle|Error initResult = initMessageSender(java:fromString(self.connectionString),
            java:fromString(self.entityType), java:fromString(self.topicOrQueueName),
            java:fromString(self.logLevel), config.amqpRetryOptions);
        if (initResult is Error) {
            return initResult;
        }

        self.senderHandle = initResult;
    }

    # Send message to queue or topic with a message body.
    #
    # + message - Azure service bus message representation (`sb:Message` record)
    # + return - An `sb:Error` if failed to send message or else `()`
    @display {label: "Send Message"}
    isolated remote function send(@display {label: "Message Record"} Message message) returns Error? {
        message.body = serializeToByteArray(message.body);
        return send(self, message);
    }

    # Send message to queue or topic with a message body.
    #
    # + messagePayload - Message body
    # + return - An `sb:Error` if failed to send message or else `()`
    @display {label: "Send Message Payload"}
    isolated remote function sendPayload(@display {label: "Message Payload"} anydata messagePayload) returns Error? {
        Message messageToSend = constructMessageFromPayload(messagePayload);
        messageToSend.body = serializeToByteArray(messageToSend.body);
        return send(self, messageToSend);
    }

    # Sends a scheduled message to the Azure Service Bus entity this sender is connected to. 
    # A scheduled message is enqueued and made available to receivers only at the scheduled enqueue time.
    #
    # + message - Message to be scheduled  
    # + scheduledEnqueueTime - Datetime at which the message should appear in the Service Bus queue or topic
    # + return - The sequence number of the scheduled message which can be used to cancel the scheduling of the message
    isolated remote function schedule(@display {label: "Message Record or Payload"} Message message,
            time:Civil scheduledEnqueueTime) returns int|Error {
        message.body = serializeToByteArray(message.body);
        return schedule(self, message, scheduledEnqueueTime);
    }

    # Cancels the enqueuing of a scheduled message, if they are not already enqueued.
    #
    # + sequenceNumber - The sequence number of the message to cancel
    # + return - If the message could not be cancelled
    isolated remote function cancel(@display {label: "Sequence Number"} int sequenceNumber) returns Error? {
        return cancel(self, sequenceNumber);
    }

    # Send batch of messages to queue or topic.
    #
    # + messageBatch - Azure service bus batch message representation (`sb:MessageBatch` record)
    # + return - An `sb:Error` if failed to send message or else `()`
    @display {label: "Send Batch Message"}
    isolated remote function sendBatch(@display {label: "Message Batch"} MessageBatch messageBatch) returns Error? {
        foreach Message message in messageBatch.messages {
            message.body = serializeToByteArray(message.body);
        }
        return sendBatch(self, messageBatch);
    }

    # Closes the ASB sender connection.
    #
    # + return - An `sb:Error` if failed to close connection or else `()`
    @display {label: "Close Sender Connection"}
    isolated remote function close() returns Error? {
        return closeSender(self);
    }
}

isolated function initMessageSender(handle connectionString, handle entityType, handle topicOrQueueName, handle isLogEnabled, AmqpRetryOptions retryOptions) returns handle|Error = @java:Method {
    name: "initializeSender",
    'class: "org.ballerinax.azure.sb.sender.MessageSender"
} external;

isolated function send(MessageSender endpointClient, Message message) returns Error? = @java:Method {
    'class: "org.ballerinax.azure.sb.sender.MessageSender"
} external;

isolated function sendBatch(MessageSender endpointClient, MessageBatch messages) returns Error? = @java:Method {
    'class: "org.ballerinax.azure.sb.sender.MessageSender"
} external;

isolated function schedule(MessageSender endpointClient, Message message, time:Civil scheduleTime) returns int|Error = @java:Method {
    'class: "org.ballerinax.azure.sb.sender.MessageSender"
} external;

isolated function cancel(MessageSender endpointClient, int sequenceNumber) returns Error? = @java:Method {
    'class: "org.ballerinax.azure.sb.sender.MessageSender"
} external;

isolated function closeSender(MessageSender endpointClient) returns Error? = @java:Method {
    'class: "org.ballerinax.azure.sb.sender.MessageSender"
} external;
