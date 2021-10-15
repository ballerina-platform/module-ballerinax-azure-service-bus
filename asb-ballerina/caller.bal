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

# Azure Service Bus Caller to perform functions on dispatched messages. 
public class Caller {

    # Complete message from queue or subscription based on messageLockToken. Declares the message processing to be 
    # successfully completed, removing the message from the queue.
    #
    # + message - Message record
    # + return - An `asb:Error` if failed to complete message or else `()`
    public isolated function complete(@display {label: "Message record"} Message message) returns Error? {
        if message?.lockToken.toString() != DEFAULT_MESSAGE_LOCK_TOKEN {
            return completeMessage(self, message?.lockToken.toString());
        }
        return error Error("Failed to complete message with ID " + message?.messageId.toString());
    }

    # Abandon message from queue or subscription based on messageLockToken. Abandon processing of the message for 
    # the time being, returning the message immediately back to the queue to be picked up by another (or the same) 
    # receiver.
    #
    # + message - Message record
    # + return - An `asb:Error` if failed to abandon message or else `()`
    public isolated function abandon(@display {label: "Message record"} Message message) returns Error? {
        if message?.lockToken.toString() != DEFAULT_MESSAGE_LOCK_TOKEN {
            return abandonMessage(self, message?.lockToken.toString());
        }
        return error Error("Failed to abandon message with ID " + message?.messageId.toString());
    }

    # Dead-Letter the message & moves the message to the Dead-Letter Queue based on messageLockToken. Transfer 
    # the message from the primary queue into a special "dead-letter sub-queue".
    #
    # + message - Message record
    # + deadLetterReason - The deadletter reason.
    # + deadLetterErrorDescription - The deadletter error description.
    # + return - An `asb:Error` if failed to deadletter message or else `()`
    public isolated function deadLetter(@display {label: "Message record"} Message message, 
                                        @display {label: "Dead letter reason (optional)"} string? deadLetterReason = (), 
                                        @display {label: "Dead letter description (optional)"} 
                                        string? deadLetterErrorDescription = ()) returns Error? {
        if message?.lockToken.toString() != DEFAULT_MESSAGE_LOCK_TOKEN {
            return deadLetterMessage(self, message?.lockToken.toString(), deadLetterReason, 
                deadLetterErrorDescription);
        }
        return error Error("Failed to deadletter message with ID " + message?.messageId.toString());
    }

    # Defer the message in a Queue or Subscription based on messageLockToken.  It prevents the message from being 
    # directly received from the queue by setting it aside such that it must be received by sequence number.
    #
    # + message - Message record
    # + return - An `asb:Error` if failed to defer message or else sequence number
    public isolated function defer(@display {label: "Message record"} Message message) 
                                    returns @display {label: "Sequence Number of the deferred message"} int|Error {
        check deferMessage(self, message?.lockToken.toString());
        return <int>message?.sequenceNumber;
    }

    # Receives a deferred Message. Deferred messages can only be received by using sequence number and return
    # Message object.
    #
    # + sequenceNumber - Unique number assigned to a message by Service Bus. The sequence number is a unique 64-bit
    #                    integer assigned to a message as it is accepted and stored by the broker and functions as
    #                    its true identifier.
    # + return - An `asb:Error` if failed to receive deferred message, a Message record if successful or else `()`
    public isolated function receiveDeferred(@display {label: "Sequence Number of the deferred message"} 
                                             int sequenceNumber) 
                                             returns @display {label: "Deferred Message record"} Message|Error? {
        Message? message = check receiveDeferredMessage(self, sequenceNumber);
        if message is Message {
            check self.mapContentType(message);
            return message;
        }
        return;
    }

    # The operation renews lock on a message in a queue or subscription based on messageLockToken.
    #
    # + message - Message record
    # + return - An `asb:Error` if failed to renew message or else `()`
    public isolated function renewLock(@display {label: "Message record"} Message message) returns Error? {
        if message?.lockToken.toString() != DEFAULT_MESSAGE_LOCK_TOKEN {
            return renewLockMessage(self, message?.lockToken.toString());
        }
        return error Error("Failed to renew lock on message with ID " + message?.messageId.toString());
    }

    # Set the prefetch count of the receiver.
    # Prefetch speeds up the message flow by aiming to have a message readily available for local retrieval when and
    # before the application asks for one using Receive. Setting a non-zero value prefetches PrefetchCount
    # number of messages. Setting the value to zero turns prefetch off. For both PEEKLOCK mode and
    # RECEIVEANDDELETE mode, the default value is 0.
    #
    # + prefetchCount - The desired prefetch count.
    # + return - An `asb:Error` if failed to renew message or else `()`
    public isolated function setPrefetchCount(@display {label: "Prefetch count"} int prefetchCount) returns Error? {
        return setPrefetchCountMessage(self, prefetchCount);
    }

    isolated function mapContentType(Message message) returns Error? {
        match message?.contentType {
            TEXT => {
                message.body = check nativeGetTextContent(<byte[]>message.body);
            }
            JSON => {
                message.body = check nativeGetJSONContent(<byte[]>message.body);
            }
            XML => {
                message.body = check nativeGetXMLContent(<byte[]>message.body);
            }
            BYTE_ARRAY => {
                message.body = <byte[]>message.body;
            }
            _ => {
                message.body = message.body.toString();
            }
        }
    }
}

isolated function completeMessage(Caller caller, string? lockToken) returns Error? = @java:Method {
    name: "complete",
    'class: "org.ballerinax.asb.listener.Caller"
} external;

isolated function abandonMessage(Caller caller, string? lockToken) returns Error? = @java:Method {
    name: "abandon",
    'class: "org.ballerinax.asb.listener.Caller"
} external;

isolated function deadLetterMessage(Caller caller, string? lockToken, string? deadLetterReason, 
                            string? deadLetterErrorDescription) returns Error? = @java:Method {
    name: "deadLetter",
    'class: "org.ballerinax.asb.listener.Caller"
} external;

isolated function deferMessage(Caller caller, string? lockToken) returns Error? = @java:Method {
    name: "defer",
    'class: "org.ballerinax.asb.listener.Caller"
} external;

isolated function receiveDeferredMessage(Caller caller, int sequenceNumber) returns Message|Error? = @java:Method {
    name: "receiveDeferred",
    'class: "org.ballerinax.asb.listener.Caller"
} external;

isolated function renewLockMessage(Caller caller, string? lockToken) returns Error? = @java:Method {
    name: "renewLock",
    'class: "org.ballerinax.asb.listener.Caller"
} external;

isolated function setPrefetchCountMessage(Caller caller, int prefetchCount) returns Error? = @java:Method {
    name: "setPrefetchCount",
    'class: "org.ballerinax.asb.listener.Caller"
} external;