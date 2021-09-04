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
@display {label: "Azure Service Bus Message Receiver", iconPath: "resources/asb.png"}
public isolated client class MessageReceiver {

    final string connectionString;
    final string entityPath;
    final string receiveMode;
    final handle receiverHandle;

    # Initializes the connector. During initialization you can pass the [Shared Access Signature (SAS) authentication credentials](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-sas)
    # Create an [Azure account](https://docs.microsoft.com/en-us/learn/modules/create-an-azure-account/) and 
    # obtain tokens following [this guide](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-portal#get-the-connection-string). 
    # Configure the connection string to have the [required permission](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-sas).
    # 
    # + connectionString - Connection String of Azure service bus
    # + entityPath - Name or path of the entity (e.g : Queue name, Subscription path)
    # + receiveMode - Receive mode as PEEKLOCK or RECEIVEANDDELETE (default : PEEKLOCK)
    public isolated function init(@display {label: "Connection String"} string connectionString, @display {label: "Entity Path"} string entityPath, 
                                  @display {label: "Receive Mode"} string receiveMode = PEEKLOCK) returns error? {
        self.connectionString = connectionString;
        self.entityPath = entityPath;
        self.receiveMode = receiveMode;
        self.receiverHandle = check initMessageReceiver(java:fromString(self.connectionString), 
        java:fromString(self.entityPath), java:fromString(self.receiveMode));
    }

    # Receive message from queue or subscription.
    # 
    # + serverWaitTime - Specified server wait time in seconds to receive message (optional)
    # + return - A `asb:Message` record if message is recieved, `()` if no message is in the queue or else an `asb:Error` 
    #            if failed to receive message
    @display {label: "Receive Message"}
    isolated remote function receive(@display {label: "Server Wait Time"} int? serverWaitTime = 60) 
                                     returns @display {label: "Message"} Message|Error? {
        Message? message = check receive(self.receiverHandle, serverWaitTime);
        if message is Message {
            check self.mapContentType(message);
            return message;
        }       
    }

    # Receive batch of messages from queue or subscription.
    # 
    # + maxMessageCount - Maximum message count to receive in a batch
    # + serverWaitTime - Specified server wait time in seconds to receive message (optional)
    # + return - A `asb:MessageBatch` record if batch is recieved, `()` if no batch is in the queue or else an `asb:Error` 
    #            if failed to receive batch
    @display {label: "Receive Batch Message"}
    isolated remote function receiveBatch(@display {label: "Maximum Message Count"} int maxMessageCount, 
                                          @display {label: "Server Wait Time"} int? serverWaitTime = ()) 
                                          returns @display {label: "Batch Message"} MessageBatch|Error? {
        MessageBatch? receivedMessages = check receiveBatch(self.receiverHandle, maxMessageCount, serverWaitTime);
        if receivedMessages is MessageBatch {
            foreach Message message in receivedMessages.messages {
                if message.toString() != "" {
                    check self.mapContentType(message);
                } 
            }
            return receivedMessages;
        }
        return;
    }

    # Complete message from queue or subscription based on messageLockToken. Declares the message processing to be 
    # successfully completed, removing the message from the queue.
    # 
    # + message - `asb:Message` record
    # + return - An `asb:Error` if failed to complete message or else `()`
    @display {label: "Complete Message"}
    isolated remote function complete(@display {label: "Message"} Message message) 
                                      returns Error? {
        if message?.lockToken.toString() != DEFAULT_MESSAGE_LOCK_TOKEN {
            return complete(self.receiverHandle, message?.lockToken.toString());
        }
        return error Error("Failed to complete message with ID " + message?.messageId.toString());
    }

    # Abandon message from queue or subscription based on messageLockToken. Abandon processing of the message for 
    # the time being, returning the message immediately back to the queue to be picked up by another (or the same) 
    # receiver.
    # 
    # + message - `asb:Message` record
    # + return - An `asb:Error` if failed to abandon message or else `()`
    @display {label: "Abandon Message"}
    isolated remote function abandon(@display {label: "Message"} Message message) returns Error? {
        if message?.lockToken.toString() != DEFAULT_MESSAGE_LOCK_TOKEN {
            return abandon(self.receiverHandle, message?.lockToken.toString());
        }
        return error Error("Failed to abandon message with ID " + message?.messageId.toString());
    }

    # Dead-Letter the message & moves the message to the Dead-Letter Queue based on messageLockToken. Transfer 
    # the message from the primary queue into a special "dead-letter sub-queue".
    # 
    # + message - `asb:Message` record
    # + deadLetterReason - The deadletter reason (optional)
    # + deadLetterErrorDescription - The deadletter error description (optional)
    # + return - An `asb:Error` if failed to deadletter message or else `()`
    @display {label: "Dead Letter Message"}
    isolated remote function deadLetter(@display {label: "Message"} Message message, 
                                        @display {label: "Dead Letter Reason"} string? deadLetterReason = (), 
                                        @display{label: "Dead Letter Description"} 
                                        string? deadLetterErrorDescription = ()) returns Error? {
        if message?.lockToken.toString() != DEFAULT_MESSAGE_LOCK_TOKEN {
            return deadLetter(self.receiverHandle, message?.lockToken.toString(), deadLetterReason, 
                deadLetterErrorDescription);
        }
        return error Error("Failed to deadletter message with ID " + message?.messageId.toString());
    }

    # Defer the message in a Queue or Subscription based on messageLockToken.  It prevents the message from being 
    # directly received from the queue by setting it aside such that it must be received by sequence number.
    # 
    # + message - `asb:Message` record
    # + return - An `asb:Error` if failed to defer message or else sequence number
    @display {label: "Defer Message"}
    isolated remote function defer(@display {label: "Message"} Message message) 
                                   returns @display {label: "Deferred Msg Seq Num"} int|Error {
        check defer(self.receiverHandle, message?.lockToken.toString());
        return <int> message?.sequenceNumber;
    }

    # Receives a deferred Message. Deferred messages can only be received by using sequence number and return
    # Message object.
    # 
    # + sequenceNumber - Unique number assigned to a message by Service Bus. The sequence number is a unique 64-bit
    #                    integer assigned to a message as it is accepted and stored by the broker and functions as
    #                    its true identifier.
    # + return - An `asb:Error` if failed to receive deferred message, a Message record if successful or else `()`
    @display {label: "Receive Deferred Message"}
    isolated remote function receiveDeferred(@display {label: "Deferred Msg Seq Num"} 
                                             int sequenceNumber) 
                                             returns @display {label: "Deferred Message"}  Message|Error? {
        Message? message = check receiveDeferred(self.receiverHandle, sequenceNumber);
        if message is Message {
            check self.mapContentType(message);
            return message;
        } 
    }

    # The operation renews lock on a message in a queue or subscription based on messageLockToken.
    # 
    # + message - `asb:Message` record
    # + return - An `asb:Error` if failed to renew message or else `()`
    @display {label: "Renew Lock On Message"}
    isolated remote function renewLock(@display {label: "Message"} Message message) returns Error? {
        if message?.lockToken.toString() != DEFAULT_MESSAGE_LOCK_TOKEN {
            return renewLock(self.receiverHandle, message?.lockToken.toString());
        }
        return error Error("Failed to renew lock on message with ID " + message?.messageId.toString());
    }

    # Set the prefetch count of the receiver.
    # Prefetch speeds up the message flow by aiming to have a message readily available for local retrieval when and
    # before the application asks for one using Receive. Setting a non-zero value prefetches PrefetchCount
    # number of messages. Setting the value to zero turns prefetch off. For both PEEKLOCK mode and
    # RECEIVEANDDELETE mode, the default value is 0.
    # 
    # + prefetchCount - The desired prefetch count
    # + return - An `asb:Error` if failed to renew message or else `()`
    @display {label: "Set Prefetch Count"}
    isolated remote function setPrefetchCount(@display {label: "Prefetch Count"} int prefetchCount) returns Error? {
        return setPrefetchCount(self.receiverHandle, prefetchCount);
    }

    # Closes the ASB sender connection.
    #
    # + return - An `asb:Error` if failed to close connection or else `()`
    @display {label: "Close Receiver Connection"}
    isolated remote function close() returns Error? {
        return closeReceiver(self.receiverHandle);
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

isolated function initMessageReceiver(handle connectionString, handle entityPath, handle receiveMode) returns handle|error = @java:Constructor {
    'class: "org.ballerinax.asb.receiver.MessageReceiver",
    paramTypes: ["java.lang.String", "java.lang.String", "java.lang.String"]
} external;

isolated function receive(handle receiverHandle, int? serverWaitTime) returns Message|Error? = @java:Method {
    'class: "org.ballerinax.asb.receiver.MessageReceiver"
} external;

isolated function receiveBatch(handle receiverHandle, int? maxMessageCount, int? serverWaitTime) 
                               returns MessageBatch|Error? = @java:Method {
    'class: "org.ballerinax.asb.receiver.MessageReceiver"
} external;

isolated function complete(handle receiverHandle, string? lockToken) returns Error? = @java:Method {
    'class: "org.ballerinax.asb.receiver.MessageReceiver"
} external;

isolated function abandon(handle receiverHandle, string? lockToken) returns Error? = @java:Method {
    'class: "org.ballerinax.asb.receiver.MessageReceiver"
} external;

isolated function deadLetter(handle receiverHandle, string? lockToken, string? deadLetterReason, 
                             string? deadLetterErrorDescription) returns Error? = @java:Method {
    'class: "org.ballerinax.asb.receiver.MessageReceiver"
} external;

isolated function defer(handle receiverHandle, string? lockToken) returns Error? = @java:Method {
    'class: "org.ballerinax.asb.receiver.MessageReceiver"
} external;

isolated function receiveDeferred(handle receiverHandle, int sequenceNumber) returns Message|Error? = @java:Method {
    'class: "org.ballerinax.asb.receiver.MessageReceiver"
} external;

isolated function renewLock(handle receiverHandle, string? lockToken) returns Error? = @java:Method {
    'class: "org.ballerinax.asb.receiver.MessageReceiver"
} external;

isolated function setPrefetchCount(handle receiverHandle, int prefetchCount) returns Error? = @java:Method {
    'class: "org.ballerinax.asb.receiver.MessageReceiver"
} external;

isolated function closeReceiver(handle receiverHandle) returns Error? = @java:Method {
    'class: "org.ballerinax.asb.receiver.MessageReceiver"
} external;
