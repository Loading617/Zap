package com.loading.television.util

import android.app.SearchManager
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.provider.BaseColumns
import android.util.Log
import androidx.core.net.toUri
import com.loading.medialibrary.interfaces.Medialibrary
import com.loading.medialibrary.interfaces.media.MediaWrapper
import com.loading.moviepedia.database.models.MediaMetadataType
import com.loading.moviepedia.database.models.getYear
import com.loading.moviepedia.database.models.subtitle
import com.loading.moviepedia.database.models.tvEpisodeSubtitle
import com.loading.moviepedia.repository.MediaMetadataRepository
import com.loading.resources.CONTENT_EPISODE
import com.loading.resources.CONTENT_RESUME
import com.loading.tools.Settings
import com.loading.vlc.BuildConfig
import com.loading.zap.R
import com.loading.zap.getFileUri
import com.loading.zap.util.ThumbnailsProvider
import java.util.Locale

class TVSearchProvider : ContentProvider() {
    override fun insert(uri: Uri, values: ContentValues?): Uri? =
        throw UnsupportedOperationException("Requested operation not supported")

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        return if (uri.pathSegments.firstOrNull() == "search") {
            selectionArgs?.firstOrNull()?.let { query ->
                if (BuildConfig.DEBUG) Log.d(this::class.java.simpleName, "Voice search for ${query.replace(Regex("[^A-Za-z0-9 ]"), "")}")
                val medialibrary = Medialibrary.getInstance()
                val columns = arrayOf(BaseColumns._ID, SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2, SearchManager.SUGGEST_COLUMN_RESULT_CARD_IMAGE, SearchManager.SUGGEST_COLUMN_PRODUCTION_YEAR, SearchManager.SUGGEST_COLUMN_DURATION)

                val matrixCursor = MatrixCursor(columns)

                val sanitizedQuery = query.replace(Regex("[^A-Za-z0-9 ]"), "").lowercase(Locale.getDefault())

                val mlIds = ArrayList<Long>()
                //Moviepedia
                context?.let { context ->
                    if (BuildConfig.DEBUG) Log.d(this::class.java.simpleName, "Looking for '${"%$sanitizedQuery%".replace(" ", "%")}' in moviepedia")
                    val mediaMetadataRepository = MediaMetadataRepository.getInstance(context)
                    val mediaMetadatas = mediaMetadataRepository.searchMedia("%$sanitizedQuery%".replace(" ", "%"))
                    if (BuildConfig.DEBUG) Log.d(this::class.java.simpleName, "Found ${mediaMetadatas.size} entries in moviepedia")
                    mediaMetadatas.forEach { mediaMetadataWithImages ->
                        mediaMetadataWithImages.metadata.mlId?.let { mlId ->
                            mlIds.add(mlId)
                            val media = medialibrary.getMedia(mlId)
                            val thumbnail = mediaMetadataWithImages.metadata.currentBackdrop
                            matrixCursor.addRow(arrayOf(media.id, "media_${media.id}", mediaMetadataWithImages.metadata.title, mediaMetadataWithImages.subtitle(), thumbnail, mediaMetadataWithImages.metadata.getYear(), media.length))
                        }
                            ?: if (mediaMetadataWithImages.metadata.type == MediaMetadataType.TV_SHOW) {
                                val provider = com.loading.moviepedia.provider.MediaScrapingTvshowProvider(context)
                                if (BuildConfig.DEBUG) Log.d(this::class.java.simpleName, "Looking for episodes for ${mediaMetadataWithImages.metadata.title}")
                                val mediaMetadataEpisodes = mediaMetadataRepository.getTvShowEpisodes(mediaMetadataWithImages.metadata.moviepediaId)

                                provider.getFirstResumableEpisode(medialibrary, mediaMetadataEpisodes)?.let { firstResumableEpisode ->
                                    val media = medialibrary.getMedia(firstResumableEpisode.metadata.mlId!!)
                                    val thumbnail = mediaMetadataWithImages.metadata.currentBackdrop
                                    matrixCursor.addRow(arrayOf(media.id, "${CONTENT_RESUME}${mediaMetadataWithImages.metadata.moviepediaId}", mediaMetadataWithImages.metadata.title, context.getString(R.string.resume_episode, firstResumableEpisode.tvEpisodeSubtitle()), thumbnail, firstResumableEpisode.metadata.getYear(), media.length))
                                }



                                if (BuildConfig.DEBUG) Log.d(this::class.java.simpleName, "Found ${mediaMetadatas.size} entries in moviepedia")
                                mediaMetadataEpisodes.forEach { mediaMetadataWithImages ->
                                    mediaMetadataWithImages.metadata.mlId?.let { mlId ->
                                        mlIds.add(mlId)
                                        val media = medialibrary.getMedia(mlId)
                                        val thumbnail = mediaMetadataWithImages.metadata.currentBackdrop
                                        matrixCursor.addRow(arrayOf(media.id, "${CONTENT_EPISODE}${mediaMetadataWithImages.metadata.moviepediaId}", mediaMetadataWithImages.metadata.title, mediaMetadataWithImages.subtitle(), thumbnail, mediaMetadataWithImages.metadata.getYear(), media.length))
                                    }
                                }
                            }
                            else { }
                    }
                }

                val searchAggregate = medialibrary.search(sanitizedQuery, Settings.includeMissing, false)
                    ?: return null
                searchAggregate.artists?.filterNotNull()?.let {
                    it.forEach { media ->
                        val thumbnail = if (media.artworkMrl != null) getFileUri(media.artworkMrl) else ""
                        if (BuildConfig.DEBUG) Log.d(this::class.java.simpleName, "Adding artist ${media.title}")
                        matrixCursor.addRow(arrayOf(media.id, "artist_${media.id}", media.title, media.description, thumbnail, "", -1))
                    }

                }
                searchAggregate.albums?.filterNotNull()?.let {
                    it.forEach { media ->
                        val thumbnail = if (media.artworkMrl != null) getFileUri(media.artworkMrl) else ""
                        if (BuildConfig.DEBUG) Log.d(this::class.java.simpleName, "Adding album ${media.title}")
                        matrixCursor.addRow(arrayOf(media.id, "album_${media.id}", media.title, media.description, thumbnail, media.releaseYear, media.duration))
                    }

                }
                searchAggregate.videos?.filterNotNull()?.let {
                    it.forEach { media ->
                        if (mlIds.contains(media.id)) return@forEach
                        val thumbnail = if (media.artworkURL != null) getFileUri(media.artworkURL) else media.getThumb()
                        if (BuildConfig.DEBUG) Log.d(this::class.java.simpleName, "Adding video ${media.title}")
                        matrixCursor.addRow(arrayOf(media.id, "media_${media.id}", media.title, media.description, thumbnail, media.date, media.length))
                    }

                }
                searchAggregate.tracks?.filterNotNull()?.let {
                    it.forEach { media ->
                        val thumbnail = if (media.artworkURL != null) getFileUri(media.artworkURL) else media.getThumb()
                        if (BuildConfig.DEBUG) Log.d(this::class.java.simpleName, "Adding track ${media.title}")
                        matrixCursor.addRow(arrayOf(media.id, "media_${media.id}", media.title, media.description, thumbnail, media.releaseYear, media.length))
                    }

                }
                if (BuildConfig.DEBUG) Log.d(this::class.java.simpleName, "Found ${matrixCursor.count} results")
                matrixCursor
            }
        } else {
            throw IllegalArgumentException("Invalid URI: $uri")
        }
    }

    override fun onCreate() = true

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int =
        throw UnsupportedOperationException("Requested operation not supported")

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int =
        throw UnsupportedOperationException("Requested operation not supported")

    override fun getType(uri: Uri): String? = null
}

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