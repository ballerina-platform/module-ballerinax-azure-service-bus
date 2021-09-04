/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinax.asb.util;

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.JsonUtils;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.XmlUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ASBUtils {

    /**
     * Convert Map to BMap.
     *
     * @param map Input Map used to convert to BMap.
     * @return Converted BMap object.
     */
    public static BMap<BString, Object> toBMap(Map<String, Object> map) {
        BMap<BString, Object> returnMap = ValueCreator.createMapValue();
        if (map != null) {
            for (Object aKey : map.keySet().toArray()) {
                returnMap.put(StringUtils.fromString(aKey.toString()),
                        StringUtils.fromString(map.get(aKey).toString()));
            }
        }
        return returnMap;
    }

    /**
     * Get the value as string or as empty based on the object value.
     *
     * @param value Input value.
     * @return value as a string or empty.
     */
    public static String valueToEmptyOrToString(Object value) {
        return (value == null || value.toString() == "") ? null : value.toString();
    }

    /**
     * Get the map value as string or as empty based on the key.
     *
     * @param map Input map.
     * @param key Input key.
     * @return map value as a string or empty.
     */
    public static String valueToStringOrEmpty(Map<String, ?> map, String key) {
        Object value = map.get(key);
        return value == null ? null : value.toString();
    }

    /**
     * Convert BMap to String Map.
     *
     * @param map Input BMap used to convert to Map.
     * @return Converted Map object.
     */
    public static Map<String, Object> toStringMap(BMap map) {
        Map<String, Object> returnMap = new HashMap<>();
        if (map != null) {
            for (Object aKey : map.getKeys()) {
                returnMap.put(aKey.toString(), map.get(aKey).toString());
            }
        }
        return returnMap;
    }

    /**
     * Convert BMap to Object Map.
     *
     * @param map Input BMap used to convert to Map.
     * @return Converted Map object.
     */
    public static Map<String, Object> toObjectMap(BMap map) {
        Map<String, Object> returnMap = new HashMap<>();
        if (map != null) {
            for (Object aKey : map.getKeys()) {
                returnMap.put(aKey.toString(), map.get(aKey));
            }
        }
        return returnMap;
    }

    public static Object getTextContent(BArray messageContent) {
        byte[] messageCont = messageContent.getBytes();
        try {
            return StringUtils.fromString(new String(messageCont, StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException exception) {
            return returnErrorValue(ASBConstants.TEXT_CONTENT_ERROR + exception.getMessage());
        }
    }

    public static Object getFloatContent(BArray messageContent) {
        try {
            return Double.parseDouble(new String(messageContent.getBytes(), StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException exception) {
            return returnErrorValue(ASBConstants.FLOAT_CONTENT_ERROR + exception.getMessage());
        }
    }

    public static Object getIntContent(BArray messageContent) {
        try {
            return Integer.parseInt(new String(messageContent.getBytes(), StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException exception) {
            return returnErrorValue(ASBConstants.INT_CONTENT_ERROR + exception.getMessage());
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
            return returnErrorValue(ASBConstants.JSON_CONTENT_ERROR + exception.getMessage());
        }
    }

    public static Object getXMLContent(BArray messageContent) {
        try {
            return XmlUtils.parse(new String(messageContent.getBytes(), StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException exception) {
            return returnErrorValue(ASBConstants.XML_CONTENT_ERROR + exception.getMessage());
        }
    }

    /**
     * Returns a Ballerina Error with the given String message.
     *
     * @param errorMessage The error message
     * @return Resulting Ballerina Error
     */
    public static BError returnErrorValue(String errorMessage) {
        return ErrorCreator.createError(StringUtils.fromString(errorMessage));
    }
}
