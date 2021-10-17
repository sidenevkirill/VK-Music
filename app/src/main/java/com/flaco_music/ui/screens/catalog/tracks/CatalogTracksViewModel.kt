package com.flaco_music.ui.screens.catalog.tracks

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.flaco_music.retrofit.models.Audio
import com.flaco_music.ui.screens.player.PlayerViewModel
import com.flaco_music.utils.extentions.coroutineIO
import com.flaco_music.utils.extentions.invoke

class CatalogTracksViewModel(application: Application) : PlayerViewModel(application) {

    override var playlistTypeName: String = ""

    val titleLiveData: LiveData<String> = MutableLiveData()
    val tracksLiveData: LiveData<List<Audio>> = MutableLiveData()
    val isLoadingLiveData: LiveData<Boolean> = MutableLiveData()
    val errorMessageLiveData: LiveData<String> = MutableLiveData()

    fun loadTracks(sectionId: String) {
        val call = audioRepositoryWithCaching.getCatalog()
        call.invoke(
            onResponseSuccessful = { response ->
                coroutineIO {
                    response.body()?.response?.items?.let { sections ->
                        val section = sections.find { it.id == sectionId }
                        val tracks = section?.audios ?: emptyList()

                        setupCurrentPlaylist(tracks)

                        val title = section?.title

                        (titleLiveData as MutableLiveData).postValue(title)
                        (tracksLiveData as MutableLiveData).postValue(tracks)
                    }
                }
            },
            onResponseNotSuccessful = { errorResponse ->
                val errorMessage = errorResponse.errorDescription
                (errorMessageLiveData as MutableLiveData).postValue(errorMessage)
            },
            anyway = {
                (isLoadingLiveData as MutableLiveData).postValue(false)
            }
        )
    }

    companion object {
        private const val TAG = "CatalogTracksViewModel"
    }
}