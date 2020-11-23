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

    public isolated function createReceiverConnection(ConnectionConfiguration connectionConfiguration) returns handle|Error? {
        self.connectionString = connectionConfiguration.connectionString;
        self.entityPath = connectionConfiguration.entityPath;
        self.asbReceiverConnection = <handle> createReceiverConnection(java:fromString(self.connectionString),java:fromString(self.entityPath));
    }

    public isolated function closeReceiverConnection() returns Error? {
        return closeReceiverConnection(self.asbReceiverConnection);
    }

    public isolated function receiveMessage() returns Message|Error {
        return receiveMessage(self.asbReceiverConnection);
    }

}

isolated function createReceiverConnection(handle connectionString, handle entityPath) returns handle|Error? = @java:Method {
    name: "createReceiverConnection",
    'class: "org.ballerinalang.asb.connection.ConUtils"
} external;

isolated function closeReceiverConnection(handle imessageSender) returns Error? = @java:Method {
    name: "closeReceiverConnection",
    'class: "org.ballerinalang.asb.connection.ConUtils"
} external;

isolated function receiveMessage(handle imessageReceiver) returns Message|Error = @java:Method {
    name: "receiveMessage",
    'class: "org.ballerinalang.asb.connection.ConUtils"
} external;