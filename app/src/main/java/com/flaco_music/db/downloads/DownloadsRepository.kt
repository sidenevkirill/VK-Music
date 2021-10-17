package com.flaco_music.db.downloads

import androidx.lifecycle.LiveData
import com.flaco_music.db.current_playlist.CurrentPlaylistItem
import com.flaco_music.retrofit.models.Audio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.util.*

class DownloadsRepository(private val downloadsDao: DownloadsDao) {

    fun getDownloads(): List<DownloadItem> {
        return runBlocking(Dispatchers.IO) {
            downloadsDao.getDownloads()
        }
    }

    fun getDownloadsLiveData(): LiveData<List<DownloadItem>> {
        return downloadsDao.getDownloadsLiveData()
    }

    suspend fun insertTrack(track: Audio) {
        if (!checkDoesTrackExist(track.decryptedUrl)) {
            val downloadItem = DownloadItem(
                trackId = track.id,
                trackName = track.title,
                artistName = track.artist,
                artistId = track.ownerId,
                downloadTime = Calendar.getInstance().timeInMillis,
                url = track.decryptedUrl,
                coverUrl34 = track.album?.thumb?.photo34 ?: "",
                coverUrl68 = track.album?.thumb?.photo68 ?: "",
                coverUrl135 = track.album?.thumb?.photo135 ?: "",
                coverUrl270 = track.album?.thumb?.photo270 ?: "",
                coverUrl300 = track.album?.thumb?.photo300 ?: "",
                coverUrl600 = track.album?.thumb?.photo600 ?: "",
                coverUrl1200 = track.album?.thumb?.photo1200 ?: ""
            )

            downloadsDao.insert(downloadItem)
        }
    }

    suspend fun insertCurrentPlaylistItem(track: CurrentPlaylistItem) {
        if (!checkDoesTrackExist(track.url)) {
            val downloadItem = DownloadItem(
                trackId = track.id,
                trackName = track.name,
                artistName = track.artist,
                artistId = track.ownerId,
                downloadTime = Calendar.getInstance().timeInMillis,
                url = track.url,
                coverUrl34 = track.coverUrl34 ?: "",
                coverUrl68 = track.coverUrl68 ?: "",
                coverUrl135 = track.coverUrl135 ?: "",
                coverUrl270 = track.coverUrl270 ?: "",
                coverUrl300 = track.coverUrl300 ?: "",
                coverUrl600 = track.coverUrl600 ?: "",
                coverUrl1200 = track.coverUrl1200 ?: ""
            )

            downloadsDao.insert(downloadItem)
        }
    }

    fun checkDoesTrackExist(trackUrl: String): Boolean {
        return runBlocking(Dispatchers.IO) {
            downloadsDao.getDownloadByTrackUrl(trackUrl) != null
        }
    }

    suspend fun delete(trackId: Int) {
        downloadsDao.deleteByTrackId(trackId)
    }

    suspend fun clear() {
        downloadsDao.clear()
    }
}