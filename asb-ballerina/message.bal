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

# Azure service bus Message representation.
#
# + body - Message body  
# + contentType - Message content type  
# + messageId - Message Id 
# + to - Message to
# + replyTo - Message reply to 
# + replyToSessionId - Identifier of the session to reply to
# + label - Message label 
# + sessionId - Message session Id
# + correlationId - Message correlationId
# + partitionKey - Message partition key
# + timeToLive - Message time to live in seconds 
# + sequenceNumber - Message sequence number
# + lockToken - Message lock token  
# + applicationProperties - Message broker application specific properties
public type Message record {|
    string|xml|json|byte[] body;
    string contentType?;
    string messageId?;
    string to?;
    string replyTo?;
    string replyToSessionId?;
    string label?;
    string sessionId?;
    string correlationId?;
    string partitionKey?;
    int timeToLive?;
    readonly int sequenceNumber?;
    readonly string lockToken?;
    ApplicationProperties applicationProperties?;
|};

# Azure service bus message, application specific properties representation.
#
# + properties - Key-value pairs for each brokered property
public type ApplicationProperties record {|
    map<string> properties?;
|};
