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

// Constants

final handle JAVA_NULL = java:createNull();

const int DEFAULT_TIME_TO_LIVE = 5;
const int DEFAULT_MAX_MESSAGE_COUNT = 1;
const int DEFAULT_SERVER_WAIT_TIME = 5;

// Sender & Receiver Client API Record Types.

# Configurations used to create a `asb:Connection`.
#
# + connectionString - Service bus connection string with Shared Access Signatures
#                      ConnectionString format: 
#                      Endpoint=sb://namespace_DNS_Name;EntityPath=EVENT_HUB_NAME;
#                      SharedAccessKeyName=SHARED_ACCESS_KEY_NAME;SharedAccessKey=SHARED_ACCESS_KEY or  
#                      Endpoint=sb://namespace_DNS_Name;EntityPath=EVENT_HUB_NAME;
#                      SharedAccessSignatureToken=SHARED_ACCESS_SIGNATURE_TOKEN
# + entityPath - Entitypath to the message broker resource
public type ConnectionConfiguration record {|
    string connectionString;
    string entityPath;
|};

# Optional application specific properties of the message.
#
# + properties - Key-value pairs for each brokered property
public type OptionalProperties record {|
    map<anydata> properties;
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
# + queueName - Entitypath to the message broker resource
public type QueueConfiguration record {|
    string connectionString;
    string queueName;
|};

# Service configurations used to create a `asb:Connection`.
# 
# + queueConfig - Configurations used to create a `asb:Connection`
public type asbServiceConfig record {|
    QueueConfiguration queueConfig;
|};

# The annotation, which is used to configure the subscription.
public annotation asbServiceConfig ServiceConfig on service;
