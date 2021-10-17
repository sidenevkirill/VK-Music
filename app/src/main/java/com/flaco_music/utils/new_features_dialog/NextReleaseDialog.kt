package com.flaco_music.utils.new_features_dialog

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.WindowManager
import com.flaco_music.R
import com.flaco_music.utils.firebase.FirebaseManager
import com.google.android.gms.tasks.RuntimeExecutionException
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


object NextReleaseDialog : KoinComponent {

    private val firebaseManager: FirebaseManager by inject()

    fun showNextReleaseIsComingDialog(context: Context, onDismiss: () -> Unit) {
        try {
            val descriptionStringBuilder =
                StringBuilder(context.getString(R.string.next_release_coming_dialog_descriptions))
            firebaseManager.getReleaseFeaturesList().addOnCompleteListener {
                val featuresList =
                    (it.result.value as List<String?>?)?.filter { !it.isNullOrEmpty() } ?: return@addOnCompleteListener
                featuresList.forEach { feature ->
                    descriptionStringBuilder.append("\n• $feature")
                }

                val title = context.getString(R.string.next_release_available_dialog_title)
                val description = descriptionStringBuilder.toString()
                val buttonText = context.getString(R.string.next_release_coming_dialog_button_text)

                val alertDialog: AlertDialog = AlertDialog.Builder(context).create()
                alertDialog.setTitle(title)
                alertDialog.setMessage(description)
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, buttonText) { dialog, _ ->
                    dialog.dismiss()
                    onDismiss()
                }
                alertDialog.setOnCancelListener {
                    try {
                        onDismiss()
                    } catch (e: WindowManager.BadTokenException) {

                    }
                }
                alertDialog.show()
            }
        } catch (e: RuntimeExecutionException) {

        }
    }

    fun showNextReleaseAvailableDialog(context: Context, onPositiveClicked: () -> Unit, onDismiss: () -> Unit = {}) {
        val descriptionStringBuilder =
            StringBuilder(context.getString(R.string.next_release_coming_dialog_descriptions))
        firebaseManager.getReleaseFeaturesList().addOnCompleteListener {
            Log.d("fsdfdsfdsfds", "showNextReleaseAvailableDialog: ${it.result.value}")
            val featuresList =
                (it.result.value as List<String?>?)?.filter { !it.isNullOrEmpty() } ?: return@addOnCompleteListener
            featuresList.forEach { feature ->
                descriptionStringBuilder.append("\n• $feature")
            }

            val title = context.getString(R.string.next_release_available_dialog_title)
            val description = descriptionStringBuilder.toString()
            val buttonText = context.getString(R.string.next_release_available_dialog_button_text)

            val alertDialog: AlertDialog = AlertDialog.Builder(context).create()
            alertDialog.setTitle(title)
            alertDialog.setMessage(description)
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, buttonText) { dialog, _ ->
                dialog.dismiss()
                onDismiss()
                onPositiveClicked()
            }
            alertDialog.setOnCancelListener {
                try {
                    onDismiss()
                } catch (e: WindowManager.BadTokenException) {

                }
            }
            alertDialog.show()
        }
    }
}