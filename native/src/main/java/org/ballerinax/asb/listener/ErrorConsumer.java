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
import io.ballerina.runtime.api.values.BObject;

import java.util.function.Consumer;

/**
 * {@code ErrorConsumer} provides the capability to invoke `onError` function of the ASB service.
 */
public class ErrorConsumer implements Consumer<ServiceBusErrorContext> {
    private final BObject bListener;

    public ErrorConsumer(BObject bListener) {
        this.bListener = bListener;
    }

    // todo: implement this method properly
    @Override
    public void accept(ServiceBusErrorContext errorContext) {
        BObject bService = NativeListener.getBallerinaSvc(this.bListener);
    }
}
