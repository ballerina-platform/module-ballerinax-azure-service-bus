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

public class Message {
    private byte[] messageContent = [];
    private string? messageContentType = ();
    private string? messageId = ();
    private string? to = ();
    private string? replyTo = ();
    private string? replyToSessionId = ();
    private string? label = ();
    private string? sessionId = ();
    private string? correlationId = ();
    private int? timeToLive = ();
    private OptionalProperties? properties = ();

    # Retrieves the text content of the Asb message.
    # ```ballerina
    # string|Asb:Error msgContent = message.getTextContent();
    # ```
    #
    # + return - Message data as string value or else a `Asb:Error` if an error is encountered
    public isolated function getTextContent() returns @tainted string|Error {
        return nativeGetTextContent(self.messageContent);
    }

    # Retrieves the float content of the Asb message.
    # ```ballerina
    # float|Asb:Error msgContent = message.getFloatContent();
    # ```
    #
    # + return - Message data as a float value or else a `Asb:Error` if an error is encountered
    public isolated function getFloatContent() returns @tainted float|Error {
        return  nativeGetFloatContent(self.messageContent);
    }

    # Retrieves the int content of the Asb message.
    # ```ballerina
    # int|Asb:Error msgContent = message.getIntContent();
    # ```
    #
    # + return - Message data as an int value or else a `Asb:Error` if an error is encountered
    public isolated function getIntContent() returns @tainted int|Error {
       return nativeGetIntContent(self.messageContent);
    }

    # Retrieves the JSON content of the Asb message.
    # ```ballerina
    # json|Asb:Error msgContent = message.getJSONContent();
    # ```
    #
    # + return - Message data as a JSON value or else a `Asb:Error` if an error is encountered
    public isolated function getJSONContent() returns @tainted json|Error {
        return nativeGetJSONContent(self.messageContent);
    }

    # Retrieves the XML content of the Asb message.
    # ```ballerina
    # xml|Asb:Error msgContent = message.getXMLContent();
    # ```
    #
    # + return - Message data as an XML value or else a `Asb:Error` if an error is encountered
    public isolated function getXMLContent() returns @tainted xml|Error {
        return nativeGetXMLContent(self.messageContent);
    }

    # Retrieves the Message content type.
    # 
    # + return - Message content type as a string or else ()
    public isolated function getMessageContentType() returns string? {
        return self.messageContentType;
    }

    # Retrieves the Message ID.
    # 
    # + return - Message ID as a string or else ()
    public isolated function getMessageId() returns string? {
        return self.messageId;
    }

    # Retrieves send to address.
    # 
    # + return - Send to address as a string or else ()
    public isolated function getTo() returns string? {
        return self.to;
    }

    # Retrieves address of the queue or subscription to reply to.
    # 
    # + return - Address of the queue or subscription to reply to as a string or else ()
    public isolated function getReplyTo() returns string? {
        return self.replyTo;
    }

    # Retrieves Identifier of the session to reply to.
    # 
    # + return - Identifier of the session to reply to as a string or else ()
    public isolated function getReplyToSessionId() returns string? {
        return self.replyToSessionId;
    }

    # Retrieves Application specific label.
    # 
    # + return - Application specific label as a string or else ()
    public isolated function getLabel() returns string? {
        return self.label;
    }

    # Retrieves Identifier of the session.
    # 
    # + return - Identifier of the session as a string or else ()
    public isolated function getSessionId() returns string? {
        return self.sessionId;
    }

    # Retrieves Identifier of the correlation.
    # 
    # + return - Identifier of the correlation as a string or else ()
    public isolated function getCorrelationId() returns string? {
        return self.correlationId;
    }

    # Retrieves the duration in seconds, that a message is valid. The duration starts from when the message is sent to 
    # the Service Bus.
    # 
    # + return - Identifier of the correlation as a int or else ()
    public isolated function getTimeToLive() returns int? {
        return self.timeToLive;
    }

    # Retrieves optional application specific properties.
    # 
    # + return - Optional properties as a record or else ()
    public isolated function getProperties() returns OptionalProperties? {
        return self.properties;
    }
}

isolated function nativeGetTextContent(byte[] messageContent) returns string|Error =
@java:Method {
    name: "getTextContent",
    'class: "org.ballerinalang.asb.ASBMessageUtils"
} external;

isolated function nativeGetFloatContent(byte[] messageContent) returns float|Error =
@java:Method {
    name: "getFloatContent",
    'class: "org.ballerinalang.asb.ASBMessageUtils"
} external;

isolated function nativeGetIntContent(byte[] messageContent) returns int|Error =
@java:Method {
    name: "getIntContent",
    'class: "org.ballerinalang.asb.ASBMessageUtils"
} external;

isolated function nativeGetJSONContent(byte[] messageContent) returns json|Error =
@java:Method {
    name: "getJSONContent",
    'class: "org.ballerinalang.asb.ASBMessageUtils"
} external;

isolated function nativeGetXMLContent(byte[] messageContent) returns xml|Error =
@java:Method {
    name: "getXMLContent",
    'class: "org.ballerinalang.asb.ASBMessageUtils"
} external;
