package nl.thecirclezzm.streaming_library.rtsp.rtp.packets;

import nl.thecirclezzm.streaming_library.rtsp.rtsp.RtpFrame;

/**
 * Created by pedro on 7/11/18.
 */

public interface AudioPacketCallback {
    void onAudioFrameCreated(RtpFrame rtpFrame);
}
