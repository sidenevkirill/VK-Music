package com.flaco_music.ui.screens.catalog.playlists

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.flaco_music.retrofit.models.CatalogSectionPlaylist
import com.flaco_music.retrofit.repository.AudioRepository
import com.flaco_music.utils.extentions.coroutineIO
import com.flaco_music.utils.extentions.invoke
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

class CatalogPlaylistsViewModel : ViewModel(), KoinComponent {

    val titleLiveData: LiveData<String> = MutableLiveData()
    val playlistsLiveData: LiveData<List<CatalogSectionPlaylist>> = MutableLiveData()
    val isLoadingLiveData: LiveData<Boolean> = MutableLiveData()
    val errorMessageLiveData: LiveData<String> = MutableLiveData()

    private val audioRepository: AudioRepository by inject { parametersOf(true) }

    fun loadPlaylists(sectionId: String) {
        val call = audioRepository.getCatalog()
        call.invoke(
            onResponseSuccessful = { response ->
                coroutineIO {
                    response.body()?.response?.items?.let { sections ->
                        val section = sections.find { it.id == sectionId }
                        val playlists = section?.playlists ?: emptyList()

                        val title = section?.title

                        (titleLiveData as MutableLiveData).postValue(title)
                        (playlistsLiveData as MutableLiveData).postValue(playlists)
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