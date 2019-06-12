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


}
