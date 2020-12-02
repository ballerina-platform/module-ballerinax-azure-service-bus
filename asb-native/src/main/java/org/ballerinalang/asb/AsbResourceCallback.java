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
