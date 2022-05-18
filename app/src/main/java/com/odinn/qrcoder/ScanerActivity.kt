package com.odinn.qrcoder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.odinn.qrcoder.Constants.SCAN_KEY
import me.dm7.barcodescanner.zbar.Result
import me.dm7.barcodescanner.zbar.ZBarScannerView

class ScannerActivity : AppCompatActivity(), ZBarScannerView.ResultHandler {
    private lateinit var zbView:ZBarScannerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        zbView = ZBarScannerView(this)
        setContentView(zbView)
    }

    override fun onPause() {
        super.onPause()
        zbView.stopCamera()
    }

    override fun onResume() {
        super.onResume()
        zbView.setResultHandler(this)
        zbView.startCamera()
    }

    override fun handleResult(result: Result?) {
        val i = Intent()
        i.putExtra(SCAN_KEY, result?.contents )
        setResult(RESULT_OK, i)
        finish()
    }
}