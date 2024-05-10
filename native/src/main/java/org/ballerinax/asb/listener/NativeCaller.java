/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.ballerinax.asb.listener;

import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.models.AbandonOptions;
import com.azure.messaging.servicebus.models.DeadLetterOptions;
import com.azure.messaging.servicebus.models.DeferOptions;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import org.ballerinax.asb.util.ASBErrorCreator;
import org.ballerinax.asb.util.ASBUtils;
import org.ballerinax.asb.util.ModuleUtils;

import java.util.Map;
import java.util.Objects;

/**
 * {@code NativeCaller} provides the utility methods for the Ballerina ASB caller implementation.
 */
public final class NativeCaller {
    private static final String CALLER = "Caller";
    private static final String NATIVE_MSG_CONTEXT = "messageContext";

    private static final BString DEAD_LETTER_REASON = StringUtils.fromString("deadLetterReason");
    private static final BString DEAD_LETTER_ERROR_DESCRIPTION = StringUtils.fromString(
            "deadLetterErrorDescription");
    private static final BString DEAD_LETTER_PROPERTIES_TO_MODIFY = StringUtils.fromString("propertiesToModify");

    private NativeCaller() {
    }

    public static BObject createCaller(ServiceBusReceivedMessageContext messageContext) {
        BObject bCaller = ValueCreator.createObjectValue(ModuleUtils.getModule(), CALLER);
        bCaller.addNativeData(NATIVE_MSG_CONTEXT, messageContext);
        return bCaller;
    }

    public static Object complete(BObject bCaller) {
        Object messageContext = bCaller.getNativeData(NATIVE_MSG_CONTEXT);
        try {
            if (Objects.nonNull(messageContext)) {
                ((ServiceBusReceivedMessageContext) messageContext).complete();
            }
        } catch (Exception e) {
            return ASBErrorCreator.createError(
                    String.format("Error occurred while marking the message as complete: %s", e.getMessage()), e);
        }
        return null;
    }

    public static Object abandon(BObject bCaller, BMap<BString, Object> propertiesToModify) {
        Object messageContext = bCaller.getNativeData(NATIVE_MSG_CONTEXT);
        try {
            if (Objects.nonNull(messageContext)) {
                Map<String, Object> applicationProperties = ASBUtils.toMap(propertiesToModify);
                AbandonOptions abandonOpt = new AbandonOptions().setPropertiesToModify(applicationProperties);
                ((ServiceBusReceivedMessageContext) messageContext).abandon(abandonOpt);
            }
        } catch (Exception e) {
            return ASBErrorCreator.createError(
                    String.format("Error occurred while marking the message as abandon: %s", e.getMessage()), e);
        }
        return null;
    }

    public static Object deadLetter(BObject bCaller, BMap<BString, Object> options) {
        Object messageContext = bCaller.getNativeData(NATIVE_MSG_CONTEXT);
        try {
            if (Objects.nonNull(messageContext)) {
                DeadLetterOptions deadLetterOpt = getDeadLetterOptions(options);
                ((ServiceBusReceivedMessageContext) messageContext).deadLetter(deadLetterOpt);
            }
        } catch (Exception e) {
            return ASBErrorCreator.createError(
                    String.format("Error occurred while moving the message to dead-letter queue: %s",
                            e.getMessage()), e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static DeadLetterOptions getDeadLetterOptions(BMap<BString, Object> options) {
        DeadLetterOptions deadLetterOpt = new DeadLetterOptions();
        if (options.containsKey(DEAD_LETTER_REASON)) {
            String reason = options.getStringValue(DEAD_LETTER_REASON).getValue();
            deadLetterOpt.setDeadLetterReason(reason);
        }
        if (options.containsKey(DEAD_LETTER_ERROR_DESCRIPTION)) {
            String errorDescription = options.getStringValue(DEAD_LETTER_ERROR_DESCRIPTION).getValue();
            deadLetterOpt.setDeadLetterErrorDescription(errorDescription);
        }
        if (options.containsKey(DEAD_LETTER_PROPERTIES_TO_MODIFY)) {
            Map<String, Object> propertiesToModify = ASBUtils.toMap(
                    (BMap<BString, Object>) options.getMapValue(DEAD_LETTER_PROPERTIES_TO_MODIFY));
            deadLetterOpt.setPropertiesToModify(propertiesToModify);
        }
        return deadLetterOpt;
    }

    public static Object defer(BObject bCaller, BMap<BString, Object> propertiesToModify) {
        Object messageContext = bCaller.getNativeData(NATIVE_MSG_CONTEXT);
        try {
            if (Objects.nonNull(messageContext)) {
                Map<String, Object> applicationProperties = ASBUtils.toMap(propertiesToModify);
                DeferOptions deferOpt = new DeferOptions().setPropertiesToModify(applicationProperties);
                ((ServiceBusReceivedMessageContext) messageContext).defer(deferOpt);
            }
        } catch (Exception e) {
            return ASBErrorCreator.createError(
                    String.format("Error occurred while marking the message as deferred: %s", e.getMessage()), e);
        }
        return null;
    }
}
