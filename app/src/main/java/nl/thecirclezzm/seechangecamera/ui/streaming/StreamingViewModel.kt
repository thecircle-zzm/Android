package nl.thecirclezzm.seechangecamera.ui.streaming

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import nl.thecirclezzm.streaming.base.ConnectionCallbacks
import nl.thecirclezzm.streaming.main.rtmp.RtmpCamera2
import nl.thecirclezzm.streaming.rtmp.ConnectCheckerRtmp

class StreamingViewModel(application: Application) : AndroidViewModel(application), ConnectCheckerRtmp {
    lateinit var streamingUrl: String

    override fun onConnectionSuccessRtmp() {
        Log.i("Stream", "Connection success")
    }

    override fun onConnectionFailedRtmp(reason: String?) {
        Log.e("Stream", "Connection failed: $reason")
    }

    override fun onDisconnectRtmp() {
        Log.i("Stream", "Disconnected")
    }

    override fun onAuthErrorRtmp() {
        Log.e("Stream", "Authentication failed")
    }

    override fun onAuthSuccessRtmp() {
        Log.i("Stream", "Authentication succeeded")
    }
}
