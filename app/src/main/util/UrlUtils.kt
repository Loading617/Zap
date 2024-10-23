package com.loading.zap.util

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.loading.zap.R


fun Context.openLinkIfPossible(url: String, size: Int = 512) {


    try {


        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        val match: List<ResolveInfo> = packageManager.queryIntentActivities(intent, 0)

        if (match.size == 1) {
            val resolveActivity = intent.resolveActivity(packageManager)
            if (resolveActivity == null || resolveActivity.packageName.startsWith("com.google.android.tv.frameworkpackagestubs")) throw IllegalStateException("No web browser found")
        }
        startActivity(intent)
    } catch (e: Exception) {
        val image = ImageView(this)


        image.setImageBitmap(UrlUtils.generateQRCode(url, size))
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.no_web_browser))
            .setMessage(getString(R.string.no_web_browser_message, url))
            .setView(image)
            .setPositiveButton(R.string.ok) { _, _ ->

            }
            .show()
    }
}

object UrlUtils {

    fun generateQRCode(url: String, size: Int = 512): Bitmap {
        val bits = QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, size, size)
        return Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    it.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        }
    }
}