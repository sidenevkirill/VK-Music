package com.flaco_music.ui.screens.genre

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.flaco_music.retrofit.models.Audio
import com.flaco_music.ui.screens.player.PlayerViewModel
import com.flaco_music.utils.extentions.coroutineIO
import com.flaco_music.utils.extentions.invoke
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent

@KoinApiExtension
class GenreViewModel(application: Application) : PlayerViewModel(application), KoinComponent {

    override var playlistTypeName: String = PLAYLIST_NAME

    val genreNameLiveData: LiveData<String> = MutableLiveData()
    val tracksLiveData: LiveData<List<Audio>> = MutableLiveData()
    val isLoadingLiveData: LiveData<Boolean> = MutableLiveData()
    val errorMessageLiveData: LiveData<String> = MutableLiveData()

    fun loadTracks(genreId: Int, genreName: String) {
        (isLoadingLiveData as MutableLiveData).postValue(true)

        (genreNameLiveData as MutableLiveData).postValue(genreName)

        playlistTypeName = "${PLAYLIST_NAME}_$genreId"

        val call = audioRepositoryWithCaching.getPopularByGenre(genreId)
        call.invoke(
            onResponseSuccessful = { response ->
                coroutineIO {
                    response.body()?.response?.let {
                        setupCurrentPlaylist(it)
                        (tracksLiveData as MutableLiveData).postValue(it)
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
        private const val PLAYLIST_NAME = "genre"
    }
}