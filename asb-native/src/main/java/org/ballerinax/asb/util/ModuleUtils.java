/*
 * Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinax.asb.util;

import java.util.logging.LogManager;
import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;

/**
 * This class will hold module related utility functions.
 */
public class ModuleUtils {

    /**
     * ASB connector package ID.
     */
    private static Module asbModule = null;

    private ModuleUtils() {
    }

    public static Module getModule() {
        return asbModule;
    }

    public static Object setModule(Environment env) {
        String logLevel = "";
        try {
            logLevel = System.getenv("ASB_CLOUD_LOGS");
        } catch (Exception e) {
            // If a security manager exists, its checkPermission method is called with a
            // RuntimePermission("getenv."+name) permission. This may result in a
            // SecurityException being thrown. If no exception is thrown the value of the
            // variable name is returned.
            BString message = StringUtils.fromString("Error returned when trying to read environment variables");
            return ErrorCreator.createError(message, e);
        }
        if (logLevel == null || logLevel.isEmpty() || !logLevel.equalsIgnoreCase("ACTIVE")) {
            LogManager.getLogManager().reset();
        }
        asbModule = env.getCurrentModule();
        return null;
    }
}
