package com.flaco_music.db.search_history

import com.flaco_music.ui.adapters.search.Album
import com.flaco_music.ui.adapters.search.Artist
import com.flaco_music.ui.adapters.search.Audio
import com.flaco_music.ui.adapters.search.SearchItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SearchHistoryRepository(private val searchHistoryDao: SearchHistoryDao) {

    fun getSearchHistory(): List<SearchItem> {
        val tracks = runBlocking(Dispatchers.IO) {
            val audios = searchHistoryDao.getAudiosHistory()
            try {
                audios.subList(audios.lastIndex - 9, audios.lastIndex + 1)
            } catch (e: IndexOutOfBoundsException) {
                audios
            }
        }
        val albums = runBlocking(Dispatchers.IO) {
            val albums = searchHistoryDao.getAlbumsHistory()
            try {
                albums.subList(albums.lastIndex - 2, albums.lastIndex + 1)
            } catch (e: IndexOutOfBoundsException) {
                albums
            }
        }
        val artists = runBlocking(Dispatchers.IO) {
            val artists = searchHistoryDao.getArtistsHistory()
            try {
                artists.subList(artists.lastIndex - 2, artists.lastIndex + 1)
            } catch (e: IndexOutOfBoundsException) {
                artists
            }
        }
        return (artists + albums + tracks)
    }

    fun insertAudio(track: Audio) {
        GlobalScope.launch(Dispatchers.IO) {
            searchHistoryDao.insertAudio(track)
        }
    }

    fun insertAlbum(album: Album) {
        GlobalScope.launch(Dispatchers.IO) {
            searchHistoryDao.insertAlbum(album)
        }
    }

    fun insertArtist(artist: Artist) {
        GlobalScope.launch(Dispatchers.IO) {
            searchHistoryDao.insertArtist(artist)
        }
    }
}