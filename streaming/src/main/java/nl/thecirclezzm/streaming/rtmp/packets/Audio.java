package nl.thecirclezzm.streaming.rtmp.packets;

import androidx.annotation.NonNull;

import nl.thecirclezzm.streaming.rtmp.io.ChunkStreamInfo;

/**
 * Audio data packet
 *
 * @author francois
 */
public class Audio extends ContentData {

    public Audio(RtmpHeader header) {
        super(header);
    }

    public Audio() {
        super(new RtmpHeader(RtmpHeader.ChunkType.TYPE_0_FULL, ChunkStreamInfo.RTMP_CID_AUDIO,
                RtmpHeader.MessageType.AUDIO));
    }

    @NonNull
    @Override
    public String toString() {
        return "RTMP Audio";
    }
}
