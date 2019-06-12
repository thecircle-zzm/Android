package nl.thecirclezzm.streaming_library.encoder.input.audio;

/**
 * Created by pedro on 19/01/17.
 */

public interface GetMicrophoneData {

    void inputPCMData(byte[] buffer, int size);
}
