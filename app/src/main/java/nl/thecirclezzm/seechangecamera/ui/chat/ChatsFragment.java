package nl.thecirclezzm.seechangecamera.ui.chat;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

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
    private EditText textField;
    private ImageButton sendButton;
    private String Username;
    private Boolean hasConnection = false;
    private ListView messageListView;
    private MessageAdapter messageAdapter;
    private final Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            ChatsFragment.this.getActivity().runOnUiThread(() -> {
                Log.i(TAG, "run: ");
                Log.i(TAG, "run: " + args.length);
                JSONObject data = (JSONObject) args[0];
                String username;
                String message;
                String id;
                try {
                    username = data.getString("username");
                    message = data.getString("message");
                    id = data.getString("uniqueId");

                    Log.i(TAG, "run: " + username + message + id);

                    MessageFormat format = new MessageFormat(id, username, message);
                    Log.i(TAG, "run:4 ");
                    messageAdapter.add(format);
                    Log.i(TAG, "run:5 ");

                } catch (Exception e) {
                }
            });
        }
    };
    private final Emitter.Listener onNewUser = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            ChatsFragment.this.getActivity().runOnUiThread(() -> {
                int length = args.length;

                if (length == 0) {
                    return;
                }

                Log.i(TAG, "run: ");
                Log.i(TAG, "run: " + args.length);
                String username = args[0].toString();
                try {
                    JSONObject object = new JSONObject(username);
                    username = object.getString("username");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                MessageFormat format = new MessageFormat(null, username, null);
                messageAdapter.add(format);
                messageListView.smoothScrollToPosition(0);
                messageListView.scrollTo(0, messageAdapter.getCount() - 1);
                Log.i(TAG, "run: " + username);
            });
        }
    };
    private Socket mSocket;

    {
        try {
            mSocket = IO.socket("https://nameless-thicket-23770.herokuapp.com/");
        } catch (URISyntaxException e) {
        }
    }

    public static ChatsFragment newInstance() {
        return new ChatsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        return layoutInflater.inflate(R.layout.activity_main, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Username = ChatsFragment.this.getActivity().getIntent().getStringExtra("username");

        uniqueId = UUID.randomUUID().toString();
        Log.i(TAG, "onCreate: " + uniqueId);

        if (savedInstanceState != null) {
            hasConnection = savedInstanceState.getBoolean("hasConnection");
        }

        if (!hasConnection) {
            mSocket.connect();
            mSocket.on("connect user", onNewUser);
            mSocket.on("chat message", onNewMessage);

            JSONObject userId = new JSONObject();
            try {
                userId.put("username", Username + " Connected");
                mSocket.emit("connect user", userId);
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
    public void onSaveInstanceState(Bundle outState) {
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
        Log.i(TAG, "sendMessage: ");
        String message = textField.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
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
        Log.i(TAG, "sendMessage: 1" + mSocket.emit("chat message", jsonObject));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (getActivity().isFinishing()) {
            Log.i(TAG, "onDestroy: ");

            JSONObject userId = new JSONObject();
            try {
                userId.put("username", Username + " DisConnected");
                mSocket.emit("connect user", userId);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            mSocket.disconnect();
            mSocket.off("chat message", onNewMessage);
            mSocket.off("connect user", onNewUser);
            Username = "";
            messageAdapter.clear();
        } else {
            Log.i(TAG, "onDestroy: is rotating.....");
        }

    }


}
