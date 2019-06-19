package nl.thecirclezzm.streaming;

import android.Manifest;
import android.content.Context;
import android.hardware.camera2.CameraCharacteristics;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import java.io.IOException;
import java.nio.ByteBuffer;

import nl.thecirclezzm.streaming.base.ConnectionCallbacks;
import nl.thecirclezzm.streaming.base.StreamStats;
import nl.thecirclezzm.streaming.base.StreamingProtocol;
import nl.thecirclezzm.streaming.encoder.audio.AudioEncoder;
import nl.thecirclezzm.streaming.encoder.audio.GetAacData;
import nl.thecirclezzm.streaming.encoder.input.audio.GetMicrophoneData;
import nl.thecirclezzm.streaming.encoder.input.audio.MicrophoneManager;
import nl.thecirclezzm.streaming.encoder.input.video.CameraApiManager;
import nl.thecirclezzm.streaming.encoder.input.video.CameraHelper;
import nl.thecirclezzm.streaming.encoder.input.video.CameraOpenException;
import nl.thecirclezzm.streaming.encoder.utils.CodecUtil;
import nl.thecirclezzm.streaming.encoder.video.FormatVideoEncoder;
import nl.thecirclezzm.streaming.encoder.video.GetVideoData;
import nl.thecirclezzm.streaming.encoder.video.VideoEncoder;

/**
 * Wrapper to stream with camera2 api and microphone. Support stream with SurfaceView, TextureView.
 * All views use Surface to buffer encoding mode for H264.
 */
public class StreamingCamera implements GetAacData, GetVideoData, GetMicrophoneData, LifecycleObserver {
    public static final int ORIENTATION_LANDSCAPE = 0;
    public static final int ORIENTATION_PORTRAIT = 90;
    public static final int ORIENTATION_LANDSCAPE_INV = 180;
    public static final int ORIENTATION_PORTRAIT_INV = 270;
    @NonNull
    private Context context;
    private VideoEncoder videoEncoder;
    private StreamingProtocol streamingProtocol;
    private CameraApiManager cameraManager;
    private MicrophoneManager microphoneManager;
    private AudioEncoder audioEncoder;
    private boolean streaming = false;
    private SurfaceView surfaceView;
    private TextureView textureView;
    private boolean videoEnabled = false;
    private boolean onPreview = false;
    private boolean isBackground = false;
    private RecordController recordController;
    private int previewWidth, previewHeight;

    @RequiresPermission(value = Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public StreamingCamera(@NonNull SurfaceView surfaceView, @NonNull StreamingProtocol streamingProtocol) {
        this.streamingProtocol = streamingProtocol;
        this.surfaceView = surfaceView;
        this.context = surfaceView.getContext().getApplicationContext();
        init();
    }

    @RequiresPermission(value = Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public StreamingCamera(@NonNull TextureView textureView, @NonNull StreamingProtocol streamingProtocol) {
        this.streamingProtocol = streamingProtocol;
        this.textureView = textureView;
        this.context = textureView.getContext().getApplicationContext();
        init();
    }

    @RequiresPermission(value = Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public StreamingCamera(@NonNull SurfaceView surfaceView, @NonNull Protocol streamingProtocol, @NonNull ConnectionCallbacks connectionCallbacks) {
        this.streamingProtocol = new RtmpProtocol(connectionCallbacks);
        this.surfaceView = surfaceView;
        this.context = surfaceView.getContext().getApplicationContext();
        init();
    }

    @RequiresPermission(value = Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public StreamingCamera(@NonNull TextureView textureView, @NonNull Protocol streamingProtocol, @NonNull ConnectionCallbacks connectionCallbacks) {
        this.streamingProtocol = new RtmpProtocol(connectionCallbacks);
        this.textureView = textureView;
        this.context = textureView.getContext().getApplicationContext();
        init();
    }

    private void init() {
        cameraManager = new CameraApiManager(this.context);
        videoEncoder = new VideoEncoder(this);
        microphoneManager = new MicrophoneManager(this);
        audioEncoder = new AudioEncoder(this);
        recordController = new RecordController();
    }

    public boolean isFrontCamera() {
        return cameraManager.isFrontCamera();
    }

    /**
     * @required: <uses-permission android:name="android.permission.FLASHLIGHT"/>
     */
    @NonNull
    public Latern getLatern() {
        return new Latern();
    }

    public class Latern {
        public void enable() throws Exception {
            cameraManager.enableLantern();
        }

        public void disable() {
            cameraManager.disableLantern();
        }

        public boolean isEnabled() {
            return cameraManager.isLanternEnabled();
        }

        public boolean isSupported() {
            return cameraManager.isLanternSupported();
        }
    }

    /**
     * Basic auth developed to work with Wowza. No tested with other server
     *
     * @param user     auth.
     * @param password auth.
     */
    public void setAuthorization(String user, String password) {
        streamingProtocol.setAuthorization(user, password);
    }

    /**
     * Call this method before use @startStream. If not you will do a stream without video.
     *
     * @param sizePixels       resolution in px.
     * @param fps              frames per second of the stream.
     * @param bitrate          H264 in kb.
     * @param hardwareRotation true if you want rotate using encoder, false if you with OpenGl if you
     *                         are using OpenGlView.
     * @param rotation         could be 90, 180, 270 or 0 (Normally 0 if you are streaming in landscape or 90
     *                         if you are streaming in Portrait). This only affect to stream result. NOTE: Rotation with
     *                         encoder is silence ignored in some devices.
     * @return true if success, false if you get a error (Normally because the encoder selected
     * doesn't support any configuration seated or your device hasn't a H264 encoder).
     */
    @RequiresPermission(value = Manifest.permission.CAMERA)
    public boolean prepareVideo(@NonNull SizePixels sizePixels, int fps, int bitrate, boolean hardwareRotation,
                                int iFrameInterval, int rotation) {
        if (onPreview && !(sizePixels.getWidth() == previewWidth && sizePixels.getHeight() == previewHeight)) {
            stopPreview();
            onPreview = true;
        }
        boolean result =
                videoEncoder.prepareVideoEncoder(sizePixels, fps, bitrate, rotation, hardwareRotation,
                        iFrameInterval, FormatVideoEncoder.SURFACE);
        prepareCameraManager();
        return result;
    }

    /**
     * backward compatibility reason
     */
    @RequiresPermission(value = Manifest.permission.CAMERA)
    public boolean prepareVideo(@NonNull SizePixels sizePixels, int fps, int bitrate, boolean hardwareRotation,
                                int rotation) {
        return prepareVideo(sizePixels, fps, bitrate, hardwareRotation, 2, rotation);
    }

    /**
     * Call this method before use @startStream. If not you will do a stream without audio.
     *
     * @param bitrate         AAC in kb.
     * @param sampleRate      of audio in hz. Can be 8000, 16000, 22500, 32000, 44100.
     * @param isStereo        true if you want Stereo audio (2 audio channels), false if you want Mono audio
     *                        (1 audio channel).
     * @param echoCanceler    true enable echo canceler, false disable.
     * @param noiseSuppressor true enable noise suppressor, false  disable.
     * @return true if success, false if you get a error (Normally because the encoder selected
     * doesn't support any configuration seated or your device hasn't a AAC encoder).
     */
    @RequiresPermission(value = Manifest.permission.RECORD_AUDIO)
    public boolean prepareAudio(int bitrate, @NonNull SampleRate sampleRate, boolean isStereo, boolean echoCanceler,
                                boolean noiseSuppressor) {
        microphoneManager.createMicrophone(sampleRate.n, isStereo, echoCanceler, noiseSuppressor);
        streamingProtocol.prepareAudio(isStereo, sampleRate.n);
        return audioEncoder.prepareAudioEncoder(bitrate, sampleRate.n, isStereo);
    }

    /**
     * Same to call: isHardwareRotation = true; if (openGlVIew) isHardwareRotation = false;
     * prepareVideo((640, 480), 30, 1200 * 1024, isHardwareRotation, 90);
     *
     * @return true if success, false if you get a error (Normally because the encoder selected
     * doesn't support any configuration seated or your device hasn't a H264 encoder).
     */
    @RequiresPermission(value = Manifest.permission.CAMERA)
    public boolean prepareVideo() {
        int rotation = CameraHelper.getCameraOrientation(context);
        return prepareVideo(new SizePixels(640, 480), 30, 1200 * 1024, true, rotation);
    }

    /**
     * Same to call: prepareAudio(64 * 1024, 32000, true, false, false);
     *
     * @return true if success, false if you get a error (Normally because the encoder selected
     * doesn't support any configuration seated or your device hasn't a AAC encoder).
     */
    @RequiresPermission(value = Manifest.permission.RECORD_AUDIO)
    public boolean prepareAudio() {
        return prepareAudio(64 * 1024, SampleRate.sr32, true, false, false);
    }

    /**
     * @param forceVideo force type codec used. FIRST_COMPATIBLE_FOUND, SOFTWARE, HARDWARE
     * @param forceAudio force type codec used. FIRST_COMPATIBLE_FOUND, SOFTWARE, HARDWARE
     */
    public void setForce(@NonNull CodecUtil.Force forceVideo, @NonNull CodecUtil.Force forceAudio) {
        videoEncoder.setForce(forceVideo);
        audioEncoder.setForce(forceAudio);
    }

    /**
     * Start record a MP4 video. Need be called while stream.
     *
     * @param path where file will be saved.
     * @throws IOException If you init it before start stream.
     */
    public void startRecord(@NonNull String path, @Nullable RecordController.Listener listener) throws IOException {
        recordController.startRecord(path, listener);
        if (!streaming) {
            startEncoders();
        } else if (videoEncoder.isRunning()) {
            resetVideoEncoder();
        }
    }

    public void startRecord(final @NonNull String path) throws IOException {
        startRecord(path, null);
    }

    /**
     * Stop record MP4 video started with @startRecord. If you don't call it file will be unreadable.
     */
    public void stopRecord() {
        recordController.stopRecord();
        if (!streaming) stopStream();
    }

    /**
     * Start camera preview. Ignored, if stream or preview is started.
     *
     * @param cameraFacing front or back camera. Like: {@link nl.thecirclezzm.streaming.encoder.input.video.CameraHelper.Facing#BACK}
     *                     {@link nl.thecirclezzm.streaming.encoder.input.video.CameraHelper.Facing#FRONT}
     * @param rotation     camera rotation (0, 90, 180, 270). Recommended: {@link
     *                     nl.thecirclezzm.streaming.encoder.input.video.CameraHelper#getCameraOrientation(Context)}
     */
    public void startPreview(@NonNull CameraHelper.Facing cameraFacing, SizePixels sizePixels, int rotation) {
        previewWidth = sizePixels.getWidth();
        previewHeight = sizePixels.getHeight();
        if (!isStreaming() && !onPreview && !isBackground) {
            if (surfaceView != null) {
                cameraManager.prepareCamera(surfaceView.getHolder().getSurface());
            } else if (textureView != null) {
                cameraManager.prepareCamera(new Surface(textureView.getSurfaceTexture()));
            }
            cameraManager.openCameraFacing(cameraFacing);
            onPreview = true;
        }
    }

    public void startPreview(@NonNull CameraHelper.Facing cameraFacing, SizePixels sizePixels) {
        startPreview(cameraFacing, sizePixels, CameraHelper.getCameraOrientation(context));
    }

    public void startPreview(@NonNull CameraHelper.Facing cameraFacing, int rotation) {
        startPreview(cameraFacing, new SizePixels(videoEncoder.getWidth(), videoEncoder.getHeight()), rotation);
    }

    public void startPreview(@NonNull CameraHelper.Facing cameraFacing) {
        startPreview(cameraFacing, new SizePixels(videoEncoder.getWidth(), videoEncoder.getHeight()),
                CameraHelper.getCameraOrientation(context));
    }

    public void startPreview() {
        startPreview(CameraHelper.Facing.BACK);
    }

    /**
     * Stop camera preview. Ignored if streaming or already stopped. You need call it after
     *
     * @stopStream to release camera properly if you will close activity.
     */
    public void stopPreview() {
        if (!isStreaming() && onPreview && !isBackground) {
            cameraManager.closeCamera(false);
            onPreview = false;
            previewWidth = 0;
            previewHeight = 0;
        }
    }

    /**
     * Need be called after @prepareVideo or/and @prepareAudio. This method override resolution of
     *
     * @param url of the stream like: protocol://ip:port/application/streamName
     *            <p>
     *            RTSP: rtsp://192.168.1.1:1935/live/pedroSG94 RTSPS: rtsps://192.168.1.1:1935/live/pedroSG94
     *            RTMP: rtmp://192.168.1.1:1935/live/pedroSG94 RTMPS: rtmps://192.168.1.1:1935/live/pedroSG94
     * @startPreview to resolution seated in @prepareVideo. If you never startPreview this method
     * startPreview for you to resolution seated in @prepareVideo.
     */
    @RequiresPermission(value = Manifest.permission.INTERNET)
    public void startStream(String url) {
        streaming = true;
        if (!recordController.isRecording()) {
            startEncoders();
        } else {
            resetVideoEncoder();
        }
        streamingProtocol.startStream(url, videoEncoder);
        onPreview = true;
    }

    private void startEncoders() {
        videoEncoder.start();
        audioEncoder.start();
        microphoneManager.start();
        if (!cameraManager.isRunning() && videoEncoder.getWidth() != previewWidth
                || videoEncoder.getHeight() != previewHeight) {
            if (onPreview) {
                cameraManager.openLastCamera();
            } else {
                cameraManager.openCameraBack();
            }
        }
        onPreview = true;
    }

    private void resetVideoEncoder() {
        videoEncoder.reset();
        cameraManager.closeCamera(false);
        cameraManager.prepareCamera(videoEncoder.getInputSurface());
        cameraManager.openLastCamera();
    }

    /**
     * Stop stream started with @startStream.
     */
    public void stopStream() {
        if (streaming) {
            streaming = false;
            streamingProtocol.stopStream();
        }
        if (!recordController.isRecording()) {
            cameraManager.closeCamera(!isBackground);
            onPreview = !isBackground;
            microphoneManager.stop();
            videoEncoder.stop();
            audioEncoder.stop();
            recordController.resetFormats();
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        stopStream();
        stopPreview();
    }

    //re connection
    public void setReTries(int reTries) {
        streamingProtocol.setReTries(reTries);
    }

    public boolean shouldRetry(String reason) {
        return streamingProtocol.shouldRetry(reason);
    }

    protected void reConnect(long delay) {
        streamingProtocol.reConnect(delay);
    }

    //cache control
    public void resizeCache(int newSize) throws RuntimeException {
        streamingProtocol.resizeCache(newSize);
    }

    public int getCacheSize() {
        return streamingProtocol.getCacheSize();
    }

    public StreamStats getStats() {
        return streamingProtocol.getStats();
    }

    /**
     * Get supported preview resolutions of back camera in px.
     *
     * @return list of preview resolutions supported by back camera
     */
    @NonNull
    public SizePixels[] getResolutionsBack() {
        return cameraManager.getCameraResolutionsBack();
    }

    /**
     * Get supported preview resolutions of front camera in px.
     *
     * @return list of preview resolutions supported by front camera
     */
    @NonNull
    public SizePixels[] getResolutionsFront() {
        return cameraManager.getCameraResolutionsFront();
    }

    /**
     * Get supported properties of the camera
     *
     * @return CameraCharacteristics object
     */
    public CameraCharacteristics getCameraCharacteristics() {
        return cameraManager.getCameraCharacteristics();
    }

    /**
     * Mute microphone, can be called before, while and after stream.
     */
    public void disableAudio() {
        microphoneManager.mute();
    }

    /**
     * Enable a muted microphone, can be called before, while and after stream.
     */
    public void enableAudio() {
        microphoneManager.unMute();
    }

    /**
     * Get mute state of microphone.
     *
     * @return true if muted, false if enabled
     */
    public boolean isAudioMuted() {
        return microphoneManager.isMuted();
    }

    /**
     * Get video camera state
     *
     * @return true if disabled, false if enabled
     */
    public boolean isVideoEnabled() {
        return videoEnabled;
    }

    /**
     * Disable send camera frames and send a black image with low bitrate(to reduce bandwith used)
     * instance it.
     */
    public void disableVideo() {
        videoEncoder.startSendBlackImage();
        videoEnabled = false;
    }

    /**
     * Enable send camera frames.
     */
    public void enableVideo() {
        videoEncoder.stopSendBlackImage();
        videoEnabled = true;
    }

    /**
     * Return max zoom level
     *
     * @return max zoom level
     */
    public float getMaxZoom() {
        return cameraManager.getMaxZoom();
    }

    /**
     * Return current zoom level
     *
     * @return current zoom level
     */
    public float getZoom() {
        return cameraManager.getZoom();
    }

    /**
     * Set zoomIn or zoomOut to camera.
     * Use this method if you use a zoom slider.
     *
     * @param level Expected to be >= 1 and <= max zoom level
     * @see StreamingCamera#getMaxZoom()
     */
    public void setZoom(float level) {
        cameraManager.setZoom(level);
    }

    /**
     * Set zoomIn or zoomOut to camera.
     *
     * @param event motion event. Expected to get event.getPointerCount() > 1
     */
    public void setZoom(MotionEvent event) {
        cameraManager.setZoom(event);
    }

    public int getBitrate() {
        return videoEncoder.getBitRate();
    }

    public int getResolutionValue() {
        return videoEncoder.getWidth() * videoEncoder.getHeight();
    }

    public int getStreamWidth() {
        return videoEncoder.getWidth();
    }

    public int getStreamHeight() {
        return videoEncoder.getHeight();
    }

    /**
     * Switch camera used. Can be called on preview or while stream, ignored with preview off.
     *
     * @throws CameraOpenException If the other camera doesn't support same resolution.
     */
    public void switchCamera() throws CameraOpenException {
        if (isStreaming() || onPreview) {
            cameraManager.switchCamera();
        }
    }

    private void prepareCameraManager() {
        if (textureView != null) {
            cameraManager.prepareCamera(textureView, videoEncoder.getInputSurface());
        } else if (surfaceView != null) {
            cameraManager.prepareCamera(surfaceView, videoEncoder.getInputSurface());
        } else {
            cameraManager.prepareCamera(videoEncoder.getInputSurface());
        }
        videoEnabled = true;
    }

    /**
     * Set video bitrate of H264 in kb while stream.
     *
     * @param bitrate H264 in kb.
     */
    public void setVideoBitrateOnFly(int bitrate) {
        videoEncoder.setVideoBitrateOnFly(bitrate);
    }

    /**
     * Set limit FPS while stream. This will be override when you call to prepareVideo method. This
     * could produce a change in iFrameInterval.
     *
     * @param fps frames per second
     */
    public void setLimitFPSOnFly(int fps) {
        videoEncoder.setFps(fps);
    }

    /**
     * Get stream state.
     *
     * @return true if streaming, false if not streaming.
     */
    public boolean isStreaming() {
        return streaming;
    }

    /**
     * Get record state.
     *
     * @return true if recording, false if not recoding.
     */
    public boolean isRecording() {
        return recordController.isRunning();
    }

    public void pauseRecord() {
        recordController.pauseRecord();
    }

    public void resumeRecord() {
        recordController.resumeRecord();
    }

    @NonNull
    public RecordController.Status getRecordStatus() {
        return recordController.getStatus();
    }

    /**
     * Get preview state.
     *
     * @return true if enabled, false if disabled.
     */
    public boolean isOnPreview() {
        return onPreview;
    }

    @Override
    public void getAacData(ByteBuffer aacBuffer, MediaCodec.BufferInfo info) {
        recordController.recordAudio(aacBuffer, info);
        if (streaming) streamingProtocol.getAacData(aacBuffer, info);
    }

    @Override
    public void onSpsPps(ByteBuffer sps, ByteBuffer pps) {
        onSpsPpsVps(sps, pps, null);
    }

    @Override
    public void onSpsPpsVps(ByteBuffer sps, ByteBuffer pps, ByteBuffer vps) {
        if (streaming) streamingProtocol.onSpsPpsVps(sps, pps, vps);
    }

    @Override
    public void getVideoData(ByteBuffer h264Buffer, MediaCodec.BufferInfo info) {
        recordController.recordVideo(h264Buffer, info);
        if (streaming) streamingProtocol.getH264Data(h264Buffer, info);
    }

    @Override
    public void inputPCMData(byte[] buffer, int size) {
        audioEncoder.inputPCMData(buffer, size);
    }

    @Override
    public void onVideoFormat(MediaFormat mediaFormat) {
        recordController.setVideoFormat(mediaFormat);
    }

    @Override
    public void onAudioFormat(MediaFormat mediaFormat) {
        recordController.setAudioFormat(mediaFormat);
    }

    public enum SampleRate {
        sr8(8000), sr16(16000), sr22_5(22500), sr32(32000), sr44_1(44100);

        public final int n;

        SampleRate(int n) {
            this.n = n;
        }
    }

    public enum Protocol {
        RTMP
    }
}
