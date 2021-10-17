package com.flaco_music.ui.screens.playlist

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.flaco_music.R
import com.flaco_music.db.player_config.PlayerConfigRepository
import com.flaco_music.retrofit.models.Audio
import com.flaco_music.retrofit.models.GetPlaylistResponseBody
import com.flaco_music.ui.screens.player.PlayerViewModel
import com.flaco_music.utils.RandomUtils
import com.flaco_music.utils.extentions.invoke
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class PlaylistViewModel(application: Application) : PlayerViewModel(application), KoinComponent {

    override var playlistTypeName: String = ""

    val playlistDataLiveData: LiveData<GetPlaylistResponseBody> = MutableLiveData()
    val tracksLiveData: LiveData<List<Audio>> = MutableLiveData()
    val isLoadingLiveData: LiveData<Boolean> = MutableLiveData()
    val errorMessageLiveData: LiveData<String> = MutableLiveData()

    val isMyPlaylist: Boolean
        get() = playlistDataLiveData.value?.playlist?.ownerId == preferences.userId

    private var playlist: List<Audio>? = null

    private val playerConfigRepository: PlayerConfigRepository by inject()

    fun loadPlaylistData(playlistId: Int, ownerId: Int) {
        (isLoadingLiveData as MutableLiveData).postValue(true)

        val playlistDataCall = audioRepository.getPlaylist(ownerId, playlistId)
        playlistDataCall.invoke(
            onResponseSuccessful = { response ->
                val body = response.body()?.response
                body?.let {
                    playlistTypeName = "${PLAYLIST_NAME}_$playlistId"
                    playlist = it.audios
                    setupCurrentPlaylist(playlist!!)
                    (tracksLiveData as MutableLiveData).postValue(it.audios)
                    (playlistDataLiveData as MutableLiveData).postValue(it)
                }
            },
            onResponseNotSuccessful = { errorResponse ->
                val errorMessage = errorResponse.errorDescription
                (errorMessageLiveData as MutableLiveData).postValue(errorMessage)
            },
            onFailure = {
                (errorMessageLiveData as MutableLiveData).postValue(context.getString(R.string.internet_connection_error_message))
            },
            anyway = {
                isLoadingLiveData.postValue(false)
            }
        )
    }

    fun onShufflePlayClicked() {
        val playlistLastIndex = runBlocking(Dispatchers.IO) {
            playlist?.lastIndex ?: -1
        }

        playerConfigRepository.setShuffleMode(true)

        play(RandomUtils.getRandomNumber(0, playlistLastIndex))
    }

    companion object {
        private const val TAG = "PlaylistViewModel"
        private const val PLAYLIST_NAME = "playlist"
    }
}