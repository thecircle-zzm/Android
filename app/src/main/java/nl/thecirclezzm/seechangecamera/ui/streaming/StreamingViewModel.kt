package nl.thecirclezzm.seechangecamera.ui.streaming

import android.util.Log
import androidx.lifecycle.ViewModel
import nl.thecirclezzm.streaming.base.ConnectionCallbacks

class StreamingViewModel : ViewModel(), ConnectionCallbacks {
    lateinit var streamingUrl: String

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
