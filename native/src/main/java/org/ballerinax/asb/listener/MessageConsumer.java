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

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.types.Parameter;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import org.ballerinax.asb.util.ASBErrorCreator;
import org.ballerinax.asb.util.ModuleUtils;

import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

import static io.ballerina.runtime.api.TypeTags.OBJECT_TYPE_TAG;
import static io.ballerina.runtime.api.TypeTags.RECORD_TYPE_TAG;
import static io.ballerina.runtime.api.creators.ValueCreator.createRecordValue;
import static org.ballerinax.asb.receiver.MessageReceiver.getMessagePayload;
import static org.ballerinax.asb.receiver.MessageReceiver.populateOptionalFieldsMap;
import static org.ballerinax.asb.util.ASBConstants.BODY;
import static org.ballerinax.asb.util.ASBUtils.getValueWithIntendedType;

/**
 * {@code MessageConsumer} provides the capability to invoke `onMessage` function of the ASB service.
 */
public class MessageConsumer implements Consumer<ServiceBusReceivedMessageContext>  {
    private static final String MESSAGE_RECORD = "Message";

    private final BObject bListener;
    private final Semaphore semaphore = new Semaphore(1);

    public MessageConsumer(BObject bListener) {
        this.bListener = bListener;
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
        OnMessageCallback callback = new OnMessageCallback(semaphore);
        Object[] params = getMethodParams(bService.getOnMessageParams(), messageContext);
        bService.invokeOnMessage(callback, params);
    }

    private Object[] getMethodParams(Parameter[] parameters, ServiceBusReceivedMessageContext messageContext) {
        Object[] args = new Object[parameters.length * 2];
        int idx = 0;
        for (Parameter param: parameters) {
            Type referredType = TypeUtils.getReferredType(param.type);
            switch (referredType.getTag()) {
                case OBJECT_TYPE_TAG:
                    args[idx++] = NativeCaller.createCaller(messageContext);
                    args[idx++] = true;
                    break;
                case RECORD_TYPE_TAG:
                    args[idx++] = constructBMessage(messageContext.getMessage());
                    args[idx++] = true;
                    break;
                default:
                    throw ASBErrorCreator.createError(
                            String.format("Unknown service method parameter type: %s", referredType));
            }
        }
        return args;
    }

    private BMap<BString, Object> constructBMessage(ServiceBusReceivedMessage message) {
        Map<String, Object> map = populateOptionalFieldsMap(message);
        Object messageBody = getMessagePayload(message);
        if (messageBody instanceof byte[]) {
            map.put(BODY, getValueWithIntendedType((byte[]) messageBody, PredefinedTypes.TYPE_ANYDATA));
        } else {
            map.put(BODY, messageBody);
        }
        return createRecordValue(ModuleUtils.getModule(), MESSAGE_RECORD, map);
    }
}
