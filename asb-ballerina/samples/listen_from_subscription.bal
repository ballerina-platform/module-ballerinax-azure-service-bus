import ballerina/lang.value as value; 
import ballerina/log;
import ballerinax/asb;

// ASB configuration parameters
configurable string connectionString = ?;
configurable string subscriptionPath1 = ?
// The entityPath for a subscription is in the following format `<topicName>/subscriptions/<subscriptionName>`

listener asb:Listener asbListener = new (connectionString, subscriptionPath1);

service asb:Service on asbListener {
    remote function onMessage(asb:Message message, asb:Caller caller) returns error? {
        log:printInfo("Azure service bus message as byte[] which is the standard according to the AMQP protocol" + 
            message.toString());
        string|xml|json|byte[] received = message.body;

        match message?.contentType {
            asb:JSON => {
                string stringMessage = check string:fromBytes(<byte[]> received);
                json jsonMessage = check value:fromJsonString(stringMessage);
                log:printInfo("The message received: " + jsonMessage.toJsonString());
            }
            asb:XML => {
                string stringMessage = check 'string:fromBytes(<byte[]> received);
                xml xmlMessage = check 'xml:fromString(stringMessage);
                log:printInfo("The message received: " + xmlMessage.toString());
            }
            asb:TEXT => {
                string stringMessage = check 'string:fromBytes(<byte[]> received);
                log:printInfo("The message received: " + stringMessage);
            }
        }
    }
}
