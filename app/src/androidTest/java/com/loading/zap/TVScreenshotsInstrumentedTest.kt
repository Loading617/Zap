package com.loading.zap

import android.app.Activity
import android.content.Intent
import android.os.SystemClock
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import androidx.test.uiautomator.Direction
import org.hamcrest.Matchers.allOf
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test
import com.loading.resources.EXTRA_TARGET
import com.loading.television.ui.MainTvActivity
import com.loading.television.ui.audioplayer.AudioPlayerActivity
import com.loading.television.ui.browser.VerticalGridActivity
import com.loading.tools.Settings
import com.loading.zap.util.DpadHelper.pressBack
import com.loading.zap.util.DpadHelper.pressDPad
import com.loading.zap.util.DpadHelper.pressDPadCenter
import com.loading.zap.util.DpadHelper.pressHome
import com.loading.zap.util.DpadHelper.pressPip
import com.loading.zap.util.ScreenshotUtil
import com.loading.zap.util.UiUtils
import com.loading.zap.util.UiUtils.waitForActivity
import com.loading.zap.util.UiUtils.waitId
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy
import tools.fastlane.screengrab.locale.LocaleTestRule

//@RunWith(AndroidJUnit4::class)
class TvScreenhotsInstrumentedTest : BaseUITest() {
    @Rule
    @JvmField
    val activityTestRule = ActivityTestRule(MainTvActivity::class.java, true, false)

    @Rule
    @JvmField
    val demoModeRule = DemoModeRule()

    lateinit var activity: MainTvActivity

    companion object {
        @ClassRule
        @JvmField
        val localeTestRule = LocaleTestRule()
    }

    override fun beforeTest() {
        Screengrab.setDefaultScreenshotStrategy(UiAutomatorScreenshotStrategy())
        Settings.getInstance(context).edit().putBoolean("auto_rescan", false).putBoolean("audio_resume_card", false).commit()
        val intent = Intent().apply {
            putExtra(EXTRA_TARGET, R.id.nav_audio)
        }
        activityTestRule.launchActivity(intent)
        activity = activityTestRule.activity
    }

    fun getCurrentActivity(): Activity? {
        var currentActivity: Activity? = null
        InstrumentationRegistry.getInstrumentation().runOnMainSync { run { currentActivity = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED).elementAtOrNull(0) } }
        return currentActivity
    }

    @Test
    fun testTakeScreenshot() {
        SystemClock.sleep(1500)
        //Audio
        ScreenshotUtil.takeScreenshot(1, "tv_home")

        onView(allOf(withId(R.id.row_header), withText(activity.getString(R.string.audio)), withEffectiveVisibility(Visibility.VISIBLE))).perform(click())
        pressDPad(Direction.RIGHT, 4)
        pressDPadCenter()

        waitForActivity(VerticalGridActivity::class.java)

        getCurrentActivity()?.let { activity ->
            UiUtils.waitUntilLoaded { activity.findViewById(com.loading.television.R.id.list) }
        } ?: throw IllegalStateException("Cannot find activity")

        ScreenshotUtil.takeScreenshot(4,"tv_audio_list")
        pressDPad(Direction.DOWN, 4)
        pressDPadCenter()
        waitForActivity(AudioPlayerActivity::class.java)

        waitId(com.loading.television.R.id.album_cover, 5000)
        ScreenshotUtil.takeScreenshot(6,"tv_audio_player")
        pressBack()
        pressBack()
        pressDPad(Direction.DOWN, 2)
        pressDPadCenter()

        waitForActivity(VerticalGridActivity::class.java)
        getCurrentActivity()?.let { activity ->
            UiUtils.waitUntilLoaded { activity.findViewById(com.loading.television.R.id.list) }
        }
        ScreenshotUtil.takeScreenshot(5,"tv_files")
        pressBack()
        pressDPad(Direction.LEFT)
        onView(allOf(withId(R.id.row_header), withText(activity.getString(R.string.video)), withEffectiveVisibility(Visibility.VISIBLE))).perform(click())
        pressDPadCenter()
        pressDPadCenter()
        waitForActivity(VerticalGridActivity::class.java)
        getCurrentActivity()?.let { activity ->
            UiUtils.waitUntilLoaded { activity.findViewById(com.loading.television.R.id.list) }
        }
        ScreenshotUtil.takeScreenshot(2,"tv_video_list")
        pressDPad(Direction.DOWN, 1)
        pressDPadCenter()
        SystemClock.sleep(1500)
        pressDPad(Direction.DOWN)
        SystemClock.sleep(600)
        ScreenshotUtil.takeScreenshot(3,"tv_video_player")
        Espresso.pressBack()
        pressPip()
        pressHome()
        pressPip()
        SystemClock.sleep(1500)
        ScreenshotUtil.takeScreenshot(7,"tv_pip")
    }
}