package com.flaco_music.ui.screens.player

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.flaco_music.db.current_playlist.CurrentPlaylistItem
import com.flaco_music.db.current_playlist.CurrentPlaylistRepository
import com.flaco_music.db.downloads.DownloadItem
import com.flaco_music.retrofit.models.Audio
import com.flaco_music.retrofit.repository.AudioRepository
import com.flaco_music.service.PlayerService
import com.flaco_music.utils.extentions.coroutineIO
import com.flaco_music.utils.extentions.coroutineMain
import com.flaco_music.utils.preferences.Preferences
import kotlinx.coroutines.Job
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

@KoinApiExtension
abstract class PlayerViewModel(application: Application) : AndroidViewModel(application), KoinComponent {

    protected abstract var playlistTypeName: String

    protected val audioRepository: AudioRepository by inject { parametersOf(false) }
    protected val audioRepositoryWithCaching: AudioRepository by inject { parametersOf(true) }

    protected val preferences: Preferences by inject()

    protected val context: Context by lazy { getApplication<Application>().applicationContext }

    private var currentPlaylist = emptyList<CurrentPlaylistItem>()

    private var startPlayingJob: Job? = null

    private val currentPlaylistRepository: CurrentPlaylistRepository by inject()

    internal fun play(position: Int, isFromStorage: Boolean = false) {
        startPlayingJob?.cancel()

        startPlayingJob = if (preferences.currentPlaylistName != playlistTypeName) {

            preferences.currentPlaylistName = playlistTypeName

            coroutineIO {
                currentPlaylistRepository.currentPlaylist = currentPlaylist
                playTrackJob(position, isFromStorage)
            }
        } else {
            playTrackJob(position, isFromStorage)
        }

        startPlayingJob?.start()
    }

    protected fun setupCurrentPlaylist(tracks: List<Audio>) {
        currentPlaylist = tracks.mapIndexed { index, audio ->
            CurrentPlaylistItem(
                position = index,
                id = audio.id,
                ownerId = audio.ownerId,
                name = audio.title,
                artist = audio.artist,
                url = audio.decryptedUrl,
                coverUrl34 = audio.album?.thumb?.photo34 ?: "",
                coverUrl68 = audio.album?.thumb?.photo68 ?: "",
                coverUrl135 = audio.album?.thumb?.photo135 ?: "",
                coverUrl270 = audio.album?.thumb?.photo270 ?: "",
                coverUrl300 = audio.album?.thumb?.photo300 ?: "",
                coverUrl600 = audio.album?.thumb?.photo600 ?: "",
                coverUrl1200 = audio.album?.thumb?.photo1200 ?: "",
                isContentRestricted = audio.contentRestricted == 1
            )
        }
    }

    protected fun setupCurrentPlaylistFromDownloadItems(tracks: List<DownloadItem>) {
        currentPlaylist = tracks.mapIndexed { index, downloadItem ->
            CurrentPlaylistItem(
                position = index,
                id = downloadItem.trackId,
                ownerId = downloadItem.artistId,
                name = downloadItem.trackName,
                artist = downloadItem.artistName,
                url = downloadItem.url,
                coverUrl34 = downloadItem.coverUrl34 ?: "",
                coverUrl68 = downloadItem.coverUrl68 ?: "",
                coverUrl135 = downloadItem.coverUrl135 ?: "",
                coverUrl270 = downloadItem.coverUrl270 ?: "",
                coverUrl300 = downloadItem.coverUrl300 ?: "",
                coverUrl600 = downloadItem.coverUrl600 ?: "",
                coverUrl1200 = downloadItem.coverUrl1200 ?: "",
                isContentRestricted = false
            )
        }
    }

    protected fun setupCurrentPlaylistFromSearchHistory(tracks: List<com.flaco_music.ui.adapters.search.Audio>) {
        currentPlaylist = tracks.mapIndexed { index, item ->
            CurrentPlaylistItem(
                position = index,
                id = item.id,
                ownerId = item.ownerId,
                name = item.title,
                artist = item.artist,
                url = item.url,
                coverUrl34 = item.coverUrl34 ?: "",
                coverUrl68 = item.coverUrl68 ?: "",
                coverUrl135 = item.coverUrl135 ?: "",
                coverUrl270 = item.coverUrl270 ?: "",
                coverUrl300 = item.coverUrl300 ?: "",
                coverUrl600 = item.coverUrl600 ?: "",
                coverUrl1200 = item.coverUrl1200 ?: "",
                isContentRestricted = false
            )
        }
    }

    private fun playTrackJob(position: Int, isFromStorage: Boolean): Job {
        return coroutineMain {
            PlayerService.startMe(context, position, isFromStorage)
        }
    }

    companion object {
        private const val TAG = "PlayerViewModel"
    }
}