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
# + body - Message body, Here the connector supports AMQP message body types - DATA and VALUE, However, DATA type message bodies
#  will be received in Ballerina Byte[] type. VALUE message bodies can be any primitive AMQP type. therefore, the connector 
#  supports for string, int or byte[]. Please refer Azure docs (https://learn.microsoft.com/en-us/java/api/com.azure.core.amqp.models.amqpmessagebody?view=azure-java-stable) 
#  and AMQP docs (https://qpid.apache.org/amqp/type-reference.html#PrimitiveTypes)
# + contentType - Message content type  
# + messageId - Message Id (optional)
# + to - Message to (optional)
# + replyTo - Message reply to (optional)
# + replyToSessionId - Identifier of the session to reply to (optional)
# + label - Message label (optional)
# + sessionId - Message session Id (optional)
# + correlationId - Message correlationId (optional)
# + partitionKey - Message partition key (optional)
# + timeToLive - Message time to live in seconds (optional)
# + sequenceNumber - Message sequence number (optional)
# + lockToken - Message lock token (optional)
# + applicationProperties - Message broker application specific properties (optional)
@display {label: "Message"}
public type Message record {|
    @display {label: "Body"}
    string|int|byte[] body;
    @display {label: "Content Type"}
    string contentType = TEXT;
    @display {label: "Message Id"}
    string messageId?;
    @display {label: "To"}
    string to?;
    @display {label: "Reply To"}
    string replyTo?;
    @display {label: "Reply To Session Id"}
    string replyToSessionId?;
    @display {label: "Label"}
    string label?;
    @display {label: "Session Id"}
    string sessionId?;
    @display {label: "Correlation Id"}
    string correlationId?;
    @display {label: "Partition Key"}
    string partitionKey?;
    @display {label: "Time To Live"}
    int timeToLive?;
    @display {label: "Sequence Number"}
    readonly int sequenceNumber?;
    @display {label: "Lock Token"}
    readonly string lockToken?;
    ApplicationProperties applicationProperties?;
|};

# Azure service bus message, application specific properties representation.
#
# + properties - Key-value pairs for each brokered property (optional)
@display {label: "Application Properties"}
public type ApplicationProperties record {|
    @display {label: "Properties"}
    map<string> properties?;
|};
