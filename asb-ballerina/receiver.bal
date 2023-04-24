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

# Ballerina Service Bus connector provides the capability to access Azure Service Bus SDK.
# Service Bus API provides data access to highly reliable queues and publish/subscribe topics of Azure Service Bus with deep feature capabilities.
@display {label: "Azure Service Bus Message Receiver", iconPath: "icon.png"}
public isolated client class MessageReceiver {
    
    private  string connectionString;
    private string queueName;
    private string subscriptionName;
    private string topicName;
    private string receiveMode;
    private int maxAutoLockRenewDuration;
    private LogLevel logLevel;
    final handle receiverHandle;

    # Initializes the connector. During initialization you can pass the [Shared Access Signature (SAS) authentication credentials](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-sas)
    # Create an [Azure account](https://docs.microsoft.com/en-us/learn/modules/create-an-azure-account/) and
    # obtain tokens following [this guide](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-portal#get-the-connection-string).
    # Configure the connection string to have the [required permission](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-sas).
    #
    # + config - Azure service bus receiver configuration.(Default receiver mode is PEEK_LOCK)
    public isolated function init(ASBServiceReceiverConfig config) returns error? {
        self.connectionString = config.connectionString;
        if config.entityConfig is QueueConfig {
            QueueConfig queueConfig = check config.entityConfig.ensureType(QueueConfig);
            self.queueName = queueConfig.queueName;
            self.subscriptionName = EMPTY_STRING;
            self.topicName = EMPTY_STRING;
        } else {
            TopicSubsConfig topicSubsConfig = check config.entityConfig.ensureType(TopicSubsConfig);
            self.topicName = topicSubsConfig.topicName;
            self.subscriptionName = topicSubsConfig.subscriptionName;
            self.queueName = EMPTY_STRING;
        }
        self.receiveMode = config.receiveMode;
        self.maxAutoLockRenewDuration = config.maxAutoLockRenewDuration;
        self.logLevel = customConfiguration.logLevel;
        self.receiverHandle = check initMessageReceiver(java:fromString(self.connectionString),
        java:fromString(self.queueName), java:fromString(self.topicName), java:fromString(self.subscriptionName),
        java:fromString(self.receiveMode), self.maxAutoLockRenewDuration, java:fromString(self.logLevel), config.amqpRetryOptions);
    }

    # Receive message from queue or subscription.
    # 
    # + serverWaitTime - Specified server wait time in seconds to receive message (optional)
    # + T - Expected type of the message. This can be either a `asb:Message` or a subtype of it.
    # + return - A `asb:Message` record if message is received, `()` if no message is in the queue or else an error
    #            if failed to receive message
    @display {label: "Receive Message"}
    isolated remote function receive(@display {label: "Server Wait Time"} int? serverWaitTime = 60,
                                     @display {label: "Expected Type"} typedesc<Message> T = <>) 
                             returns @display {label: "Message"} T|error? = @java:Method {
        'class: "org.ballerinax.asb.receiver.MessageReceiver"
    } external;

    # Receive message payload from queue or subscription.
    #
    # + serverWaitTime - Specified server wait time in seconds to receive message (optional)
    # + T - Expected type of the message. This can be any subtype of `anydata` type
    # + return - A `asb:Message` record if message is received, `()` if no message is in the queue or else an error
    #            if failed to receive message
    @display {label: "Receive Message Payload"}
    isolated remote function receivePayload(@display {label: "Server Wait Time"} int? serverWaitTime = 60, 
                                            @display {label: "Expected Type"} typedesc<anydata> T = <>)
                                    returns @display {label: "Message Payload"} T|error = @java:Method {
        'class: "org.ballerinax.asb.receiver.MessageReceiver"
    } external;

    # Receive batch of messages from queue or subscription.
    # 
    # + maxMessageCount - Maximum message count to receive in a batch
    # + serverWaitTime - Specified server wait time in seconds to receive message (optional)
    # + return - A `asb:MessageBatch` record if batch is received, `()` if no batch is in the queue or else an error
    #            if failed to receive batch
    @display {label: "Receive Batch Message"}
    isolated remote function receiveBatch(@display {label: "Maximum Message Count"} int maxMessageCount, 
                                          @display {label: "Server Wait Time"} int? serverWaitTime = ()) 
                                          returns @display {label: "Batch Message"} MessageBatch|error? {
        MessageBatch|error? receivedMessages = receiveBatch(self, maxMessageCount, serverWaitTime);
        return receivedMessages;
    }

    # Complete message from queue or subscription based on messageLockToken. Declares the message processing to be 
    # successfully completed, removing the message from the queue.
    # 
    # + message - `asb:Message` record
    # + return - An error if failed to complete message or else `()`
    @display {label: "Complete Message"}
    isolated remote function complete(@display {label: "Message"} Message message) 
                                      returns error? {
        if message?.lockToken.toString() != DEFAULT_MESSAGE_LOCK_TOKEN {
            return complete(self, message?.lockToken.toString());
        }
        return error("Failed to complete message with ID " + message?.messageId.toString());
    }

    # Abandon message from queue or subscription based on messageLockToken. Abandon processing of the message for 
    # the time being, returning the message immediately back to the queue to be picked up by another (or the same) 
    # receiver.
    # 
    # + message - `asb:Message` record
    # + return - An error if failed to abandon message or else `()`
    @display {label: "Abandon Message"}
    isolated remote function abandon(@display {label: "Message"} Message message) returns error? {
        if message?.lockToken.toString() != DEFAULT_MESSAGE_LOCK_TOKEN {
            return abandon(self, message?.lockToken.toString());
        }
        return error("Failed to abandon message with ID " + message?.messageId.toString());
    }

    # Dead-Letter the message & moves the message to the Dead-Letter Queue based on messageLockToken. Transfer 
    # the message from the primary queue into a special "dead-letter sub-queue".
    # 
    # + message - `asb:Message` record
    # + deadLetterReason - The deadletter reason (optional)
    # + deadLetterErrorDescription - The deadletter error description (optional)
    # + return - An error if failed to deadletter message or else `()`
    @display {label: "Dead Letter Message"}
    isolated remote function deadLetter(@display {label: "Message"} Message message, 
                                        @display {label: "Dead Letter Reason"} string? deadLetterReason = (), 
                                        @display{label: "Dead Letter Description"} 
                                        string? deadLetterErrorDescription = ()) returns error? {
        if message?.lockToken.toString() != DEFAULT_MESSAGE_LOCK_TOKEN {
            return deadLetter(self, message?.lockToken.toString(), deadLetterReason, 
                deadLetterErrorDescription);
        }
        return error("Failed to deadletter message with ID " + message?.messageId.toString());
    }

    # Defer the message in a Queue or Subscription based on messageLockToken.  It prevents the message from being 
    # directly received from the queue by setting it aside such that it must be received by sequence number.
    # 
    # + message - `asb:Message` record
    # + return - An error if failed to defer message or else sequence number
    @display {label: "Defer Message"}
    isolated remote function defer(@display {label: "Message"} Message message) 
                                   returns @display {label: "Deferred Msg Seq Num"} int|error {
        check defer(self, message?.lockToken.toString());
        return <int> message?.sequenceNumber;
    }

    # Receives a deferred Message. Deferred messages can only be received by using sequence number and return
    # Message object.
    # 
    # + sequenceNumber - Unique number assigned to a message by Service Bus. The sequence number is a unique 64-bit
    #                    integer assigned to a message as it is accepted and stored by the broker and functions as
    #                    its true identifier.
    # + return - An error if failed to receive deferred message, a Message record if successful or else `()`
    @display {label: "Receive Deferred Message"}
    isolated remote function receiveDeferred(@display {label: "Deferred Msg Seq Num"} 
                                             int sequenceNumber) 
                                             returns @display {label: "Deferred Message"}  Message|error? {
        Message? message = check receiveDeferred(self, sequenceNumber);
        return message;
    }

    # The operation renews lock on a message in a queue or subscription based on messageLockToken.
    # 
    # + message - `asb:Message` record
    # + return - An error if failed to renew message or else `()`
    @display {label: "Renew Lock On Message"}
    isolated remote function renewLock(@display {label: "Message"} Message message) returns error? {
        if message?.lockToken.toString() != DEFAULT_MESSAGE_LOCK_TOKEN {
            return renewLock(self, message?.lockToken.toString());
        }
        return error("Failed to renew lock on message with ID " + message?.messageId.toString());
    }

    # Closes the ASB sender connection.
    #
    # + return - An error if failed to close connection or else `()`
    @display {label: "Close Receiver Connection"}
    isolated remote function close() returns error? = @java:Method {
        name: "closeReceiver",
        'class: "org.ballerinax.asb.receiver.MessageReceiver"
    } external;
}

isolated function initMessageReceiver(handle connectionString, handle queueName, handle topicName, 
        handle subscriptionName, handle receiveMode, int maxAutoLockRenewDuration, handle isLogActive, AmqpRetryOptions retryOptions) returns handle|error = @java:Method {
    name: "initializeReceiver",
    'class: "org.ballerinax.asb.receiver.MessageReceiver"
} external;

isolated function receiveBatch(MessageReceiver endpointClient, int? maxMessageCount, int? serverWaitTime) 
                               returns MessageBatch|error? = @java:Method {
    'class: "org.ballerinax.asb.receiver.MessageReceiver"
} external;

isolated function complete(MessageReceiver endpointClient, string lockToken) returns error? = @java:Method {
    'class: "org.ballerinax.asb.receiver.MessageReceiver"
} external;

isolated function abandon(MessageReceiver endpointClient, string lockToken) returns error? = @java:Method {
    'class: "org.ballerinax.asb.receiver.MessageReceiver"
} external;

isolated function deadLetter(MessageReceiver endpointClient, string lockToken, string? deadLetterReason, string? deadLetterErrorDescription) returns 
                       error? = @java:Method {
    'class: "org.ballerinax.asb.receiver.MessageReceiver"
} external;

isolated function defer(MessageReceiver endpointClient, string lockToken) returns error? = @java:Method {
    'class: "org.ballerinax.asb.receiver.MessageReceiver"
} external;

isolated function receiveDeferred(MessageReceiver endpointClient, int sequenceNumber) returns Message|error? = @java:Method {
    'class: "org.ballerinax.asb.receiver.MessageReceiver"
} external;

isolated function renewLock(MessageReceiver endpointClient, string lockToken) returns error? = @java:Method {
    'class: "org.ballerinax.asb.receiver.MessageReceiver"
} external;
