package com.flaco_music.ui.screens.home

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.flaco_music.R
import com.flaco_music.ui.adapters.catalog.CatalogAudiosItem
import com.flaco_music.ui.adapters.catalog.CatalogItem
import com.flaco_music.ui.adapters.catalog.CatalogOthersItem
import com.flaco_music.ui.adapters.catalog.CatalogPlaylistsItem
import com.flaco_music.ui.screens.player.PlayerViewModel
import com.flaco_music.utils.extentions.coroutineIO
import com.flaco_music.utils.extentions.flatten
import com.flaco_music.utils.extentions.invoke

class HomeViewModel(application: Application) : PlayerViewModel(application) {

    override var playlistTypeName: String = PLAYLIST_NAME

    val catalogItemsLiveData: LiveData<List<CatalogItem>> = MutableLiveData()
    val isLoadingLiveData: LiveData<Boolean> = MutableLiveData()
    val errorMessageLiveData: LiveData<String> = MutableLiveData()

    fun loadCatalog() {
        (isLoadingLiveData as MutableLiveData).postValue(true)

        val call = audioRepositoryWithCaching.getCatalog()
        call.invoke(
            onResponseSuccessful = { response ->
                coroutineIO {
                    response.body()?.response?.items?.let { sections ->

                        val audios = sections.map {
                            try {
                                it.audios?.subList(0, 6)
                            } catch (e: IndexOutOfBoundsException) {
                                it.audios?.subList(0, it.audios.lastIndex)
                            }
                        }

                        val firstSongTitle = audios[0]?.get(0)?.title ?: ""

                        playlistTypeName = if (playlistTypeName != firstSongTitle) {
                            firstSongTitle
                        } else {
                            playlistTypeName
                        }

                        setupCurrentPlaylist(audios.flatten)

                        val catalogItems = sections.mapIndexed { index, section ->
                            when {
                                section.audios != null -> CatalogAudiosItem(
                                    id = section.id,
                                    title = section.title,
                                    audios = audios[index] ?: emptyList(),
                                )

                                section.playlists != null -> CatalogPlaylistsItem(
                                    id = section.id,
                                    title = section.title,
                                    playlists = section.playlists,
                                )

                                section.items != null -> CatalogOthersItem(
                                    id = section.id,
                                    title = section.title,
                                    items = section.items
                                )

                                else -> throw Exception("Unknown section.")
                            }
                        }

                        if (catalogItemsLiveData.value?.map { it.id } != catalogItems.map { it.id }) {
                            (catalogItemsLiveData as MutableLiveData).postValue(catalogItems)
                        }

                        isLoadingLiveData.postValue(false)
                    }
                }
            },
            onResponseNotSuccessful = { errorResponse ->
                val errorMessage = errorResponse.errorDescription
                (errorMessageLiveData as MutableLiveData).postValue(errorMessage)
            }
        )
    }

    fun deepLinker(trackId: String) {
        audioRepository.getById(trackId).invoke(
            onResponseSuccessful = {
                coroutineIO {
                    val audios = it.body()?.response
                    playlistTypeName = "deeplink_${audios?.get(0)?.id}"
                    if (audios.isNullOrEmpty()) {
                        val errorMessage = context.getString(R.string.no_access_to_audio)
                        (errorMessageLiveData as MutableLiveData).postValue(errorMessage)
                    }
                    setupCurrentPlaylist(audios ?: emptyList())
                    play(0)
                }
            }
        )
    }

    companion object {
        private const val TAG = "HomeViewModel"
        private const val PLAYLIST_NAME = "catalog"
    }
}