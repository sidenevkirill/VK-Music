package com.flaco_music.ui.dialogs.playlist_options

import android.app.Application
import com.flaco_music.R
import com.flaco_music.retrofit.models.Audio
import com.flaco_music.ui.screens.player.PlayerViewModel
import com.flaco_music.utils.RandomUtils
import com.flaco_music.utils.SnackbarManager
import org.koin.core.component.inject

class PlaylistOptionsViewModel(application: Application) : PlayerViewModel(application) {

    override var playlistTypeName: String = PLAYLIST_NAME

    private val snackbarManager: SnackbarManager by inject()

    fun shufflePlay(playlistId: Int, tracks: List<Audio>) {
        playlistTypeName = "${PLAYLIST_NAME}_$playlistId"
        setupCurrentPlaylist(tracks)

        try {
            play(RandomUtils.getRandomNumber(0, tracks.lastIndex))
        } catch (e: NoSuchElementException) {
            snackbarManager.showSnackbar(
                context.getString(R.string.playlist_is_empty),
                SnackbarManager.SnackbarType.ERROR
            )
        }
    }

    companion object {
        private const val PLAYLIST_NAME = "playlist_options"
    }
}