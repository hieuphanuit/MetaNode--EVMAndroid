package com.example.nativec;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.nativec.databinding.ActivityMainBinding;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'nativec' library on application startup.
    static {
        System.loadLibrary("native");
    }

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_main);

        Button deployButton = (Button) findViewById(R.id.deploy_button);
        Button callButton = (Button) findViewById(R.id.btn_call);

        TextInputEditText senderAddress = (TextInputEditText)findViewById(R.id.sender_address);
        TextInputEditText contractAddress = (TextInputEditText)findViewById(R.id.contract_address);
        TextInputEditText contractCode = (TextInputEditText)findViewById(R.id.contract_code);
        TextInputEditText contractStorage = (TextInputEditText)findViewById(R.id.contract_storage);
        TextInputEditText callData = (TextInputEditText)findViewById(R.id.call_data);
        TextInputEditText callResult = (TextInputEditText)findViewById(R.id.call_result);

        senderAddress.setText("e3f32c4b6b86e018c8c6fed0653da210c8363705");


        deployButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                // Example of a call to a native method
                EVMDeployRs deployRs = deploy(
                        HexStringToByteArray(senderAddress.getText().toString()),
                        HexStringToByteArray("4e03657aea45a94fc7d47ba826c8d667c0d1e6e33a64a036ec44f58fa12d6c45"),
                        HexStringToByteArray("0000000000000000000000000000000000000000000000000000000000000000"),
                        HexStringToByteArray("608060405234801561001057600080fd5b506101e8806100206000396000f30060806040526004361061004c576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806398cdb09814610051578063ef79cae214610087575b600080fd5b610085600480360381019080803573ffffffffffffffffffffffffffffffffffffffff1690602001909291905050506100b2565b005b34801561009357600080fd5b5061009c6100f5565b6040518082815260200191505060405180910390f35b806000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555050565b60008060009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166312065fe06040518163ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401602060405180830381600087803b15801561017c57600080fd5b505af1158015610190573d6000803e3d6000fd5b505050506040513d60208110156101a657600080fd5b81019080805190602001909291905050509050905600a165627a7a72305820a305eaf51d296dc9b9a4f76180ce695ac88d4c662ef54b15fb58ec9abb7b51e80029")
                );


                StringBuilder sbContractAddress = new StringBuilder();
                for (byte b : deployRs.getAddress()) {
                    sbContractAddress.append(String.format("%02x", b));
                }
                contractAddress.setText(sbContractAddress.toString());

                StringBuilder sbContractCode = new StringBuilder();
                for (byte b : deployRs.getCode()) {
                    sbContractCode.append(String.format("%02x", b));
                }
                contractCode.setText(sbContractCode.toString());


                contractStorage.setText(new String(deployRs.getStorage(), Charset.defaultCharset()));

                // get balance of sender address
//                callData.setText("70a08231000000000000000000000000e3f32c4b6b86e018c8c6fed0653da210c8363705");
//                 get balance of receiver address
//                callData.setText("70a08231000000000000000000000000f854c27c46e3fbf2abbacd29ec4aff517369c667");

                // send 1000 to f854c27c46e3fbf2abbacd29ec4aff517369c667
                callData.setText("ef79cae2");
            }
        });

        callButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EVMCallRs callRs = call(
                        HexStringToByteArray(senderAddress.getText().toString()),
                        HexStringToByteArray("4e03657aea45a94fc7d47ba826c8d667c0d1e6e33a64a036ec44f58fa12d6c45"),
                        HexStringToByteArray(contractAddress.getText().toString()),
                        HexStringToByteArray("0000000000000000000000000000000000000000000000000000000000000000"),
                        contractStorage.getText().toString(),
                        HexStringToByteArray(contractCode.getText().toString()),
                        HexStringToByteArray(callData.getText().toString())
                        );

                StringBuilder sbCallRs = new StringBuilder();
                for (byte b : callRs.getOutput()) {
                    sbCallRs.append(String.format("%02x", b));
                }

                // use c++ from_big_endian function to view result in int number
                ByteBuffer bb = ByteBuffer.wrap(callRs.getOutput());

                BigInteger intRs = new BigInteger(callRs.getOutput());
                callResult.setText(intRs.toString());

                contractStorage.setText(new String(callRs.getStorage(), Charset.defaultCharset()));
            }
        });


//        sampleTextView.setText(sb.toString());
    }

    public static byte[] HexStringToByteArray(String s) {
        byte data[] = new byte[s.length()/2];
        for(int i=0;i < s.length();i+=2) {
            data[i/2] = (Integer.decode("0x"+s.charAt(i)+s.charAt(i+1))).byteValue();
        }
        return data;
    }
    /**
     * A native method that is implemented by the 'nativec' native library,
     * which is packaged with this application.
     */
    public native EVMDeployRs deploy(
            byte[] jb_caller_address,
            byte[] jb_caller_last_hash,
            byte[] jb_contract_balance,
            byte[] jb_contract_constructor
    );

    public native EVMCallRs call(
            byte[] jb_caller_address,
            byte[] jb_caller_last_hash,
            byte[] jb_contract_address,
            byte[] jb_contract_balance,
            String oldStorage,
            byte[] code,
            byte[] input
    );
}