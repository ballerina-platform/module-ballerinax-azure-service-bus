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

# Represents a single network receiver connection to the Asb broker.
@display {label: "Azure Service Bus Receiver Client", iconPath: "AzureServiceBusLogo.png"}
public client class ReceiverConnection {

    handle asbReceiverConnection  = JAVA_NULL;

    private string connectionString;
    private string entityPath;

    # Initiates an Asb Receiver Connection using the given connection configuration.
    # 
    # + connectionConfiguration - Configurations used to create a `asb:ReceiverConnection`
    # + return - An `asb:Error` if failed to create connection or else `()`
    public isolated function init(ConnectionConfiguration connectionConfiguration) returns Error? {
        self.connectionString = connectionConfiguration.connectionString;
        self.entityPath = connectionConfiguration.entityPath;
        var connectionResult = createReceiverConnection(java:fromString(self.connectionString),
            java:fromString(self.entityPath));
        if (connectionResult is handle) {
            self.asbReceiverConnection = <handle> connectionResult;
            return;
        } else {
            return connectionResult;
        }    
    }

    # Creates a Asb Receiver Connection using the given connection parameters.
    # 
    # + connectionConfiguration - Configurations used to create a `asb:ReceiverConnection`
    # + return - An `asb:Error` if failed to create connection or else `()`
    @display {label: "Create Receiver Client Connection"}
    public isolated function createReceiverConnection(@display {label: "Receiver Client Connection Configuration"} 
                                                      ConnectionConfiguration connectionConfiguration) 
                                                      returns @display {label: "Result"} Error? {
        self.connectionString = connectionConfiguration.connectionString;
        self.entityPath = connectionConfiguration.entityPath;
        var connectionResult = createReceiverConnection(java:fromString(self.connectionString),
            java:fromString(self.entityPath));
        if (connectionResult is handle) {
            self.asbReceiverConnection = <handle> connectionResult;
            return;
        } else {
            return connectionResult;
        }
    }

    # Closes the Asb Receiver Connection using the given connection parameters.
    #
    # + return - An `asb:Error` if failed to close connection or else `()`
    @display {label: "Close Receiver Client Connection"}
    public isolated function closeReceiverConnection() returns @display {label: "Result"} Error? {
        var connectionResult = closeReceiverConnection(self.asbReceiverConnection);
        if (connectionResult is ()) {
            return;
        } else {
            return connectionResult;
        }
    }

    # Receive Message from queue.
    # 
    # + serverWaitTime - Specified server wait time in seconds to receive message.
    # + return - A Message object
    @display {label: "Receive Message"}
    isolated remote function receiveMessage(@display {label: "Server wait time in seconds to receive message (optional)"} 
                                            int? serverWaitTime = DEFAULT_SERVER_WAIT_TIME) 
                                            returns @display {label: "Message"} Message|Error? {
        return receiveMessage(self.asbReceiverConnection, serverWaitTime);
    }

    # Receive messages from queue.
    # 
    # + serverWaitTime - Specified server wait time in seconds to receive message.
    # + maxMessageCount - Maximum no. of messages in a batch 
    # + return - A Messages object with an array of Message objects
    @display {label: "Receive Messages"}
    isolated remote function receiveMessages(@display {label: "Server wait time in seconds to receive message (optional)"} 
                                             int? serverWaitTime = DEFAULT_SERVER_WAIT_TIME, 
                                             @display {label: "Maximum number of messages in a batch (optional)"} 
                                             int? maxMessageCount = DEFAULT_MAX_MESSAGE_COUNT) 
                                             returns @display {label: "Messages"} Messages|Error {
        return receiveMessages(self.asbReceiverConnection, serverWaitTime, maxMessageCount);
    }

    # Receive batch of messages from queue.
    # 
    # + maxMessageCount - Maximum no. of messages in a batch
    # + return - A Message object
    @display {label: "Receive Batch of Messages"}
    isolated remote function receiveBatchMessage(@display {label: "Server wait time in seconds to receive message (optional)"} 
                                                 int? maxMessageCount = DEFAULT_MAX_MESSAGE_COUNT) 
                                                 returns @display {label: "Messages"} Messages|Error {
        return receiveBatchMessage(self.asbReceiverConnection, maxMessageCount);
    }

    # Complete Messages from Queue or Subscription based on messageLockToken.
    # 
    # + return - An `asb:Error` if failed to complete message or else `()`
    @display {label: "Complete Messages"}
    isolated remote function completeMessages() returns @display {label: "Result"} Error? {
        return completeMessages(self.asbReceiverConnection);
    }

    # Complete One Message from Queue or Subscription based on messageLockToken.
    # 
    # + return - An `asb:Error` if failed to complete messages or else `()`
    @display {label: "Complete Message"}
    isolated remote function completeOneMessage() returns @display {label: "Result"} Error? {
        return completeOneMessage(self.asbReceiverConnection);
    }

    # Abandon message & make available again for processing from Queue or Subscription based on messageLockToken.
    # 
    # + return - An `asb:Error` if failed to abandon message or else `()`
    @display {label: "Abandon Message"}
    isolated remote function abandonMessage() returns @display {label: "Result"} Error? {
        return abandonMessage(self.asbReceiverConnection);
    }

    # Dead-Letter the message & moves the message to the Dead-Letter Queue based on messageLockToken.
    # 
    # + deadLetterReason - The deadletter reason.
    # + deadLetterErrorDescription - The deadletter error description.
    # + return - An `asb:Error` if failed to deadletter message or else `()`
    @display {label: "Dead Letter Message"}
    isolated remote function deadLetterMessage(@display {label: "Dead letter reason (optional)"} 
                                               string? deadLetterReason = (), 
                                               @display{label: "Dead letter description (optional)"} 
                                               string? deadLetterErrorDescription = ()) 
                                               returns @display {label: "Result"} Error? {
        return deadLetterMessage(self.asbReceiverConnection, deadLetterReason, deadLetterErrorDescription);
    }

    #  Defer the message in a Queue or Subscription based on messageLockToken.
    # 
    # + return - An `asb:Error` if failed to defer message or else sequence number
    @display {label: "Defer Message"}
    isolated remote function deferMessage() returns @display {label: "Sequence Number of the deferred message"} int|Error {
        return deferMessage(self.asbReceiverConnection);
    }

    #  Receives a deferred Message. Deferred messages can only be received by using sequence number and return
    #  Message object.
    # 
    # + sequenceNumber - Unique number assigned to a message by Service Bus. The sequence number is a unique 64-bit
    #                    integer assigned to a message as it is accepted and stored by the broker and functions as
    #                    its true identifier.
    # + return - An `asb:Error` if failed to receive deferred message or else `()`
    @display {label: "Receive Deferred Message"}
    isolated remote function receiveDeferredMessage(@display {label: "Sequence Number of the deferred message"} 
                                                    int sequenceNumber) 
                                                    returns @display {label: "Deferred Message"} Message|Error? {
        return receiveDeferredMessage(self.asbReceiverConnection, sequenceNumber);
    }

    # The operation renews lock on a message in a queue or subscription based on messageLockToken.
    # 
    # + return - An `asb:Error` if failed to renew message or else `()`
    @display {label: "Renew Lock on Message"}
    isolated remote function renewLockOnMessage() returns @display {label: "Result"} Error? {
        return renewLockOnMessage(self.asbReceiverConnection);
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
    isolated remote function setPrefetchCount(@display {label: "Prefetch count"} int prefetchCount) 
                                              returns @display {label: "Result"} Error? {
        return setPrefetchCount(self.asbReceiverConnection, prefetchCount);
    }

    # Get the asbReceiverConnection instance
    # 
    # + return - asbReceiverConnection instance
    @display {label: "Get Receiver Client Connection"}
    isolated function getAsbReceiverConnection() returns @display {label: "Receiver Client Connection"} handle {
        return self.asbReceiverConnection;
    }
}

isolated function createReceiverConnection(handle connectionString, handle entityPath) 
    returns handle|Error? = @java:Method {
    name: "createReceiverConnection",
    'class: "org.ballerinalang.asb.connection.ConnectionUtils"
} external;

isolated function closeReceiverConnection(handle imessageSender) returns Error? = @java:Method {
    name: "closeReceiverConnection",
    'class: "org.ballerinalang.asb.connection.ConnectionUtils"
} external;

isolated function receiveMessage(handle imessageReceiver, int? serverWaitTime) returns Message|Error? = @java:Method {
    name: "receiveMessage",
    'class: "org.ballerinalang.asb.connection.ConnectionUtils"
} external;

isolated function receiveMessages(handle imessageReceiver, int? serverWaitTime, int? maxMessageCount) 
    returns Messages|Error = @java:Method {
    name: "receiveMessages",
    'class: "org.ballerinalang.asb.connection.ConnectionUtils"
} external;

isolated function receiveBatchMessage(handle imessageReceiver, int? maxMessageCount) 
    returns Messages|Error = @java:Method {
    name: "receiveBatchMessage",
    'class: "org.ballerinalang.asb.connection.ConnectionUtils"
} external;

isolated function completeMessages(handle imessageReceiver) returns Error? = @java:Method {
    name: "completeMessages",
    'class: "org.ballerinalang.asb.connection.ConnectionUtils"
} external;

isolated function completeOneMessage(handle imessageReceiver) returns Error? = @java:Method {
    name: "completeOneMessage",
    'class: "org.ballerinalang.asb.connection.ConnectionUtils"
} external;

isolated function abandonMessage(handle imessageReceiver) returns Error? = @java:Method {
    name: "abandonMessage",
    'class: "org.ballerinalang.asb.connection.ConnectionUtils"
} external;

isolated function deadLetterMessage(handle imessageReceiver, string? deadLetterReason, 
    string? deadLetterErrorDescription) returns Error? = @java:Method {
    name: "deadLetterMessage",
    'class: "org.ballerinalang.asb.connection.ConnectionUtils"
} external;

isolated function deferMessage(handle imessageReceiver) returns int|Error = @java:Method {
    name: "deferMessage",
    'class: "org.ballerinalang.asb.connection.ConnectionUtils"
} external;

isolated function receiveDeferredMessage(handle imessageReceiver, int sequenceNumber) 
    returns Message|Error? = @java:Method {
    name: "receiveDeferredMessage",
    'class: "org.ballerinalang.asb.connection.ConnectionUtils"
} external;

isolated function renewLockOnMessage(handle imessageReceiver) returns Error? = @java:Method {
    name: "renewLockOnMessage",
    'class: "org.ballerinalang.asb.connection.ConnectionUtils"
} external;

isolated function setPrefetchCount(handle imessageReceiver, int prefetchCount) returns Error? = @java:Method {
    name: "setPrefetchCount",
    'class: "org.ballerinalang.asb.connection.ConnectionUtils"
} external;
