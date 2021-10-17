package com.flaco_music.ui.screens.downloads

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.flaco_music.db.downloads.DownloadItem
import com.flaco_music.db.downloads.DownloadsRepository
import com.flaco_music.ui.screens.player.PlayerViewModel
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class DownloadsViewModel(application: Application) : PlayerViewModel(application), KoinComponent {

    override var playlistTypeName: String = PLAYLIST_NAME

    private val downloadsRepository: DownloadsRepository by inject()

    val tracksLiveData: LiveData<List<DownloadItem>> = MutableLiveData()
    val isLoadingLiveData: LiveData<Boolean> = MutableLiveData()
    val noDownloadsLiveData: LiveData<Boolean> = MutableLiveData()

    fun loadTracks() {
        (isLoadingLiveData as MutableLiveData).postValue(true)

        val downloads = downloadsRepository.getDownloads()

        (noDownloadsLiveData as MutableLiveData).postValue(downloads.isEmpty())

        playlistTypeName = "${PLAYLIST_NAME}_${downloads.size}"

        setupCurrentPlaylistFromDownloadItems(downloads)
        (tracksLiveData as MutableLiveData).postValue(downloads)
        isLoadingLiveData.postValue(false)
    }

    companion object {
        private const val PLAYLIST_NAME = "downloads"
    }
}