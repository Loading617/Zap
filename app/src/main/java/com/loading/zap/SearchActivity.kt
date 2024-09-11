package com.loading.television.ui

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.TextView
import org.videolan.television.R
import org.videolan.television.ui.browser.BaseTvActivity

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
class SearchActivity : BaseTvActivity() {

    private lateinit var fragment: SearchFragment
    private var emptyView: TextView? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tv_search)
        fragment = supportFragmentManager.findFragmentById(R.id.search_fragment) as SearchFragment
        emptyView = findViewById(R.id.empty)
    }

    override fun refresh() { }

    fun updateEmptyView(empty: Boolean) {
        emptyView!!.visibility = if (empty) View.VISIBLE else View.GONE
    }

    override fun onSearchRequested(): Boolean {
        fragment.startRecognition()
        return true
    }

    companion object {
        private const val TAG = "VLC/SearchActivity"
    }
}