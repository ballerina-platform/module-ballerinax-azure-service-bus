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

# Represents a client to the Azure service bus message broker.
@display {label: "Azure Service Bus", iconPath: "logo.png"}
public client class AsbClient {
    handle asbSenderConnection = JAVA_NULL;
    handle asbReceiverConnection  = JAVA_NULL;

    private string connectionString;

    # Initiates an Asb client using the given connection configuration.
    # 
    # + connectionConfiguration - Configurations used to create a `asb:AsbClient`
    public isolated function init(AsbConnectionConfiguration connectionConfiguration) {
        self.connectionString = connectionConfiguration.connectionString;
    }

    # Creates a queue sender connection to the given queue.
    # 
    # + queueName - Name of the queue
    # + return - An `asb:Error` if failed to create connection or else `QueueSender`
    @display {label: "Create Queue Sender"}
    isolated remote function createQueueSender(@display {label: "Queue Name"} string queueName) 
                                               returns @display {label: "QueueSender"} handle|Error {
        string entityPath = queueName;
        var connectionResult = createSender(java:fromString(self.connectionString),
            java:fromString(entityPath));
        if (connectionResult is handle) {
            return <handle> connectionResult;
        } else {
            return connectionResult;
        }
    }

    # Creates a topic sender connection to the given topic.
    # 
    # + topicName - Name of the topic
    # + return - An `asb:Error` if failed to create connection or else `TopicSender`
    @display {label: "Create Topic Sender"}
    isolated remote function createTopicSender(@display {label: "Topic Name"} string topicName) 
                                               returns @display {label: "TopicSender"} handle|Error {
        string entityPath = topicName;
        var connectionResult = createSender(java:fromString(self.connectionString),
            java:fromString(entityPath));
        if (connectionResult is handle) {
            return <handle> connectionResult;
        } else {
            return connectionResult;
        }
    }

    # Creates a queue receiver connection to the given queue.
    # 
    # + queueName - Name of the queue
    # + receiveMode - Receive mode of the receiver. It can be PEEKLOCK or RECEIVEANDDELETE. Default is PEEKLOCK. (optional)
    # + return - An `asb:Error` if failed to create connection or else `QueueReceiver`
    @display {label: "Create Queue Receiver"}
    isolated remote function createQueueReceiver(@display {label: "Queue Name"} string queueName, 
                                                 @display {label: "Receive Mode"} 
                                                 string? receiveMode = PEEKLOCK) 
                                                 returns @display {label: "QueueReceiver"} handle|Error {
        string entityPath = queueName;
        var connectionResult = createReceiver(java:fromString(self.connectionString),
            java:fromString(entityPath), receiveMode);
        if (connectionResult is handle) {
            return <handle> connectionResult;
        } else {
            return connectionResult;
        }
    }

    # Creates a subscription receiver connection to the given queue.
    # 
    # + topicName - Name of the topic
    # + subscriptionName - Name of the subscription
    # + receiveMode - Receive mode of the receiver. It can be PEEKLOCK or RECEIVEANDDELETE. Default is PEEKLOCK. (optional)
    # + return - An `asb:Error` if failed to create connection or else `SubscriptionReceiver`
    @display {label: "Create Subscription Receiver"}
    isolated remote function createSubscriptionReceiver(@display {label: "Topic Name"} string topicName, 
                                                        @display {label: "Subscription Name"} string subscriptionName, 
                                                        @display {label: "Receive Mode"} 
                                                        string? receiveMode = PEEKLOCK) 
                                                        returns @display {label: "SubscriptionReceiver"} handle|Error {
        string entityPath = topicName + "/subscriptions/" + subscriptionName;
        var connectionResult = createReceiver(java:fromString(self.connectionString),
            java:fromString(entityPath), receiveMode);
        if (connectionResult is handle) {
            return <handle> connectionResult;
        } else {
            return connectionResult;
        }
    }

    # Closes the Asb sender connection.
    #
    # + asbSender - Asb Sender
    # + return - An `asb:Error` if failed to close connection or else `()`
    @display {label: "Close Sender Connection"}
    isolated remote function closeSender(@display {label: "Asb Sender"} handle asbSender) returns Error? {
        var connectionResult = closeSender(asbSender);
        if (connectionResult is ()) {
            return;
        } else {
            return connectionResult;
        }
    }

    # Closes the Asb receiver connection.
    #
    # + asbReceiver - Asb Receiver
    # + return - An `asb:Error` if failed to close connection or else `()`
    @display {label: "Close Receiver Connection"}
    isolated remote function closeReceiver(@display {label: "Asb Receiver"} handle asbReceiver) returns Error? {
        var connectionResult = closeReceiver(asbReceiver);
        if (connectionResult is ()) {
            return;
        } else {
            return connectionResult;
        }
    }

    # Send message to queue or topic with a message body.
    #
    # + asbSender - Asb Sender
    # + message - Azure service bus message representation (Message record)
    # + return - An `asb:Error` if failed to send message or else `()`
    @display {label: "Send Message"}
    isolated remote function send(@display {label: "Asb Sender"} handle asbSender, 
                                  @display {label: "Message"} Message message) returns Error? {
        if (message.body is byte[]) {
            return send(asbSender, message.body, message?.contentType, 
                message?.messageId, message?.to, message?.replyTo, message?.replyToSessionId, message?.label, 
                message?.sessionId, message?.correlationId, message?.partitionKey, message?.timeToLive, 
                message?.applicationProperties?.properties);
        } else {
            byte[] messageBodyAsByteArray = message.body.toString().toBytes();
            return send(asbSender, messageBodyAsByteArray, message?.contentType, 
                message?.messageId, message?.to, message?.replyTo, message?.replyToSessionId, message?.label, 
                message?.sessionId, message?.correlationId, message?.partitionKey, message?.timeToLive, 
                message?.applicationProperties?.properties);
        }
    }

    # Send batch of messages to queue or topic.
    #
    # + asbSender - Asb Sender
    # + messages - Azure service bus batch message representation (MessageBatch record)
    # + return - An `asb:Error` if failed to send message or else `()`
    @display {label: "Send Batch Message"}
    isolated remote function sendBatch(@display {label: "Asb Sender"} handle asbSender,
                                       @display {label: "Batch Message"} MessageBatch messages) returns Error? {
        self.modifyContentToByteArray(messages);
        return sendBatch(asbSender, messages);
    }

    # Receive message from queue or subscription.
    # 
    # + asbReceiver - Asb Receiver
    # + serverWaitTime - Specified server wait time in seconds to receive message. (optional)
    # + return - A Message record. An `asb:Error` if failed to receive message or else `()`.
    @display {label: "Receive Message"}
    isolated remote function receive(@display {label: "Asb Receiver"} handle asbReceiver, 
                                     @display {label: "Server Wait Time"} int? serverWaitTime) 
                                     returns @display {label: "Message"} Message|Error? {
        Message|() message = check receive(asbReceiver, serverWaitTime);
        if (message is Message) {
            _ = check self.mapContentType(message);
            return message;
        }
        return;       
    }

    # Receive batch of messages from queue or subscription.
    # 
    # + asbReceiver - Asb Receiver
    # + maxMessageCount - Maximum message count to receive in a batch
    # + serverWaitTime - Specified server wait time in seconds to receive message. (optional)
    # + return - A MessageBatch record. An `asb:Error` if failed to receive message or else `()`.
    @display {label: "Receive Batch Message"}
    isolated remote function receiveBatch(@display {label: "Asb Receiver"} handle asbReceiver, 
                                          @display {label: "Maximum Message Count"} int maxMessageCount, 
                                          @display {label: "Server Wait Time"} int? serverWaitTime = ()) 
                                          returns @display {label: "Batch Message"} MessageBatch|Error? {
        MessageBatch|() receivedMessages = check receiveBatch(asbReceiver, maxMessageCount, 
            serverWaitTime);
        if (receivedMessages is MessageBatch) {
            foreach Message message in receivedMessages.messages {
                if (message.toString() != "") {
                    _ = check self.mapContentType(message);
                } 
            }
            return receivedMessages;
        }
        return;
    }

    # Complete message from queue or subscription based on messageLockToken. Declares the message processing to be 
    # successfully completed, removing the message from the queue.
    # 
    # + asbReceiver - Asb Receiver
    # + message - Message record
    # + return - An `asb:Error` if failed to complete message or else `()`
    @display {label: "Complete Message"}
    isolated remote function complete(@display {label: "Asb Receiver"} handle asbReceiver,
                                      @display {label: "Message"} Message message) returns Error? {
        if (message?.lockToken.toString() != DEFAULT_MESSAGE_LOCK_TOKEN) {
            return complete(asbReceiver, message?.lockToken.toString());
        }
        return error Error("Failed to complete message with ID " + message?.messageId.toString());
    }

    # Abandon message from queue or subscription based on messageLockToken. Abandon processing of the message for 
    # the time being, returning the message immediately back to the queue to be picked up by another (or the same) 
    # receiver.
    # 
    # + asbReceiver - Asb Receiver
    # + message - Message record
    # + return - An `asb:Error` if failed to abandon message or else `()`
    @display {label: "Abandon Message"}
    isolated remote function abandon(@display {label: "Asb Receiver"} handle asbReceiver,
                                     @display {label: "Message"} Message message) returns Error? {
        if (message?.lockToken.toString() != DEFAULT_MESSAGE_LOCK_TOKEN) {
            return abandon(asbReceiver, message?.lockToken.toString());
        }
        return error Error("Failed to abandon message with ID " + message?.messageId.toString());
    }

    # Dead-Letter the message & moves the message to the Dead-Letter Queue based on messageLockToken. Transfer 
    # the message from the primary queue into a special "dead-letter sub-queue".
    # 
    # + asbReceiver - Asb Receiver
    # + message - Message record
    # + deadLetterReason - The deadletter reason. (optional)
    # + deadLetterErrorDescription - The deadletter error description. (optional)
    # + return - An `asb:Error` if failed to deadletter message or else `()`
    @display {label: "Dead Letter Message"}
    isolated remote function deadLetter(@display {label: "Asb Receiver"} handle asbReceiver,
                                        @display {label: "Message"} Message message, 
                                        @display {label: "Dead Letter Reason"} string? deadLetterReason = (), 
                                        @display{label: "Dead Letter Description"} 
                                        string? deadLetterErrorDescription = ()) returns Error? {
        if (message?.lockToken.toString() != DEFAULT_MESSAGE_LOCK_TOKEN) {
            return deadLetter(asbReceiver, message?.lockToken.toString(), deadLetterReason, 
                deadLetterErrorDescription);
        }
        return error Error("Failed to deadletter message with ID " + message?.messageId.toString());
    }

    # Defer the message in a Queue or Subscription based on messageLockToken.  It prevents the message from being 
    # directly received from the queue by setting it aside such that it must be received by sequence number.
    # 
    # + asbReceiver - Asb Receiver
    # + message - Message record
    # + return - An `asb:Error` if failed to defer message or else sequence number
    @display {label: "Defer Message"}
    isolated remote function defer(@display {label: "Asb Receiver"} handle asbReceiver,
                                   @display {label: "Message"} Message message) 
                                   returns @display {label: "Deferred Msg Seq Num"} int|Error {
        _ = check defer(asbReceiver, message?.lockToken.toString());
        return <int> message?.sequenceNumber;
    }

    # Receives a deferred Message. Deferred messages can only be received by using sequence number and return
    # Message object.
    # 
    # + asbReceiver - Asb Receiver
    # + sequenceNumber - Unique number assigned to a message by Service Bus. The sequence number is a unique 64-bit
    #                    integer assigned to a message as it is accepted and stored by the broker and functions as
    #                    its true identifier.
    # + return - An `asb:Error` if failed to receive deferred message, a Message record if successful or else `()`
    @display {label: "Receive Deferred Message"}
    isolated remote function receiveDeferred(@display {label: "Asb Receiver"} handle asbReceiver,
                                             @display {label: "Deferred Msg Seq Num"} 
                                             int sequenceNumber) 
                                             returns @display {label: "Deferred Message"}  Message|Error? {
        Message|() message = check receiveDeferred(asbReceiver, sequenceNumber);
        if (message is Message) {
            _ = check self.mapContentType(message);
            return message;
        }
        return; 
    }

    # The operation renews lock on a message in a queue or subscription based on messageLockToken.
    # 
    # + asbReceiver - Asb Receiver
    # + message - Message record
    # + return - An `asb:Error` if failed to renew message or else `()`
    @display {label: "Renew Lock On Message"}
    isolated remote function renewLock(@display {label: "Asb Receiver"} handle asbReceiver,
                                       @display {label: "Message"} Message message) returns Error? {
        if (message?.lockToken.toString() != DEFAULT_MESSAGE_LOCK_TOKEN) {
            return renewLock(asbReceiver, message?.lockToken.toString());
        }
        return error Error("Failed to renew lock on message with ID " + message?.messageId.toString());
    }

    # Set the prefetch count of the receiver.
    # Prefetch speeds up the message flow by aiming to have a message readily available for local retrieval when and
    # before the application asks for one using Receive. Setting a non-zero value prefetches PrefetchCount
    # number of messages. Setting the value to zero turns prefetch off. For both PEEKLOCK mode and
    # RECEIVEANDDELETE mode, the default value is 0.
    # 
    # + asbReceiver - Asb Receiver
    # + prefetchCount - The desired prefetch count.
    # + return - An `asb:Error` if failed to renew message or else `()`
    @display {label: "Set Prefetch Count"}
    isolated remote function setPrefetchCount(@display {label: "Asb Receiver"} handle asbReceiver,
                                              @display {label: "Prefetch Count"} int prefetchCount) returns Error? {
        return setPrefetchCount(asbReceiver, prefetchCount);
    }

    isolated function mapContentType(Message message) returns Error? {
        match message?.contentType {
            TEXT => {
                message.body = check nativeGetTextContent(<byte[]> message.body);
            }
            JSON => {
                message.body = check nativeGetJSONContent(<byte[]> message.body);
            }
            XML => {
                message.body = check nativeGetXMLContent(<byte[]> message.body);
            }
            BYTE_ARRAY => {
                message.body = <byte[]> message.body;
            }
            _ => {
                message.body = message.body.toString();
            }
        }
        return; 
    }

    isolated function modifyContentToByteArray(MessageBatch messagesRecord) {
        foreach Message message in messagesRecord.messages {
            if (message.body is byte[]) {
                message.body = message.body;
            } else {
                message.body = message.body.toString().toBytes();
            }
        }
    }
}

isolated function createSender(handle connectionString, handle entityPath) returns handle|Error = @java:Method {
    name: "createSender",
    'class: "org.ballerinalang.asb.connection.ConnectionUtils"
} external;

isolated function closeSender(handle imessageSender) returns Error? = @java:Method {
    name: "closeSender",
    'class: "org.ballerinalang.asb.connection.ConnectionUtils"
} external;

isolated function createReceiver(handle connectionString, handle entityPath, string? receiveMode) 
                                 returns handle|Error = @java:Method {
    name: "createReceiver",
    'class: "org.ballerinalang.asb.connection.ConnectionUtils"
} external;

isolated function closeReceiver(handle imessageSender) returns Error? = @java:Method {
    name: "closeReceiver",
    'class: "org.ballerinalang.asb.connection.ConnectionUtils"
} external;

isolated function send(handle imessageSender, string|xml|json|byte[] body, string? contentType, 
                       string? messageId, string? to, string? replyTo, string? replyToSessionId, string? label, 
                       string? sessionId, string? correlationId, string? partitionKey, int? timeToLive, 
                       map<string>? properties) returns Error? = @java:Method {
    name: "send",
    'class: "org.ballerinalang.asb.connection.ConnectionUtils"
} external;

isolated function sendBatch(handle imessageSender, MessageBatch messages) returns Error? = @java:Method {
    name: "sendBatch",
    'class: "org.ballerinalang.asb.connection.ConnectionUtils"
} external;

isolated function receive(handle imessageReceiver, int? serverWaitTime) returns Message|Error? = @java:Method {
    name: "receive",
    'class: "org.ballerinalang.asb.connection.ConnectionUtils"
} external;

isolated function receiveBatch(handle imessageReceiver, int? maxMessageCount, int? serverWaitTime) 
                               returns MessageBatch|Error? = @java:Method {
    name: "receiveBatch",
    'class: "org.ballerinalang.asb.connection.ConnectionUtils"
} external;

isolated function complete(handle imessageReceiver, string? lockToken) returns Error? = @java:Method {
    name: "complete",
    'class: "org.ballerinalang.asb.connection.ConnectionUtils"
} external;

isolated function abandon(handle imessageReceiver, string? lockToken) returns Error? = @java:Method {
    name: "abandon",
    'class: "org.ballerinalang.asb.connection.ConnectionUtils"
} external;

isolated function deadLetter(handle imessageReceiver, string? lockToken, string? deadLetterReason, 
                             string? deadLetterErrorDescription) returns Error? = @java:Method {
    name: "deadLetter",
    'class: "org.ballerinalang.asb.connection.ConnectionUtils"
} external;

isolated function defer(handle imessageReceiver, string? lockToken) returns Error? = @java:Method {
    name: "defer",
    'class: "org.ballerinalang.asb.connection.ConnectionUtils"
} external;

isolated function receiveDeferred(handle imessageReceiver, int sequenceNumber) returns Message|Error? = @java:Method {
    name: "receiveDeferred",
    'class: "org.ballerinalang.asb.connection.ConnectionUtils"
} external;

isolated function renewLock(handle imessageReceiver, string? lockToken) returns Error? = @java:Method {
    name: "renewLock",
    'class: "org.ballerinalang.asb.connection.ConnectionUtils"
} external;

isolated function setPrefetchCount(handle imessageReceiver, int prefetchCount) returns Error? = @java:Method {
    name: "setPrefetchCount",
    'class: "org.ballerinalang.asb.connection.ConnectionUtils"
} external;

isolated function nativeGetTextContent(byte[] messageContent) returns string|Error =
@java:Method {
    name: "getTextContent",
    'class: "org.ballerinalang.asb.ASBMessageUtils"
} external;

isolated function nativeGetFloatContent(byte[] messageContent) returns float|Error =
@java:Method {
    name: "getFloatContent",
    'class: "org.ballerinalang.asb.ASBMessageUtils"
} external;

isolated function nativeGetIntContent(byte[] messageContent) returns int|Error =
@java:Method {
    name: "getIntContent",
    'class: "org.ballerinalang.asb.ASBMessageUtils"
} external;

isolated function nativeGetJSONContent(byte[] messageContent) returns json|Error =
@java:Method {
    name: "getJSONContent",
    'class: "org.ballerinalang.asb.ASBMessageUtils"
} external;

isolated function nativeGetXMLContent(byte[] messageContent) returns xml|Error =
@java:Method {
    name: "getXMLContent",
    'class: "org.ballerinalang.asb.ASBMessageUtils"
} external;
