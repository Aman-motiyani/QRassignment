package com.example.qrassignment

import android.Manifest.permission.CAMERA
import android.Manifest.permission_group.CAMERA
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.hardware.SensorPrivacyManager.Sensors.CAMERA
import android.media.MediaRecorder.VideoSource.CAMERA
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class ScanActivity : AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore
    val CAMERA_PERMISSION_REQUEST_CODE = 123
    override fun onCreate(savedInstanceState: Bundle?) {

        val scanner = IntentIntegrator(this)
        firestore = FirebaseFirestore.getInstance()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        if (requestPermission()) {
           scanner.initiateScan()
            scanner.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            scanner.setPrompt("Scan a QR Code")
            scanner.setOrientationLocked(false)
        } else {
            Toast.makeText(this, "Please enable Camera Permission first", Toast.LENGTH_LONG).show()
        }
    }


    private fun requestPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result : IntentResult? =  IntentIntegrator.parseActivityResult(requestCode,resultCode,data)
        result?.let {
            if (it.contents != null) {
                val decodedValue = it.contents
                saveData(decodedValue)
            }
        }
        finish()
    }

    private fun saveData(value: String) {
        val qrCodeData = hashMapOf(
            "value" to value,
            "timestamp" to System.currentTimeMillis()
        )

        val collectionRef = firestore.collection("qrcodes")

        collectionRef.document() // Generate a new document reference
            .set(qrCodeData)
            .addOnSuccessListener {
                applicationContext.run {
                    Toast.makeText(applicationContext, "QR Code value saved successfully", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                applicationContext.run {
                    Toast.makeText(applicationContext, "Error saving QR Code value: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

}

