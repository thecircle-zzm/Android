package nl.thecirclezzm.seechangecamera;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private EditText textField;
    private ImageButton sendButton;

    public static final String TAG  = "MainActivity";
    public static String uniqueId;

    private String Username;
    private String Room;

    private Boolean hasConnection = false;

    private ListView messageListView;
    private MessageAdapter messageAdapter;


    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://145.49.6.171:5000");
        } catch (URISyntaxException e) {}
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Username = getIntent().getStringExtra("username");
        Room = "1";

        uniqueId = UUID.randomUUID().toString();
        Log.i(TAG, "onCreate: " + uniqueId);

        if(savedInstanceState != null){
            hasConnection = savedInstanceState.getBoolean("hasConnection");
        }

        if(hasConnection){

        }else {
            mSocket.connect();
            mSocket.on("join", onNewUser);
            mSocket.on("sendMessage", onNewMessage);

            JSONObject user = new JSONObject();
            try {
                user.put("username", Username);
                user.put("room", Room);

                Log.i(TAG,"user" + user.toString());

                mSocket.emit("join", user.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Log.i(TAG, "onCreate: " + hasConnection);
        hasConnection = true;


        Log.i(TAG, "onCreate: " + Username + " " + "Connected");

        textField = findViewById(R.id.textField);
        sendButton = findViewById(R.id.sendButton);
        messageListView = findViewById(R.id.messageListView);

        List<MessageFormat> messageFormatList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, R.layout.item_message, messageFormatList);
        messageListView.setAdapter(messageAdapter);

        onTypeButtonEnable();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("hasConnection", hasConnection);
    }

    public void onTypeButtonEnable(){
        textField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {


                if (charSequence.toString().trim().length() > 0) {
                    sendButton.setEnabled(true);
                } else {
                    sendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    JSONObject data = (JSONObject) args[0];
                    String username;
                    String message;
                    String id;
                    try {
                        username = data.getString("username");
                        message = data.getString("message");
                        id = data.getString("uniqueId");

                        Log.i(TAG, "run: " + username + message + id);

                        MessageFormat format = new MessageFormat(id, username, Room, message);
                        Log.i(TAG, "run:4 ");
                        messageAdapter.add(format);
                        Log.i(TAG, "run:5 ");

                    } catch (Exception e) {
                        return;
                    }
                }
            });
        }
    };

    Emitter.Listener onNewUser = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int length = args.length;

                    if(length == 0){
                        return;
                    }

                    String username =args[0].toString();
                    String room = "1";
                    try {
                        JSONObject object = new JSONObject();

                        object.put("username", Username);
                        object.put("room", Room);


                        username = object.getString("username");
                        room = object.getString("room");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    MessageFormat format = new MessageFormat(null, username, null, room);
                    messageAdapter.add(format);
                    messageListView.smoothScrollToPosition(0);
                    messageListView.scrollTo(0, messageAdapter.getCount()-1);

                }
            });
        }
    };



    public void sendMessage(View view){
        Log.i(TAG, "sendMessage: ");
        String message = textField.getText().toString().trim();
        if(TextUtils.isEmpty(message)){
            Log.i(TAG, "sendMessage:2 ");
            return;
        }
        textField.setText("");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("message", message);
            jsonObject.put("username", Username);
            jsonObject.put("uniqueId", uniqueId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "sendMessage: 1"+ mSocket.emit("sendMessage", jsonObject));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(isFinishing()){
            Log.i(TAG, "onDestroy: ");

            JSONObject userId = new JSONObject();
            try {
                userId.put("username", Username + " DisConnected");
                mSocket.emit("join", userId);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mSocket.disconnect();
            mSocket.off("sendMessage", onNewMessage);
            mSocket.off("join", onNewUser);
            Username = "";
            messageAdapter.clear();
        }else {
            Log.i(TAG, "onDestroy: is rotating.....");
        }

    }


}
