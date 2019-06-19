package nl.thecirclezzm.seechangecamera.ui.login;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import nl.thecirclezzm.seechangecamera.R;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView usernameTextView = findViewById(R.id.usernameTextView);
        TextView passwordTextView = findViewById(R.id.passwordTextView);

        EditText usernameInput = findViewById(R.id.usernameInputField);
        EditText passwordInput = findViewById(R.id.passwordInputField);

        Button loginButton = findViewById(R.id.loginButton);
    }
}
