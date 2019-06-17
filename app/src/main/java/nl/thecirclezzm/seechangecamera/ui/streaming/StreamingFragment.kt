package nl.thecirclezzm.seechangecamera.ui.streaming

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.annotation.RequiresPermission
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import nl.thecirclezzm.seechangecamera.R
import nl.thecirclezzm.seechangecamera.databinding.StreamingFragmentBinding
import nl.thecirclezzm.streaming.StreamingCamera

class StreamingFragment : Fragment() {
    companion object {
        @RequiresPermission(allOf = [INTERNET, CAMERA, RECORD_AUDIO, WRITE_EXTERNAL_STORAGE])
        fun newInstance() = StreamingFragment()
    }

    private lateinit var viewModel: StreamingViewModel
    private var surfaceView: SurfaceView? = null

    private var cameraStream: StreamingCamera? = null
        set(value) {
            field = value
            if (value != null)
                lifecycle.addObserver(value)
        }

    @RequiresPermission(allOf = [INTERNET, CAMERA, RECORD_AUDIO, WRITE_EXTERNAL_STORAGE])
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val streamingFragmentBinding: StreamingFragmentBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.streaming_fragment, container, false)
        return streamingFragmentBinding.root.apply {
            surfaceView = findViewById(R.id.surfaceView)
            surfaceView?.holder?.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {}

                override fun surfaceDestroyed(p0: SurfaceHolder?) {
                    cameraStream?.onStop()
                    cameraStream = null
                }

                @SuppressLint("MissingPermission")
                override fun surfaceCreated(p0: SurfaceHolder?) {
                    cameraStream = StreamingCamera(surfaceView!!, StreamingCamera.Protocol.RTMP, viewModel).also {
                        it.prepareAudio()
                        it.prepareVideo()
                        surfaceView?.layoutParams = surfaceView?.layoutParams?.apply {
                            width = it.streamHeight
                            height = it.streamWidth
                        }
                        it.startStream(viewModel.streamingUrl)
                    }
                }
            })
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(StreamingViewModel::class.java)
    }
}
