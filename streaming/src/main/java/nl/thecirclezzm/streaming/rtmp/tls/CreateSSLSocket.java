package nl.thecirclezzm.streaming.rtmp.tls;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by pedro on 25/02/17.
 * <p>
 * this class is used for secure transport, to use replace socket on RtmpConnection with this and
 * you will have a secure stream under ssl/tls.
 */

public class CreateSSLSocket {

    /**
     * @param host variable from RtmpConnection
     * @param port variable from RtmpConnection
     */
    public static Socket createSSlSocket(String host, int port) {
        try {
            TLSSocketFactory socketFactory = new TLSSocketFactory();
            return socketFactory.createSocket(host, port);
        } catch (@NonNull NoSuchAlgorithmException | KeyManagementException | IOException e) {
            Log.e("CreateSSLSocket", "Error", e);
            return null;
        }
    }
}
