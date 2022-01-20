package com.example.nativec;

public class EVMDeployRs {
    byte[] address;
    byte[] code;
    byte[] storage;

    public EVMDeployRs(byte[] _address, byte[] _code, byte[] _storage) {
        address = _address;  // Set the initial value for the class attribute x
        code = _code;  // Set the initial value for the class attribute x
        storage = _storage;
    }

    public byte[] getAddress() {
        return address;
    }

    public byte[] getCode() {
        return code;
    }

    public byte[] getStorage() { return storage; }
}
