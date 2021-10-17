package com.flaco_music.ui.screens.splash

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.flaco_music.ui.screens.MainActivity
import com.flaco_music.ui.screens.login.LoginActivity
import com.flaco_music.utils.preferences.Preferences
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinApiExtension

class SplashActivity : AppCompatActivity() {

    private val preferences: Preferences by inject()

    @KoinApiExtension
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (preferences.isLoggedIn) {
            MainActivity.startMe(this)
        } else {
            LoginActivity.startMe(this)
        }

        finish()
    }

    companion object {
        private const val TAG = "SplashActivity"
    }
}