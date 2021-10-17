package com.flaco_music.utils.firebase

import android.content.Context
import com.flaco_music.R
import com.flaco_music.utils.RandomUtils
import com.flaco_music.utils.SnackbarManager
import com.flaco_music.utils.preferences.Preferences
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale


class FirebaseManager(private val context: Context) : KoinComponent {

    private val preferences: Preferences by inject()
    private val snackbarManager: SnackbarManager by inject()

    private val database = Firebase.database
    private val usersReference = database.getReference("users")
    private val nextReleaseReference = database.getReference("next_release")
    private val feedbacksReference = database.getReference("feedbacks")

    private val currentDate: String
        get() {
            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            return dateFormat.format(Calendar.getInstance().time)
        }

    fun registerUser() {
        val user = User(
            first_name = preferences.firstName,
            last_name = preferences.lastName,
            has_subscription = preferences.hasSubscription
        )
        usersReference
            .child(preferences.userId.toString())
            .setValue(user)
    }

    fun updateSubscription(hasSubscription: Boolean) {
        usersReference
            .child(preferences.userId.toString())
            .child("has_subscription")
            .setValue(hasSubscription)
    }

    fun sendFeedback(message: String) {
        val feedbackId = RandomUtils.getSaltNumbersString()
        val feedback = Feedback(
            message = message,
            date = currentDate
        )
        feedbacksReference
            .child(feedbackId)
            .setValue(feedback)
            .addOnSuccessListener {
                snackbarManager.showSnackbar(
                    context.getString(R.string.feedback_sent),
                    SnackbarManager.SnackbarType.SUCCESS
                )
            }
            .addOnFailureListener {
                snackbarManager.showSnackbar(
                    context.getString(R.string.feedback_not_saved),
                    SnackbarManager.SnackbarType.ERROR
                )
            }
    }

    fun isNextReleaseComing(): Task<DataSnapshot> {
        return nextReleaseReference.child("is_coming").get()
    }

    fun getCurrentMarketVersion(): Task<DataSnapshot> {
        return nextReleaseReference.child("current_market_version").get()
    }

    fun getReleaseFeaturesList(): Task<DataSnapshot> {
        val langRefKey = if (Locale.getDefault().language == "ru") "ru" else "en"
        return nextReleaseReference.child("features")
            .child(langRefKey)
            .get()
    }

    data class User(
        val first_name: String,
        val last_name: String,
        val has_subscription: Boolean
    )

    data class Feedback(
        val message: String,
        val date: String
    )
}