package io.ballerina.lib.asb.util;

import io.ballerina.runtime.api.values.BError;

public interface CallbackHandler {
    public void notifySuccess(Object result);

    public void notifyFailure(BError error);
}
