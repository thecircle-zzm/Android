package nl.thecirclezzm.seechangecamera;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.math.BigInteger;

public class login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView usernameTextView = findViewById(R.id.usernameTextView);
        TextView passwordTextView = findViewById(R.id.passwordTextView);

//        EditText usernameInputField = findViewById(R.id.usernameInputField);
//        EditText passwordInputField = findViewById(R.id.passwordInputField);

        Button loginButton = findViewById(R.id.loginButton);
    }

    public void loginButton(View view){
        EditText usernameInputField = findViewById(R.id.usernameInputField);
        EditText passwordInputField = findViewById(R.id.passwordInputField);
        TextView passwordTextView = findViewById(R.id.passwordTextView);


        byte[] md5Input = passwordInputField.getText().toString().getBytes();
        BigInteger md5Data = null;

        try {
            md5Data = new BigInteger(1, md5.encryptMD5(md5Input));
        } catch(Exception e){
            e.printStackTrace();
        }

        String md55tr = md5Data.toString(16);

        passwordTextView.setText(md55tr);
    }
}
