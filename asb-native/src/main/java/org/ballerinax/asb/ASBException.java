package org.ballerinax.asb;

/**
 * Exception class for Azure Service Bus
 */
public class ASBException extends Exception {
    /**
     * Create an exception with given exception message
     *
     * @param msg an exception message
     */
    public ASBException(String msg) {
        super(msg);
    }

    /**
     * Create an exception with given message and wrapping the given exception object
     *
     * @param msg exception message
     * @param e   exception
     */
    public ASBException(String msg, Exception e) {
        super(msg, e);
    }
}
