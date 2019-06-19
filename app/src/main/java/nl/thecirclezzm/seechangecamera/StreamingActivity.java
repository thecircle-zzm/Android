package nl.thecirclezzm.seechangecamera;

import android.annotation.SuppressLint;
import android.app.PictureInPictureParams;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Rational;
import android.view.View;

import androidx.annotation.Nullable;

import nl.thecirclezzm.seechangecamera.ui.chat.ChatsFragment;
import nl.thecirclezzm.seechangecamera.ui.streaming.StreamingFragment;
import nl.thecirclezzm.seechangecamera.utils.PermissionCompatActivity;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class StreamingActivity extends PermissionCompatActivity {
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.streaming_activity);

        if (savedInstanceState == null) {
            requestPermissions(new String[]{INTERNET, CAMERA, RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, permissionsGranted -> {
                if (permissionsGranted) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.streamContainer, StreamingFragment.Companion.newInstance())
                            .commitNow();

                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.chatContainer, ChatsFragment.newInstance())
                            .commitNow();
                }

                return null;
            });
        }
    }

    @Override
    public void onUserLeaveHint() {
        enterPictureInPictureMode(
                new PictureInPictureParams.Builder()
                        .setSourceRectHint(new Rect(0, 0, 640, 480))
                        .setAspectRatio(new Rational(3, 4))
                        .build());
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        findViewById(R.id.chatContainer).setVisibility(isInPictureInPictureMode ? View.GONE : View.VISIBLE);
    }
}
