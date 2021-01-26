/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.asb;

import io.ballerina.runtime.api.utils.JsonUtils;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.XmlUtils;
import io.ballerina.runtime.api.values.BArray;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * Util class for Asb Message handling.
 */
public class ASBMessageUtils {
    public static Object getTextContent(BArray messageContent) {
        byte[] messageCont = messageContent.getBytes();
        try {
            return StringUtils.fromString(new String(messageCont, StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException exception) {
            return ASBUtils.returnErrorValue(ASBConstants.TEXT_CONTENT_ERROR + exception.getMessage());
        }
    }

    public static Object getFloatContent(BArray messageContent) {
        try {
            return Double.parseDouble(new String(messageContent.getBytes(), StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException exception) {
            return ASBUtils.returnErrorValue(ASBConstants.FLOAT_CONTENT_ERROR + exception.getMessage());
        }
    }

    public static Object getIntContent(BArray messageContent) {
        try {
            return Integer.parseInt(new String(messageContent.getBytes(), StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException exception) {
            return ASBUtils.returnErrorValue(ASBConstants.INT_CONTENT_ERROR + exception.getMessage());
        }
    }

    public static Object getJSONContent(BArray messageContent) {
        try {
            Object json = JsonUtils.parse(new String(messageContent.getBytes(), StandardCharsets.UTF_8.name()));
            if (json instanceof String) {
                return StringUtils.fromString((String) json);
            }
            return json;
        } catch (UnsupportedEncodingException exception) {
            return ASBUtils.returnErrorValue(ASBConstants.JSON_CONTENT_ERROR + exception.getMessage());
        }
    }

    public static Object getXMLContent(BArray messageContent) {
        try {
            return XmlUtils.parse(new String(messageContent.getBytes(), StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException exception) {
            return ASBUtils.returnErrorValue(ASBConstants.XML_CONTENT_ERROR + exception.getMessage());
        }
    }
}
