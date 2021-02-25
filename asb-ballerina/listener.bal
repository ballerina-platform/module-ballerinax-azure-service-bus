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
}  

# The ASB service type
public type Service service object {
    // TBD when support for optional params in remote functions is available in lang
};

isolated function externInit(Listener lis) =
@java:Method {
    name: "init",
    'class: "org.ballerinalang.asb.connection.ListenerUtils"
} external;

isolated function registerListener(Listener lis, Service serviceType) returns Error? =
@java:Method {
    'class: "org.ballerinalang.asb.connection.ListenerUtils"
} external;

isolated function 'start(Listener lis) returns Error? =
@java:Method {
    'class: "org.ballerinalang.asb.connection.ListenerUtils"
} external;

isolated function detach(Listener lis, Service serviceType) returns Error? =
@java:Method {
    'class: "org.ballerinalang.asb.connection.ListenerUtils"
} external;

isolated function stop(Listener lis) returns Error? =
@java:Method {
    'class: "org.ballerinalang.asb.connection.ListenerUtils"
} external;

isolated function abortConnection(Listener lis) returns Error? =
@java:Method {
    'class: "org.ballerinalang.asb.connection.ListenerUtils"
} external;
