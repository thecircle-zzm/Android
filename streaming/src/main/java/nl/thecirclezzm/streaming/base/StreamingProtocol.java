package nl.thecirclezzm.streaming.base;

import android.media.MediaCodec;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.nio.ByteBuffer;

import nl.thecirclezzm.streaming.encoder.video.VideoEncoder;

public interface StreamingProtocol {
    void resizeCache(int newSize) throws RuntimeException;

    int getCacheSize();

    @NonNull
    StreamStats getStats();

    void setAuthorization(String user, String password);

    void prepareAudio(boolean isStereo, int sampleRate);

    void startStream(String url, VideoEncoder videoEncoder);

    void stopStream();

    void setReTries(int reTries);

    boolean shouldRetry(@NonNull String reason);

    void reConnect(long delay);

    void getAacData(ByteBuffer aacBuffer, MediaCodec.BufferInfo info);

    void onSpsPpsVps(ByteBuffer sps, ByteBuffer pps, @Nullable ByteBuffer vps);

    void getH264Data(ByteBuffer h264Buffer, MediaCodec.BufferInfo info);
}
