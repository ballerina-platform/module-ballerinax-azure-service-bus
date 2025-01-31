/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package io.ballerina.lib.asb.listener;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import io.ballerina.lib.asb.receiver.MessageReceiver;
import io.ballerina.lib.asb.util.ASBConstants;
import io.ballerina.lib.asb.util.ASBErrorCreator;
import io.ballerina.lib.asb.util.ASBUtils;
import io.ballerina.lib.asb.util.CallbackHandler;
import io.ballerina.lib.asb.util.ModuleUtils;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.Parameter;
import io.ballerina.runtime.api.types.PredefinedTypes;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;

import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

import static io.ballerina.runtime.api.types.TypeTags.OBJECT_TYPE_TAG;
import static io.ballerina.runtime.api.types.TypeTags.RECORD_TYPE_TAG;

/**
 * {@code MessageConsumer} provides the capability to invoke `onMessage` function of the ASB service.
 */
public class MessageConsumer implements Consumer<ServiceBusReceivedMessageContext>  {
    private static final String MESSAGE_RECORD = "Message";

    private final BObject bListener;
    private final boolean isAutoCompleteEnabled;
    private final Semaphore semaphore = new Semaphore(1);

    public MessageConsumer(BObject bListener, boolean autoCompleteEnabled) {
        this.bListener = bListener;
        this.isAutoCompleteEnabled = autoCompleteEnabled;
    }

    @Override
    public void accept(ServiceBusReceivedMessageContext messageContext) {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            throw ASBErrorCreator.createError(
                    String.format("Error occurred while acquiring the native lock: %s", e.getMessage()), e);
        }
        NativeBServiceAdaptor bService = NativeListener.getBallerinaSvc(this.bListener);
        CallbackHandler callback = getBRuntimeCallback(messageContext);
        Object[] params = getMethodParams(bService.getOnMessageParams(), messageContext);
        bService.invokeOnMessage(callback, params);
    }

    private CallbackHandler getBRuntimeCallback(ServiceBusReceivedMessageContext messageContext) {
        if (isAutoCompleteEnabled) {
            return new OnMessageAutoCompletableCallback(semaphore, messageContext);
        }
        return new OnMessageCallback(semaphore);
    }

    private Object[] getMethodParams(Parameter[] parameters, ServiceBusReceivedMessageContext messageContext) {
        Object[] args = new Object[parameters.length];
        int idx = 0;
        for (Parameter param: parameters) {
            Type referredType = TypeUtils.getReferredType(param.type);
            switch (referredType.getTag()) {
                case OBJECT_TYPE_TAG:
                    args[idx++] = NativeCaller.createCaller(messageContext);
                    break;
                case RECORD_TYPE_TAG:
                    args[idx++] = constructBMessage(messageContext.getMessage());
                    break;
                default:
                    throw ASBErrorCreator.createError(
                            String.format("Unknown service method parameter type: %s", referredType));
            }
        }
        return args;
    }

    private BMap<BString, Object> constructBMessage(ServiceBusReceivedMessage message) {
        Map<String, Object> map = MessageReceiver.populateOptionalFieldsMap(message);
        Object messageBody = MessageReceiver.getMessagePayload(message);
        if (messageBody instanceof byte[]) {
            map.put(ASBConstants.BODY, ASBUtils.getValueWithIntendedType(
                    (byte[]) messageBody, PredefinedTypes.TYPE_ANYDATA));
        } else {
            map.put(ASBConstants.BODY, messageBody);
        }
        return ValueCreator.createRecordValue(ModuleUtils.getModule(), MESSAGE_RECORD, map);
    }
}
