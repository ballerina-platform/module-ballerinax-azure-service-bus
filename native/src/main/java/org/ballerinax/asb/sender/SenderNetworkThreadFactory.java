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
 * KINDither express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinax.asb.sender;

import java.util.concurrent.ThreadFactory;

/**
 * A {@link ThreadFactory} object that creates new threads on demand for ASB message-sender network actions.
 */
public class SenderNetworkThreadFactory implements ThreadFactory {
    private final String threadGroupName = "asb-sender-network-thread";

    @Override
    public Thread newThread(Runnable runnable) {
        Thread senderThread = new Thread(runnable);
        senderThread.setName(threadGroupName);
        return senderThread;
    }
}
