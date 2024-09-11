package com.loading.television.ui

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import org.videolan.medialibrary.interfaces.media.MediaWrapper
import org.videolan.resources.util.parcelable
import org.videolan.television.R
import org.videolan.television.ui.browser.BaseTvActivity

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
class MediaScrapingTvActivity : BaseTvActivity() {

    private lateinit var fragment: MediaScrapingTvFragment
    private lateinit var emptyView: TextView

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tv_next)

        fragment = MediaScrapingTvFragment().apply { arguments = bundleOf(MEDIA to
                intent.parcelable<MediaWrapper>(MEDIA)) }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_placeholder, fragment)
            .commit()


        emptyView = findViewById(R.id.empty)
    }

    override fun refresh() {
        fragment.refresh()
    }

    fun updateEmptyView(empty: Boolean) {
        emptyView.visibility = if (empty) View.VISIBLE else View.GONE
    }

    override fun onSearchRequested(): Boolean {
        fragment.startRecognition()
        return true
    }

    companion object {
        const val MEDIA: String = "MEDIA"
        private const val TAG = "VLC/MediaScrapingTvActivity"
    }
}