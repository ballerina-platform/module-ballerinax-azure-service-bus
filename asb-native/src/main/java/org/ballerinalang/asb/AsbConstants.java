package org.ballerinalang.asb;

import org.ballerinalang.jvm.api.BStringUtils;
import org.ballerinalang.jvm.api.values.BString;
import org.ballerinalang.jvm.types.BPackage;

public class AsbConstants {
    // RabbitMQ package name constant fields
    public static final String ORG_NAME = "ballerinax";
    static final String ASB = "asb";
    public static final String ASB_VERSION = "0.1.0";
    public static final BPackage PACKAGE_ID_ASB =
            new BPackage(ORG_NAME, "asb", ASB_VERSION);

    // Error constant fields
    static final String ASB_ERROR = "AsbError";

    // Message constant fields
    public static final String MESSAGE_OBJECT = "Message";
    public static final BString MESSAGE_CONTENT = BStringUtils.fromString("messageContent");
    public static final String XML_CONTENT_ERROR = "Error while retrieving the xml content of the message. ";
    public static final String JSON_CONTENT_ERROR = "Error while retrieving the json content of the message. ";
    public static final String TEXT_CONTENT_ERROR = "Error while retrieving the string content of the message. ";
    public static final String INT_CONTENT_ERROR = "Error while retrieving the int content of the message. ";
    public static final String FLOAT_CONTENT_ERROR = "Error while retrieving the float content of the message. ";

}
