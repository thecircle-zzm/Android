package nl.thecirclezzm.seechangecamera.ui.chat;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import nl.thecirclezzm.seechangecamera.R;

public class ChatsFragment extends Fragment {
    public static final String TAG = "ChatsFragment";
    static String uniqueId;
    private TextInputEditText textField;
    private ImageButton sendButton;
    private String Username;
    private String Room;
    private Boolean hasConnection = false;
    private ListView messageListView;
    private MessageAdapter messageAdapter;
    private final Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            ChatsFragment.this.getActivity().runOnUiThread(() -> {
                JSONObject data = (JSONObject) args[0];
                try {
                    Log.i(TAG, data.toString(4));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String username;
                String message;
                try {
                    username = data.getString("username");
                    message = data.getString("message");

                    Log.i(TAG, "run: " + username + message);

                    MessageFormat format = new MessageFormat(null, username, message, null);
                    Log.i(TAG, "run:4 ");
                    messageAdapter.add(format);
                    Log.i(TAG, "run:5 ");

                } catch (Exception e) {
                    return;
                }
            });
        }
    };
    private final Emitter.Listener onNewUser = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            ChatsFragment.this.getActivity().runOnUiThread(() -> {
                int length = args.length;

                if(length == 0){
                    return;
                }

                JSONObject data = (JSONObject) args[0];

                try {
                    JSONObject body = data.getJSONObject("user");
                    Log.i(TAG, data.toString(4));


                    String username = body.getString("username");
                    MessageFormat format = new MessageFormat(null, username, null, Room);
                    messageAdapter.add(format);
                    messageListView.smoothScrollToPosition(0);
                    messageListView.scrollTo(0, messageAdapter.getCount()-1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            });
        }
    };
    private Socket mSocket;

    {
        try {
            mSocket = IO.socket("http://145.49.6.171:5000");
        } catch (URISyntaxException e) {
        }
    }

    public static @NonNull
    ChatsFragment newInstance() {
        return new ChatsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        return layoutInflater.inflate(R.layout.chats_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Username = ChatsFragment.this.getActivity().getIntent().getStringExtra("username");
        Room = "1";

        uniqueId = UUID.randomUUID().toString();
        Log.i(TAG, "onCreate: " + uniqueId);

        if (savedInstanceState != null) {
            hasConnection = savedInstanceState.getBoolean("hasConnection");
        }

        if (!hasConnection) {
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

        textField = this.getView().findViewById(R.id.textField);
        sendButton = this.getView().findViewById(R.id.sendButton);
        sendButton.setOnClickListener(this::sendMessage);
        messageListView = this.getView().findViewById(R.id.messageListView);

        List<MessageFormat> messageFormatList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this.getContext(), R.layout.item_message, messageFormatList);
        messageListView.setAdapter(messageAdapter);

        onTypeButtonEnable();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("hasConnection", hasConnection);
    }

    private void onTypeButtonEnable() {
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

    private void sendMessage(View view) {
        Log.i(TAG, "sendMefffssage: ");
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


            Log.i(TAG, "sendMessage: " + jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "sendMessage: 1"+ mSocket.emit("sendMessage", jsonObject.toString()));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(getActivity().isFinishing()){
            Log.i(TAG, "onDestroy: ");

            JSONObject user = new JSONObject();
            try {
                user.put("username", Username + " DisConnected");
                mSocket.emit("join", user);
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
