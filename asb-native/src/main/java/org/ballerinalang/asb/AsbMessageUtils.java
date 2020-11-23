package org.ballerinalang.asb;

import org.ballerinalang.jvm.JSONParser;
import org.ballerinalang.jvm.XMLFactory;
import org.ballerinalang.jvm.api.values.BArray;
import org.ballerinalang.jvm.api.BStringUtils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class AsbMessageUtils {
    public static Object getTextContent(BArray messageContent) {
        byte[] messageCont = messageContent.getBytes();
        try {
            return BStringUtils.fromString(new String(messageCont, StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException exception) {
            return AsbUtils.returnErrorValue(AsbConstants.TEXT_CONTENT_ERROR
                    + exception.getMessage());
        }
    }

    public static Object getFloatContent(BArray messageContent) {
        try {
            return Double.parseDouble(new String(messageContent.getBytes(), StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException exception) {
            return AsbUtils.returnErrorValue(AsbConstants.FLOAT_CONTENT_ERROR
                    + exception.getMessage());
        }
    }

    public static Object getIntContent(BArray messageContent) {
        try {
            return Integer.parseInt(new String(messageContent.getBytes(), StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException exception) {
            return AsbUtils.returnErrorValue(AsbConstants.INT_CONTENT_ERROR
                    + exception.getMessage());
        }
    }

    public static Object getJSONContent(BArray messageContent) {
        try {
            Object json = JSONParser.parse(new String(messageContent.getBytes(), StandardCharsets.UTF_8.name()));
            if (json instanceof String) {
                return BStringUtils.fromString((String) json);
            }
            return json;
        } catch (UnsupportedEncodingException exception) {
            return AsbUtils.returnErrorValue
                    (AsbConstants.JSON_CONTENT_ERROR + exception.getMessage());
        }
    }

    public static Object getXMLContent(BArray messageContent) {
        try {
            return XMLFactory.parse(new String(messageContent.getBytes(), StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException exception) {
            return AsbUtils.returnErrorValue(AsbConstants.XML_CONTENT_ERROR
                    + exception.getMessage());
        }
    }
}
