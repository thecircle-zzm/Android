package nl.thecirclezzm.seechangecamera.ui.streaming

import android.Manifest.permission.*
import android.annotation.SuppressLint
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.annotation.RequiresPermission
import androidx.constraintlayout.widget.ConstraintLayout
import nl.thecirclezzm.streaming_library.CameraStream
import nl.thecirclezzm.seechangecamera.R
import nl.thecirclezzm.streaming_library.VideoResolutions

class StreamingFragment : Fragment() {
    companion object {
        @RequiresPermission(allOf = [INTERNET, CAMERA, RECORD_AUDIO, WRITE_EXTERNAL_STORAGE])
        fun newInstance() = StreamingFragment()
    }

    private lateinit var viewModel: StreamingViewModel
    private var surfaceView: SurfaceView? = null

    private var cameraStream: CameraStream? = null
        set(value) {
            field = value
            if(value != null)
                lifecycle.addObserver(value)
        }

    @RequiresPermission(allOf = [INTERNET, CAMERA, RECORD_AUDIO, WRITE_EXTERNAL_STORAGE])
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.streaming_fragment, container, false).also {
            surfaceView = it.findViewById<SurfaceView>(R.id.cameraView).apply {
                layoutParams = ConstraintLayout.LayoutParams(VideoResolutions.VGA.width, VideoResolutions.VGA.height)
            }
            surfaceView?.holder?.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {}

                override fun surfaceDestroyed(p0: SurfaceHolder?) {}

                @SuppressLint("MissingPermission")
                override fun surfaceCreated(p0: SurfaceHolder?) {
                    cameraStream = CameraStream(
                        viewModel.streamingUrl,
                        surfaceView!!
                    )
                    cameraStream?.streaming = true
                }
            })

        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(StreamingViewModel::class.java)
    }
}
