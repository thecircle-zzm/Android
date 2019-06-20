package nl.thecirclezzm.streaming.main.rtmp;

import android.media.MediaCodec;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;

import nl.thecirclezzm.streaming.main.base.OnlyAudioBase;
import nl.thecirclezzm.streaming.rtmp.ConnectCheckerRtmp;
import nl.thecirclezzm.streaming.rtmp.SrsFlvMuxer;

/**
 * More documentation see:
 * {@link nl.thecirclezzm.streaming.main.base.OnlyAudioBase}
 * <p>
 * Created by pedro on 10/07/18.
 */
public class RtmpOnlyAudio extends OnlyAudioBase {

    @NonNull
    private final SrsFlvMuxer srsFlvMuxer;

    public RtmpOnlyAudio(ConnectCheckerRtmp connectChecker) {
        super();
        srsFlvMuxer = new SrsFlvMuxer(connectChecker);
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

    @Override
    public void setAuthorization(String user, String password) {
        srsFlvMuxer.setAuthorization(user, password);
    }

    @Override
    protected void prepareAudioRtp(boolean isStereo, int sampleRate) {
        srsFlvMuxer.setIsStereo(isStereo);
        srsFlvMuxer.setSampleRate(sampleRate);
    }

    @Override
    protected void startStreamRtp(@NonNull String url) {
        srsFlvMuxer.start(url);
    }

    @Override
    protected void stopStreamRtp() {
        srsFlvMuxer.stop();
    }

    @Override
    public void setReTries(int reTries) {
        srsFlvMuxer.setReTries(reTries);
    }

    @Override
    public boolean shouldRetry(@NonNull String reason) {
        return srsFlvMuxer.shouldRetry(reason);
    }

    @Override
    public void reConnect(long delay) {
        srsFlvMuxer.reConnect(delay);
    }

    @Override
    protected void getAacDataRtp(@NonNull ByteBuffer aacBuffer, @NonNull MediaCodec.BufferInfo info) {
        srsFlvMuxer.sendAudio(aacBuffer, info);
    }
}
