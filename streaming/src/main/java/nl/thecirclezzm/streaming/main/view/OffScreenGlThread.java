package nl.thecirclezzm.streaming.main.view;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import nl.thecirclezzm.streaming.encoder.input.gl.SurfaceManager;
import nl.thecirclezzm.streaming.encoder.input.gl.render.ManagerRender;
import nl.thecirclezzm.streaming.encoder.input.gl.render.filters.BaseFilterRender;
import nl.thecirclezzm.streaming.encoder.input.video.FpsLimiter;
import nl.thecirclezzm.streaming.encoder.utils.gl.GlUtil;

/**
 * Created by pedro on 4/03/18.
 */

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class OffScreenGlThread
        implements GlInterface, Runnable, SurfaceTexture.OnFrameAvailableListener {

    private final Context context;
    private final Semaphore semaphore = new Semaphore(0);
    private final BlockingQueue<Filter> filterQueue = new LinkedBlockingQueue<>();
    private final Object sync = new Object();
    @NonNull
    private final FpsLimiter fpsLimiter = new FpsLimiter();
    @Nullable
    private Thread thread = null;
    private boolean frameAvailable = false;
    private boolean running = true;
    private boolean initialized = false;
    @Nullable
    private SurfaceManager surfaceManager = null;
    @Nullable
    private SurfaceManager surfaceManagerEncoder = null;
    @Nullable
    private ManagerRender textureManager = null;
    private int encoderWidth, encoderHeight;
    private boolean loadAA = false;
    private boolean AAEnabled = false;
    private int fps = 30;
    //used with camera
    @Nullable
    private TakePhotoCallback takePhotoCallback;

    public OffScreenGlThread(Context context) {
        this.context = context;
    }

    @Override
    public void init() {
        if (!initialized) textureManager = new ManagerRender();
        initialized = true;
    }

    @Override
    public void setEncoderSize(int width, int height) {
        this.encoderWidth = width;
        this.encoderHeight = height;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }

    @Nullable
    @Override
    public SurfaceTexture getSurfaceTexture() {
        return textureManager.getSurfaceTexture();
    }

    @Nullable
    @Override
    public Surface getSurface() {
        return textureManager.getSurface();
    }

    @Override
    public void addMediaCodecSurface(Surface surface) {
        synchronized (sync) {
            surfaceManagerEncoder = new SurfaceManager(surface, surfaceManager);
        }
    }

    @Override
    public void removeMediaCodecSurface() {
        synchronized (sync) {
            if (surfaceManagerEncoder != null) {
                surfaceManagerEncoder.release();
                surfaceManagerEncoder = null;
            }
        }
    }

    @Override
    public void takePhoto(TakePhotoCallback takePhotoCallback) {
        this.takePhotoCallback = takePhotoCallback;
    }

    @Override
    public void setFilter(int filterPosition, BaseFilterRender baseFilterRender) {
        filterQueue.add(new Filter(filterPosition, baseFilterRender));
    }

    @Override
    public void setFilter(BaseFilterRender baseFilterRender) {
        setFilter(0, baseFilterRender);
    }

    @Override
    public void enableAA(boolean AAEnabled) {
        this.AAEnabled = AAEnabled;
        loadAA = true;
    }

    @Override
    public void setRotation(int rotation) {
        textureManager.setCameraRotation(rotation);
    }

    @Override
    public boolean isAAEnabled() {
        return textureManager != null && textureManager.isAAEnabled();
    }

    @Override
    public void start() {
        synchronized (sync) {
            thread = new Thread(this);
            running = true;
            thread.start();
            semaphore.acquireUninterruptibly();
        }
    }

    @Override
    public void stop() {
        synchronized (sync) {
            if (thread != null) {
                thread.interrupt();
                try {
                    thread.join(100);
                } catch (InterruptedException e) {
                    thread.interrupt();
                }
                thread = null;
            }
            running = false;
            fpsLimiter.reset();
        }
    }

    private void releaseSurfaceManager() {
        if (surfaceManager != null) {
            surfaceManager.release();
            surfaceManager = null;
        }
    }

    @Override
    public void run() {
        releaseSurfaceManager();
        surfaceManager = new SurfaceManager();
        surfaceManager.makeCurrent();
        textureManager.initGl(context, encoderWidth, encoderHeight, encoderWidth, encoderHeight);
        textureManager.getSurfaceTexture().setOnFrameAvailableListener(this);
        semaphore.release();
        try {
            while (running) {
                if (fpsLimiter.limitFPS(fps)) continue;
                if (frameAvailable) {
                    frameAvailable = false;
                    surfaceManager.makeCurrent();
                    textureManager.updateFrame();
                    textureManager.drawOffScreen();
                    textureManager.drawScreen(encoderWidth, encoderHeight, false);
                    surfaceManager.swapBuffer();

                    synchronized (sync) {
                        if (surfaceManagerEncoder != null) {
                            surfaceManagerEncoder.makeCurrent();
                            textureManager.drawScreen(encoderWidth, encoderHeight, false);
                            long ts = textureManager.getSurfaceTexture().getTimestamp();
                            surfaceManagerEncoder.setPresentationTime(ts);
                            surfaceManagerEncoder.swapBuffer();
                            if (takePhotoCallback != null) {
                                takePhotoCallback.onTakePhoto(
                                        GlUtil.getBitmap(encoderWidth, encoderHeight, encoderWidth, encoderHeight));
                                takePhotoCallback = null;
                            }
                        }
                    }
                }
                if (!filterQueue.isEmpty()) {
                    Filter filter = filterQueue.take();
                    textureManager.setFilter(filter.getPosition(), filter.getBaseFilterRender());
                } else if (loadAA) {
                    textureManager.enableAA(AAEnabled);
                    loadAA = false;
                }
            }
        } catch (InterruptedException ignore) {
            Thread.currentThread().interrupt();
        } finally {
            textureManager.release();
            releaseSurfaceManager();
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        synchronized (sync) {
            frameAvailable = true;
            sync.notifyAll();
        }
    }
}
