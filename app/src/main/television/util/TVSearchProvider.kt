package com.loading.television.util

import android.net.Uri
import androidx.core.net.toUri
import com.loading.medialibrary.interfaces.media.MediaWrapper
import com.loading.zap.BuildConfig
import com.loading.zap.R
import com.loading.zap.getFileUri
import com.loading.zap.util.ThumbnailsProvider

private fun MediaWrapper.getThumb(): Uri {
    if (!isThumbnailGenerated) {
        ThumbnailsProvider.getVideoThumbnail(this@getThumb, 512)
    }
    val resourceUri = "android.resource://${BuildConfig.APP_ID}/${R.drawable.ic_video_big}".toUri()
    val mrl = artworkMrl ?: return resourceUri
    return try {
        getFileUri(mrl)
    } catch (ex: IllegalArgumentException) {
        resourceUri
    }