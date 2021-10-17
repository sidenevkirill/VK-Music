package com.flaco_music.ui.screens.my_music

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.flaco_music.db.my_music_ids.MyMusicIdItem
import com.flaco_music.db.my_music_ids.MyMusicIdsRepository
import com.flaco_music.retrofit.models.Audio
import com.flaco_music.retrofit.models.Playlist
import com.flaco_music.ui.screens.player.PlayerViewModel
import com.flaco_music.utils.extentions.coroutineIO
import com.flaco_music.utils.extentions.invoke
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class MyMusicViewModel(application: Application) : PlayerViewModel(application), KoinComponent {

    override var playlistTypeName: String = PLAYLIST_NAME

    val playlistsLiveData: LiveData<List<Playlist>> = MutableLiveData()
    val tracksLiveData: LiveData<List<Audio>> = MutableLiveData()
    val isLoadingLiveData: LiveData<Boolean> = MutableLiveData()
    val errorMessageLiveData: LiveData<String> = MutableLiveData()
    val isScrolledLiveData: LiveData<Boolean> = MutableLiveData(false)

    private val myMusicIdsRepository: MyMusicIdsRepository by inject()

    fun loadMusicPage(isForcibly: Boolean, delay: Long = 0) {
        (isLoadingLiveData as MutableLiveData).postValue(true)

        coroutineIO {
            delay(delay)

            val call = if (isForcibly) {
                audioRepository.getMusicPage(preferences.userId)
            } else {
                audioRepositoryWithCaching.getMusicPage(preferences.userId)
            }

            call.invoke(
                onResponseSuccessful = { response ->
                    coroutineIO {
                        response.body()?.response?.let {
                            val tracks = it.audios.items
                            val playlists = it.playlists.items

                            playlistTypeName = "${PLAYLIST_NAME}_${tracks.size}_${playlists.size}"

                            setupCurrentPlaylist(tracks)

                            myMusicIdsRepository.insert(tracks.map { track ->
                                MyMusicIdItem(
                                    track.id,
                                    track.decryptedUrl
                                )
                            })

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
                    GlobalScope.launch {
                        isLoadingLiveData.postValue(false)
                    }
                }
            )
        }
    }

    fun setScrolled(isScrolled: Boolean) {
        (isScrolledLiveData as MutableLiveData).postValue(isScrolled)
    }

    companion object {
        private const val TAG = "MyMusicViewModel"
        private const val PLAYLIST_NAME = "my_music"
    }
}