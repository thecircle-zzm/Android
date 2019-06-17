package nl.thecirclezzm.seechangecamera.utils

import android.content.pm.PackageManager
import android.util.SparseArray
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.util.set

abstract class PermissionCompatActivity : AppCompatActivity() {
    private val permissionsCallback = SparseArray<(permissionsGranted: Boolean) -> Unit>()
    private var lastRequestCode = 50 // Start at a high number, so we don't break existing permission requests

    protected fun requestPermissions(
        vararg permissions: String,
        onResultCallback: (permissionsGranted: Boolean) -> Unit
    ) {
        if (permissions.any { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }) {
            // One of the permissions is not granted
            lastRequestCode++
            permissionsCallback[lastRequestCode] = onResultCallback
            ActivityCompat.requestPermissions(this, permissions, lastRequestCode)
        } else {
            // All permissions are granted
            onResultCallback(true)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        permissionsCallback[requestCode]?.invoke(grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED })
    }

    override fun onDestroy() {
        super.onDestroy()
        permissionsCallback.clear()
    }
}