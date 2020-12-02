// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/java;

# Represents a single network receiver connection to the Asb broker.
public class ReceiverConnection {

    handle asbReceiverConnection;

    private string connectionString;
    private string entityPath;

    # Initiates an Asb Receiver Connection using the given connection configuration.
    # 
    # + connectionConfiguration - Configurations used to create a `asb:Connection`
    public isolated function init(ConnectionConfiguration connectionConfiguration) {
        self.connectionString = connectionConfiguration.connectionString;
        self.entityPath = connectionConfiguration.entityPath;
        self.asbReceiverConnection = <handle> createReceiverConnection(java:fromString(self.connectionString),
            java:fromString(self.entityPath));
    }

    # Creates a Asb Receiver Connection using the given connection parameters.
    # 
    # + connectionConfiguration - Configurations used to create a `asb:Connection`
    # + return - An `asb:Error` if failed to create connection or else `()`
    public isolated function createReceiverConnection(ConnectionConfiguration connectionConfiguration) 
        returns handle|Error? {
        self.connectionString = connectionConfiguration.connectionString;
        self.entityPath = connectionConfiguration.entityPath;
        self.asbReceiverConnection = <handle> createReceiverConnection(java:fromString(self.connectionString),
            java:fromString(self.entityPath));
    }

    # Closes the Asb Receiver Connection using the given connection parameters.
    #
    # + return - An `asb:Error` if failed to close connection or else `()`
    public isolated function closeReceiverConnection() returns Error? {
        return closeReceiverConnection(self.asbReceiverConnection);
    }

    # Receive Message from queue.
    # 
    # + return - A Message object
    public isolated function receiveMessage() returns Message|Error {
        return receiveMessage(self.asbReceiverConnection);
    }

    # Receive messages from queue.
    # 
    # + return - A Messages object with an array of Message objects
    public isolated function receiveMessages() returns Messages|Error {
        return receiveMessages(self.asbReceiverConnection);
    }

    # Receive batch of messages from queue.
    # 
    # + maxMessageCount - Maximum no. of messages in a batch
    # + return - A Message object
    public isolated function receiveBatchMessage(int maxMessageCount) returns Messages|Error {
        return receiveBatchMessage(self.asbReceiverConnection, maxMessageCount);
    }

    # Complete Messages from Queue or Subscription based on messageLockToken.
    # 
    # + return - An `asb:Error` if failed to complete message or else `()`
    public isolated function completeMessages() returns Error? {
        return completeMessages(self.asbReceiverConnection);
    }

    # Complete One Message from Queue or Subscription based on messageLockToken.
    # 
    # + return - An `asb:Error` if failed to complete messages or else `()`
    public isolated function completeOneMessage() returns Error? {
        return completeOneMessage(self.asbReceiverConnection);
    }

    # Abandon message & make available again for processing from Queue or Subscription based on messageLockToken
    # 
    # + return - An `asb:Error` if failed to abandon message or else `()`
    public isolated function abandonMessage() returns Error? {
        return abandonMessage(self.asbReceiverConnection);
    }

    # Get the asbReceiverConnection instance
    # 
    # + return - asbReceiverConnection instance
    isolated function getAsbReceiverConnection() returns handle {
        return self.asbReceiverConnection;
    }
}

isolated function createReceiverConnection(handle connectionString, handle entityPath) 
    returns handle|Error? = @java:Method {
    name: "createReceiverConnection",
    'class: "org.ballerinalang.asb.connection.ConUtils"
} external;

isolated function closeReceiverConnection(handle imessageSender) returns Error? = @java:Method {
    name: "closeReceiverConnection",
    'class: "org.ballerinalang.asb.connection.ConUtils"
} external;

isolated function receiveMessage(handle imessageReceiver) returns Message|Error = @java:Method {
    name: "receiveMessage",
    'class: "org.ballerinalang.asb.connection.ConUtils"
} external;

isolated function receiveMessages(handle imessageReceiver) returns Messages|Error = @java:Method {
    name: "receiveMessages",
    'class: "org.ballerinalang.asb.connection.ConUtils"
} external;

isolated function receiveBatchMessage(handle imessageReceiver, int maxMessageCount) 
    returns Messages|Error = @java:Method {
    name: "receiveBatchMessage",
    'class: "org.ballerinalang.asb.connection.ConUtils"
} external;

isolated function completeMessages(handle imessageReceiver) returns Error? = @java:Method {
    name: "completeMessages",
    'class: "org.ballerinalang.asb.connection.ConUtils"
} external;

isolated function completeOneMessage(handle imessageReceiver) returns Error? = @java:Method {
    name: "completeOneMessage",
    'class: "org.ballerinalang.asb.connection.ConUtils"
} external;

isolated function abandonMessage(handle imessageReceiver) returns Error? = @java:Method {
    name: "abandonMessage",
    'class: "org.ballerinalang.asb.connection.ConUtils"
} external;
