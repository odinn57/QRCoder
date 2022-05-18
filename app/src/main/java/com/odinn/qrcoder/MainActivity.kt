package com.odinn.qrcoder

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.widget.Toast
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidmads.library.qrgenearator.QRGSaver
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.zxing.WriterException
import com.odinn.qrcoder.Constants.SCAN_KEY
import com.odinn.qrcoder.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private var launcher:ActivityResultLauncher<Intent>? = null
    var qrCode: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (qrCode == null) binding.bSave.setImageResource(R.drawable.ic_save_disable) else binding.bSave.setImageResource(R.drawable.ic_save)

        //зарегестрировали лаунчер
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result: ActivityResult ->
            if (result.resultCode == RESULT_OK){
                // получаем данные с закрытого активити
                val str = result.data?.getStringExtra(SCAN_KEY)
                binding.resultText.text = str?.toEditable()
                //инициализируем рекламу
                initAdMob()
            }
        }

        //инициализируем рекламу
        initAdMob()

        binding.button.setOnClickListener {
            generateQrCode(binding.resultText.text.toString())
            if (qrCode == null) binding.bSave.setImageResource(R.drawable.ic_save_disable) else binding.bSave.setImageResource(R.drawable.ic_save)
        }
        binding.bScann.setOnClickListener {
            getScan()
            generateQrCode(binding.resultText.text.toString())
            if (qrCode == null) binding.bSave.setImageResource(R.drawable.ic_save_disable) else binding.bSave.setImageResource(R.drawable.ic_save)
        }
        binding.copyButton.setOnClickListener {
            val clipboard: ClipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("text", binding.resultText.text)
            clipboard.setPrimaryClip(clipData)
            Toast.makeText(this,getString(R.string.text_copied), Toast.LENGTH_LONG).show()
        }
        binding.bSave.setOnClickListener {
            if (qrCode != null){
                saveQrCode()
            }
        }

    }

    private fun generateQrCode(text: String){
        val qrGenerator = QRGEncoder(text,null, QRGContents.Type.TEXT, 550)
        try {
            val bMap = qrGenerator.encodeAsBitmap()
            binding.qrCode.setImageBitmap(bMap)
            qrCode = bMap
            //инициализируем рекламу
            initAdMob()
        }catch (e: WriterException){
            Toast.makeText(this, R.string.enter_the_text_toast, Toast.LENGTH_LONG).show()
        }
    }
    private fun saveQrCode(){
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),1)
        }else{
            val savePath = Environment.DIRECTORY_PICTURES + "/"
            val name = "qr_${System.currentTimeMillis()}"
            try{
                QRGSaver.save(
                    savePath,
                    name.trim(),
                    qrCode,
                    QRGContents.ImageType.IMAGE_JPEG
                )
                Toast.makeText(this, getString(R.string.qr_was_save), Toast.LENGTH_LONG).show()

            }catch (e:Exception){
                e.printStackTrace();
                Toast.makeText(this, "Save file error!", Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun getScan(){
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE),2)

        }else{
            launcher?.launch(Intent(this, ScannerActivity::class.java))
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                launcher?.launch(Intent(this, ScannerActivity::class.java))
            }
        }
    }

    fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)

    override fun onResume() {
        super.onResume()
        binding.adView1.resume()
        binding.adView2.resume()
    }

    override fun onPause() {
        super.onPause()
        binding.adView1.pause()
        binding.adView2.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.adView1.destroy()
        binding.adView2.destroy()
    }

    private fun initAdMob(){
        MobileAds.initialize(this)
        val adRequest1 = AdRequest.Builder().build()
        val adRequest2 = AdRequest.Builder().build()
        binding.adView1.loadAd(adRequest1)
        binding.adView2.loadAd(adRequest2)
    }

}