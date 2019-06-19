package nl.thecirclezzm.streaming.base;

public interface StreamStats {
    long getSentAudioFrames();

    long getSentVideoFrames();

    long getDroppedAudioFrames();

    long getDroppedVideoFrames();

    void resetSentAudioFrames();

    void resetSentVideoFrames();

    void resetDroppedAudioFrames();

    void resetDroppedVideoFrames();
}
