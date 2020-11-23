public class Message {
    private byte[] messageContent = [];

    public isolated function getTextContent1() returns @tainted string|Error {

        return nativeGetTextContent(self.messageContent);
    }
}