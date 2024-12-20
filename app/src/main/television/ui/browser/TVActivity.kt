package com.loading.zap.television.ui.browser

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.loading.television.R
import com.loading.zap.gui.network.MRLPanelFragment
import com.loading.television.ui.MainTvActivity
import com.loading.resources.HEADER_STREAM

class TVActivity : BaseTvActivity() {

    private lateinit var fragment: Fragment

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tv_vertical_grid)
        if (savedInstanceState == null) {
            val type = intent.getLongExtra(MainTvActivity.BROWSER_TYPE, -1)
            if (type == HEADER_STREAM) {
                fragment = MRLPanelFragment()
            } else {
                finish()
                return
            }
            supportFragmentManager.beginTransaction()
                .add(R.id.tv_fragment_placeholder, fragment)
                .commit()
        }
    }

    override fun refresh() { }
}