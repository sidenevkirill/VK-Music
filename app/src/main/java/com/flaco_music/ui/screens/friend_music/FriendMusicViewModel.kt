package com.flaco_music.ui.screens.friend_music

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.flaco_music.retrofit.models.Audio
import com.flaco_music.retrofit.models.Playlist
import com.flaco_music.ui.screens.player.PlayerViewModel
import com.flaco_music.utils.extentions.coroutineIO
import com.flaco_music.utils.extentions.invoke
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent

@KoinApiExtension
class FriendMusicViewModel(application: Application) : PlayerViewModel(application), KoinComponent {

    override var playlistTypeName: String = PLAYLIST_NAME

    val friendMusicInfoLiveData: LiveData<FriendMusicInfo> = MutableLiveData()
    val playlistsLiveData: LiveData<List<Playlist>> = MutableLiveData()
    val tracksLiveData: LiveData<List<Audio>> = MutableLiveData()
    val isLoadingLiveData: LiveData<Boolean> = MutableLiveData()
    val errorMessageLiveData: LiveData<String> = MutableLiveData()

    fun loadMusicPage(ownerId: Int) {
        (isLoadingLiveData as MutableLiveData).postValue(true)

        playlistTypeName = "${PLAYLIST_NAME}_$ownerId"

        val call = audioRepositoryWithCaching.getMusicPage(ownerId)

        call.invoke(
            onResponseSuccessful = { response ->
                coroutineIO {
                    response.body()?.response?.let {
                        val friendMusicInfo = FriendMusicInfo(
                            name = it.owner.name ?: "${it.owner.firstName} ${it.owner.lastName}",
                            numberOfTracks = it.audios.count
                        )

                        val tracks = it.audios.items

                        setupCurrentPlaylist(tracks)

                        val playlists = it.playlists.items

                        (friendMusicInfoLiveData as MutableLiveData).postValue(friendMusicInfo)
                        (tracksLiveData as MutableLiveData).postValue(tracks)
                        (playlistsLiveData as MutableLiveData).postValue(playlists)
                    }
                }
            },
            onResponseNotSuccessful = { errorResponse ->
                val errorMessage = errorResponse.errorDescription
                (errorMessageLiveData as MutableLiveData).postValue(errorMessage)
            },
            anyway = {
                isLoadingLiveData.postValue(false)
            }
        )
    }

    companion object {
        private const val TAG = "FriendMusicViewModel"
        private const val PLAYLIST_NAME = "friend_music"
    }
}