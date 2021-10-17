package com.flaco_music.utils

import android.app.Activity
import com.flaco_music.R
import com.google.android.play.core.review.ReviewManagerFactory
import org.koin.core.component.KoinComponent

object RateDialogManager : KoinComponent {

    fun show(activity: Activity) {
        val alertDialogManager = AlertDialogManager(activity)
        alertDialogManager.showDialog(
            title = activity.getString(R.string.rate_dialog_title),
            positiveButtonText = activity.getString(R.string.yes),
            negativeButtonText = activity.getString(R.string.cancel),
            onPositiveButtonClicked = {
                val manager = ReviewManagerFactory.create(activity)
                val request = manager.requestReviewFlow()
                request.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val reviewInfo = task.result
                        manager.launchReviewFlow(activity, reviewInfo)
                    }
                }
            }
        )
    }
}