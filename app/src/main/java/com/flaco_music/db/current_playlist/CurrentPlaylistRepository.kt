package com.flaco_music.db.current_playlist

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class CurrentPlaylistRepository(private val currentPlaylistDao: CurrentPlaylistDao) {

    var currentPlaylist: List<CurrentPlaylistItem> = emptyList()
        get() = runBlocking(Dispatchers.IO) { currentPlaylistDao.getCurrentPlaylist() }
        set(value) {
            field = value
            currentPlaylistDao.clear()
            currentPlaylistDao.insert(value)
        }

    val currentPlaylistLiveData: LiveData<List<CurrentPlaylistItem>>
        get() = currentPlaylistDao.getCurrentPlaylistLiveData()

    val currentPlayingTrack: CurrentPlaylistItem
        get() = runBlocking(Dispatchers.IO) { currentPlaylistDao.getCurrentPlayingTrack() }

    val currentPlayingTrackLiveData: LiveData<CurrentPlaylistItem?>
        get() = currentPlaylistDao.getCurrentPlayingTrackLiveData()

    fun setCurrentPlayingTrack(positionId: Int) {
        runBlocking(Dispatchers.IO) {
            currentPlaylistDao.setCurrentPlayingTrack(positionId)
        }
    }
}