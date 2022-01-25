package com.example.nativec;

public class EVMDeployRs {
    byte exitReason;
    byte exception;
    byte[] exmsg;

    byte[] address;
    byte[] code;
    byte[] storage;

    public EVMDeployRs(
            byte _exitReason,
            byte _exception,
            byte[] _exmsg,
            byte[] _address,
            byte[] _code,
            byte[] _storage
    ) {
        exitReason = exitReason;
        exception = _exception;
        exmsg = _exmsg;
        address = _address;  // Set the initial value for the class attribute x
        code = _code;  // Set the initial value for the class attribute x
        storage = _storage;
    }


    public byte getExitReason() { return exitReason; }

    public byte getException() { return exception; }

    public byte[] getExmsg() { return exmsg; }

    public byte[] getAddress() {
        return address;
    }

    public byte[] getCode() {
        return code;
    }

    public byte[] getStorage() { return storage; }
}
