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

# Ballerina Asb Message Listener.
# Provides a listener to consume messages from the Azure Service Bus.
@display {label: "Azure Service Bus Listener"}
public class Listener {

    # Initializes a Listener object. 
    #
    public isolated function init() {
        externInit(self);
    }

    # Attaches the service to the `asb:Listener` endpoint.
    #
    # + s - Type descriptor of the service
    # + name - Name of the service
    # + return - `()` or else a `asb:Error` upon failure to register the service
    public isolated function attach(Service s, string[]|string? name = ()) returns error? {
        return registerListener(self, s);
    }

    # Starts consuming the messages on all the attached services.
    #
    # + return - `()` or else a `asb:Error` upon failure to start
    public isolated function 'start() returns error? {
        return 'start(self);
    }

    # Stops consuming messages and detaches the service from the `asb:Listener` endpoint.
    #
    # + s - Type descriptor of the service
    # + return - `()` or else  a `asb:Error` upon failure to detach the service
    public isolated function detach(Service s) returns error? {
        return detach(self, s);
    }

    # Stops consuming messages through all consumer services by terminating the connection and all its channels.
    #
    # + return - `()` or else  a `asb:Error` upon failure to close the `ChannelListener`
    public isolated function gracefulStop() returns error? {
        return stop(self);
    }

    # Stops consuming messages through all the consumer services and terminates the connection
    # with the server.
    #
    # + return - `()` or else  a `asb:Error` upon failure to close ChannelListener.
    public isolated function immediateStop() returns error? {
        return abortConnection(self);
    }

    # Complete message from queue or subscription based on messageLockToken. Declares the message processing to be 
    # successfully completed, removing the message from the queue.
    #
    # + message - Message record
    # + return - An `asb:Error` if failed to complete message or else `()`
    @display {label: "Complete Messages"}
    public isolated function complete(@display {label: "Message record"} Message message) returns error? {
        handle asbReceiverConnection = check getReceiver(self);
        if (message?.lockToken.toString() != DEFAULT_MESSAGE_LOCK_TOKEN) {
            return completeMessage(asbReceiverConnection, message?.lockToken.toString());
        }
        return error Error("Failed to complete message with ID " + message?.messageId.toString());
    }

    # Abandon message from queue or subscription based on messageLockToken. Abandon processing of the message for 
    # the time being, returning the message immediately back to the queue to be picked up by another (or the same) 
    # receiver.
    #
    # + message - Message record
    # + return - An `asb:Error` if failed to abandon message or else `()`
    @display {label: "Abandon Message"}
    public isolated function abandon(@display {label: "Message record"} Message message) returns Error? {
        handle asbReceiverConnection = check getReceiver(self);
        if (message?.lockToken.toString() != DEFAULT_MESSAGE_LOCK_TOKEN) {
            return abandonMessage(asbReceiverConnection, message?.lockToken.toString());
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
    @display {label: "Dead Letter Message"}
    public isolated function deadLetter(@display {label: "Message record"} Message message, 
                                        @display {label: "Dead letter reason (optional)"} string? deadLetterReason = (), 
                                        @display {label: "Dead letter description (optional)"} 
                                        string? deadLetterErrorDescription = ()) returns Error? {
        handle asbReceiverConnection = check getReceiver(self);
        if (message?.lockToken.toString() != DEFAULT_MESSAGE_LOCK_TOKEN) {
            return deadLetterMessage(asbReceiverConnection, message?.lockToken.toString(), deadLetterReason, 
                deadLetterErrorDescription);
        }
        return error Error("Failed to deadletter message with ID " + message?.messageId.toString());
    }

    # Defer the message in a Queue or Subscription based on messageLockToken.  It prevents the message from being 
    # directly received from the queue by setting it aside such that it must be received by sequence number.
    #
    # + message - Message record
    # + return - An `asb:Error` if failed to defer message or else sequence number
    @display {label: "Defer Message"}
    public isolated function defer(@display {label: "Message record"} Message message) 
                                    returns @display {label: "Sequence Number of the deferred message"} int|Error {
        handle asbReceiverConnection = check getReceiver(self);
        _ = check deferMessage(asbReceiverConnection, message?.lockToken.toString());
        return <int>message?.sequenceNumber;
    }

    # Receives a deferred Message. Deferred messages can only be received by using sequence number and return
    # Message object.
    #
    # + sequenceNumber - Unique number assigned to a message by Service Bus. The sequence number is a unique 64-bit
    # integer assigned to a message as it is accepted and stored by the broker and functions as
    # its true identifier.
    # + return - An `asb:Error` if failed to receive deferred message, a Message record if successful or else `()`
    @display {label: "Receive Deferred Message"}
    public isolated function receiveDeferred(@display {label: "Sequence Number of the deferred message"} 
                                            int sequenceNumber) 
                                            returns @display {label: "Deferred Message record"} Message|Error? {
        handle asbReceiverConnection = check getReceiver(self);
        Message|() message = check receiveDeferredMessage(asbReceiverConnection, sequenceNumber);
        if (message is Message) {
            _ = check self.mapContentType(message);
            return message;
        }
        return;
    }

    # The operation renews lock on a message in a queue or subscription based on messageLockToken.
    #
    # + message - Message record
    # + return - An `asb:Error` if failed to renew message or else `()`
    @display {label: "Renew Lock on Message"}
    public isolated function renewLock(@display {label: "Message record"} Message message) returns Error? {
        handle asbReceiverConnection = check getReceiver(self);
        if (message?.lockToken.toString() != DEFAULT_MESSAGE_LOCK_TOKEN) {
            return renewLockMessage(asbReceiverConnection, message?.lockToken.toString());
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
    @display {label: "Set Prefetch Count"}
    public isolated function setPrefetchCount(@display {label: "Prefetch count"} int prefetchCount) returns Error? {
        handle asbReceiverConnection = check getReceiver(self);
        return setPrefetchCountMessage(asbReceiverConnection, prefetchCount);
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
        return;
    }
}

# The ASB service type
public type Service service object {
// TBD when support for optional params in remote functions is available in lang
};

isolated function externInit(Listener lis) = 
@java:Method {
    name: "init",
    'class: "org.ballerinax.asb.util.ListenerUtils"
} external;

isolated function registerListener(Listener lis, Service serviceType) returns Error? = 
@java:Method {
    'class: "org.ballerinax.asb.util.ListenerUtils"
} external;

isolated function 'start(Listener lis) returns Error? = 
@java:Method {
    'class: "org.ballerinax.asb.util.ListenerUtils"
} external;

isolated function detach(Listener lis, Service serviceType) returns Error? = 
@java:Method {
    'class: "org.ballerinax.asb.util.ListenerUtils"
} external;

isolated function stop(Listener lis) returns Error? = 
@java:Method {
    'class: "org.ballerinax.asb.util.ListenerUtils"
} external;

isolated function abortConnection(Listener lis) returns Error? = 
@java:Method {
    'class: "org.ballerinax.asb.util.ListenerUtils"
} external;

isolated function getReceiver(Listener lis) returns handle|Error = 
@java:Method {
    'class: "org.ballerinax.asb.util.ListenerUtils"
} external;

isolated function completeMessage(handle asbHandle, string? lockToken) returns Error? = @java:Method {
    name: "complete",
    'class: "org.ballerinax.asb.util.ListenerUtils"
} external;

isolated function abandonMessage(handle asbHandle, string? lockToken) returns Error? = @java:Method {
    name: "abandon",
    'class: "org.ballerinax.asb.util.ListenerUtils"
} external;

isolated function deadLetterMessage(handle asbHandle, string? lockToken, string? deadLetterReason, 
                            string? deadLetterErrorDescription) returns Error? = @java:Method {
    name: "deadLetter",
    'class: "org.ballerinax.asb.util.ListenerUtils"
} external;

isolated function deferMessage(handle asbHandle, string? lockToken) returns Error? = @java:Method {
    name: "defer",
    'class: "org.ballerinax.asb.util.ListenerUtils"
} external;

isolated function receiveDeferredMessage(handle asbHandle, int sequenceNumber) returns Message|Error? = @java:Method {
    name: "receiveDeferred",
    'class: "org.ballerinax.asb.util.ListenerUtils"
} external;

isolated function renewLockMessage(handle asbHandle, string? lockToken) returns Error? = @java:Method {
    name: "renewLock",
    'class: "org.ballerinax.asb.util.ListenerUtils"
} external;

isolated function setPrefetchCountMessage(handle asbHandle, int prefetchCount) returns Error? = @java:Method {
    name: "setPrefetchCount",
    'class: "org.ballerinax.asb.util.ListenerUtils"
} external;
