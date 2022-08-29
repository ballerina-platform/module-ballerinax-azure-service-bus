// Copyright (c) 2022 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
@display {label: "Azure Service Bus Message Sender", iconPath: "icon.png"}
public isolated client class MessageSender {

    final string connectionString;
    final string entityPath;
    final handle senderHandle;

    # Initializes the connector. During initialization you can pass the [Shared Access Signature (SAS) authentication credentials](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-sas)
    # Create an [Azure account](https://docs.microsoft.com/en-us/learn/modules/create-an-azure-account/) and 
    # obtain tokens following [this guide](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-portal#get-the-connection-string). 
    # Configure the connection string to have the [required permission](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-sas).
    # 
    # + config - Connection config of ASB Sender
    public isolated function init(ConnectionConfig config) returns error? {
        self.connectionString = config.connectionString;
        self.entityPath = config.entityPath;
        self.senderHandle = check initMessageSender(java:fromString(self.connectionString), java:fromString(self.entityPath));
    }

    # Send message to queue or topic with a message body.
    #
    # + message - Azure service bus message representation (`asb:Message` record)
    # + return - An `asb:Error` if failed to send message or else `()`
    @display {label: "Send Message"}
    isolated remote function send(@display {label: "Message"} Message message) returns error? {
        Error? response;
        if message.body is byte[] {
            response = check send(self.senderHandle, message.body, message?.contentType, 
                message?.messageId, message?.to, message?.replyTo, message?.replyToSessionId, message?.label, 
                message?.sessionId, message?.correlationId, message?.partitionKey, message?.timeToLive, 
                message?.applicationProperties?.properties);
        } else {
            byte[] messageBodyAsByteArray = message.body.toString().toBytes();
            response = check send(self.senderHandle, messageBodyAsByteArray, message?.contentType, 
                message?.messageId, message?.to, message?.replyTo, message?.replyToSessionId, message?.label, 
                message?.sessionId, message?.correlationId, message?.partitionKey, message?.timeToLive, 
                message?.applicationProperties?.properties);
        }
        return response;
    }

    # Send batch of messages to queue or topic.
    #
    # + messages - Azure service bus batch message representation (`asb:MessageBatch` record)
    # + return - An `asb:Error` if failed to send message or else `()`
    @display {label: "Send Batch Message"}
    isolated remote function sendBatch(@display {label: "Batch Message"} MessageBatch messages) returns error? {
        self.modifyContentToByteArray(messages);
        return sendBatch(self.senderHandle, messages);
    }

    # Closes the ASB sender connection.
    #
    # + return - An `asb:Error` if failed to close connection or else `()`
    @display {label: "Close Sender Connection"}
    isolated remote function close() returns error? {
        return closeSender(self.senderHandle);
    }

    isolated function modifyContentToByteArray(MessageBatch messagesRecord) {
        foreach Message message in messagesRecord.messages {
            if message.body is byte[] {
                message.body = message.body;
            } else {
                message.body = message.body.toString().toBytes();
            }
        }
    }
}

isolated function initMessageSender(handle connectionString, handle entityPath) returns handle|error = @java:Constructor {
    'class: "org.ballerinax.asb.sender.MessageSender",
    paramTypes: ["java.lang.String", "java.lang.String"]
} external;

isolated function send(handle senderHandle, string|xml|json|byte[] body, string? contentType, 
                       string? messageId, string? to, string? replyTo, string? replyToSessionId, string? label, 
                       string? sessionId, string? correlationId, string? partitionKey, int? timeToLive, 
                       map<string>? properties) returns error? = @java:Method {
    'class: "org.ballerinax.asb.sender.MessageSender"
} external;

isolated function sendBatch(handle senderHandle, MessageBatch messages) returns error? = @java:Method {
    'class: "org.ballerinax.asb.sender.MessageSender"
} external;

isolated function closeSender(handle senderHandle) returns error? = @java:Method {
    'class: "org.ballerinax.asb.sender.MessageSender"
} external;

