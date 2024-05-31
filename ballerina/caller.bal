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

# Represents a ASB caller, which can be used to mark messages as complete, abandon, deadLetter, or defer.
public isolated client class Caller {

    # Complete message from queue or subscription based on messageLockToken. Declares the message processing to be 
    # successfully completed, removing the message from the queue.
    # 
    # + return - An `asb:Error` if failed to complete message or else `()`
    isolated remote function complete() returns Error? = @java:Method {
        'class: "org.ballerinax.asb.listener.NativeCaller"
    } external;

    # Abandon message from queue or subscription based on messageLockToken. Abandon processing of the message for 
    # the time being, returning the message immediately back to the queue to be picked up by another (or the same) 
    # receiver.
    # 
    # + propertiesToModify - Message properties to modify
    # + return - An `asb:Error` if failed to abandon message or else `()`
    isolated remote function abandon(*record {|anydata...;|} propertiesToModify) returns Error? = @java:Method {
        'class: "org.ballerinax.asb.listener.NativeCaller"
    } external;

    # Dead-Letter the message & moves the message to the Dead-Letter Queue based on messageLockToken. Transfer 
    # the message from the primary queue into a special "dead-letter sub-queue".
    # 
    # + options - Options to specify while putting message in dead-letter queue
    # + return - An `asb:Error` if failed to deadletter message or else `()`
    isolated remote function deadLetter(*DeadLetterOptions options) returns Error? = @java:Method {
        'class: "org.ballerinax.asb.listener.NativeCaller"
    } external;

    # Defer the message in a Queue or Subscription based on messageLockToken.  It prevents the message from being 
    # directly received from the queue by setting it aside such that it must be received by sequence number.
    # 
    # + propertiesToModify - Message properties to modify
    # + return - An `asb:Error` if failed to defer message or else sequence number
    isolated remote function defer(*record {|anydata...;|} propertiesToModify) returns Error? = @java:Method {
        'class: "org.ballerinax.asb.listener.NativeCaller"
    } external;
}
