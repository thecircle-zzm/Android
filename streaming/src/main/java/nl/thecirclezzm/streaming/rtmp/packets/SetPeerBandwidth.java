package nl.thecirclezzm.streaming.rtmp.packets;

import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import nl.thecirclezzm.streaming.rtmp.Util;
import nl.thecirclezzm.streaming.rtmp.io.ChunkStreamInfo;

/**
 * Set Peer Bandwidth
 * <p>
 * Also known as ClientrBW ("client bandwidth") in some RTMP implementations.
 *
 * @author francois
 */
public class SetPeerBandwidth extends RtmpPacket {

    private int acknowledgementWindowSize;
    private LimitType limitType;

    public SetPeerBandwidth(RtmpHeader header) {
        super(header);
    }

    public SetPeerBandwidth(int acknowledgementWindowSize, LimitType limitType,
                            ChunkStreamInfo channelInfo) {
        super(new RtmpHeader(channelInfo.canReusePrevHeaderTx(RtmpHeader.MessageType.SET_PEER_BANDWIDTH)
                ? RtmpHeader.ChunkType.TYPE_2_RELATIVE_TIMESTAMP_ONLY : RtmpHeader.ChunkType.TYPE_0_FULL,
                ChunkStreamInfo.RTMP_CID_PROTOCOL_CONTROL,
                RtmpHeader.MessageType.WINDOW_ACKNOWLEDGEMENT_SIZE));
        this.acknowledgementWindowSize = acknowledgementWindowSize;
        this.limitType = limitType;
    }

    public int getAcknowledgementWindowSize() {
        return acknowledgementWindowSize;
    }

    public void setAcknowledgementWindowSize(int acknowledgementWindowSize) {
        this.acknowledgementWindowSize = acknowledgementWindowSize;
    }

    public LimitType getLimitType() {
        return limitType;
    }

    public void setLimitType(LimitType limitType) {
        this.limitType = limitType;
    }

    @Override
    public void readBody(@NonNull InputStream in) throws IOException {
        acknowledgementWindowSize = Util.readUnsignedInt32(in);
        limitType = LimitType.valueOf(in.read());
    }

    @Override
    protected void writeBody(@NonNull OutputStream out) throws IOException {
        Util.writeUnsignedInt32(out, acknowledgementWindowSize);
        out.write(limitType.getIntValue());
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

    @NonNull
    @Override
    public String toString() {
        return "RTMP Set Peer Bandwidth";
    }

    /**
     * Bandwidth limiting type
     */
    public enum LimitType {

        /**
         * In a hard (0) request, the peer must send the data in the provided bandwidth.
         */
        HARD(0),
        /**
         * In a soft (1) request, the bandwidth is at the discretion of the peer
         * and the sender can limit the bandwidth.
         */
        SOFT(1),
        /**
         * In a dynamic (2) request, the bandwidth can be hard or soft.
         */
        DYNAMIC(2);
        private static final SparseArray<LimitType> quickLookupMap = new SparseArray<>();

        static {
            for (LimitType type : LimitType.values()) {
                quickLookupMap.put(type.getIntValue(), type);
            }
        }

        private int intValue;

        LimitType(int intValue) {
            this.intValue = intValue;
        }

        public static LimitType valueOf(int intValue) {
            return quickLookupMap.get(intValue);
        }

        public int getIntValue() {
            return intValue;
        }
    }
}
