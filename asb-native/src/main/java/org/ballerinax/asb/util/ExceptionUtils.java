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

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BError;

import static org.ballerinax.asb.util.ASBConstants.ASB_ERROR;
import static org.ballerinax.asb.util.ModuleUtils.getModule;

/**
 * ASB module exceptions related utilities.
 */
public class ExceptionUtils {

    public static final String ASB_ERR_PREFIX = "ASB request Failed due to: ";
    public static final String ASB_ERR_DEFAULT_PREFIX = "Unexpected error occurred: ";

    public static BError createAsbError(String message) {
        return ErrorCreator.createDistinctError(ASB_ERROR, getModule(), StringUtils.fromString(message));
    }

    public static BError createAsbError(BError error) {
        return createAsbError(error.getMessage(), error.getCause());
    }

    public static BError createAsbError(String message, Throwable cause) {
        return createAsbError(message, ErrorCreator.createError(cause));
    }

    public static BError createAsbError(String message, BError cause) {
        return ErrorCreator.createDistinctError(ASB_ERROR, getModule(), StringUtils.fromString(message), cause);
    }
}
