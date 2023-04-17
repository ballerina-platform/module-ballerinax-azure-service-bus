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

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ErrorType;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.ballerinax.asb.util.ASBConstants.DELAY;
import static org.ballerinax.asb.util.ASBConstants.MAX_DELAY;
import static org.ballerinax.asb.util.ASBConstants.MAX_RETRIES;
import static org.ballerinax.asb.util.ASBConstants.RETRY_MODE;
import static org.ballerinax.asb.util.ASBConstants.TRY_TIMEOUT;

/**
 * Utility class for Azure Service Bus.
 */
public class ASBUtils {

    /**
     * Convert Map to BMap.
     *
     * @param map Input Map used to convert to BMap.
     * @return Converted BMap object.
     */
    public static BMap<BString, Object> toBMap(Map<String, Object> map) {
        MapType mapType = TypeCreator.createMapType(PredefinedTypes.TYPE_ANY);
        BMap<BString, Object> envMap = ValueCreator.createMapValue(mapType);
        if (map != null) {
            for (Object aKey : map.keySet().toArray()) {
                Object value = map.get(aKey);
                String classType = value.getClass().getName();
                switch (classType) {
                    case "java.lang.String":
                        envMap.put(StringUtils.fromString(aKey.toString()), StringUtils.fromString(value.toString()));
                        break;
                    case "java.lang.Integer":
                        envMap.put(StringUtils.fromString(aKey.toString()), (Integer) value);
                        break;
                    case "java.lang.Long":
                        envMap.put(StringUtils.fromString(aKey.toString()), (Long) value);
                        break;
                    case "java.lang.Float":
                        envMap.put(StringUtils.fromString(aKey.toString()), (Float) value);
                        break;
                    case "java.lang.Double":
                        envMap.put(StringUtils.fromString(aKey.toString()), (Double) value);
                        break;
                    case "java.lang.Boolean":
                        envMap.put(StringUtils.fromString(aKey.toString()), (Boolean) value);
                        break;
                    case "java.lang.Character":
                        envMap.put(StringUtils.fromString(aKey.toString()), (Character) value);
                        break;
                    case "java.lang.Byte":
                        envMap.put(StringUtils.fromString(aKey.toString()), (Byte) value);
                        break;
                    case "java.lang.Short":
                        envMap.put(StringUtils.fromString(aKey.toString()), (Short) value);
                        break;
                    default:
                        envMap.put(StringUtils.fromString(aKey.toString()),
                                StringUtils.fromString(value.toString()));
                        break;
                }
            }
        }
        return envMap;
    }

    /**
     * Get the value as string or as empty based on the object value.
     *
     * @param value Input value.
     * @return value as a string or empty.
     */
    public static String convertString(Object value) {
        return (value == null || Objects.equals(value.toString(), "")) ? null : value.toString();
    }

    /**
     * Get the map value as string or as empty based on the key.
     *
     * @param map Input map.
     * @param key Input key.
     * @return map value as a string or empty.
     */
    public static String convertString(Map<String, ?> map, String key) {
        Object value = map.get(key);
        return value == null ? null : value.toString();
    }

    /**
     * Convert BMap to String Map.
     *
     * @param map Input BMap used to convert to Map.
     * @return Converted Map object.
     */
    public static Map<String, Object> toMap(BMap<BString, Object> map) {
        Map<String, Object> returnMap = new HashMap<>();
        Object value;
        String classType;
        String key;
        if (map != null) {
            for (Object aKey : map.getKeys()) {
                value = map.get(aKey);
                classType = value.getClass().getName();
                key = aKey.toString();
                switch (classType) {
                    case "BmpStringValue":
                        returnMap.put(key, value.toString());
                        break;
                    case "java.lang.Long":
                        returnMap.put(key, value);
                        break;
                    case "java.lang.Integer":
                        returnMap.put(key, (Integer) value);
                        break;
                    case "java.lang.Float":
                        returnMap.put(key, (Float) value);
                        break;
                    case "java.lang.Double":
                        returnMap.put(key, (Double) value);
                        break;
                    case "java.lang.Boolean":
                        returnMap.put(key, (Boolean) value);
                        break;
                    case "java.lang.Character":
                        returnMap.put(key, (Character) value);
                        break;
                    case "java.lang.Byte":
                        returnMap.put(key, (Byte) value);
                        break;
                    case "java.lang.Short":
                        returnMap.put(key, (Short) value);
                        break;
                    default:
                        returnMap.put(key, value.toString());
                }
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
    public static Map<String, Object> toObjectMap(BMap<BString, Object> map) {
        Map<String, Object> returnMap = new HashMap<>();
        if (map != null) {
            for (Object aKey : map.getKeys()) {
                returnMap.put(aKey.toString(), map.get(aKey));
            }
        }
        return returnMap;
    }

    /**
     * Returns a Ballerina Error with the given String message and exception.
     *
     * @param message The error message
     * @return Resulting Ballerina Error
     */
    public static BError returnErrorValue(String message, Exception error) {
        ErrorType errorType = TypeCreator.createErrorType(error.getClass().getTypeName(), ModuleUtils.getModule());
        String errorFromClass = error.getStackTrace()[0].getClassName();
        String errorMessage = "An error occurred while processing your request.\n\n";
        errorMessage += "Error Details:\n";
        errorMessage += "Message: " + error.getMessage() + "\n";
        errorMessage += "Cause: " + error.getCause() + "\n";
        errorMessage += "Class: " + error.getClass() + "\n";
        BError er = ErrorCreator.createError(StringUtils.fromString(errorMessage));
        return ErrorCreator.createError(errorType, StringUtils.fromString(message + "error from " + errorFromClass), er,
                null);
    }

    public static AmqpRetryOptions getRetryOptions(BMap<BString, Object> retryConfigs) {
        Long maxRetries = retryConfigs.getIntValue(MAX_RETRIES);
        BigDecimal delayConfig = ((BDecimal) retryConfigs.get(DELAY)).decimalValue();
        BigDecimal maxDelay = ((BDecimal) retryConfigs.get(MAX_DELAY)).decimalValue();
        BigDecimal tryTimeout = ((BDecimal) retryConfigs.get(TRY_TIMEOUT)).decimalValue();
        String retryMode = retryConfigs.getStringValue(RETRY_MODE).getValue();
        return new AmqpRetryOptions()
                .setMaxRetries(maxRetries.intValue())
                .setDelay(Duration.ofSeconds(delayConfig.intValue()))
                .setMaxDelay(Duration.ofSeconds(maxDelay.intValue()))
                .setTryTimeout(Duration.ofSeconds(tryTimeout.intValue()))
                .setMode(AmqpRetryMode.valueOf(retryMode));
    }
}
