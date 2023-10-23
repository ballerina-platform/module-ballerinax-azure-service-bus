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

# Ballerina Azure Service Bus Message Listener.
public class Listener {

    private final string connectionString;
    private final handle listenerHandle;
    private final LogLevel logLevel;
    Caller caller;

    # Gets invoked to initialize the `listener`.
    # The listener initialization requires setting the credentials. 
    # Create an [Azure account](https://azure.microsoft.com) and obtain tokens following [this guide](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-portal).
    #
    # + listenerConfig - The configurations to be used when initializing the `listener`
    # + return - An error if listener initialization failed
    public isolated function init(*ListenerConfig listenerConfig) returns error? {
        self.connectionString = listenerConfig.connectionString;
        self.logLevel = customConfiguration.logLevel;
        self.listenerHandle = initListener(java:fromString(self.connectionString), java:fromString(self.logLevel));
        self.caller = new Caller(self.logLevel);
        externalInit(self.listenerHandle, self.caller);
    }

    # Starts consuming the messages on all the attached services.
    #
    # + return - `()` or else an error upon failure to start
    public isolated function 'start() returns Error? {
        return 'start(self.listenerHandle, self);
    }

    # Attaches the service to the `asb:Listener` endpoint.
    #
    # + s - Type descriptor of the service
    # + name - Name of the service
    # + return - `()` or else an error upon failure to register the service
    public isolated function attach(MessageService s, string[]|string? name = ()) returns Error? {
        return attach(self.listenerHandle, self, s);
    }

    # Stops consuming messages and detaches the service from the `asb:Listener` endpoint.
    #
    # + s - Type descriptor of the service
    # + return - `()` or else  an error upon failure to detach the service
    public isolated function detach(MessageService s) returns Error? {
        return detach(self.listenerHandle, self, s);
    }

    # Stops consuming messages through all consumer services by terminating the connection and all its channels.
    #
    # + return - `()` or else  an error upon failure to close the `ChannelListener`
    public isolated function gracefulStop() returns Error? {
        return stop(self.listenerHandle, self);
    }

    # Stops consuming messages through all the consumer services and terminates the connection
    # with the server.
    #
    # + return - `()` or else  an error upon failure to close the `ChannelListener`.
    public isolated function immediateStop() returns Error? {
        return forceStop(self.listenerHandle, self);
    }
}

isolated function initListener(handle connectionString, handle logLevel)
returns handle = @java:Constructor {
    'class: "org.ballerinax.asb.listener.MessageListener",
    paramTypes: [
        "java.lang.String",
        "java.lang.String"
    ]
} external;

isolated function externalInit(handle listenerHandle, Caller caller) = @java:Method {
    'class: "org.ballerinax.asb.listener.MessageListener"
} external;

isolated function 'start(handle listenerHandle, Listener lis) returns Error? = @java:Method {
    'class: "org.ballerinax.asb.listener.MessageListener"
} external;

isolated function stop(handle listenerHandle, Listener lis) returns Error? = @java:Method {
    'class: "org.ballerinax.asb.listener.MessageListener"
} external;

isolated function attach(handle listenerHandle, Listener lis, MessageService serviceType) returns Error? = @java:Method {
    'class: "org.ballerinax.asb.listener.MessageListener"
} external;

isolated function detach(handle listenerHandle, Listener lis, MessageService serviceType) returns Error? = @java:Method {
    'class: "org.ballerinax.asb.listener.MessageListener"
} external;

isolated function forceStop(handle listenerHandle, Listener lis) returns Error? = @java:Method {
    'class: "org.ballerinax.asb.listener.MessageListener"
} external;

