package nl.thecirclezzm.streaming;

import android.media.MediaCodec;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

import nl.thecirclezzm.streaming.base.ConnectionCallbacks;
import nl.thecirclezzm.streaming.base.StreamStats;
import nl.thecirclezzm.streaming.base.StreamingProtocol;
import nl.thecirclezzm.streaming.encoder.utils.CodecUtil;
import nl.thecirclezzm.streaming.encoder.video.VideoEncoder;
import nl.thecirclezzm.streaming.rtsp.rtsp.Protocol;
import nl.thecirclezzm.streaming.rtsp.rtsp.RtspClient;
import nl.thecirclezzm.streaming.rtsp.rtsp.VideoCodec;

public class RtspProtocol implements StreamingProtocol {
    private RtspClient rtspClient;

    public RtspProtocol(@NonNull ConnectionCallbacks connectCheckerRtsp) {
        rtspClient = new RtspClient(connectCheckerRtsp);
    }

    /**
     * Internet protocol used.
     *
     * @param protocol Could be Protocol.TCP or Protocol.UDP.
     */
    public void setProtocol(Protocol protocol) {
        rtspClient.setProtocol(protocol);
    }

    @Override
    public void resizeCache(int newSize) throws RuntimeException {
        rtspClient.resizeCache(newSize);
    }

    @Override
    public int getCacheSize() {
        return rtspClient.getCacheSize();
    }

    @Override
    public @NonNull
    StreamStats getStats() {
        return new Stats();
    }

    public void setVideoCodec(VideoEncoder videoEncoder, VideoCodec videoCodec) {
        videoEncoder.setType(videoCodec == VideoCodec.H265 ? CodecUtil.H265_MIME : CodecUtil.H264_MIME);
    }

    @Override
    public void setAuthorization(String user, String password) {
        rtspClient.setAuthorization(user, password);
    }

    @Override
    public void prepareAudio(boolean isStereo, int sampleRate) {
        rtspClient.setIsStereo(isStereo);
        rtspClient.setSampleRate(sampleRate);
    }

    @Override
    public void startStream(String url, VideoEncoder videoEncoder) {
        rtspClient.setUrl(url);
    }

    @Override
    public void stopStream() {
        rtspClient.disconnect();
    }

    @Override
    public void setReTries(int reTries) {
        rtspClient.setReTries(reTries);
    }

    @Override
    public boolean shouldRetry(@NotNull String reason) {
        return rtspClient.shouldRetry(reason);
    }

    @Override
    public void reConnect(long delay) {
        rtspClient.reConnect(delay);
    }

    @Override
    public void getAacData(ByteBuffer aacBuffer, MediaCodec.BufferInfo info) {
        rtspClient.sendAudio(aacBuffer, info);
    }

    @Override
    public void onSpsPpsVps(ByteBuffer sps, ByteBuffer pps, ByteBuffer vps) {
        ByteBuffer newSps = sps.duplicate();
        ByteBuffer newPps = pps.duplicate();
        ByteBuffer newVps = vps != null ? vps.duplicate() : null;
        rtspClient.setSPSandPPS(newSps, newPps, newVps);
        rtspClient.connect();
    }

    @Override
    public void getH264Data(ByteBuffer h264Buffer, MediaCodec.BufferInfo info) {
        rtspClient.sendVideo(h264Buffer, info);
    }

    private class Stats implements StreamStats {
        @Override
        public long getSentAudioFrames() {
            return rtspClient.getSentAudioFrames();
        }

        @Override
        public long getSentVideoFrames() {
            return rtspClient.getSentVideoFrames();
        }

        @Override
        public long getDroppedAudioFrames() {
            return rtspClient.getDroppedAudioFrames();
        }

        @Override
        public long getDroppedVideoFrames() {
            return rtspClient.getDroppedVideoFrames();
        }

        @Override
        public void resetSentAudioFrames() {
            rtspClient.resetSentAudioFrames();
        }

        @Override
        public void resetSentVideoFrames() {
            rtspClient.resetSentVideoFrames();
        }

        @Override
        public void resetDroppedAudioFrames() {
            rtspClient.resetDroppedAudioFrames();
        }

        @Override
        public void resetDroppedVideoFrames() {
            rtspClient.resetDroppedVideoFrames();
        }
    }
}

