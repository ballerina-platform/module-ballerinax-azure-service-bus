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

# Triggers when Choreo recieves a new message from Azure service bus. Available action: onMessage
public type MessageService service object {
    # Triggers when a new message is received from Azure service bus
    # + message - The Azure service bus message recieved
    # + caller - The Azure service bus caller instance
    # + return - Error on failure else nil()
    isolated remote function onMessage(Message message, Caller caller) returns error?;

    # Triggers when there is an error in message processing
    #
    # + context - Error message details  
    # + error - Ballerina error
    # + return - Error on failure else nil()
    isolated remote function onError(ErrorContext context, error 'error) returns error?;
};
