import ballerina/java;

public class SenderConnection{

    handle asbSenderConnection;

    private string connectionString;
    private string entityPath;

    public isolated function init(ConnectionConfiguration connectionConfiguration) {
        self.connectionString = connectionConfiguration.connectionString;
        self.entityPath = connectionConfiguration.entityPath;
        self.asbSenderConnection = <handle> createSenderConnection(java:fromString(self.connectionString),java:fromString(self.entityPath));
    }

    public isolated function createSenderConnection(ConnectionConfiguration connectionConfiguration) returns handle|Error? {
        self.connectionString = connectionConfiguration.connectionString;
        self.entityPath = connectionConfiguration.entityPath;
        self.asbSenderConnection = <handle> createSenderConnection(java:fromString(self.connectionString),java:fromString(self.entityPath));
    }

    public isolated function closeSenderConnection() returns Error? {
        return closeSenderConnection(self.asbSenderConnection);
    }

    public isolated function sendMessageWithConfigurableParameters(byte[] content, map<string> parameters,map<string> properties) returns Error? {
        return sendMessageWithConfigurableParameters(self.asbSenderConnection, content, parameters, properties);
    }

    public isolated function sendMessage(byte[] content, map<string> properties) returns Error? {
        return sendMessage(self.asbSenderConnection, content, java:fromString("content"), java:fromString("content"), java:fromString("content"), java:fromString("content"), java:fromString("content"), java:fromString("content"), java:fromString("content"), properties, 1);
    }

}

isolated function createSenderConnection(handle connectionString, handle entityPath) returns handle|Error? = @java:Method {
    name: "createSenderConnection",
    'class: "org.ballerinalang.asb.connection.ConUtils"
} external;

isolated function closeSenderConnection(handle imessageSender) returns Error? = @java:Method {
    name: "closeSenderConnection",
    'class: "org.ballerinalang.asb.connection.ConUtils"
} external;

isolated function sendMessageWithConfigurableParameters(handle imessageSender, byte[] content, map<string> parameters, map<string> properties) returns Error? = @java:Method {
    name: "sendMessageWithConfigurableParameters",
    'class: "org.ballerinalang.asb.connection.ConUtils"
} external;

isolated function sendMessage(handle imessageSender, byte[] content,handle contentType, handle messageId, handle to, handle replyTo, handle label, handle sessionId, handle correlationId, map<string> properties, int timeToLive) returns Error? = @java:Method {
    name: "sendMessage",
    'class: "org.ballerinalang.asb.connection.ConUtils"
} external;