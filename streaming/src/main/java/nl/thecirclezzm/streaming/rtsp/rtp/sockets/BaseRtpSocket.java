package nl.thecirclezzm.streaming.rtsp.rtp.sockets;

import java.io.IOException;
import java.io.OutputStream;

import nl.thecirclezzm.streaming.rtsp.rtsp.Protocol;
import nl.thecirclezzm.streaming.rtsp.rtsp.RtpFrame;

/**
 * Created by pedro on 7/11/18.
 */

public abstract class BaseRtpSocket {

    protected final static String TAG = "BaseRtpSocket";

    public static BaseRtpSocket getInstance(Protocol protocol) {
        return protocol == Protocol.TCP ? new RtpSocketTcp() : new RtpSocketUdp();
    }

    public abstract void setDataStream(OutputStream outputStream, String host);

    public abstract void sendFrame(RtpFrame rtpFrame) throws IOException;

    public abstract void close();
}
