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

import io.ballerina.lib.asb.util.ASBErrorCreator;
import io.ballerina.lib.asb.util.CallbackHandler;
import io.ballerina.lib.asb.util.ModuleUtils;
import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.concurrent.StrandMetadata;
import io.ballerina.runtime.api.types.FunctionType;
import io.ballerina.runtime.api.types.MethodType;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.Parameter;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BObject;

import java.util.Optional;
import java.util.stream.Stream;

import static io.ballerina.runtime.api.types.TypeTags.OBJECT_TYPE_TAG;

/**
 * {@code NativeBServiceAdaptor} represents a native wrapper for ballerina service object.
 */
public final class NativeBServiceAdaptor {
    private static final String ON_MESSAGE_METHOD = "onMessage";
    private static final String ON_ERROR_METHOD = "onError";

    private final Runtime bRuntime;
    private final BObject bServiceObj;
    private final Object bServiceName;
    private final boolean isolated;
    private final MethodType onMessage;
    private final Optional<MethodType> onError;

    public NativeBServiceAdaptor(Runtime bRuntime, BObject bService, Object svcName) {
        this.bRuntime = bRuntime;
        this.bServiceObj = bService;
        this.bServiceName = svcName;
        ObjectType serviceType = (ObjectType) TypeUtils.getReferredType(TypeUtils.getType(bService));
        this.isolated = serviceType.isIsolated() && serviceType.isIsolated(ON_MESSAGE_METHOD);
        Optional<MethodType> onMessageFuncOpt = Stream.of(serviceType.getMethods())
                .filter(methodType -> ON_MESSAGE_METHOD.equals(methodType.getName()))
                .findFirst();
        if (onMessageFuncOpt.isEmpty()) {
            throw ASBErrorCreator.createError("Required method `onMessage` not found in the service");
        }
        this.onMessage = onMessageFuncOpt.get();
        this.onError = Stream.of(serviceType.getMethods())
                .filter(methodType -> ON_ERROR_METHOD.equals(methodType.getName()))
                .findFirst();
    }

    public void validate(boolean autoComplete) {
        Parameter[] onMessageParams = onMessage.getParameters();
        if (onMessageParams.length == 0) {
            throw ASBErrorCreator.createError(
                    "Required parameter `message` has not been defined in the `onMessage` method");
        }

        if (autoComplete) {
            for (Parameter param: onMessage.getParameters()) {
                Type referredType = TypeUtils.getReferredType(param.type);
                if (OBJECT_TYPE_TAG == referredType.getTag()) {
                    throw ASBErrorCreator.createError("Cannot use `asb:Caller` when using auto-complete mode");
                }
            }
        }

        if (onError.isPresent()) {
            Parameter[] onErrorParams = onError.get().getParameters();
            if (onErrorParams.length == 0) {
                throw ASBErrorCreator.createError(
                        "Required parameter `error` has not been defined in the `onError` method");
            }
        }
    }

    public Parameter[] getOnMessageParams() {
        return onMessage.getParameters();
    }

    public void invokeOnMessage(CallbackHandler callback, Object[] params) {
        Thread.startVirtualThread(() -> {
            try {
                Object result = bRuntime.callMethod(bServiceObj, ON_MESSAGE_METHOD,
                        new StrandMetadata(isolated, ModuleUtils.getProperties(ON_MESSAGE_METHOD)), params);
                callback.notifySuccess(result);
            } catch (BError bError) {
                callback.notifyFailure(bError);
            }
        });
    }

    public Parameter[] getOnErrorParams() {
        return onError.map(FunctionType::getParameters).orElse(new Parameter[]{});
    }

    public void invokeOnError(CallbackHandler callback, Object[] params) {
        if (onError.isEmpty()) {
            return;
        }
        Thread.startVirtualThread(() -> {
            try {
                Object result = bRuntime.callMethod(bServiceObj, ON_ERROR_METHOD,
                        new StrandMetadata(isolated, ModuleUtils.getProperties(ON_ERROR_METHOD)), params);
                callback.notifySuccess(result);
            } catch (BError bError) {
                callback.notifyFailure(bError);
            }
        });
    }
}
