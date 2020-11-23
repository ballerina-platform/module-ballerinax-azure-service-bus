import ballerina/java;

public class Message {
    private byte[] messageContent = [];

    public isolated function getTextContent() returns @tainted string|Error {
        return nativeGetTextContent(self.messageContent);
    }
}

isolated function nativeGetTextContent(byte[] messageContent) returns string|Error =
@java:Method {
    name: "getTextContent",
    'class: "org.ballerinalang.asb.AsbMessageUtils"
} external;