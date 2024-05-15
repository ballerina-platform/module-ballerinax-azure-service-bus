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
import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.Parameter;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import org.ballerinax.asb.util.ModuleUtils;

import java.util.function.Consumer;

/**
 * {@code ErrorConsumer} provides the capability to invoke `onError` function of the ASB service.
 */
public class ErrorConsumer implements Consumer<ServiceBusErrorContext> {
    private static final String MESSAGE_RETRIEVAL_ERROR = "MessageRetrievalError";
    private static final String ERROR_CONTEXT_RECORD = "ErrorContext";

    private static final BString ERROR_CTX_ENTITY_PATH = StringUtils.fromString("entityPath");
    private static final BString ERROR_CTX_CLASS_NAME = StringUtils.fromString("className");
    private static final BString ERROR_CTX_NAMESPACE = StringUtils.fromString("namespace");
    private static final BString ERROR_CTX_ERROR_SRC = StringUtils.fromString("errorSource");
    private static final BString ERROR_CTX_REASON = StringUtils.fromString("reason");

    private final BObject bListener;

    public ErrorConsumer(BObject bListener) {
        this.bListener = bListener;
    }

    @Override
    public void accept(ServiceBusErrorContext errorContext) {
        NativeBServiceAdaptor bService = NativeListener.getBallerinaSvc(this.bListener);
        Object[] params = getMethodParams(bService.getOnErrorParams(), errorContext);
        Callback callback = OnErrorCallback.getInstance();
        bService.invokeOnError(callback, params);
    }

    private Object[] getMethodParams(Parameter[] parameters, ServiceBusErrorContext errorContext) {
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
