package nl.thecirclezzm.seechangecamera;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class FakeLoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(FakeLoginActivity.this, StreamingActivity.class);
        intent.putExtra("username", "Streamer");
        intent.putExtra("streamingUrl", "rtmp://188.166.38.127:1935/live/a37b62f8ea0d838d");
        intent.putExtra("chatsUrl", "http://188.166.38.127:5000");
        intent.putExtra("roomId", "1");
        startActivity(intent);
        finish();
    }
}