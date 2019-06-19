package nl.thecirclezzm.seechangecamera;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import nl.thecirclezzm.seechangecamera.model.User;
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

        EditText username = findViewById(R.id.usernameInputField);

        usernameTextView.setText(username.getText());

        Button loginAccount = (Button) findViewById(R.id.loginButton);
        loginAccount.setOnClickListener((view) -> {

            sendNetworkRequest(user);

        });
    }

    private void sendNetworkRequest(User user){
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl("http://188.166.38.127:8080/api/")
                .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = builder.build();

        UserClient client = retrofit.create(UserClient.class);
        Call<User> call = client.loginAccount(user);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.code() == 200){
                    Toast.makeText(login.this, "Och jongens das handel"/* + response.body().getAnswer()*/, Toast.LENGTH_SHORT).show();
                } else if(response.code() == 400){
                    Toast.makeText(login.this, "Och jongens das nie goed, verkeerd wachtwoord of gebruikersnaam" /*+ response.body().getAnswer()*/, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(login.this, "Och jongens das nie goed, status code " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(login.this, "Och jongens nie goed, " + t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
