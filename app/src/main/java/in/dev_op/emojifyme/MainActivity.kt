package `in`.dev_op.emojifyme

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException

/**
 * Created by harsh on 31/12/17.
 * MainActivity as launcher activity
 */
class MainActivity : AppCompatActivity() {

    private val REQUEST_STORAGE_PERMISSION = 1
    private val REQUEST_IMAGE_CAPTURE = 1
    private val FILE_PROVIDER_AUTHORITY = "in.dev_op.fileprovider"

    private lateinit var mTempPhotoPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setListeners()
    }

    private fun setListeners() {
        btEmojify.setOnClickListener({ emojify() })
        fabClear.setOnClickListener({ clearImage() })
        fabSave.setOnClickListener({ saveImage() })
        fabShare.setOnClickListener({ shareImage() })
    }

    private fun emojify() {
        if (hasWritePermissions()) launchCamera()
        else requestWritePermissions()
    }

    private fun saveImage() {
        BitmapUtils.deleteImageFile(this, mTempPhotoPath)
        BitmapUtils.saveImage(this, mResultBitmap)
    }

    private fun shareImage() {
        saveImage()
        BitmapUtils.shareImage(this, mTempPhotoPath, FILE_PROVIDER_AUTHORITY)
    }

    private fun clearImage() {
        ivPhoto.setImageResource(0)
        btEmojify.visibility = View.VISIBLE
        tvTitle.visibility = View.VISIBLE
        fabShare.visibility = View.GONE
        fabSave.visibility = View.GONE
        fabClear.visibility = View.GONE

        BitmapUtils.deleteImageFile(this, mTempPhotoPath)
    }

    private lateinit var mResultBitmap: Bitmap

    private fun processAndSetImage() {
        btEmojify.visibility = View.GONE
        tvTitle.visibility = View.GONE
        fabShare.visibility = View.VISIBLE
        fabSave.visibility = View.VISIBLE
        fabClear.visibility = View.VISIBLE

        mResultBitmap = BitmapUtils.resampleImage(this, mTempPhotoPath)

        ivPhoto.setImageBitmap(mResultBitmap)
    }

    private fun launchCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            val photoFile: File
            try {
                photoFile = BitmapUtils.createTempImageFile(this)

            } catch (e: IOException) {
                e.printStackTrace()
                return
            }

            mTempPhotoPath = photoFile.absolutePath
            val photoURI = FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, photoFile)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)

        }
    }


    private fun hasWritePermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestWritePermissions() {
        ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_STORAGE_PERMISSION)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_STORAGE_PERMISSION -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    launchCamera()
                else Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            processAndSetImage()
        } else {
            BitmapUtils.deleteImageFile(this, mTempPhotoPath)
        }
    }

}
