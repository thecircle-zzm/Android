package nl.thecirclezzm.streaming.rtsp.rtsp;

import android.media.MediaCodec;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.thecirclezzm.streaming.base.ConnectionCallbacks;
import nl.thecirclezzm.streaming.rtsp.utils.CreateSSLSocket;

/**
 * Created by pedro on 10/02/17.
 */

public class RtspClient {

    private static final Pattern rtspUrlPattern =
            Pattern.compile("^rtsps?://([^/:]+)(?::(\\d+))*/([^/]+)/?([^*]*)$");
    private final String TAG = "RtspClient";
    private ConnectionCallbacks connectCheckerRtsp;
    //sockets objects
    private Socket connectionSocket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private Thread thread;
    //for tcp
    private OutputStream outputStream;
    private volatile boolean streaming = false;
    //for secure transport
    private boolean tlsEnabled = false;
    private RtspSender rtspSender;
    private CommandsManager commandsManager;
    private int numRetry;
    private int reTries;
    private Handler handler;
    private Runnable runnable;

    public RtspClient(@NonNull ConnectionCallbacks connectCheckerRtsp) {
        this.connectCheckerRtsp = connectCheckerRtsp;
        commandsManager = new CommandsManager();
        rtspSender = new RtspSender(connectCheckerRtsp);
        handler = new Handler(Looper.getMainLooper());
    }

    public void setProtocol(Protocol protocol) {
        commandsManager.setProtocol(protocol);
    }

    public void setAuthorization(String user, String password) {
        commandsManager.setAuth(user, password);
    }

    public void setReTries(int reTries) {
        numRetry = reTries;
        this.reTries = reTries;
    }

    public boolean shouldRetry(@NonNull String reason) {
        boolean validReason = !reason.contains("Endpoint malformed");
        return validReason && reTries > 0;
    }

    public boolean isStreaming() {
        return streaming;
    }

    public void setUrl(String url) {
        Matcher rtspMatcher = rtspUrlPattern.matcher(url);
        if (rtspMatcher.matches()) {
            tlsEnabled = rtspMatcher.group(0).startsWith("rtsps");
        } else {
            streaming = false;
            connectCheckerRtsp.onConnectionFailed(
                    "Endpoint malformed, should be: rtsp://ip:port/appname/streamname");
            return;
        }
        String host = rtspMatcher.group(1);
        int port = Integer.parseInt((rtspMatcher.group(2) != null) ? rtspMatcher.group(2) : "554");
        String path = "/" + rtspMatcher.group(3) + "/" + rtspMatcher.group(4);
        commandsManager.setUrl(host, port, path);
    }

    public void setSampleRate(int sampleRate) {
        commandsManager.setSampleRate(sampleRate);
    }

    public String getHost() {
        return commandsManager.getHost();
    }

    public int getPort() {
        return commandsManager.getPort();
    }

    public String getPath() {
        return commandsManager.getPath();
    }

    public ConnectionCallbacks getConnectChecker() {
        return connectCheckerRtsp;
    }

    public void setSPSandPPS(ByteBuffer sps, ByteBuffer pps, ByteBuffer vps) {
        commandsManager.setVideoInfo(sps, pps, vps);
    }

    public void setIsStereo(boolean isStereo) {
        commandsManager.setIsStereo(isStereo);
    }

    public void connect() {
        if (!streaming) {
            rtspSender.setInfo(commandsManager.getProtocol(), commandsManager.getSps(),
                    commandsManager.getPps(), commandsManager.getVps(), commandsManager.getSampleRate());
            thread = new Thread(() -> {
                try {
                    if (!tlsEnabled) {
                        connectionSocket = new Socket();
                        SocketAddress socketAddress =
                                new InetSocketAddress(commandsManager.getHost(), commandsManager.getPort());
                        connectionSocket.connect(socketAddress, 5000);
                    } else {
                        connectionSocket = CreateSSLSocket.createSSlSocket(commandsManager.getHost(),
                                commandsManager.getPort());
                        if (connectionSocket == null)
                            throw new IOException("Socket creation failed");
                    }
                    connectionSocket.setSoTimeout(5000);
                    reader = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                    outputStream = connectionSocket.getOutputStream();
                    writer = new BufferedWriter(new OutputStreamWriter(outputStream));
                    writer.write(commandsManager.createOptions());
                    writer.flush();
                    commandsManager.getResponse(reader, connectCheckerRtsp, false, false);
                    writer.write(commandsManager.createAnnounce());
                    writer.flush();
                    //check if you need credential for stream, if you need try connect with credential
                    String response = commandsManager.getResponse(reader, connectCheckerRtsp, false, false);
                    int status = commandsManager.getResponseStatus(response);
                    if (status == 403) {
                        connectCheckerRtsp.onConnectionFailed("Error configure stream, access denied");
                        Log.e(TAG, "Response 403, access denied");
                        return;
                    } else if (status == 401) {
                        if (commandsManager.getUser() == null || commandsManager.getPassword() == null) {
                            connectCheckerRtsp.onAuthError();
                            return;
                        } else {
                            writer.write(commandsManager.createAnnounceWithAuth(response));
                            writer.flush();
                            int statusAuth = commandsManager.getResponseStatus(
                                    commandsManager.getResponse(reader, connectCheckerRtsp, false, false));
                            if (statusAuth == 401) {
                                connectCheckerRtsp.onAuthError();
                                return;
                            } else if (statusAuth == 200) {
                                connectCheckerRtsp.onAuthSuccess();
                            } else {
                                connectCheckerRtsp.onConnectionFailed(
                                        "Error configure stream, announce with auth failed");
                            }
                        }
                    } else if (status != 200) {
                        connectCheckerRtsp.onConnectionFailed("Error configure stream, announce failed");
                    }
                    writer.write(commandsManager.createSetup(commandsManager.getTrackAudio()));
                    writer.flush();
                    commandsManager.getResponse(reader, connectCheckerRtsp, true, true);
                    writer.write(commandsManager.createSetup(commandsManager.getTrackVideo()));
                    writer.flush();
                    commandsManager.getResponse(reader, connectCheckerRtsp, false, true);
                    writer.write(commandsManager.createRecord());
                    writer.flush();
                    commandsManager.getResponse(reader, connectCheckerRtsp, false, true);

                    rtspSender.setDataStream(outputStream, commandsManager.getHost());
                    int[] videoPorts = commandsManager.getVideoPorts();
                    int[] audioPorts = commandsManager.getAudioPorts();
                    rtspSender.setVideoPorts(videoPorts[0], videoPorts[1]);
                    rtspSender.setAudioPorts(audioPorts[0], audioPorts[1]);
                    rtspSender.start();
                    streaming = true;
                    reTries = numRetry;
                    connectCheckerRtsp.onConnectionSuccess();
                } catch (IOException | NullPointerException e) {
                    Log.e(TAG, "connection error", e);
                    connectCheckerRtsp.onConnectionFailed("Error configure stream, " + e.getMessage());
                    streaming = false;
                }
            });
            thread.start();
        }
    }

    public void disconnect() {
        handler.removeCallbacks(runnable);
        disconnect(true);
    }

    private void disconnect(final boolean clear) {
        streaming = false;
        rtspSender.stop();
        thread = new Thread(() -> {
            try {
                if (writer != null) writer.write(commandsManager.createTeardown());
                if (connectionSocket != null) connectionSocket.close();
                writer = null;
                connectionSocket = null;
            } catch (IOException e) {
                Log.e(TAG, "disconnect error", e);
            }
        });
        thread.start();
        if (clear) {
            reTries = 0;
            commandsManager.clear();
            connectCheckerRtsp.onDisconnect();
        }
    }

    public void sendVideo(ByteBuffer h264Buffer, MediaCodec.BufferInfo info) {
        if (isStreaming()) {
            rtspSender.sendVideoFrame(h264Buffer, info);
        }
    }

    public void sendAudio(ByteBuffer aacBuffer, MediaCodec.BufferInfo info) {
        if (isStreaming()) {
            rtspSender.sendAudioFrame(aacBuffer, info);
        }
    }

    public void reConnect(long delay) {
        reTries--;
        disconnect(false);
        runnable = this::connect;
        handler.postDelayed(runnable, delay);
    }

    public long getDroppedAudioFrames() {
        return rtspSender.getDroppedAudioFrames();
    }

    public long getDroppedVideoFrames() {
        return rtspSender.getDroppedVideoFrames();
    }

    public void resetSentAudioFrames() {
        rtspSender.resetSentAudioFrames();
    }

    public void resetSentVideoFrames() {
        rtspSender.resetSentVideoFrames();
    }

    public void resetDroppedAudioFrames() {
        rtspSender.resetDroppedAudioFrames();
    }

    public void resetDroppedVideoFrames() {
        rtspSender.resetDroppedVideoFrames();
    }

    public void resizeCache(int newSize) throws RuntimeException {
        rtspSender.resizeCache(newSize);
    }

    public int getCacheSize() {
        return rtspSender.getCacheSize();
    }

    public long getSentAudioFrames() {
        return rtspSender.getSentAudioFrames();
    }

    public long getSentVideoFrames() {
        return rtspSender.getSentVideoFrames();
    }
}

