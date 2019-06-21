package nl.thecirclezzm.seechangecamera.ui.login.model;

import androidx.annotation.NonNull;

import java.util.Objects;

public class Chats {
    @NonNull
    private String protocol;
    @NonNull
    private String domain;
    private int port;
    @NonNull
    private String room;

    public Chats(@NonNull String protocol, @NonNull String domain, int port, @NonNull String room) {
        this.protocol = protocol;
        this.domain = domain;
        this.port = port;
        this.room = room;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chats chats = (Chats) o;
        return port == chats.port &&
                protocol.equals(chats.protocol) &&
                domain.equals(chats.domain) &&
                room.equals(chats.room);
    }

    @Override
    public int hashCode() {
        return Objects.hash(protocol, domain, port, room);
    }

    @NonNull
    public String getProtocol() {
        return protocol;
    }

    @NonNull
    public String getDomain() {
        return domain;
    }

    public int getPort() {
        return port;
    }

    @NonNull
    public String getRoom() {
        return room;
    }
}