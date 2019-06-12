package nl.thecirclezzm.streaming_library

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.util.Log
import android.view.SurfaceView
import androidx.annotation.Dimension
import androidx.annotation.RequiresPermission
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import nl.thecirclezzm.streaming_library.rtmp.ossrs.ConnectCheckerRtmp
import nl.thecirclezzm.streaming_library.rtsp.utils.ConnectCheckerRtsp
import java.net.URISyntaxException

data class SizePX (
    @Dimension(unit = Dimension.PX) var width: Int,
    @Dimension(unit = Dimension.PX) var height: Int
)

object VideoResolutions {
    val QVGA = SizePX(320, 240)
    val HVGA = SizePX(480, 320)
    val VGA = SizePX(640, 480)
    val HD = SizePX(1280, 720)
    val FHD = SizePX(1920, 1080)
}

enum class Orientation(val degrees: Int) {
    PORTRAIT(90), LANDSCAPE(0), PORTRAIT_INV(270), LANDSCAPE_INV(180)
}

enum class SampleRate(val n: Int) {
    `8`(8000), `16`(16000), `22_5`(22500), `32`(32000), `44_1`(44100)
}

enum class Protocol {
    AUTO, RTMP, RTSP
}

class CameraStream @RequiresPermission(allOf = [INTERNET, CAMERA, RECORD_AUDIO, WRITE_EXTERNAL_STORAGE]) constructor(
    val url: String,
    private val view: SurfaceView,
    val protocol: Protocol = Protocol.AUTO,
    val audioBitrate: Int = 64 * 1_024,
    val audioSampleRate: SampleRate = SampleRate.`32`,
    val videoSize: SizePX = VideoResolutions.VGA,
    val videoFPS: Int = 30,
    videoBitrate: Int = 1200 * 1_024,
    val videoRotation: Orientation = Orientation.PORTRAIT
) : LifecycleObserver {
    // A set of listeners that can be subscribed to
    var onAuthErrorListener: (() -> Unit)? = null
    var onAuthSuccessListener: (() -> Unit)? = null
    var onConnectionSuccessListener: (() -> Unit)? = null
    var onConnectionDisconnectListener: (() -> Unit)? = null
    var onConnectionErrorListener: ((String?) -> Unit)? = null

    private var cameraStream: CameraBase? = null

    var streaming: Boolean
        get() = cameraStream?.isStreaming ?: false
        @SuppressLint("MissingPermission")
        set(value) {
            if(value){
                if (cameraStream == null) {
                    createCameraStreamObject()
                }

                cameraStream?.startStream(url)
            } else {
                cameraStream?.stopStream()
            }
        }

    var videoBitrate = videoBitrate
        set(value){
            field = value
            cameraStream?.setVideoBitrateOnFly(value)
        }

    private val connectionCallbacks = object : ConnectCheckerRtmp, ConnectCheckerRtsp {
        override fun onAuthErrorRtsp() { onAuthErrorListener?.invoke() }
        override fun onAuthErrorRtmp() { onAuthErrorListener?.invoke() }
        override fun onAuthSuccessRtsp() { onAuthSuccessListener?.invoke() }
        override fun onAuthSuccessRtmp() { onAuthSuccessListener?.invoke() }
        override fun onConnectionSuccessRtsp() { onConnectionSuccessListener?.invoke() }
        override fun onConnectionSuccessRtmp() { onConnectionSuccessListener?.invoke() }
        override fun onDisconnectRtsp() { onConnectionDisconnectListener?.invoke() }
        override fun onDisconnectRtmp() { onConnectionDisconnectListener?.invoke() }
        override fun onConnectionFailedRtsp(reason: String?) { onConnectionErrorListener?.invoke(reason) }
        override fun onConnectionFailedRtmp(reason: String?) { onConnectionErrorListener?.invoke(reason); Log.e("ERROR", reason) }
    }

    @SuppressLint("MissingPermission")
    private fun propagateBreakingChanges(changes: CameraBase.() -> Unit){
        cameraStream?.let {
            val isStreaming = streaming

            if(isStreaming)
                it.stopStream() // Stop stream so we can make our changes

            changes(it) // Apply changes that might require the stream to be stopped

            // Only restart stream if it was originally streaming
            if(isStreaming)
                it.startStream(url)
        }
    }

    @SuppressLint("MissingPermission")
    @Throws(URISyntaxException::class, RuntimeException::class)
    private fun createCameraStreamObject(){
        if (cameraStream == null) {
            // Create the CameraStream object
            cameraStream = when {
                (protocol == Protocol.AUTO && url.startsWith("rtmp")) || protocol == Protocol.RTMP -> RtmpCamera(
                    view,
                    connectionCallbacks
                )
                (protocol == Protocol.AUTO && url.startsWith("rtsp")) || protocol == Protocol.RTSP -> RtspCamera(
                    view,
                    connectionCallbacks
                )
                else -> throw URISyntaxException(url, "URL should have protocol rtmp(s):// or rtsp(s)://")
            }.apply {
                prepareAudio(audioBitrate, audioSampleRate.n, true, true, true)

                // Audio failures are not important, but video failures are. Throw an error when this fails.
                if(!prepareVideo(videoSize.width, videoSize.height, videoFPS, videoBitrate, false, videoRotation.degrees))
                    throw RuntimeException("H264 encoder not found")
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        if(!streaming) cameraStream?.startStream(url)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        if(streaming) streaming = false
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy(){
        if(streaming) streaming = false
        cameraStream = null
    }
}