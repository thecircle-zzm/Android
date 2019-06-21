package nl.thecirclezzm.seechangecamera;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class FakeLoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String streamingKeyEncrypt;
        String streamingKey;
        String username;

        Intent key = getIntent();

        streamingKeyEncrypt = key.getStringExtra("streamingKeyEncrypt");
        streamingKey = key.getStringExtra("streamingKey");
        username = key.getStringExtra("username");

        Log.i("streamingKeyEncrypt", streamingKeyEncrypt);
        Log.i("streamingKey", streamingKey);

        Intent intent = new Intent(FakeLoginActivity.this, StreamingActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("streamingUrl", "rtmp://188.166.38.127:1935/live/" + streamingKey);
        intent.putExtra("chatsUrl", "http://188.166.38.127:5000");
        intent.putExtra("roomId", streamingKeyEncrypt);
        startActivity(intent);
        finish();
    }
}