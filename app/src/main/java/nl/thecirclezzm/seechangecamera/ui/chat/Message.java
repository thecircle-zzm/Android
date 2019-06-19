package nl.thecirclezzm.seechangecamera.ui.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public class Message {
    private final String username;
    private final String message;
    private final String room;
    private final MessageType type;

    enum MessageType { SENT, RECEIVED, CHANNEL }

    Message(@NonNull String username, @NonNull String message, @NonNull String room, @NonNull MessageType messageType) {
        this.username = username;
        this.message = message;
        this.room = room;
        this.type = messageType;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    @NonNull
    public String getMessage() {
        return message;
    }

    @NonNull
    public String getRoom() {
        return room;
    }

    @NonNull
    public MessageType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message that = (Message) o;
        return username.equals(that.username) &&
                Objects.equals(message, that.message) &&
                Objects.equals(room, that.room);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, message, room);
    }
}
