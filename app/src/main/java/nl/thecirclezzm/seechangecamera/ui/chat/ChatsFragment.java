package nl.thecirclezzm.seechangecamera.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import nl.thecirclezzm.seechangecamera.R;
import nl.thecirclezzm.seechangecamera.utils.SocketIO;

public class ChatsFragment extends Fragment {
    private static final String EVENT_NEW_MESSAGE = "sendMessage";
    private static final String EVENT_NEW_USER = "join";
    private SocketIO mSocket;

    private String currentUsername;
    private String currentRoom;

    private MessageAdapter messageAdapter;

    public static @NonNull ChatsFragment newInstance() {
        return new ChatsFragment();
    }

    @Override
    public @Nullable View onCreateView(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return layoutInflater.inflate(R.layout.chats_fragment, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Intent intent = getActivity().getIntent();
        currentUsername = intent.getStringExtra("username");
        currentRoom = intent.getStringExtra("roomId");
        mSocket = new SocketIO(intent.getStringExtra("chatsUrl"));

        if (savedInstanceState == null || !savedInstanceState.getBoolean("hasConnection")) {
            try {
                mSocket.connect();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            mSocket.onNewJsonEvent(EVENT_NEW_MESSAGE, (json) -> {
                String username = json.getString("username");
                String message = json.getString("message");
                Message.MessageType type;
                if(Objects.equals(username, currentUsername)){
                    type = Message.MessageType.SENT;
                } else if(Objects.equals(username, "Channel")){
                    type = Message.MessageType.CHANNEL;
                } else {
                    type = Message.MessageType.RECEIVED;
                }
                Message format = new Message(username, message, currentRoom, type);

                // Run on UI Thread because we need to modify the views.
                getActivity().runOnUiThread(() -> {
                    messageAdapter.add(format);
                });
            });

            try {
                JSONObject user = new JSONObject();
                user.put("username", currentUsername);
                user.put("room", currentRoom);
                mSocket.sendJSON(EVENT_NEW_USER, user);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        final ImageButton sendButton = this.getView().findViewById(R.id.sendButton);
        final TextInputEditText textField = this.getView().findViewById(R.id.textField);

        sendButton.setOnClickListener((view) -> {
            String message = textField.getText().toString().trim();

            if (TextUtils.isEmpty(message))
                return;

            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("message", message);
                jsonObject.put("username", currentUsername);
                mSocket.sendJSON(EVENT_NEW_MESSAGE, jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            textField.setText("");
        });

        textField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Disable the send button when the text field is empty.
                boolean isEnabled = charSequence.toString().trim().length() > 0;
                sendButton.setEnabled(isEnabled);
                sendButton.setImageAlpha(isEnabled ? 255 : 100);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        ListView messageListView = this.getView().findViewById(R.id.messageListView);
        List<Message> messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this.getContext(), R.layout.item_message, messageList);
        messageListView.setAdapter(messageAdapter);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("hasConnection", mSocket.getHasConnection());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();
        currentUsername = "";
        messageAdapter.clear();
    }
}
