import ballerina/java;

public class ReceiverConnection{

    handle asbReceiverConnection;

    private string connectionString;
    private string entityPath;

    public isolated function init(ConnectionConfiguration connectionConfiguration) {
        self.connectionString = connectionConfiguration.connectionString;
        self.entityPath = connectionConfiguration.entityPath;
        self.asbReceiverConnection = <handle> createReceiverConnection(java:fromString(self.connectionString),java:fromString(self.entityPath));
    }

    public isolated function createReceiverConnection(ConnectionConfiguration connectionConfiguration) returns handle|error? {
        self.connectionString = connectionConfiguration.connectionString;
        self.entityPath = connectionConfiguration.entityPath;
        self.asbReceiverConnection = <handle> createReceiverConnection(java:fromString(self.connectionString),java:fromString(self.entityPath));
    }

    public isolated function closeReceiverConnection() returns error? {
        return closeReceiverConnection(self.asbReceiverConnection);
    }

    public isolated function receiveBytesMessageViaReceiverConnectionWithConfigurableParameters() returns handle|error? {
        return receiveBytesMessageViaReceiverConnectionWithConfigurableParameters(self.asbReceiverConnection);
    }

    public isolated function checkMessage(handle imessages) returns error? {
        checkpanic checkMessage(imessages);
    }

    public isolated function getTextContent(byte[] content) returns @tainted string|Error {

        return nativeGetTextContent(content);
    }

    public isolated function receiveOneBytesMessageViaReceiverConnectionWithConfigurableParameters() returns Message|error {
        return receiveOneBytesMessageViaReceiverConnectionWithConfigurableParameters(self.asbReceiverConnection);
    }

    public isolated function receiveTwoBytesMessageViaReceiverConnectionWithConfigurableParameters() returns handle|error {
        return receiveTwoBytesMessageViaReceiverConnectionWithConfigurableParameters(self.asbReceiverConnection);
    }

}

isolated function createReceiverConnection(handle connectionString, handle entityPath) returns handle|error? = @java:Method {
    name: "createReceiverConnection",
    'class: "com.roland.asb.connection.ConUtils"
} external;

isolated function closeReceiverConnection(handle imessageSender) returns error? = @java:Method {
    name: "closeReceiverConnection",
    'class: "com.roland.asb.connection.ConUtils"
} external;

isolated function receiveBytesMessageViaReceiverConnectionWithConfigurableParameters(handle imessageReceiver) returns handle|error? = @java:Method {
    name: "receiveBytesMessageViaReceiverConnectionWithConfigurableParameters",
    'class: "com.roland.asb.connection.ConUtils"
} external;

isolated function checkMessage(handle imessage) returns error? = @java:Method {
    name: "checkMessage",
    'class: "com.roland.asb.connection.ConUtils"
} external;

isolated function nativeGetTextContent(byte[] messageContent) returns string|Error =
@java:Method {
    name: "getTextContent",
    'class: "com.roland.asb.AsbMessageUtils"
} external;

isolated function receiveOneBytesMessageViaReceiverConnectionWithConfigurableParameters(handle imessageReceiver) returns Message|error = @java:Method {
    name: "receiveOneBytesMessageViaReceiverConnectionWithConfigurableParameters",
    'class: "com.roland.asb.connection.ConUtils"
} external;

isolated function receiveTwoBytesMessageViaReceiverConnectionWithConfigurableParameters(handle imessageReceiver) returns handle|error = @java:Method {
    name: "receiveTwoBytesMessageViaReceiverConnectionWithConfigurableParameters",
    'class: "com.roland.asb.connection.ConUtils"
} external;