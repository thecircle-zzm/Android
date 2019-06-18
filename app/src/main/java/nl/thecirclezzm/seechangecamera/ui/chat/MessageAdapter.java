package nl.thecirclezzm.seechangecamera.ui.chat;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import nl.thecirclezzm.seechangecamera.R;

public class MessageAdapter extends ArrayAdapter<MessageFormat> {
    public MessageAdapter(@NonNull Context context, int resource, @NonNull List<MessageFormat> objects) {
        super(context, resource, objects);
    }

    @Override
    public @NonNull
    View getView(int position, @Nullable View convertView, @Nullable ViewGroup parent) {
        Log.i(ChatsFragment.TAG, "getView:");

        MessageFormat message = getItem(position);

        if (TextUtils.isEmpty(message.getMessage())) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.user_connected, parent, false);

            AppCompatTextView messageText = convertView.findViewById(R.id.message_body);

            Log.i(ChatsFragment.TAG, "getView: is empty ");
            String userConnected = message.getUsername();
            messageText.setText(userConnected);

        } else if (Objects.equals(message.getUniqueId(), ChatsFragment.uniqueId)) {
            Log.i(ChatsFragment.TAG, "getView: " + message.getUniqueId() + " " + ChatsFragment.uniqueId);

            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.my_message, parent, false);
            AppCompatTextView messageText = convertView.findViewById(R.id.message_body);
            messageText.setText(message.getMessage());

        } else {
            Log.i(ChatsFragment.TAG, "getView: is not empty");

            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.their_message, parent, false);

            AppCompatTextView messageText = convertView.findViewById(R.id.message_body);
            AppCompatTextView usernameText = convertView.findViewById(R.id.name);
            AppCompatTextView avatar = convertView.findViewById(R.id.avatar);

            messageText.setText(message.getMessage());
            usernameText.setText(message.getUsername());
            avatar.setText(message.getUsername().toUpperCase(Locale.getDefault()).charAt(0));
        }

        return convertView;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }
}
