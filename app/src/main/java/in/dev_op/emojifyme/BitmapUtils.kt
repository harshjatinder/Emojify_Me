package `in`.dev_op.emojifyme

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment.DIRECTORY_PICTURES
import android.os.Environment.getExternalStoragePublicDirectory
import android.support.v4.content.FileProvider
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.Toast
import android.widget.Toast.makeText
import java.io.File
import java.io.FileOutputStream

/**
 * Created by harsh on 31/12/17.
 * Utility class to handle all Bitmap related operations
 */
object BitmapUtils {

    private val IMAGE_EXTENSION = ".jpg"
    private val APP_NAME = "Emojify"

    fun createTempImageFile(context: Context): File {
        val fileName = "JPEG_" + System.currentTimeMillis().toString()
        val storageDir = context.externalCacheDir
        return File.createTempFile(fileName, IMAGE_EXTENSION, storageDir)
    }

    fun deleteImageFile(context: Context, photoPath: String): Boolean {
        val imageFile = File(photoPath)
        val fileDeleted = imageFile.delete()
        Toast.makeText(context,
                context.getString(R.string.delete_success.takeIf { fileDeleted } ?: R.string.delete_failure)
                , Toast.LENGTH_SHORT).show()
        return fileDeleted
    }

    fun resampleImage(context: Context, photoPath: String): Bitmap {
        val displayMetrics = DisplayMetrics()
        val windowMgr = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowMgr.defaultDisplay.getMetrics(displayMetrics)

        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(photoPath, options)
        val scaleFactor = Math.min(options.outWidth / displayMetrics.widthPixels,
                options.outHeight / displayMetrics.heightPixels)
        options.inJustDecodeBounds = false
        options.inSampleSize = scaleFactor

        return BitmapFactory.decodeFile(photoPath, options)
    }

    fun saveImage(context: Context, image: Bitmap) {
        val savedImagePath: String
        val storageDir = File(getExternalStoragePublicDirectory(DIRECTORY_PICTURES)
                .absolutePath + APP_NAME)
        var success = true
        if (!storageDir.exists()) success = storageDir.mkdir()
        if (success) {
            val fileName = "JPEG_" + System.currentTimeMillis().toString()
            val imageFile = File(storageDir, fileName)
            savedImagePath = imageFile.absolutePath
            val fos = FileOutputStream(savedImagePath)
            image.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.close()
            gallaryAddPic(context, savedImagePath)
            makeText(context, context.getString(R.string.saved_message), Toast.LENGTH_SHORT).show()
        } else makeText(context, context.getString(R.string.not_saved), Toast.LENGTH_LONG).show()
    }

    private fun gallaryAddPic(context: Context, imagePath: String) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val imageUri = Uri.fromFile(File(imagePath))
        mediaScanIntent.data = imageUri
        context.sendBroadcast(mediaScanIntent)
    }

    fun shareImage(context: Context, imagePath: String, fileProviderAuthority: String) {
        val file = File(imagePath)
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "image/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM,
                FileProvider.getUriForFile(context, fileProviderAuthority, file))
        context.startActivity(shareIntent)
    }

}