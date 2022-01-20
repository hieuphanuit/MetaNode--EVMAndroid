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
                        HexStringToByteArray("608060405234801561001057600080fd5b5060405160208061040783398101604090815290516000818155338152600160205291909120556103c1806100466000396000f3006080604052600436106100775763ffffffff7c0100000000000000000000000000000000000000000000000000000000600035041663095ea7b3811461007c57806318160ddd146100b457806323b872dd146100db57806370a0823114610105578063a9059cbb14610126578063dd62ed3e1461014a575b600080fd5b34801561008857600080fd5b506100a0600160a060020a0360043516602435610171565b604080519115158252519081900360200190f35b3480156100c057600080fd5b506100c96101d8565b60408051918252519081900360200190f35b3480156100e757600080fd5b506100a0600160a060020a03600435811690602435166044356101de565b34801561011157600080fd5b506100c9600160a060020a03600435166102c4565b34801561013257600080fd5b506100a0600160a060020a03600435166024356102df565b34801561015657600080fd5b506100c9600160a060020a036004358116906024351661036a565b336000818152600260209081526040808320600160a060020a038716808552908352818420869055815186815291519394909390927f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925928290030190a35060015b92915050565b60005490565b600160a060020a03831660009081526001602052604081205482118015906102295750600160a060020a03841660009081526002602090815260408083203384529091529020548211155b156102b957600160a060020a038085166000818152600160209081526040808320805488900390559387168083528483208054880190559282526002815283822033808452908252918490208054879003905583518681529351929391927fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef9281900390910190a35060016102bd565b5060005b9392505050565b600160a060020a031660009081526001602052604090205490565b3360009081526001602052604081205482116103625733600081815260016020908152604080832080548790039055600160a060020a03871680845292819020805487019055805186815290519293927fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef929181900390910190a35060016101d2565b5060006101d2565b600160a060020a039182166000908152600260209081526040808320939094168252919091522054905600a165627a7a7230582048867a831c7e97d2f7475a6a0c393fabe9c04714f8255cb9995d0531763eb2fb002900000000000000000000000000000000000000000000000000000000000f4240")
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
                callData.setText("a9059cbb000000000000000000000000f854c27c46e3fbf2abbacd29ec4aff517369c66700000000000000000000000000000000000000000000000000000000000003e8");
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