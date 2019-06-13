package nl.thecirclezzm.seechangecamera;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.Nullable;

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
                if (permissionsGranted)
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, StreamingFragment.Companion.newInstance())
                            .commitNow();

                return null;
            });
        }
    }
}
