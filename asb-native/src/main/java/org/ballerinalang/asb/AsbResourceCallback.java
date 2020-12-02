/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.asb;

import org.ballerinalang.jvm.api.values.BError;
import org.ballerinalang.jvm.runtime.AsyncFunctionCallback;

import java.util.concurrent.CountDownLatch;

public class AsbResourceCallback extends AsyncFunctionCallback {
    private CountDownLatch countDownLatch;
    private String queueName;
    private int size;

    AsbResourceCallback(CountDownLatch countDownLatch, String queueName, int size) {
        this.countDownLatch = countDownLatch;
        this.queueName = queueName;
        this.size = size;
    }

    @Override
    public void notifySuccess() {
        countDownLatch.countDown();
    }

    @Override
    public void notifyFailure(BError error) {
        countDownLatch.countDown();
    }
}
