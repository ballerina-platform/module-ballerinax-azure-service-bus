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

    public isolated function createSenderConnection(ConnectionConfiguration connectionConfiguration) returns handle|error? {
        self.connectionString = connectionConfiguration.connectionString;
        self.entityPath = connectionConfiguration.entityPath;
        self.asbSenderConnection = <handle> createSenderConnection(java:fromString(self.connectionString),java:fromString(self.entityPath));
    }

    public isolated function closeSenderConnection() returns error? {
        return closeSenderConnection(self.asbSenderConnection);
    }

    public isolated function sendMessageWithConfigurableParameters(byte[] content, map<string> parameters,map<string> properties) returns error? {
        return sendMessageWithConfigurableParameters(self.asbSenderConnection, content, parameters, properties);
    }

    public isolated function sendMessage(byte[] content) returns error? {
        map<string> m = {a: "rol", b: "12"};
        return sendMessage(self.asbSenderConnection, content, java:fromString("content"), java:fromString("content"), java:fromString("content"), java:fromString("content"), java:fromString("content"), java:fromString("content"), java:fromString("content"),m, 1);
    }

}

isolated function createSenderConnection(handle connectionString, handle entityPath) returns handle|error? = @java:Method {
    name: "createSenderConnection",
    'class: "org.ballerinalang.asb.connection.ConUtils"
} external;

isolated function closeSenderConnection(handle imessageSender) returns error? = @java:Method {
    name: "closeSenderConnection",
    'class: "org.ballerinalang.asb.connection.ConUtils"
} external;

isolated function sendMessageWithConfigurableParameters(handle imessageSender, byte[] content, map<string> parameters, map<string> properties) returns error? = @java:Method {
    name: "sendMessageWithConfigurableParameters",
    'class: "org.ballerinalang.asb.connection.ConUtils"
} external;

isolated function sendMessage(handle imessageSender, byte[] content,handle contentType, handle messageId, handle to, handle replyTo, handle label, handle sessionId, handle correlationId, map<string> properties, int timeToLive) returns error? = @java:Method {
    name: "sendMessage",
    'class: "org.ballerinalang.asb.connection.ConUtils"
} external;