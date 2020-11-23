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

    public isolated function getTextContent(byte[] content) returns @tainted string|Error {

        return nativeGetTextContent(content);
    }

    public isolated function receiveOneBytesMessageViaReceiverConnectionWithConfigurableParameters() returns Message|error {
        return receiveOneBytesMessageViaReceiverConnectionWithConfigurableParameters(self.asbReceiverConnection);
    }

}

isolated function createReceiverConnection(handle connectionString, handle entityPath) returns handle|error? = @java:Method {
    name: "createReceiverConnection",
    'class: "org.ballerinalang.asb.connection.ConUtils"
} external;

isolated function closeReceiverConnection(handle imessageSender) returns error? = @java:Method {
    name: "closeReceiverConnection",
    'class: "org.ballerinalang.asb.connection.ConUtils"
} external;

isolated function nativeGetTextContent(byte[] messageContent) returns string|Error =
@java:Method {
    name: "getTextContent",
    'class: "org.ballerinalang.asb.AsbMessageUtils"
} external;

isolated function receiveOneBytesMessageViaReceiverConnectionWithConfigurableParameters(handle imessageReceiver) returns Message|error = @java:Method {
    name: "receiveOneBytesMessageViaReceiverConnectionWithConfigurableParameters",
    'class: "org.ballerinalang.asb.connection.ConUtils"
} external;