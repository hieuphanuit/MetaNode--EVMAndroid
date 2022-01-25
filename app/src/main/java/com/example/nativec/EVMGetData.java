package com.example.nativec;

public class EVMGetData {
    public static byte[] HexStringToByteArray(String s) {
        byte data[] = new byte[s.length()/2];
        for(int i=0;i < s.length();i+=2) {
            data[i/2] = (Integer.decode("0x"+s.charAt(i)+s.charAt(i+1))).byteValue();
        }
        return data;
    }

    public static byte[] GetCode(byte[] address) {
        // TEMPORARY HARDCODE
        return HexStringToByteArray("608060405260043610610057576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806312065fe01461005c5780633ccfd60b14610087578063d0e30db01461009e575b600080fd5b34801561006857600080fd5b506100716100c0565b6040518082815260200191505060405180910390f35b34801561009357600080fd5b5061009c6100df565b005b6100a6610143565b604051808215151515815260200191505060405180910390f35b60003073ffffffffffffffffffffffffffffffffffffffff1631905090565b3373ffffffffffffffffffffffffffffffffffffffff166108fc6107d03073ffffffffffffffffffffffffffffffffffffffff1631019081150290604051600060405180830381858888f19350505050158015610140573d6000803e3d6000fd5b50565b600060019050905600a165627a7a723058203af50ab840a139cea1f1cdfc17bfd13b0d635533cd093bad92e1428938139f690029");
    }

    public static String GetStorage(byte[] address) {
        // TEMPORARY HARDCODE
        return "{}";
    }

    public static byte[] GetBalance(byte[] address) {
        // TEMPORARY HARDCODE
        return HexStringToByteArray("00000000000000000000000000000000000000000000000000000000000f4240");
    }
}
