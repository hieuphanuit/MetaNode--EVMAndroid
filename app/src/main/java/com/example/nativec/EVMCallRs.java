package com.example.nativec;

public class EVMCallRs {
    byte exitReason;
    byte exception;
    byte[] exmsg;

    byte[] output;
    byte[] storage;

    public EVMCallRs(
            byte _exitReason,
            byte _exception,
            byte[] _exmsg,
            byte[] _output,
            byte[] _storage
    ) {
        exitReason = exitReason;
        exception = _exception;
        exmsg = _exmsg;
        output = _output;
        storage = _storage;
    }

    public byte getExitReason() { return exitReason; }

    public byte getException() { return exception; }

    public byte[] getExmsg() { return exmsg; }

    public byte[] getOutput() {
        return output;
    }

    public byte[] getStorage() { return storage; }
}
