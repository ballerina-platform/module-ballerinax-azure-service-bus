# The ASB service type
public type Service service object {
    remote function onMessage(Message message, Caller caller) returns error?;
};
