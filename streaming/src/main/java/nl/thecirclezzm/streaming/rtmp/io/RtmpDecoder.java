package nl.thecirclezzm.streaming.rtmp.io;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import nl.thecirclezzm.streaming.rtmp.packets.Abort;
import nl.thecirclezzm.streaming.rtmp.packets.Acknowledgement;
import nl.thecirclezzm.streaming.rtmp.packets.Audio;
import nl.thecirclezzm.streaming.rtmp.packets.Command;
import nl.thecirclezzm.streaming.rtmp.packets.Data;
import nl.thecirclezzm.streaming.rtmp.packets.RtmpHeader;
import nl.thecirclezzm.streaming.rtmp.packets.RtmpPacket;
import nl.thecirclezzm.streaming.rtmp.packets.SetChunkSize;
import nl.thecirclezzm.streaming.rtmp.packets.SetPeerBandwidth;
import nl.thecirclezzm.streaming.rtmp.packets.UserControl;
import nl.thecirclezzm.streaming.rtmp.packets.Video;
import nl.thecirclezzm.streaming.rtmp.packets.WindowAckSize;

/**
 * @author francois
 */
public class RtmpDecoder {

    private static final String TAG = "RtmpDecoder";

    private RtmpSessionInfo rtmpSessionInfo;

    public RtmpDecoder(RtmpSessionInfo rtmpSessionInfo) {
        this.rtmpSessionInfo = rtmpSessionInfo;
    }

    public RtmpPacket readPacket(InputStream in) throws IOException {

        RtmpHeader header = RtmpHeader.readHeader(in, rtmpSessionInfo);
        // Log.d(TAG, "readPacket(): header.messageType: " + header.getMessageType());

        ChunkStreamInfo chunkStreamInfo = rtmpSessionInfo.getChunkStreamInfo(header.getChunkStreamId());
        chunkStreamInfo.setPrevHeaderRx(header);

        if (header.getPacketLength() > rtmpSessionInfo.getRxChunkSize()) {
            // If the packet consists of more than one chunk,
            // store the chunks in the chunk stream until everything is read
            if (!chunkStreamInfo.storePacketChunk(in, rtmpSessionInfo.getRxChunkSize())) {
                // return null because of incomplete packet
                return null;
            } else {
                // stored chunks complete packet, get the input stream of the chunk stream
                in = chunkStreamInfo.getStoredPacketInputStream();
            }
        }

        RtmpPacket rtmpPacket;
        switch (header.getMessageType()) {
            case SET_CHUNK_SIZE:
                SetChunkSize setChunkSize = new SetChunkSize(header);
                setChunkSize.readBody(in);
                Log.d(TAG, "readPacket(): Setting chunk size to: " + setChunkSize.getChunkSize());
                rtmpSessionInfo.setRxChunkSize(setChunkSize.getChunkSize());
                return null;
            case ABORT:
                rtmpPacket = new Abort(header);
                break;
            case USER_CONTROL_MESSAGE:
                rtmpPacket = new UserControl(header);
                break;
            case WINDOW_ACKNOWLEDGEMENT_SIZE:
                rtmpPacket = new WindowAckSize(header);
                break;
            case SET_PEER_BANDWIDTH:
                rtmpPacket = new SetPeerBandwidth(header);
                break;
            case AUDIO:
                rtmpPacket = new Audio(header);
                break;
            case VIDEO:
                rtmpPacket = new Video(header);
                break;
            case COMMAND_AMF0:
                rtmpPacket = new Command(header);
                break;
            case DATA_AMF0:
                rtmpPacket = new Data(header);
                break;
            case ACKNOWLEDGEMENT:
                rtmpPacket = new Acknowledgement(header);
                break;
            default:
                throw new IOException(
                        "No packet body implementation for message type: " + header.getMessageType());
        }
        rtmpPacket.readBody(in);
        return rtmpPacket;
    }
}
