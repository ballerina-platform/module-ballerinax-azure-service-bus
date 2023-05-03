/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinax.asb.util;

import com.azure.messaging.servicebus.ServiceBusException;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;

import static org.ballerinax.asb.util.ASBConstants.ASB_ERROR;
import static org.ballerinax.asb.util.ModuleUtils.getModule;

/**
 * ASB module error related utilities.
 */
public class ASBErrorCreator {

    public static final String ASB_ERROR_PREFIX = "ASB request failed due to: ";
    public static final String UNHANDLED_ERROR_PREFIX = "Unexpected error occurred while processing request: ";

    public static BError fromASBException(ServiceBusException e) {
        return fromJavaException(ASB_ERROR_PREFIX + e.getReason().toString(), e.getCause());
    }

    public static BError fromUnhandledException(Exception e) {
        return fromJavaException(UNHANDLED_ERROR_PREFIX + e.getMessage(), e.getCause());
    }

    public static BError fromBError(BError error) {
        return fromBError(error.getMessage(), error.getCause());
    }

    public static BError fromBError(String message, BError cause) {
        return ErrorCreator.createDistinctError(ASB_ERROR, getModule(), StringUtils.fromString(message), cause);
    }

    private static BError fromJavaException(String message, Throwable cause) {
        return fromBError(message, ErrorCreator.createError(cause));
    }
}
