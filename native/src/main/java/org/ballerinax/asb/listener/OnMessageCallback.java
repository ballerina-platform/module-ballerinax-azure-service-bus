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

package org.ballerinax.asb.listener;

import io.ballerina.runtime.api.async.Callback;
import io.ballerina.runtime.api.values.BError;

import java.util.concurrent.Semaphore;

/**
 * Callback code to be executed when the message-listener complete a `onMessage` invocation of the ballerina service.
 */
public class OnMessageCallback implements Callback {
    private final Semaphore semaphore;

    public OnMessageCallback(Semaphore semaphore) {
        this.semaphore = semaphore;
    }

    @Override
    public void notifySuccess(Object o) {
        semaphore.release();
        if (o instanceof BError) {
            ((BError) o).printStackTrace();
        }
    }

    @Override
    public void notifyFailure(BError bError) {
        semaphore.release();
        bError.printStackTrace();
        System.exit(1);
    }
}
