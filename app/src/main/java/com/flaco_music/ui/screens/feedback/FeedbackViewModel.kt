package com.flaco_music.ui.screens.feedback

import androidx.lifecycle.ViewModel
import com.flaco_music.utils.extentions.coroutineIO
import com.flaco_music.utils.firebase.FirebaseManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class FeedbackViewModel : ViewModel(), KoinComponent {

    private val firebaseManager: FirebaseManager by inject()

    fun sendFeedback(message: String) {
        coroutineIO {
            firebaseManager.sendFeedback(message)
        }
    }
}