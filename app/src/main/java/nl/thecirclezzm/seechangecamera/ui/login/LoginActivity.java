package nl.thecirclezzm.seechangecamera.ui.login;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigInteger;
import java.security.MessageDigest;

import nl.thecirclezzm.seechangecamera.R;
import nl.thecirclezzm.seechangecamera.ui.login.model.User;
import nl.thecirclezzm.seechangecamera.ui.login.model.sha256;
import nl.thecirclezzm.seechangecamera.ui.login.service.UserClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView usernameTextView = findViewById(R.id.usernameTextView);

        EditText usernameInput = findViewById(R.id.usernameInputField);

        Button loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener((view) -> {

            String user =usernameInput.getText().toString();
            Log.i("Yes", user);
            sendNetworkRequest(user);

        });
    }

    private void sendNetworkRequest(String user){
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl("http://188.166.38.127:8080/api/")
                .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = builder.build();
        Log.i("Yes", user);
        UserClient client = retrofit.create(UserClient.class);
        Call<User> call = client.loginAccount(user);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                Log.i("Yes", user);
                Log.i("Yesyeseeys", response.body().toString());
                if(response.code() == 200){
                    Toast.makeText(LoginActivity.this, "Och jongens das handel" + response.body().toString() /* + response.body().getAnswer()*/, Toast.LENGTH_SHORT).show();
                    encryptStreamingkey(response.body().getStreamingKey());
                } else if(response.code() == 400){
                    Toast.makeText(LoginActivity.this, "Och jongens das nie goed, verkeerd wachtwoord of gebruikersnaam" /*+ response.body().getAnswer()*/, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Och jongens das nie goed, status code " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Och jongens nie goed, " + t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void encryptStreamingkey(String streamingkey){
        Log.i("StreamingKey", streamingkey);
        byte[] sha256Input = streamingkey.getBytes();
        Log.i("Sha256Input", sha256Input.toString());
        BigInteger sha256Data = null;

        try {
            sha256Data = new BigInteger(1, sha256.encryptSha256(sha256Input));
        } catch(Exception e){
            e.printStackTrace();
        }

        String md55tr = sha256Data.toString(16);
        Log.i("Sha256", md55tr);
        }
}
