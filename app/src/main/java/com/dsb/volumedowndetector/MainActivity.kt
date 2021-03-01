package com.dsb.volumedowndetector

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
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
import java.time.LocalDate
import java.time.LocalDateTime

class MainActivity : AppCompatActivity() {
    private val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> by lazy {
        ProcessCameraProvider.getInstance(this)
    }

    private val permissions = listOf(
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.CAMERA,
    )
    private val permissionRequestCode = 12

    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraSelector: CameraSelector
    private lateinit var imageCapture: ImageCapture
    private val cameraExecutor by lazy {
        ContextCompat.getMainExecutor(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        askForPermissions()
    }

    private fun askForPermissions() {
        requestPermissions(permissions.toTypedArray(), permissionRequestCode)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults.first() == PackageManager.PERMISSION_GRANTED) {
                continueAfterGettingPermissions()
            } else {
                this.askForPermissions()
            }
        }
    }

    private fun continueAfterGettingPermissions() {
        setup()
        binding.testCapture.setOnClickListener {
            onClick()
        }
    }

    private fun setup() {
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
        }, cameraExecutor)
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview: Preview = Preview.Builder().build()

        cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        imageCapture = ImageCapture.Builder()
            .setTargetRotation(binding.root.display.rotation)
            .build()

        preview.setSurfaceProvider(binding.previewView.surfaceProvider)

        var camera = cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, imageCapture, preview)
    }

    fun onClick() {
        val directory = Environment.getExternalStorageState() + "/VolumeDownPhoto/"
        File(directory).mkdirs()
        val name = "$directory${LocalDateTime.now()}.jpg"
        Log.i("onclick", name)
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(File(name)).build()
        imageCapture.takePicture(
            outputFileOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(error: ImageCaptureException) {
                    Toast.makeText(this@MainActivity, "Error ${error.toString()}", Toast.LENGTH_LONG).show()
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Toast.makeText(this@MainActivity, "Saved ${outputFileResults.savedUri.toString()}", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        HardwareKeyPressedEventObserver.sendEvent(context = this, keyCode = keyCode)
        return super.onKeyDown(keyCode, event)
    }
}