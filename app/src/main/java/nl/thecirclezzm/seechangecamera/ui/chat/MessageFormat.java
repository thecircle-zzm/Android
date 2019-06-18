package nl.thecirclezzm.seechangecamera.ui.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public class MessageFormat {
    private final String username;
    private final String message;
    private final String room;
    private final String uniqueId;

    MessageFormat(@Nullable String uniqueId, @NonNull String username, @Nullable String message, @Nullable String room) {
        this.username = username;
        this.message = message;
        this.uniqueId = uniqueId;
        this.room = room;
    }

    @NonNull
    String getUsername() {
        return username;
    }

    @Nullable
    String getMessage() {
        return message;
    }

    @Nullable
    String getUniqueId() {
        return uniqueId;
    }

    @Nullable
    String getRoom() {
        return room;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageFormat that = (MessageFormat) o;
        return username.equals(that.username) &&
                Objects.equals(message, that.message) &&
                Objects.equals(uniqueId, that.uniqueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, message, uniqueId);
    }
}
