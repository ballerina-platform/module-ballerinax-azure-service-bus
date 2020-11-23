package org.ballerinalang.asb;

import org.ballerinalang.jvm.api.values.BError;
import org.ballerinalang.jvm.api.BErrorCreator;
import org.ballerinalang.jvm.api.BStringUtils;

public class AsbUtils {
    public static BError returnErrorValue(String errorMessage) {
        return BErrorCreator.createDistinctError(AsbConstants.ASB_ERROR,
                AsbConstants.PACKAGE_ID_ASB,
                BStringUtils.fromString(errorMessage));
    }
}

