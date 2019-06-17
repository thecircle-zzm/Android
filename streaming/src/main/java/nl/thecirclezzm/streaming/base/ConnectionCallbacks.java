package nl.thecirclezzm.streaming.base;

public interface ConnectionCallbacks {
    void onConnectionSuccess();

    void onConnectionFailed(String reason);

    void onDisconnect();

    void onAuthError();

    void onAuthSuccess();
}
