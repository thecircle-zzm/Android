package nl.thecirclezzm.seechangecamera.utils;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class SocketIO {
    private final static String TAG = "SocketIO";
    private final String url;
    private Socket socket;
    private boolean hasConnection;

    public SocketIO(@NonNull String url) {
        this.url = url;
        this.hasConnection = false;
    }

    public void connect() throws URISyntaxException {
        log("Connecting...");

        if(socket == null){
            socket = IO.socket(url);
        }

        socket.connect();
        hasConnection = true;

        log("Connected.");
    }

    public void disconnect(){
        log("Disconnecting...");

        if(socket != null) {
            socket.disconnect();
            socket.off();
            hasConnection = false;
        }

        socket = null;

        log("Disconnected.");
    }

    public interface Callback {
        void onNewEvent(@Nullable Object... args);
    }

    public interface JSONCallback {
        void onJSONObjectReceived(@NonNull JSONObject object) throws JSONException;
    }

    public void onNewJsonEvent(@NonNull String eventName, @NonNull JSONCallback callback){
        Emitter.Listener listener = (args) -> {
            if(args.length > 0 && args[0] instanceof JSONObject){
                try {
                    JSONObject json = (JSONObject) args[0];
                    log("New JSON object received:\n" + json.toString(4));
                    callback.onJSONObjectReceived(json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        socket.on(eventName, listener);
    }

    public void onNewEvent(@NonNull String eventName, @NonNull Callback callback){
        Emitter.Listener listener = callback::onNewEvent;
        socket.on(eventName, listener);
    }

    public void sendJSON(@NonNull String event, @NonNull JSONObject jsonObject){
        try {
            log("Sending JSON:\n" + jsonObject.toString(4));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        sendText(event, jsonObject.toString());
    }

    public void sendText(@NonNull String event, @NonNull String text){
        socket.emit(event, text);
    }

    public @NonNull String getUrl() {
        return url;
    }

    public boolean getHasConnection() {
        return hasConnection;
    }

    private static void log(@NonNull String s) {
        Log.i(TAG, s);
    }
}
