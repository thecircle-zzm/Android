package nl.thecirclezzm.seechangecamera.ui.streaming

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.annotation.RequiresPermission
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import nl.thecirclezzm.seechangecamera.R
import nl.thecirclezzm.seechangecamera.databinding.StreamingFragmentBinding
import nl.thecirclezzm.streaming.encoder.input.video.CameraHelper
import nl.thecirclezzm.streaming.main.rtmp.RtmpCamera2
import nl.thecirclezzm.streaming.main.view.OpenGlView

class StreamingFragment : Fragment() {
    companion object {
        @RequiresPermission(allOf = [INTERNET, CAMERA, RECORD_AUDIO, WRITE_EXTERNAL_STORAGE])
        fun newInstance() = StreamingFragment()
    }

    private lateinit var viewModel: StreamingViewModel
    private var surfaceView: OpenGlView? = null

    private var cameraStream: RtmpCamera2? = null
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
                    cameraStream = RtmpCamera2(surfaceView!!, viewModel).also {
                        val resolutions = it.resolutionsBack
                        resolutions.sortBy { it.width * it.height }
                        val resolution = resolutions.firstOrNull {
                            Math.max(it.width, it.height) >= 720 && Math.min(it.width, it.height) >= 480
                        } ?: resolutions.last()
                        it.prepareAudio()
                        val rotation = CameraHelper.getCameraOrientation(context)
                        it.prepareVideo(resolution.width, resolution.height, 30, 1200 * 1024, false, rotation)
                        surfaceView?.layoutParams = surfaceView?.layoutParams?.apply {
                            width = Math.min(resolution.width, resolution.height)
                            height = Math.max(resolution.width, resolution.height)
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
        viewModel.streamingUrl = activity?.intent?.getStringExtra("streamingUrl") ?: error("Streaming url is null")
    }
}
