package nl.thecirclezzm.seechangecamera;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import nl.thecirclezzm.seechangecamera.ui.chat.ChatsFragment;

public class FakeLoginActivity extends AppCompatActivity {

    /*private Button setNickName;
    private EditText userNickName;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*setContentView(R.layout.fake_login_activity);

        userNickName = findViewById(R.id.userNickName);
        setNickName = findViewById(R.id.setNickName);


        userNickName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    setNickName.setEnabled(true);
                    Log.i(ChatsFragment.TAG, "onTextChanged: ABLED");
                } else {
                    Log.i(ChatsFragment.TAG, "onTextChanged: DISABLED");
                    setNickName.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


        setNickName.setOnClickListener(v -> {
            Intent intent = new Intent(FakeLoginActivity.this, StreamingActivity.class);
            intent.putExtra("username", userNickName.getText().toString());
            startActivity(intent);
            finish();
        });*/

        Intent intent = new Intent(FakeLoginActivity.this, StreamingActivity.class);
        intent.putExtra("username", "Streamer");
        startActivity(intent);
        finish();
    }
}
