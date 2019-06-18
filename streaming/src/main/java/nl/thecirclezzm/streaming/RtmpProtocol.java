package nl.thecirclezzm.streaming;

import android.media.MediaCodec;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

import nl.thecirclezzm.streaming.base.ConnectionCallbacks;
import nl.thecirclezzm.streaming.base.StreamStats;
import nl.thecirclezzm.streaming.base.StreamingProtocol;
import nl.thecirclezzm.streaming.encoder.video.VideoEncoder;
import nl.thecirclezzm.streaming.rtmp.ossrs.SrsFlvMuxer;

public class RtmpProtocol implements StreamingProtocol {
    private SrsFlvMuxer srsFlvMuxer;

    public RtmpProtocol(@NonNull ConnectionCallbacks connectChecker) {
        srsFlvMuxer = new SrsFlvMuxer(connectChecker);
    }

    /**
     * H264 profile.
     *
     * @param profileIop Could be ProfileIop.BASELINE or ProfileIop.CONSTRAINED
     */
    public void setProfileIop(byte profileIop) {
        srsFlvMuxer.setProfileIop(profileIop);
    }

    @Override
    public void resizeCache(int newSize) throws RuntimeException {
        srsFlvMuxer.resizeFlvTagCache(newSize);
    }

    @Override
    public int getCacheSize() {
        return srsFlvMuxer.getFlvTagCacheSize();
    }

    @Override
    public @NonNull
    StreamStats getStats() {
        return new Stats();
    }

    @Override
    public void setAuthorization(String user, String password) {
        srsFlvMuxer.setAuthorization(user, password);
    }

    @Override
    public void prepareAudio(boolean isStereo, int sampleRate) {
        srsFlvMuxer.setIsStereo(isStereo);
        srsFlvMuxer.setSampleRate(sampleRate);
    }

    @Override
    public void startStream(String url, VideoEncoder videoEncoder) {
        if (videoEncoder.getRotation() == 90 || videoEncoder.getRotation() == 270) {
            srsFlvMuxer.setVideoResolution(videoEncoder.getHeight(), videoEncoder.getWidth());
        } else {
            srsFlvMuxer.setVideoResolution(videoEncoder.getWidth(), videoEncoder.getHeight());
        }
        srsFlvMuxer.start(url);
    }

    @Override
    public void stopStream() {
        srsFlvMuxer.stop();
    }

    @Override
    public void setReTries(int reTries) {
        srsFlvMuxer.setReTries(reTries);
    }

    @Override
    public boolean shouldRetry(@NotNull @NonNull String reason) {
        return srsFlvMuxer.shouldRetry(reason);
    }

    @Override
    public void reConnect(long delay) {
        srsFlvMuxer.reConnect(delay);
    }

    @Override
    public void getAacData(ByteBuffer aacBuffer, MediaCodec.BufferInfo info) {
        srsFlvMuxer.sendAudio(aacBuffer, info);
    }

    @Override
    public void onSpsPpsVps(ByteBuffer sps, ByteBuffer pps, ByteBuffer vps) {
        srsFlvMuxer.setSpsPPs(sps, pps);
    }

    @Override
    public void getH264Data(ByteBuffer h264Buffer, MediaCodec.BufferInfo info) {
        srsFlvMuxer.sendVideo(h264Buffer, info);
    }

    private class Stats implements StreamStats {
        @Override
        public long getSentAudioFrames() {
            return srsFlvMuxer.getSentAudioFrames();
        }

        @Override
        public long getSentVideoFrames() {
            return srsFlvMuxer.getSentVideoFrames();
        }

        @Override
        public long getDroppedAudioFrames() {
            return srsFlvMuxer.getDroppedAudioFrames();
        }

        @Override
        public long getDroppedVideoFrames() {
            return srsFlvMuxer.getDroppedVideoFrames();
        }

        @Override
        public void resetSentAudioFrames() {
            srsFlvMuxer.resetSentAudioFrames();
        }

        @Override
        public void resetSentVideoFrames() {
            srsFlvMuxer.resetSentVideoFrames();
        }

        @Override
        public void resetDroppedAudioFrames() {
            srsFlvMuxer.resetDroppedAudioFrames();
        }

        @Override
        public void resetDroppedVideoFrames() {
            srsFlvMuxer.resetDroppedVideoFrames();
        }
    }
}

