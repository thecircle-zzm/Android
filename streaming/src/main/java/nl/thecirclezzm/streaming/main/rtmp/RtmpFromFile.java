package nl.thecirclezzm.streaming.main.rtmp;

import android.content.Context;
import android.media.MediaCodec;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.nio.ByteBuffer;

import nl.thecirclezzm.streaming.encoder.input.decoder.AudioDecoderInterface;
import nl.thecirclezzm.streaming.encoder.input.decoder.VideoDecoderInterface;
import nl.thecirclezzm.streaming.main.base.FromFileBase;
import nl.thecirclezzm.streaming.main.view.LightOpenGlView;
import nl.thecirclezzm.streaming.main.view.OpenGlView;
import nl.thecirclezzm.streaming.rtmp.ConnectCheckerRtmp;
import nl.thecirclezzm.streaming.rtmp.SrsFlvMuxer;

/**
 * More documentation see:
 * {@link nl.thecirclezzm.streaming.main.base.FromFileBase}
 * <p>
 * Created by pedro on 26/06/17.
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class RtmpFromFile extends FromFileBase {

    @NonNull
    private final SrsFlvMuxer srsFlvMuxer;

    public RtmpFromFile(ConnectCheckerRtmp connectChecker,
                        VideoDecoderInterface videoDecoderInterface, AudioDecoderInterface audioDecoderInterface) {
        super(videoDecoderInterface, audioDecoderInterface);
        srsFlvMuxer = new SrsFlvMuxer(connectChecker);
    }

    public RtmpFromFile(Context context, ConnectCheckerRtmp connectChecker,
                        VideoDecoderInterface videoDecoderInterface, AudioDecoderInterface audioDecoderInterface) {
        super(context, videoDecoderInterface, audioDecoderInterface);
        srsFlvMuxer = new SrsFlvMuxer(connectChecker);
    }

    public RtmpFromFile(@NonNull OpenGlView openGlView, ConnectCheckerRtmp connectChecker,
                        VideoDecoderInterface videoDecoderInterface, AudioDecoderInterface audioDecoderInterface) {
        super(openGlView, videoDecoderInterface, audioDecoderInterface);
        srsFlvMuxer = new SrsFlvMuxer(connectChecker);
    }

    public RtmpFromFile(@NonNull LightOpenGlView lightOpenGlView, ConnectCheckerRtmp connectChecker,
                        VideoDecoderInterface videoDecoderInterface, AudioDecoderInterface audioDecoderInterface) {
        super(lightOpenGlView, videoDecoderInterface, audioDecoderInterface);
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
        if (videoEncoder.getRotation() == 90 || videoEncoder.getRotation() == 270) {
            srsFlvMuxer.setVideoResolution(videoEncoder.getHeight(), videoEncoder.getWidth());
        } else {
            srsFlvMuxer.setVideoResolution(videoEncoder.getWidth(), videoEncoder.getHeight());
        }
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
    protected void onSpsPpsVpsRtp(ByteBuffer sps, ByteBuffer pps, ByteBuffer vps) {
        srsFlvMuxer.setSpsPPs(sps, pps);
    }

    @Override
    protected void getH264DataRtp(@NonNull ByteBuffer h264Buffer, @NonNull MediaCodec.BufferInfo info) {
        srsFlvMuxer.sendVideo(h264Buffer, info);
    }

    @Override
    protected void getAacDataRtp(@NonNull ByteBuffer aacBuffer, @NonNull MediaCodec.BufferInfo info) {
        srsFlvMuxer.sendAudio(aacBuffer, info);
    }
}
