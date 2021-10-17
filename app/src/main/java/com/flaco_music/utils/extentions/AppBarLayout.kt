package com.flaco_music.utils.extentions

import android.animation.ArgbEvaluator
import android.app.Activity
import android.content.res.Configuration
import android.graphics.Color
import android.view.View
import androidx.core.content.ContextCompat
import com.baoyz.widget.PullRefreshLayout
import com.flaco_music.R
import com.google.android.material.appbar.AppBarLayout
import kotlin.math.abs


fun AppBarLayout.setupWithStatusBar(
    activity: Activity,
    refreshView: PullRefreshLayout? = null,
    expandedColor: Int = Color.TRANSPARENT
) {
    addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
        val maxVerticalOffset = appBarLayout.totalScrollRange
        activity.window.apply {
            statusBarColor = ArgbEvaluator().evaluate(
                (verticalOffset.toFloat() * -1) / maxVerticalOffset,
                expandedColor,
                ContextCompat.getColor(activity, R.color.background)
            ) as Int
        }

        val isFullyShown = abs(verticalOffset) - appBarLayout.totalScrollRange != 0

        val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        if (!isFullyShown) {
            if (nightModeFlags != Configuration.UI_MODE_NIGHT_YES) {
                val view: View = activity.window.decorView
                view.systemUiVisibility = view.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        } else {
            val view: View = activity.window.decorView
            view.systemUiVisibility = view.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        }

        refreshView?.let { it.isEnabled = isFullyShown }
    })
}