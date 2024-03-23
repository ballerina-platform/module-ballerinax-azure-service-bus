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

    # Gets invoked to initialize the `listener`.
    # The listener initialization requires setting the credentials. 
    # Create an [Azure account](https://azure.microsoft.com) and obtain tokens following [this guide](https://docs.microsoft.com/en-us/azure/service-bus-messaging/service-bus-quickstart-portal).
    #
    # + listenerConfig - The configurations to be used when initializing the `listener`
    # + return - An error if listener initialization failed
    public isolated function init(*ListenerConfig listenerConfig) returns error? {
        check initializeListner(java:fromString(listenerConfig.connectionString), java:fromString(customConfiguration.logLevel), new Caller(customConfiguration.logLevel));
    }

    # Starts consuming the messages on all the attached services.
    #
    # + return - `()` or else an error upon failure to start
    public isolated function 'start() returns Error? = @java:Method {
        'class: "org.ballerinax.asb.listener.MessageListener"
    } external;

    # Attaches the service to the `asb:Listener` endpoint.
    #
    # + messageService - Type descriptor of the service
    # + name - Name of the service
    # + return - `()` or else an error upon failure to register the service
    public isolated function attach(MessageService messageService, string[]|string? name = ()) returns Error? {
        return attach(messageService);
    }

    # Stops consuming messages and detaches the service from the `asb:Listener` endpoint.
    #
    # + messageService - Type descriptor of the service
    # + return - `()` or else an error upon failure to detach the service
    public isolated function detach(MessageService messageService) returns Error? = @java:Method {
        'class: "org.ballerinax.asb.listener.MessageListener"
    } external;

    # Stops consuming messages through all consumer services by terminating the connection and all its channels.
    #
    # + return - `()` or else an error upon failure to close the `ChannelListener`
    public isolated function gracefulStop() returns Error? = @java:Method {
        'class: "org.ballerinax.asb.listener.MessageListener"
    } external;

    # Stops consuming messages through all the consumer services and terminates the connection
    # with the server.
    #
    # + return - `()` or else an error upon failure to close the `ChannelListener`.
    public isolated function immediateStop() returns Error? = @java:Method {
        'class: "org.ballerinax.asb.listener.MessageListener"
    } external;
}

isolated function initializeListner(handle connectionString, handle logLevel, Caller caller) returns Error? = @java:Method {
    'class: "org.ballerinax.asb.listener.MessageListener"
} external;

isolated function attach(MessageService serviceType) returns Error? = @java:Method {
    'class: "org.ballerinax.asb.listener.MessageListener"
} external;

