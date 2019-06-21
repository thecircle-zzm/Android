package nl.thecirclezzm.seechangecamera.ui.login.model;

import androidx.annotation.NonNull;

import java.util.Objects;

public class Streaming {
    @NonNull
    private String protocol;
    @NonNull
    private String domain;
    private int port;
    @NonNull
    private String appName;
    @NonNull
    private String streamName;

    public Streaming(@NonNull String protocol, @NonNull String domain, int port, @NonNull String appName, @NonNull String streamName) {
        this.protocol = protocol;
        this.domain = domain;
        this.port = port;
        this.appName = appName;
        this.streamName = streamName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Streaming streaming = (Streaming) o;
        return port == streaming.port &&
                protocol.equals(streaming.protocol) &&
                domain.equals(streaming.domain) &&
                appName.equals(streaming.appName) &&
                streamName.equals(streaming.streamName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(protocol, domain, port, appName, streamName);
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
    public String getAppName() {
        return appName;
    }

    @NonNull
    public String getStreamName() {
        return streamName;
    }
}