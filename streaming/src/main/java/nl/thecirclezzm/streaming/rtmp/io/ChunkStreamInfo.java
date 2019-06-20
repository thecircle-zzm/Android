package nl.thecirclezzm.streaming.rtmp.io;

import androidx.annotation.NonNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import nl.thecirclezzm.streaming.rtmp.Util;
import nl.thecirclezzm.streaming.rtmp.packets.RtmpHeader;

/**
 * Chunk stream channel information
 *
 * @author francois, leo
 */
public class ChunkStreamInfo {

    public static final byte RTMP_CID_PROTOCOL_CONTROL = 0x02;
    public static final byte RTMP_CID_OVER_CONNECTION = 0x03;
    public static final byte RTMP_CID_OVER_CONNECTION2 = 0x04;
    public static final byte RTMP_CID_OVER_STREAM = 0x05;
    public static final byte RTMP_CID_VIDEO = 0x06;
    public static final byte RTMP_CID_AUDIO = 0x07;
    private static long sessionBeginTimestamp;
    @NonNull
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream(1024 * 128);
    private RtmpHeader prevHeaderRx;
    private RtmpHeader prevHeaderTx;
    private long realLastTimestamp = System.nanoTime() / 1000000;  // Do not use wall time!

    /**
     * Sets the session beginning timestamp for all chunks
     */
    public static void markSessionTimestampTx() {
        sessionBeginTimestamp = System.nanoTime() / 1000000;
    }

    /**
     * @return the previous header that was received on this channel, or <code>null</code> if no previous header was received
     */
    public RtmpHeader getPrevHeaderRx() {
        return prevHeaderRx;
    }

    /**
     * Sets the previous header that was received on this channel, or <code>null</code> if no previous header was sent
     */
    public void setPrevHeaderRx(RtmpHeader previousHeader) {
        this.prevHeaderRx = previousHeader;
    }

    /**
     * @return the previous header that was transmitted on this channel
     */
    public RtmpHeader getPrevHeaderTx() {
        return prevHeaderTx;
    }

    /**
     * Sets the previous header that was transmitted on this channel
     */
    public void setPrevHeaderTx(RtmpHeader prevHeaderTx) {
        this.prevHeaderTx = prevHeaderTx;
    }

    public boolean canReusePrevHeaderTx(RtmpHeader.MessageType forMessageType) {
        return (prevHeaderTx != null && prevHeaderTx.getMessageType() == forMessageType);
    }

    /**
     * Utility method for calculating & synchronizing transmitted timestamps
     */
    public long markAbsoluteTimestampTx() {
        return System.nanoTime() / 1000000 - sessionBeginTimestamp;
    }

    /**
     * Utility method for calculating & synchronizing transmitted timestamp deltas
     */
    public long markDeltaTimestampTx() {
        long currentTimestamp = System.nanoTime() / 1000000;
        long diffTimestamp = currentTimestamp - realLastTimestamp;
        realLastTimestamp = currentTimestamp;
        return diffTimestamp;
    }

    /**
     * @return <code>true</code> if all packet data has been stored, or <code>false</code> if not
     */
    public boolean storePacketChunk(@NonNull InputStream in, int chunkSize) throws IOException {
        final int remainingBytes = prevHeaderRx.getPacketLength() - baos.size();
        byte[] chunk = new byte[Math.min(remainingBytes, chunkSize)];
        Util.readBytesUntilFull(in, chunk);
        baos.write(chunk);
        return (baos.size() == prevHeaderRx.getPacketLength());
    }

    @NonNull
    public ByteArrayInputStream getStoredPacketInputStream() {
        ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
        baos.reset();
        return bis;
    }

    /**
     * Clears all currently-stored packet chunks (used when an ABORT packet is received)
     */
    public void clearStoredChunks() {
        baos.reset();
    }
}
