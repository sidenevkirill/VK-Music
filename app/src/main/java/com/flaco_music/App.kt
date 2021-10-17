package com.flaco_music

import android.app.Application
import android.util.Log
import com.downloader.PRDownloader
import com.downloader.PRDownloaderConfig
import com.flaco_music.db.my_music_ids.MyMusicIdItem
import com.flaco_music.db.my_music_ids.MyMusicIdsRepository
import com.flaco_music.db.player_config.PlayerConfigRepository
import com.flaco_music.koin.module
import com.flaco_music.retrofit.repository.AudioRepository
import com.flaco_music.utils.extentions.coroutineIO
import com.flaco_music.utils.extentions.invoke
import com.flaco_music.utils.firebase.FirebaseManager
import com.flaco_music.utils.preferences.Preferences
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.parameter.parametersOf


class App : Application(), KoinComponent {

    private val preferences: Preferences by inject()
    private val firebaseManager: FirebaseManager by inject()

    private val audioRepository: AudioRepository by inject { parametersOf(true) }
    private val myMusicIdsRepository: MyMusicIdsRepository by inject()
    private val playerConfigRepository: PlayerConfigRepository by inject()

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(module)
        }

        preferences.isDarkModeEnabled = resources.configuration.uiMode == 33
        preferences.appLaunches++

        initDownloader()

        checkNextReleaseIsComing()
        checkNextReleaseIsAvailable()

        fetchMyAudios()

        playerConfigRepository.create()
    }

    private fun initDownloader() {
        val config = PRDownloaderConfig.newBuilder()
            .setDatabaseEnabled(true)
            .setReadTimeout(30_000)
            .setConnectTimeout(30_000)
            .build()

        PRDownloader.initialize(applicationContext, config)
    }

    private fun fetchMyAudios() {
        val call = audioRepository.getMusicPage(preferences.userId)

        call.invoke(
            onResponseSuccessful = { response ->
                coroutineIO {
                    response.body()?.response?.let {
                        val tracks = it.audios.items
                        preferences.firstName = it.owner.firstName ?: ""
                        preferences.lastName = it.owner.lastName ?: ""
                        coroutineIO {
                            myMusicIdsRepository.insert(tracks.map { track ->
                                MyMusicIdItem(
                                    track.id,
                                    track.decryptedUrl
                                )
                            })
                        }
                    }
                }
            }
        )
    }

    private fun checkNextReleaseIsComing() {
        firebaseManager.isNextReleaseComing().addOnCompleteListener {
            try {
                val isNextReleaseComing = it.result.value as Boolean
                preferences.showNextReleaseIsComingDialog = isNextReleaseComing
                if (!isNextReleaseComing) {
                    preferences.hasNextReleaseComingDialogBeenShown = false
                }
            } catch (e: Exception) {
            }
        }
    }

    private fun checkNextReleaseIsAvailable() {
        firebaseManager.getCurrentMarketVersion().addOnCompleteListener {
            try {
                val currentMarketVersionName = it.result.value as Double
                val currentVersionName = BuildConfig.VERSION_NAME.toDouble()
                Log.d("fdsfsdfdsfsd", "currentMarketVersionName: $currentMarketVersionName")
                Log.d("fdsfsdfdsfsd", "currentVersionName: $currentVersionName")
                preferences.showNextReleaseIsAvailableDialog = currentVersionName < currentMarketVersionName
            } catch (e: Exception) {
            }
        }
    }
}