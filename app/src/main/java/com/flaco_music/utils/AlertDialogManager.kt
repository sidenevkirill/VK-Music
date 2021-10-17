package com.flaco_music.utils

import android.app.AlertDialog
import android.content.Context

class AlertDialogManager(private val context: Context) {
    fun showDialog(
        title: String,
        message: String? = null,
        iconResId: Int? = null,
        positiveButtonText: String? = null,
        onPositiveButtonClicked: (() -> Unit)? = null,
        negativeButtonText: String? = null,
        onNegativeButtonClicked: (() -> Unit)? = null
    ) {
        val builder = AlertDialog.Builder(context)
            .setTitle(title)

        if (message != null) {
            builder.setMessage(message)
        }

        if (iconResId != null) {
            builder.setIcon(iconResId)
        }

        if (positiveButtonText != null) {
            builder.setPositiveButton(positiveButtonText) { _, _ ->
                onPositiveButtonClicked?.invoke()
            }
        }

        if (negativeButtonText != null) {
            builder.setNegativeButton(negativeButtonText) { _, _ ->
                onNegativeButtonClicked?.invoke()
            }
        }

        builder.show()
    }
}