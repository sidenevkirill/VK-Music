package com.flaco_music.ui.dialogs.artist_options

import android.app.Application
import com.flaco_music.ui.screens.player.PlayerViewModel
import com.flaco_music.utils.RandomUtils
import com.flaco_music.utils.extentions.invoke

class ArtistOptionsViewModel(application: Application) : PlayerViewModel(application) {

    override var playlistTypeName: String = PLAYLIST_NAME

    fun shufflePlay(artistId: String) {
        audioRepository.getAudiosByArtist(artistId).invoke(
            onResponseSuccessful = {
                it.body()?.response?.items?.let { tracks ->
                    playlistTypeName = "${PLAYLIST_NAME}_$artistId"
                    setupCurrentPlaylist(tracks)
                    play(RandomUtils.getRandomNumber(0, tracks.lastIndex))
                }
            }
        )
    }

    companion object {
        private const val PLAYLIST_NAME = "artist_options"
    }
}