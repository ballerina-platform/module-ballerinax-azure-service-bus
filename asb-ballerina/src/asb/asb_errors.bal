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
