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

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.types.FunctionType;
import io.ballerina.runtime.api.types.MethodType;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.Parameter;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BObject;
import org.ballerinax.asb.util.ASBErrorCreator;
import org.ballerinax.asb.util.ModuleUtils;

import java.util.Optional;
import java.util.stream.Stream;

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

    public void validate() {
        Parameter[] onMessageParams = onMessage.getParameters();
        if (onMessageParams.length == 0) {
            throw ASBErrorCreator.createError(
                    "Required parameter `message` has not been defined in the `onMessage` method");
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

    public void invokeOnMessage(Callback callback, Object[] params) {
        Module module = ModuleUtils.getModule();
        StrandMetadata metadata = new StrandMetadata(
                module.getOrg(), module.getName(), module.getMajorVersion(), ON_MESSAGE_METHOD);
        if (isolated) {
            bRuntime.invokeMethodAsyncConcurrently(
                    bServiceObj, ON_MESSAGE_METHOD, null, metadata, callback, null, PredefinedTypes.TYPE_NULL, params);
        } else {
            bRuntime.invokeMethodAsyncSequentially(
                    bServiceObj, ON_MESSAGE_METHOD, null, metadata, callback, null, PredefinedTypes.TYPE_NULL, params);
        }
    }

    public Parameter[] getOnErrorParams() {
        return onError.map(FunctionType::getParameters).orElse(new Parameter[]{});
    }

    public void invokeOnError(Callback callback, Object[] params, Throwable rootCause) {
        if (onError.isEmpty()) {
            return;
        }
        Module module = ModuleUtils.getModule();
        StrandMetadata metadata = new StrandMetadata(
                module.getOrg(), module.getName(), module.getMajorVersion(), ON_ERROR_METHOD);
        if (isolated) {
            bRuntime.invokeMethodAsyncConcurrently(
                    bServiceObj, ON_ERROR_METHOD, null, metadata, callback, null, PredefinedTypes.TYPE_NULL,
                    params);
        } else {
            bRuntime.invokeMethodAsyncSequentially(
                    bServiceObj, ON_ERROR_METHOD, null, metadata, callback, null, PredefinedTypes.TYPE_NULL,
                    params);
        }
    }
}
