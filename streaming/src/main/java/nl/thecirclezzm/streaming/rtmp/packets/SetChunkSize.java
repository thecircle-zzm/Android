package nl.thecirclezzm.streaming.rtmp.packets;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import nl.thecirclezzm.streaming.rtmp.Util;
import nl.thecirclezzm.streaming.rtmp.io.ChunkStreamInfo;

/**
 * A "Set chunk size" RTMP message, received on chunk stream ID 2 (control channel)
 *
 * @author francois
 */
public class SetChunkSize extends RtmpPacket {

    private int chunkSize;

    public SetChunkSize(RtmpHeader header) {
        super(header);
    }

    public SetChunkSize(int chunkSize) {
        super(new RtmpHeader(RtmpHeader.ChunkType.TYPE_1_RELATIVE_LARGE,
                ChunkStreamInfo.RTMP_CID_PROTOCOL_CONTROL, RtmpHeader.MessageType.SET_CHUNK_SIZE));
        this.chunkSize = chunkSize;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    @Override
    public void readBody(@NonNull InputStream in) throws IOException {
        // Value is received in the 4 bytes of the body
        chunkSize = Util.readUnsignedInt32(in);
    }

    @Override
    protected void writeBody(@NonNull OutputStream out) throws IOException {
        Util.writeUnsignedInt32(out, chunkSize);
    }

    @Nullable
    @Override
    protected byte[] array() {
        return null;
    }

    @Override
    protected int size() {
        return 0;
    }
}
