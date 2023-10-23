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

# Azure Service Bus Caller to perform functions on dispatched messages. 
public class Caller {

    private final LogLevel logLevel;

    isolated function init(LogLevel logLevel) {
        self.logLevel = logLevel;
    }

    # Complete message from queue or subscription based on messageLockToken. Declares the message processing to be 
    # successfully completed, removing the message from the queue.
    #
    # + message - Message record
    # + return - An `asb:Error` if failed to complete message or else `()`
    public isolated function complete(@display {label: "Message record"} Message message) returns Error? {
        return completeMessage(self, message.lockToken, self.logLevel);
    }

    # Abandon message from queue or subscription based on messageLockToken. Abandon processing of the message for 
    # the time being, returning the message immediately back to the queue to be picked up by another (or the same) 
    # receiver.
    #
    # + message - Message record
    # + return - An `asb:Error` if failed to abandon message or else `()`
    public isolated function abandon(@display {label: "Message record"} Message message) returns Error? {
        return abandonMessage(self, message.lockToken, self.logLevel);
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
        return deadLetterMessage(self, message.lockToken, deadLetterReason,
                deadLetterErrorDescription, self.logLevel);
    }

    # Defer the message in a Queue or Subscription based on messageLockToken.  It prevents the message from being 
    # directly received from the queue by setting it aside such that it must be received by sequence number.
    #
    # + message - Message record
    # + return - An `asb:Error` if failed to defer message or else sequence number
    public isolated function defer(@display {label: "Message record"} Message message)
                                    returns @display {label: "Sequence Number of the deferred message"} int|Error {
        check deferMessage(self, message.lockToken, self.logLevel);
        return <int>message.sequenceNumber;
    }
}

isolated function completeMessage(Caller caller, string? lockToken, string? logLevel) returns Error? = @java:Method {
    name: "complete",
    'class: "org.ballerinax.asb.listener.Caller"
} external;

isolated function abandonMessage(Caller caller, string? lockToken, string? logLevel) returns Error? = @java:Method {
    name: "abandon",
    'class: "org.ballerinax.asb.listener.Caller"
} external;

isolated function deadLetterMessage(Caller caller, string? lockToken, string? deadLetterReason,
        string? deadLetterErrorDescription, string? logLevel) returns Error? = @java:Method {
    name: "deadLetter",
    'class: "org.ballerinax.asb.listener.Caller"
} external;

isolated function deferMessage(Caller caller, string? lockToken, string? logLevel) returns Error? = @java:Method {
    name: "defer",
    'class: "org.ballerinax.asb.listener.Caller"
} external;
