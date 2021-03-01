package com.dsb.volumedowndetector

import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.dsb.volumedowndetector.databinding.ActivityMainBinding
import com.google.common.util.concurrent.ListenableFuture
import java.io.File

class MainActivity : AppCompatActivity() {
    private val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> by lazy {
        ProcessCameraProvider.getInstance(this)
    }

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)
        lifecycle.addObserver(
            HardwareKeyPressedEventObserver(
                context = this,
                onKeyPressed = {
                    Toast.makeText(this, "Volume Down", Toast.LENGTH_LONG).show()
                },
                keyToObserve = KeyEvent.KEYCODE_VOLUME_DOWN
            )
        )

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(this))
    }

    fun captureImage() {

    }

    private fun bindPreview(cameraProvider : ProcessCameraProvider) {
        val preview : Preview = Preview.Builder().build()

        val cameraSelector : CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        val imageCapture = ImageCapture.Builder()
            .setTargetRotation(binding.previewView.display.rotation)
            .build()

        preview.setSurfaceProvider(binding.previewView.surfaceProvider)

        var camera = cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector,imageCapture, preview)
    }

    fun onClick() {
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(File("sdcard/")).build()
        imageCapture.takePicture(outputFileOptions, cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(error: ImageCaptureException)
                {
                    // insert your code here.
                }
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    // insert your code here.
                }
            })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        HardwareKeyPressedEventObserver.sendEvent(context = this, keyCode = keyCode)
        return super.onKeyDown(keyCode, event)
    }
}