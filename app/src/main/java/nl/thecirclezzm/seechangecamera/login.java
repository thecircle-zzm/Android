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
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;

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

        PrintInstalledCertificates();

        TextView usernameTextView = findViewById(R.id.usernameTextView);
        TextView passwordTextView = findViewById(R.id.passwordTextView);

        EditText username = findViewById(R.id.usernameInputField);
        EditText password = findViewById(R.id.passwordInputField);

        usernameTextView.setText(username.getText());
        passwordTextView.setText(password.getText());

        Button loginAccount = (Button) findViewById(R.id.loginButton);
        loginAccount.setOnClickListener((view) -> {
            User user = new User(
                username.getText().toString(),
                password.getText().toString()
            );

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

    public void PrintInstalledCertificates() {
        try {
            KeyStore ks = KeyStore.getInstance("AndroidCAStore");

            if (ks != null) {
                ks.load(null, null);
                Enumeration<String> aliases = ks.aliases();

                while (aliases.hasMoreElements()) {

                    String alias = (String) aliases.nextElement();

                    java.security.cert.X509Certificate cert = (java.security.cert.X509Certificate) ks.getCertificate(alias);
                    //To print System Certs only
                    if(cert.getIssuerDN().getName().contains("system")){
                        System.out.println(cert.getIssuerDN().getName());
                    }

                    //To print User Certs only
                    if(cert.getIssuerDN().getName().contains("user")){
                        System.out.println(cert.getIssuerDN().getName());
                    }

                    //To print all certs
                    System.out.println(cert.getIssuerDN().getName());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (java.security.cert.CertificateException e) {
            e.printStackTrace();
        }
    }

    public void loginButtonOnClick(View v){

    }
}
