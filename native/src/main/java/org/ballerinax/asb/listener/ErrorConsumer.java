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

import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusFailureReason;
import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.Runtime;
import io.ballerina.runtime.api.async.StrandMetadata;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.MethodType;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.Parameter;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import org.ballerinax.asb.util.ASBErrorCreator;
import org.ballerinax.asb.util.ModuleUtils;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * {@code ErrorConsumer} provides the capability to invoke `onError` function of the ASB service.
 */
public class ErrorConsumer implements Consumer<ServiceBusErrorContext> {
    private static final String ON_ERROR_METHOD = "onError";
    private static final String MESSAGE_RETRIEVAL_ERROR = "MessageRetrievalError";
    private static final String ERROR_CONTEXT_RECORD = "ErrorContext";

    private static final BString ERROR_CTX_ENTITY_PATH = StringUtils.fromString("entityPath");
    private static final BString ERROR_CTX_CLASS_NAME = StringUtils.fromString("className");
    private static final BString ERROR_CTX_NAMESPACE = StringUtils.fromString("namespace");
    private static final BString ERROR_CTX_ERROR_SRC = StringUtils.fromString("errorSource");
    private static final BString ERROR_CTX_REASON = StringUtils.fromString("reason");

    private final BObject bListener;
    private final Runtime bRuntime;

    public ErrorConsumer(BObject bListener, Runtime bRuntime) {
        this.bListener = bListener;
        this.bRuntime = bRuntime;
    }

    @Override
    public void accept(ServiceBusErrorContext errorContext) {
        BObject bService = NativeListener.getBallerinaSvc(this.bListener);
        Module module = ModuleUtils.getModule();
        StrandMetadata metadata = new StrandMetadata(
                module.getOrg(), module.getName(), module.getMajorVersion(), ON_ERROR_METHOD);
        ObjectType serviceType = (ObjectType) TypeUtils.getReferredType(TypeUtils.getType(bService));
        Optional<MethodType> onErrorFuncOpt = Stream.of(serviceType.getMethods())
                .filter(methodType -> ON_ERROR_METHOD.equals(methodType.getName()))
                .findFirst();
        if (onErrorFuncOpt.isEmpty()) {
            errorContext.getException().printStackTrace();
            return;
        }
        MethodType onErrorFunction = onErrorFuncOpt.get();
        Object[] params = methodParameters(onErrorFunction, errorContext);
        OnErrorCallback callback = OnErrorCallback.getInstance();
        if (serviceType.isIsolated() && serviceType.isIsolated(ON_ERROR_METHOD)) {
            bRuntime.invokeMethodAsyncConcurrently(
                    bService, ON_ERROR_METHOD, null, metadata, callback, null, PredefinedTypes.TYPE_NULL,
                    params);
        } else {
            bRuntime.invokeMethodAsyncSequentially(
                    bService, ON_ERROR_METHOD, null, metadata, callback, null, PredefinedTypes.TYPE_NULL,
                    params);
        }
    }

    private Object[] methodParameters(MethodType onErrorFunction, ServiceBusErrorContext errorContext) {
        Parameter[] parameters = onErrorFunction.getParameters();
        if (parameters.length == 0) {
            throw ASBErrorCreator.createError(
                    "Required parameter `error` has not been defined in the `onError` method");
        }
        Object[] args = new Object[parameters.length * 2];
        args[0] = createError(errorContext);
        args[1] = true;
        return args;
    }

    private BError createError(ServiceBusErrorContext errorContext) {
        Throwable rootException = errorContext.getException();
        BString message = StringUtils.fromString(
                String.format("Error occurred while receiving messages from ASB: %s", rootException.getMessage()));
        BMap<BString, Object> details = getErrorContext(errorContext);
        BError cause = ErrorCreator.createError(rootException);
        return ErrorCreator.createError(ModuleUtils.getModule(), MESSAGE_RETRIEVAL_ERROR, message, cause, details);
    }

    private BMap<BString, Object> getErrorContext(ServiceBusErrorContext context) {
        BMap<BString, Object> errorContextRecord = ValueCreator.createRecordValue(
                ModuleUtils.getModule(), ERROR_CONTEXT_RECORD);
        errorContextRecord.put(ERROR_CTX_ENTITY_PATH, StringUtils.fromString(context.getEntityPath()));
        errorContextRecord.put(ERROR_CTX_CLASS_NAME, StringUtils.fromString(context.getClass().getSimpleName()));
        errorContextRecord.put(ERROR_CTX_NAMESPACE, StringUtils.fromString(context.getFullyQualifiedNamespace()));
        errorContextRecord.put(ERROR_CTX_ERROR_SRC, StringUtils.fromString(context.getErrorSource().toString()));
        Throwable throwable = context.getException();
        ServiceBusException exception = (throwable instanceof ServiceBusException) ? (ServiceBusException) throwable :
                new ServiceBusException(throwable, context.getErrorSource());
        ServiceBusFailureReason reason = exception.getReason();
        errorContextRecord.put(ERROR_CTX_REASON, StringUtils.fromString(reason.toString()));
        return errorContextRecord;
    }
}
