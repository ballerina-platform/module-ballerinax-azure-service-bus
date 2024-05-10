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

# Represents a ASB consumer listener.
public isolated class Listener {

    # Creates a new `asb:Listener`.
    # ```ballerina
    # listener asb:Listener asbListener = check new (
    #   connectionString = "xxxxxxxx",
    #   entityConfig = {
    #       queueName: "test-queue"
    #   },
    #   autoComplete = false
    # );
    # ```
    # 
    # + config - ASB listener configurations
    # + return - An `asb:Error` if an error is encountered or else '()'
    public isolated function init(*ListenerConfiguration config) returns Error? {
        return self.externInit(config);
    }


    private isolated function externInit(ListenerConfiguration config) returns Error? = @java:Method {
        'class: "org.ballerinax.asb.listener.NativeListener"
    } external;

    # Attaches an `asb:Service` to a listener.
    # ```ballerina
    # check asbListener.attach(asbService);
    # ```
    # 
    # + 'service - The service instance
    # + name - Name of the service
    # + return - An `asb:Error` if there is an error or else `()`
    public isolated function attach(Service 'service, string[]|string? name = ()) returns Error? = @java:Method {
        'class: "org.ballerinax.asb.listener.NativeListener"
    } external;

    # Detaches an `asb:Service` from the the listener.
    # ```ballerina
    # check asbListener.detach(asbService);
    # ```
    #
    # + 'service - The service to be detached
    # + return - An `asb:Error` if there is an error or else `()`
    public isolated function detach(Service 'service) returns Error? = @java:Method {
        'class: "org.ballerinax.asb.listener.NativeListener"
    } external;

    # Starts the `asb:Listener`.
    # ```ballerina
    # check asbListener.'start();
    # ```
    #
    # + return - An `asb:Error` if there is an error or else `()`
    public isolated function 'start() returns Error? = @java:Method {
        'class: "org.ballerinax.asb.listener.NativeListener"
    } external;

    # Stops the `asb:Listener` gracefully.
    # ```ballerina
    # check asbListener.gracefulStop();
    # ```
    #
    # + return - An `asb:Error` if there is an error or else `()`
    public isolated function gracefulStop() returns Error? = @java:Method {
        'class: "org.ballerinax.asb.listener.NativeListener"
    } external;

    # Stops the `asb:Listener` immediately.
    # ```ballerina
    # check asbListener.immediateStop();
    # ```
    #
    # + return - An `asb:Error` if there is an error or else `()`
    public isolated function immediateStop() returns Error? = @java:Method {
        'class: "org.ballerinax.asb.listener.NativeListener"
    } external;
}
