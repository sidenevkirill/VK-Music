package com.flaco_music.utils.downloader

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.flaco_music.R
import com.flaco_music.db.current_playlist.CurrentPlaylistItem
import com.flaco_music.db.downloads.DownloadsRepository
import com.flaco_music.retrofit.models.Audio
import com.flaco_music.utils.SnackbarManager
import com.flaco_music.utils.preferences.Preferences
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File


class Downloader(private val context: Context) : KoinComponent {

    val DIRECTORY_PATH = "${context.filesDir.absolutePath}/Music"

    private val downloadsRepository: DownloadsRepository by inject()
    private val snackbarManager: SnackbarManager by inject()
    private val preferences: Preferences by inject()

    fun saveTrack(track: Audio) {
        performDownload(track)
    }

    fun saveCurrentPlaylistItem(track: CurrentPlaylistItem) {
        performDownload(track)
    }

    fun delete(trackId: Int) {
        val filePath = "$DIRECTORY_PATH/$trackId.mp3"
        val file = File(filePath)
        file.delete()
        GlobalScope.launch { downloadsRepository.delete(trackId) }
        snackbarManager.showSnackbar(
            context.getString(R.string.track_deleted_from_saved),
            SnackbarManager.SnackbarType.SUCCESS
        )
    }

    private fun performDownload(track: Audio): Int {
        Log.d("PlayerService", "performDownload: ${"${DIRECTORY_PATH}/${track.id}.mp3".toUri()}")
        return PRDownloader.download(track.decryptedUrl, DIRECTORY_PATH, "${track.id}.mp3")
            .build()
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    GlobalScope.launch { downloadsRepository.insertTrack(track) }
                    if (!preferences.isAutoCachingEnabled) {
                        snackbarManager.showSnackbar(
                            context.getString(R.string.track_saved),
                            SnackbarManager.SnackbarType.SUCCESS
                        )
                    }
                }

                override fun onError(error: Error?) {
                    if (!preferences.isAutoCachingEnabled) {
                        snackbarManager.showSnackbar(
                            context.getString(R.string.track_not_saved),
                            SnackbarManager.SnackbarType.ERROR
                        )
                    }
                }
            })
    }

    private fun performDownload(track: CurrentPlaylistItem): Int {
        return PRDownloader.download(track.url, DIRECTORY_PATH, "${track.id}.mp3")
            .build()
            .start(object : OnDownloadListener {
                override fun onDownloadComplete() {
                    GlobalScope.launch { downloadsRepository.insertCurrentPlaylistItem(track) }
                    snackbarManager.showSnackbar(
                        context.getString(R.string.track_saved),
                        SnackbarManager.SnackbarType.SUCCESS
                    )
                }

                override fun onError(error: Error?) {
                    snackbarManager.showSnackbar(
                        context.getString(R.string.track_not_saved),
                        SnackbarManager.SnackbarType.ERROR
                    )
                }
            })
    }
}