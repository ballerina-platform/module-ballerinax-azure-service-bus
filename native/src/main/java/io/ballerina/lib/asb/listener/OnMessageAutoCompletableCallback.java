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

import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import io.ballerina.lib.asb.util.CallbackHandler;
import io.ballerina.runtime.api.values.BError;

import java.util.concurrent.Semaphore;

/**
 * Callback code to be executed when the message-listener complete a `onMessage` invocation of the ballerina service.
 * This particular callback implementation will mark the messages complete/abandon automatically once the remote
 * functions return the results.
 */
public class OnMessageAutoCompletableCallback implements CallbackHandler {
    private final Semaphore semaphore;
    private final ServiceBusReceivedMessageContext messageContext;

    public OnMessageAutoCompletableCallback(Semaphore semaphore, ServiceBusReceivedMessageContext messageContext) {
        this.semaphore = semaphore;
        this.messageContext = messageContext;
    }

    @Override
    public void notifySuccess(Object o) {
        semaphore.release();
        if (o instanceof BError) {
            messageContext.abandon();
            ((BError) o).printStackTrace();
            return;
        }
        messageContext.complete();
    }

    @Override
    public void notifyFailure(BError bError) {
        semaphore.release();
        messageContext.abandon();
        bError.printStackTrace();
        System.exit(1);
    }
}
