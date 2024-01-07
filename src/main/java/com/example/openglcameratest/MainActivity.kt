package com.example.openglcameratest

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Surface
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private var permissions = arrayOf(Manifest.permission.CAMERA)
    lateinit var surfaceTexture1: SurfaceTexture


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val glView = OpenGLView(this){
            Log.d("gltestlog", "Starting Camera")
            surfaceTexture1 = it
            if (!startCamera()){
                ActivityCompat.requestPermissions(this, permissions, 0)
            }
        }
        setContentView(glView)
    }

    private fun checkPermissions(): Boolean{
        return permissions.all{
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 0 && grantResults.all {it == PackageManager.PERMISSION_GRANTED}){
            startCamera()
        } else {
            AlertDialog.Builder(this).setPositiveButton("OK", null)
                .setMessage("Will not work as camera permission is not granted").show()
        }
    }


    private fun startCamera(): Boolean {
        if (checkPermissions()) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
            cameraProviderFuture.addListener({
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {

                    val surfaceProvider: (SurfaceRequest) -> Unit = { request ->
                        val resolution = request.resolution
                        surfaceTexture1?.apply {
                            setDefaultBufferSize(resolution.width, resolution.height)
                            val surface = Surface(this)
                            request.provideSurface(
                                surface,
                                ContextCompat.getMainExecutor(this@MainActivity.baseContext))
                            { }

                        }
                    }
                    it.setSurfaceProvider(surfaceProvider)

                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview)

                } catch (e: Exception) {
                    Log.e("OpenGL01Log", e.stackTraceToString())
                }
            }, ContextCompat.getMainExecutor(this))
            return true
        } else {
            return false
        }
    }
}