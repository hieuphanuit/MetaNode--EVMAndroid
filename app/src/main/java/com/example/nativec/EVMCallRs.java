package com.example.nativec;

public class EVMCallRs {
    byte[] output;
    byte[] storage;

    public EVMCallRs(byte[] _output, byte[] _storage) {
        output = _output;
        storage = _storage;
    }

    public byte[] getOutput() {
        return output;
    }

    public byte[] getStorage() { return storage; }
}
