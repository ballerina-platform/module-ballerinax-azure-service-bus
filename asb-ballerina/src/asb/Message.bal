import ballerina/java;

public class Message {
    private byte[] messageContent = [];

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
}

isolated function nativeGetTextContent(byte[] messageContent) returns string|Error =
@java:Method {
    name: "getTextContent",
    'class: "org.ballerinalang.asb.AsbMessageUtils"
} external;

isolated function nativeGetFloatContent(byte[] messageContent) returns float|Error =
@java:Method {
    name: "getFloatContent",
    'class: "org.ballerinalang.asb.AsbMessageUtils"
} external;

isolated function nativeGetIntContent(byte[] messageContent) returns int|Error =
@java:Method {
    name: "getIntContent",
    'class: "org.ballerinalang.asb.AsbMessageUtils"
} external;

isolated function nativeGetJSONContent(byte[] messageContent) returns json|Error =
@java:Method {
    name: "getJSONContent",
    'class: "org.ballerinalang.asb.AsbMessageUtils"
} external;

isolated function nativeGetXMLContent(byte[] messageContent) returns xml|Error =
@java:Method {
    name: "getXMLContent",
    'class: "org.ballerinalang.asb.AsbMessageUtils"
} external;