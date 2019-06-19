package nl.thecirclezzm.seechangecamera.ui.chat;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import nl.thecirclezzm.seechangecamera.R;

public class MessageAdapter extends ArrayAdapter<Message> {
    public MessageAdapter(@NonNull Context context, int resource, @NonNull List<Message> objects) {
        super(context, resource, objects);
    }

    @Override
    public @NonNull
    View getView(int position, @Nullable View convertView, @Nullable ViewGroup parent) {
        Message message = getItem(position);

        @LayoutRes int layoutId;

        if (message.getType() == Message.MessageType.CHANNEL) {
            layoutId = R.layout.user_connected;
        } else if (message.getType() == Message.MessageType.SENT) {
            layoutId = R.layout.my_message;
        } else {
            layoutId = R.layout.their_message;
        }

        convertView = ((Activity) getContext()).getLayoutInflater().inflate(layoutId, parent, false);
        AppCompatTextView messageText = convertView.findViewById(R.id.message_body);
        messageText.setText(message.getMessage());

        if(message.getType() == Message.MessageType.RECEIVED){
            AppCompatTextView usernameText = convertView.findViewById(R.id.name);
            AppCompatTextView avatar = convertView.findViewById(R.id.avatar);
            usernameText.setText(message.getUsername());
            avatar.setText(String.valueOf(message.getUsername().toUpperCase(Locale.getDefault()).charAt(0)));
        }

        return convertView;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }
}
