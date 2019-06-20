package nl.thecirclezzm.seechangecamera;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class FakeLoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String domain = "188.166.38.127";
        final String appName = "a37b62f8ea0d838d";

        Intent intent = new Intent(FakeLoginActivity.this, StreamingActivity.class);
        intent.putExtra("username", "Streamer");
        intent.putExtra("streamingUrl", "rtmp://" + domain + ":1935/live/" + appName);
        intent.putExtra("chatsUrl", "http://" + domain + ":5000");
        intent.putExtra("roomId", appName);
        startActivity(intent);
        finish();
    }
}