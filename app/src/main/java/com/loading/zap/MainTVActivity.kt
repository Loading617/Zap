package com.loading.zap.television.ui

import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.ProgressBar
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.loading.medialibrary.interfaces.Medialibrary
import com.loading.television.R
import com.loading.television.ui.browser.BaseTvActivity
import com.loading.tools.KEY_SHOW_UPDATE
import com.loading.tools.RESULT_RESCAN
import com.loading.tools.RESULT_RESTART
import com.loading.tools.RESULT_RESTART_APP
import com.loading.tools.Settings
import com.loading.zap.ScanProgress
import com.loading.zap.StartActivity
import com.loading.zap.gui.dialogs.UPDATE_DATE
import com.loading.zap.gui.dialogs.UPDATE_URL
import com.loading.zap.gui.dialogs.UpdateDialog
import com.loading.zap.gui.helpers.hf.StoragePermissionsDelegate
import com.loading.zap.reloadLibrary
import com.loading.zap.util.AutoUpdate
import com.loading.zap.util.LifecycleAwareScheduler
import com.loading.zap.util.SchedulerCallback
import com.loading.zap.util.Util

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
class MainTvActivity : BaseTvActivity(), StoragePermissionsDelegate.CustomActionController, SchedulerCallback {

    private lateinit var browseFragment: MainTvFragment
    private lateinit var progressBar: ProgressBar
    lateinit var scheduler: LifecycleAwareScheduler


    override fun onTaskTriggered(id: String, data: Bundle) {
        when (id) {
            SHOW_LOADING -> progressBar.visibility = View.VISIBLE
            HIDE_LOADING -> {
                scheduler.cancelAction(SHOW_LOADING)
                progressBar.visibility = View.GONE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scheduler =  LifecycleAwareScheduler(this)

        Util.checkCpuCompatibility(this)

        setContentView(R.layout.tv_main)

        val fragmentManager = supportFragmentManager
        browseFragment = fragmentManager.findFragmentById(R.id.browse_fragment) as MainTvFragment
        progressBar = findViewById(R.id.tv_main_progress)
        lifecycleScope.launch {
            AutoUpdate.clean(this@MainTvActivity.application)
            if (!Settings.getInstance(this@MainTvActivity).getBoolean(KEY_SHOW_UPDATE, true)) return@launch
            AutoUpdate.checkUpdate(this@MainTvActivity.application) {url, date ->
                val updateDialog = UpdateDialog().apply {
                    arguments = bundleOf(UPDATE_URL to url, UPDATE_DATE to date.time)
                }
                updateDialog.show(supportFragmentManager, "fragment_update")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ACTIVITY_RESULT_PREFERENCES) {
            when (resultCode) {
                RESULT_RESCAN -> this.reloadLibrary()
                RESULT_RESTART, RESULT_RESTART_APP -> {
                    val intent = Intent(this, if (resultCode == RESULT_RESTART_APP) StartActivity::class.java else MainTvActivity::class.java)
                    finish()
                    startActivity(intent)
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || keyCode == KeyEvent.KEYCODE_BUTTON_Y) {
            browseFragment.showDetails()
        } else super.onKeyDown(keyCode, event)
    }

    override fun onParsingServiceStarted() {
        scheduler.startAction(SHOW_LOADING)
    }

    override fun onParsingServiceProgress(scanProgress: ScanProgress?) {
        if (progressBar.visibility == View.GONE && Medialibrary.getInstance().isWorking)
            scheduler.startAction(SHOW_LOADING)
    }

    override fun onParsingServiceFinished() {
        if (!Medialibrary.getInstance().isWorking)
            scheduler.scheduleAction(HIDE_LOADING, 500)
    }

    fun hideLoading() {
        scheduler.scheduleAction(HIDE_LOADING, 500)
    }

    override fun onStorageAccessGranted() {
        refresh()
    }

    override fun refresh() {
        this.reloadLibrary()
    }

    companion object {

        const val ACTIVITY_RESULT_PREFERENCES = 1

        const val BROWSER_TYPE = "browser_type"

        const val TAG = "VLC/MainTvActivity"
        private const val SHOW_LOADING = "show_loading"
        private const val HIDE_LOADING = "hide_loading"
    }
}
