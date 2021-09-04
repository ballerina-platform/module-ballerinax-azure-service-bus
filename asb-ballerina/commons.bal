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

// Constants

final handle JAVA_NULL = java:createNull();

// Default values
public const int DEFAULT_TIME_TO_LIVE = 300; // In seconds
public const int DEFAULT_POOL_SIZE= 20;
public const int DEFAULT_MAX_MESSAGE_COUNT = 1;
public const int DEFAULT_MAX_POOL_SIZE = 20;
public const int DEFAULT_SERVER_WAIT_TIME = 300; // In seconds
public const string DEFAULT_MESSAGE_LOCK_TOKEN = "00000000-0000-0000-0000-000000000000";

// Message content types
public const string TEXT = "text/plain";
public const string JSON = "application/json";
public const string XML = "application/xml";
public const string BYTE_ARRAY = "application/octet-stream";

// Message receive modes
@display {label: "Receive Modes"}
public enum receiveModes {
    @display {label: "PEEKLOCK"}
    PEEKLOCK,
    @display {label: "RECEIVEANDDELETE"}
    RECEIVEANDDELETE
}

// Azure Service Bus Client API Record Types.

# Configurations used to create an `asb:Connection`.
#
# + connectionString - Service bus connection string with Shared Access Signatures
#                      ConnectionString format: 
#                      Endpoint=sb://namespace_DNS_Name;EntityPath=EVENT_HUB_NAME;
#                      SharedAccessKeyName=SHARED_ACCESS_KEY_NAME;SharedAccessKey=SHARED_ACCESS_KEY or  
#                      Endpoint=sb://namespace_DNS_Name;EntityPath=EVENT_HUB_NAME;
#                      SharedAccessSignatureToken=SHARED_ACCESS_SIGNATURE_TOKEN
@display {label: "Connection Config"}
public type AsbConnectionConfiguration record {|
    @display {label: "Connection String"}
    string connectionString;
|};

// Listener API Record Types and Annotations.

# Configurations used to create a `asb:Connection`.
#
# + connectionString - Service bus connection string with Shared Access Signatures
#                      ConnectionString format: 
#                      Endpoint=sb://namespace_DNS_Name;EntityPath=EVENT_HUB_NAME;
#                      SharedAccessKeyName=SHARED_ACCESS_KEY_NAME;SharedAccessKey=SHARED_ACCESS_KEY or  
#                      Endpoint=sb://namespace_DNS_Name;EntityPath=EVENT_HUB_NAME;
#                      SharedAccessSignatureToken=SHARED_ACCESS_SIGNATURE_TOKEN
# + entityPath - Entitypath to the message broker resource
# + receiveMode - Message receive modes (optional)
@display {label: "Entity Config"}
public type EntityConfiguration record {|
    @display {label: "Connection String"}
    string connectionString;
    @display {label: "Entity Path"}
    string entityPath;
    @display {label: "Receive Mode"}
    string receiveMode?;
|};

# Service configurations used to create a `asb:Connection`.
# 
# + entityConfig - Configurations used to create a `asb:Connection`
@display {label: "Connection Config"}
public type asbServiceConfig record {|
    EntityConfiguration entityConfig;
|};

# The annotation, which is used to configure the subscription.
public annotation asbServiceConfig ServiceConfig on service;

// # This function returns the user defined poolSize value or deafult value.
// # 
// # + poolSize - configured pool size value.
// # + return - return maximum pool size that need to be configured.
// isolated function getMaxPoolSize(int? poolSize) returns int {
//     if (poolSize != null && poolSize is int) {
//         return poolSize;
//     } else {
//         return DEFAULT_MAX_POOL_SIZE;
//     }
// }

// # This function returns the receive mode that needs to be set.
// # 
// # + receiveMode - configured receive mode.
// # + return - return receive mode that need to be configured.
// isolated function setReceiveMode(string? receiveMode) returns string {
//     if (receiveMode != null && receiveMode == RECEIVEANDDELETE) { //check
//         return RECEIVEANDDELETE;
//     } else {
//         return PEEKLOCK;
//     }
// }

isolated function nativeGetTextContent(byte[] messageContent) returns string|Error =
@java:Method {
    name: "getTextContent",
    'class: "org.ballerinax.asb.util.ASBUtils"
} external;

isolated function nativeGetFloatContent(byte[] messageContent) returns float|Error =
@java:Method {
    name: "getFloatContent",
    'class: "org.ballerinax.asb.util.ASBUtils"
} external;

isolated function nativeGetIntContent(byte[] messageContent) returns int|Error =
@java:Method {
    name: "getIntContent",
    'class: "org.ballerinax.asb.util.ASBUtils"
} external;

isolated function nativeGetJSONContent(byte[] messageContent) returns json|Error =
@java:Method {
    name: "getJSONContent",
    'class: "org.ballerinax.asb.util.ASBUtils"
} external;

isolated function nativeGetXMLContent(byte[] messageContent) returns xml|Error =
@java:Method {
    name: "getXMLContent",
    'class: "org.ballerinax.asb.util.ASBUtils"
} external;