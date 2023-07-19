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

package org.ballerinax.azure.asb.util;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.api.utils.JsonUtils;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.XmlUtils;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;
import org.apache.qpid.proton.amqp.Binary;
import org.ballerinalang.langlib.value.CloneWithType;
import org.ballerinalang.langlib.value.FromJsonWithType;
import org.ballerinax.azure.asb.receiver.MessageReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static io.ballerina.runtime.api.TypeTags.ANYDATA_TAG;
import static io.ballerina.runtime.api.TypeTags.ARRAY_TAG;
import static io.ballerina.runtime.api.TypeTags.BYTE_TAG;
import static io.ballerina.runtime.api.TypeTags.NULL_TAG;
import static io.ballerina.runtime.api.TypeTags.RECORD_TYPE_TAG;
import static io.ballerina.runtime.api.TypeTags.STRING_TAG;
import static io.ballerina.runtime.api.TypeTags.UNION_TAG;
import static io.ballerina.runtime.api.TypeTags.XML_TAG;
import static io.ballerina.runtime.api.utils.TypeUtils.getReferredType;
import static org.ballerinax.azure.asb.util.ASBConstants.DELAY;
import static org.ballerinax.azure.asb.util.ASBConstants.MAX_DELAY;
import static org.ballerinax.azure.asb.util.ASBConstants.MAX_RETRIES;
import static org.ballerinax.azure.asb.util.ASBConstants.RETRY_MODE;
import static org.ballerinax.azure.asb.util.ASBConstants.TRY_TIMEOUT;

/**
 * Utility class for Azure Service Bus.
 */
public class ASBUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageReceiver.class);

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

    public static RecordType getRecordType(BTypedesc bTypedesc) {
        RecordType recordType;
        if (bTypedesc.getDescribingType().isReadOnly()) {
            recordType = (RecordType) getReferredType(((IntersectionType) getReferredType(
                    bTypedesc.getDescribingType())).getConstituentTypes().get(0));
        } else {
            recordType = (RecordType) getReferredType(bTypedesc.getDescribingType());
        }
        return recordType;
    }

    /**
     * Converts AMPQ Body value to Java objects.
     *
     * @param amqpValue AMQP Value type object
     */
    public static Object convertAMQPToJava(String messageId, Object amqpValue) {
        LOGGER.debug("Type of amqpValue object  of received message " + messageId + " is " + amqpValue.getClass());
        Class<?> clazz = amqpValue.getClass();
        switch (clazz.getSimpleName()) {
            case "Integer":
            case "Long":
            case "Float":
            case "Double":
            case "String":
            case "Boolean":
            case "Byte":
            case "Short":
            case "Character":
            case "BigDecimal":
            case "Date":
            case "UUID":
                return amqpValue;
            case "Binary":
                return ((Binary) amqpValue).getArray();
            default:
                LOGGER.debug("The type of amqpValue object " + clazz + " is not supported");
                return null;
        }
    }

    /**
     * Converts a given java value its counterpart BValue instance.
     *
     * @param jValue java value
     */
    public static Optional<Object> convertJavaToBValue(String messageId, Object jValue) {
        LOGGER.debug("Type of java object of received message " + messageId + " is " + jValue.getClass());
        Class<?> clazz = jValue.getClass();
        switch (clazz.getSimpleName()) {
            case "Integer":
            case "Long":
            case "Float":
            case "Double":
            case "Boolean":
            case "Byte":
            case "Short":
            case "Character":
                return Optional.of(jValue);
            case "String":
                return Optional.of(StringUtils.fromString((String) jValue));
            case "BigDecimal":
                return Optional.of(ValueCreator.createDecimalValue((BigDecimal) jValue));
            default:
                LOGGER.debug("java object with type '" + clazz + "' can not be converted to as a Ballerina value");
                return Optional.empty();
        }
    }

    /**
     * Converts `byte[]` value to the intended Ballerina type.
     *
     * @param type  expected type
     * @param value Value to be converted
     * @return value with the intended type
     */
    public static Object getValueWithIntendedType(byte[] value, Type type) {
        try {
            return getValueWithIntendedTypeRecursive(value, type);
        } catch (BError be) {
            throw ASBErrorCreator.fromBError(String.format("Failed to deserialize the message payload " +
                            "into the contextually expected type '%s'. Use a compatible Ballerina type or, " +
                            "use 'byte[]' type along with an appropriate deserialization logic afterwards.",
                    type.toString()), be);
        }
    }

    /**
     * Converts `byte[]` value to the intended Ballerina type.
     *
     * @param type  expected type
     * @param value Value to be converted
     * @return value with the intended type
     */
    private static Object getValueWithIntendedTypeRecursive(byte[] value, Type type) {
        String strValue = new String(value, StandardCharsets.UTF_8);
        Object intendedValue;
        switch (type.getTag()) {
            case STRING_TAG:
                intendedValue = StringUtils.fromString(strValue);
                break;
            case XML_TAG:
                intendedValue = XmlUtils.parse(strValue);
                break;
            case ANYDATA_TAG:
                intendedValue = ValueCreator.createArrayValue(value);
                break;
            case RECORD_TYPE_TAG:
                intendedValue = CloneWithType.convert(type, JsonUtils.parse(strValue));
                break;
            case UNION_TAG:
                if (isSupportedUnionType(type)) {
                    intendedValue = getValueWithIntendedType(value, getExpectedTypeFromNilableType(type));
                } else {
                    intendedValue = ErrorCreator.createError(StringUtils.fromString("Union types are not " +
                            "supported for the contextually expected type, except for nilable types"));
                }
                break;
            case ARRAY_TAG:
                if (getReferredType(((ArrayType) type).getElementType()).getTag() == BYTE_TAG) {
                    intendedValue = ValueCreator.createArrayValue(value);
                    break;
                }
                /*-fallthrough*/
            default:
                intendedValue = getValueFromJson(type, strValue);
        }

        if (intendedValue instanceof BError) {
            throw (BError) intendedValue;
        }
        return intendedValue;
    }

    private static boolean hasStringType(UnionType type) {
        return type.getMemberTypes().stream().anyMatch(memberType -> memberType.getTag() == STRING_TAG);
    }

    /**
     * Checks whether the given type is a union type of two member types, including the nil type.
     *
     * @param type Type to be checked
     * @return True if the given type is a union type of two member types, including nil type.
     */
    private static boolean isSupportedUnionType(Type type) {
        return type.getTag() == UNION_TAG
                && ((UnionType) type).getMemberTypes().size() == 2
                && ((UnionType) type).getMemberTypes().stream().anyMatch(memberType -> memberType.getTag() == NULL_TAG);
    }

    public static Type getExpectedTypeFromNilableType(Type type) {
        return ((UnionType) type).getMemberTypes().stream()
                .filter(memberType -> memberType.getTag() != NULL_TAG)
                .findFirst()
                .orElse(null);
    }

    private static Object getValueFromJson(Type type, String stringValue) {
        BTypedesc typeDesc = ValueCreator.createTypedescValue(type);
        return FromJsonWithType.fromJsonWithType(JsonUtils.parse(stringValue), typeDesc);
    }

    /**
     * Adds the ASB properties to the Ballerina record response, only if present.
     *
     * @param map              Ballerina record map
     * @param key              Key of the property
     * @param receivedProperty Received property
     */
    public static void addMessageFieldIfPresent(Map<String, Object> map, String key, Object receivedProperty) {
        if (receivedProperty != null) {
            map.put(key, receivedProperty);
        }
    }
}
