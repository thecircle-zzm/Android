package nl.thecirclezzm.seechangecamera.ui.streaming

import android.util.Log
import androidx.lifecycle.ViewModel
import nl.thecirclezzm.streaming.base.ConnectionCallbacks

class StreamingViewModel : ViewModel(), ConnectionCallbacks {
    val streamingUrl = "rtmp://188.166.38.127:1935/live/a37b62f8ea0d838d"

    override fun onConnectionSuccess() {
        Log.i("Stream", "Connection success")
    }

    override fun onConnectionFailed(reason: String?) {
        Log.e("Stream", "Connection failed: $reason")
    }

    override fun onDisconnect() {
        Log.i("Stream", "Disconnected")
    }

    override fun onAuthError() {
        Log.e("Stream", "Authentication failed")
    }

    override fun onAuthSuccess() {
        Log.i("Stream", "Authentication succeeded")
    }

}
