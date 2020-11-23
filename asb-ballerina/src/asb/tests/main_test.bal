import ballerina/test;
import ballerina/log;
import ballerina/system;
import ballerina/config;

// Connection Configuration
string connectionString = getConfigValue("CONNECTION_STRING");
string queuePath = getConfigValue("QUEUE_PATH");

SenderConnection? senderConnection = ();
ReceiverConnection? receiverConnection = ();

// Input values
string stringContent = "This is My Message Body"; 
byte[] byteContent = stringContent.toBytes();
json jsonContent = {name: "apple", color: "red", price: 5.36};
byte[] byteContentFromJson = jsonContent.toJsonString().toBytes();
json[] jsonArrayContent = [{name: "apple", color: "red", price: 5.36}, {first: "John", last: "Pala"}];
string[] stringArrayContent = ["apple","mango","lemon","orange"];
int[] integerArrayContent = [4, 5, 6];
map<string> parameters = {contentType: "application/json", messageId: "one", to: "sanju", replyTo: "carol", label: "a1", sessionId: "b1", correlationId: "c1", timeToLive: "2"};
map<string> properties = {a: "nimal", b: "saman"};

# Before Suite Function
@test:BeforeSuite
function beforeSuiteFunc() {
    log:printInfo("Creating a ballerina Asb Sender connection.");
    SenderConnection? con = new ({connectionString: connectionString, entityPath: queuePath});
    senderConnection = con;

    log:printInfo("Creating a ballerina Asb Receiver connection.");
    ReceiverConnection? rec = new ({connectionString: connectionString, entityPath: queuePath});
    receiverConnection = rec;
}

# Test Sender Connection
@test:Config{enable: false}
public function testSenderConnection() {
    boolean flag = false;
    SenderConnection? senderConnection = new ({connectionString: connectionString, entityPath: queuePath});
    if (senderConnection is SenderConnection) {
        flag = true;
    }
    test:assertTrue(flag, msg = "Asb Sender Connection creation failed.");
}

# Test Receiver Connection
@test:Config{enable: false}
public function testReceieverConnection() {
    boolean flag = false;
    ReceiverConnection? receiverConnection = new ({connectionString: connectionString, entityPath: queuePath});
    if (receiverConnection is ReceiverConnection) {
        flag = true;
    }
    test:assertTrue(flag, msg = "Asb Receiver Connection creation failed.");
}

# Test send to queue operation
@test:Config{enable: true}
function testSendToQueueOperation() {
    log:printInfo("Creating Asb sender connection.");
    SenderConnection? senderConnection = new ({connectionString: connectionString, entityPath: queuePath});

    if (senderConnection is SenderConnection) {
        log:printInfo("Sending via Asb sender connection.");
        checkpanic senderConnection.sendMessageWithConfigurableParameters(byteContent, parameters, properties);
    } else {
        test:assertFail("Asb sender connection creation failed.");
    }

    if (senderConnection is SenderConnection) {
        log:printInfo("Closing Asb sender connection.");
        checkpanic senderConnection.closeSenderConnection();
    }
}

# Test receive from queue operation
@test:Config{enable: true}
function testReceiveFromQueueOperation() {
    log:printInfo("Creating Asb receiver connection.");
    ReceiverConnection? receiverConnection = new ({connectionString: connectionString, entityPath: queuePath});

    if (receiverConnection is ReceiverConnection) {
        log:printInfo("Receiving from Asb receiver connection.");
        Message|Error messageReceived = receiverConnection.receiveMessage();
        if (messageReceived is Message) {
            string messageRead = checkpanic messageReceived.getTextContent();
            log:printInfo("Reading Received Message : " + messageRead);
        } else {
            test:assertFail("Receiving message via Asb receiver connection failed.");
        }
    } else {
        test:assertFail("Asb receiver connection creation failed.");
    }

    if (receiverConnection is ReceiverConnection) {
        log:printInfo("Closing Asb receiver connection.");
        checkpanic receiverConnection.closeReceiverConnection();
    }
}

# After Suite Function
@test:AfterSuite {}
function afterSuiteFunc() {
    SenderConnection? con = senderConnection;
    if (con is SenderConnection) {
        log:printInfo("Closing the Sender Connection");
        checkpanic con.closeSenderConnection();
    }

    ReceiverConnection? rec = receiverConnection;
    if (rec is ReceiverConnection) {
        log:printInfo("Closing the Receiver Connection");
        checkpanic rec.closeReceiverConnection();
    }
}

function getConfigValue(string key) returns string {
    return (system:getEnv(key) != "") ? system:getEnv(key) : config:getAsString(key);
}