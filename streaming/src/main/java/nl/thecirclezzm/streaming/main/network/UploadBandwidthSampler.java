package nl.thecirclezzm.streaming.main.network;

/*
 *  Copyright (c) 2015, Facebook, Inc.
 *  All rights reserved.
 *
 *  This source code is licensed under the BSD-style license found in the
 *  LICENSE file in the root directory of this source tree. An additional grant
 *  of patent rights can be found in the PATENTS file in the same directory.
 *
 */

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;

import androidx.annotation.NonNull;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class used to read from the file {@code /proc/net/xt_qtaguid/stats} periodically, in order to
 * determine a ConnectionClass.
 */
public class UploadBandwidthSampler {

    /**
     * Time between polls in ms.
     */
    static final long SAMPLE_TIME = 1000;

    /**
     * The DownloadBandwidthManager that keeps track of the moving average and ConnectionClass.
     */
    private final ConnectionClassManager mConnectionClassManager;

    @NonNull
    private final AtomicInteger mSamplingCounter;

    @NonNull
    private final Handler mHandler;

    private long mLastTimeReading;

    private UploadBandwidthSampler(ConnectionClassManager connectionClassManager) {
        mConnectionClassManager = connectionClassManager;
        mSamplingCounter = new AtomicInteger();
        HandlerThread mThread = new HandlerThread("ParseThread");
        mThread.start();
        mHandler = new SamplingHandler(mThread.getLooper());
    }

    /**
     * Retrieval method for the DeviceBandwidthSampler singleton.
     *
     * @return The singleton instance of DeviceBandwidthSampler.
     */
    @NonNull
    public static UploadBandwidthSampler getInstance() {
        return UploadBandwidthSamplerHolder.instance;
    }

    /**
     * Method call to start sampling for download bandwidth.
     */
    public void startSampling() {
        if (mSamplingCounter.getAndIncrement() == 0) {
            mHandler.sendEmptyMessage(SamplingHandler.MSG_START);
            mLastTimeReading = SystemClock.elapsedRealtime();
        }
    }

    /**
     * Finish sampling and prevent further changes to the
     * ConnectionClass until another timer is started.
     */
    public void stopSampling() {
        if (mSamplingCounter.decrementAndGet() == 0) {
            mHandler.sendEmptyMessage(SamplingHandler.MSG_STOP);
        }
    }

    /**
     * @return True if there are still threads which are sampling, false otherwise.
     */
    public boolean isSampling() {
        return (mSamplingCounter.get() != 0);
    }

    // Singleton.
    private static class UploadBandwidthSamplerHolder {
        public static final UploadBandwidthSampler instance =
                new UploadBandwidthSampler(ConnectionClassManager.getInstance());
    }

    private class SamplingHandler extends Handler {
        static final int MSG_START = 1;
        static final int MSG_STOP = 2;

        public SamplingHandler(@NonNull Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_START:
                    addSample();
                    sendEmptyMessageDelayed(MSG_START, SAMPLE_TIME);
                    break;
                case MSG_STOP:
                    addSample();
                    removeMessages(MSG_START);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown what=" + msg.what);
            }
        }

        /**
         * Method for polling for the change in total bytes since last update and
         * adding it to the BandwidthManager.
         */
        private void addSample() {
            long byteDiff = QTagTxParser.getInstance().parseDataUsageForUidAndTag(Process.myUid());
            synchronized (this) {
                long curTimeReading = SystemClock.elapsedRealtime();
                if (byteDiff != -1) {
                    mConnectionClassManager.addBandwidth(byteDiff, curTimeReading - mLastTimeReading);
                }
                mLastTimeReading = curTimeReading;
            }
        }
    }
}