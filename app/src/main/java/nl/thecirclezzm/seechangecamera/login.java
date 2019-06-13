package nl.thecirclezzm.seechangecamera;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import nl.thecirclezzm.seechangecamera.model.User;
import nl.thecirclezzm.seechangecamera.model.streamingKey;
import nl.thecirclezzm.seechangecamera.service.UserClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView usernameTextView = findViewById(R.id.usernameTextView);
        TextView passwordTextView = findViewById(R.id.passwordTextView);

        EditText usernameInputField = findViewById(R.id.usernameInputField);
        EditText passwordInputField = findViewById(R.id.passwordInputField);

        usernameTextView.setText(usernameInputField.getText());
        passwordTextView.setText(passwordInputField.getText());

        Button loginAccount = (Button) findViewById(R.id.loginButton);
        loginAccount.setOnClickListener((view) -> {
            User user = new User(
                usernameInputField.getText().toString(),
                passwordInputField.getText().toString()
            );

            sendNetworkRequest(user);
        });
    }

    private void sendNetworkRequest(User user){
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl("http://188.166.38.127/api/login")
                .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = builder.build();

        UserClient client = retrofit.create(UserClient.class);
        Call<streamingKey> call = client.loginAccount(user);

        call.enqueue(new Callback<streamingKey>() {
            @Override
            public void onResponse(Call<streamingKey> call, Response<streamingKey> response) {
                Toast.makeText(login.this, "Och jongens das handel", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<streamingKey> call, Throwable t) {
                Toast.makeText(login.this, "Och jongens nie goed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void loginButtonOnClick(View v){

    }
}
