package nl.thecirclezzm.seechangecamera.ui.login.model;

import androidx.annotation.NonNull;

import java.util.Objects;

public class User {
    @NonNull
    private String username;
    @NonNull
    private Chats chats;
    @NonNull
    private Streaming streaming;

    public User(@NonNull String username, @NonNull Chats chats, @NonNull Streaming streaming) {
        this.username = username;
        this.chats = chats;
        this.streaming = streaming;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    @NonNull
    public Chats getChats() {
        return chats;
    }

    @NonNull
    public Streaming getStreaming() {
        return streaming;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return username.equals(user.username) &&
                chats.equals(user.chats) &&
                streaming.equals(user.streaming);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, chats, streaming);
    }
}