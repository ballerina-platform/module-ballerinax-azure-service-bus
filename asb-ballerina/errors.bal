// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

# Represents the Asb module related errors.
public type AsbError distinct error;

# The union of the Asb module related errors.
public type Error AsbError;

# Prepare the `error` as a `Error`.
#
# + message - The error message
# + err - The `error` instance
# + return - Prepared `nats:Error` instance
isolated function prepareError(string message, error? err = ()) returns Error {
    AsbError asbError;
    if (err is error) {
        asbError = AsbError(message, err);
    } else {
        asbError = AsbError(message);
    }
    return asbError;
}
