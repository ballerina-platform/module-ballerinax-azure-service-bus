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

import io.ballerina.lib.asb.util.CallbackHandler;
import io.ballerina.runtime.api.values.BError;

/**
 * Callback code to be executed when the message-listener complete a `onError` invocation of the ballerina service.
 */
public class OnErrorCallback implements CallbackHandler {
    private static final OnErrorCallback INSTANCE = new OnErrorCallback();

    static OnErrorCallback getInstance() {
        return INSTANCE;
    }

    @Override
    public void notifySuccess(Object o) {
        if (o instanceof BError) {
            ((BError) o).printStackTrace();
        }
    }

    @Override
    public void notifyFailure(BError bError) {
        bError.printStackTrace();
        System.exit(1);
    }
}
