package com.flaco_music.ui.screens.search

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.flaco_music.R
import com.flaco_music.db.search_history.SearchHistoryRepository
import com.flaco_music.retrofit.models.Album
import com.flaco_music.retrofit.models.Audio
import com.flaco_music.retrofit.models.SearchArtist
import com.flaco_music.ui.adapters.search.Artist
import com.flaco_music.ui.adapters.search.SearchItem
import com.flaco_music.ui.screens.player.PlayerViewModel
import kotlinx.coroutines.*
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.inject
import retrofit2.await

@KoinApiExtension
class SearchViewModel(application: Application) : PlayerViewModel(application) {

    override var playlistTypeName: String = PLAYLIST_NAME

    val searchItemsLiveData: LiveData<List<SearchItem>> = MutableLiveData()
    val isLoadingLiveData: LiveData<Boolean> = MutableLiveData()
    val errorMessageLiveData: LiveData<String> = MutableLiveData()

    private val searchHistoryRepository: SearchHistoryRepository by inject()

    private var searchJob: Job? = null

    fun loadSearchHistory() {
        val searchHistory = searchHistoryRepository.getSearchHistory()
        val tracks =
            searchHistory.filter { it is com.flaco_music.ui.adapters.search.Audio } as List<com.flaco_music.ui.adapters.search.Audio>
        playlistTypeName = "${PLAYLIST_NAME}_FLACO_SEARCH_HISTORY_100200300"
        setupCurrentPlaylistFromSearchHistory(tracks)

        (searchItemsLiveData as MutableLiveData).postValue(searchHistory)
    }

    fun search(query: String, isFromInput: Boolean) {
        searchJob?.cancel()

        (isLoadingLiveData as MutableLiveData).postValue(true)

        searchJob = GlobalScope.launch(Dispatchers.IO) {
            if (isFromInput) delay(600)

            playlistTypeName = "${PLAYLIST_NAME}_$query"

            val tracks = fetchTracks(query)
            setupCurrentPlaylist(tracks)

            val albums = fetchAlbums(query)
            val artists = fetchArtists(query)

            val searchItems = (artists + albums + tracks).map { item ->
                when (item) {
                    is Audio -> com.flaco_music.ui.adapters.search.Audio(
                        id = item.id,
                        ownerId = item.ownerId,
                        title = item.title,
                        artist = item.artist,
                        url = item.decryptedUrl,
                        duration = item.duration,
                        coverUrl34 = item.album?.thumb?.photo34,
                        coverUrl68 = item.album?.thumb?.photo68,
                        coverUrl135 = item.album?.thumb?.photo135,
                        coverUrl270 = item.album?.thumb?.photo270,
                        coverUrl300 = item.album?.thumb?.photo300,
                        coverUrl600 = item.album?.thumb?.photo600,
                        coverUrl1200 = item.album?.thumb?.photo1200
                    )

                    is Album -> com.flaco_music.ui.adapters.search.Album(
                        id = item.id,
                        ownerId = item.ownerId,
                        title = item.title,
                        artist = item.mainArtists.joinToString { artist -> artist.name },
                        coverUrl = item.photo.photo135 ?: item.photo.photo270 ?: item.photo.photo300 ?: ""
                    )

                    is SearchArtist -> Artist(
                        id = item.id,
                        title = item.name,
                        coverUrl = getArtistCoverUrl(item) ?: ""
                    )

                    else -> throw Exception("Unknown search item.")
                }
            }

            isLoadingLiveData.postValue(false)
            (searchItemsLiveData as MutableLiveData).postValue(searchItems)
        }
    }

    fun saveToSearchHistory(searchItem: SearchItem) {
        when (searchItem) {
            is com.flaco_music.ui.adapters.search.Audio -> searchHistoryRepository.insertAudio(searchItem)
            is com.flaco_music.ui.adapters.search.Album -> searchHistoryRepository.insertAlbum(searchItem)
            is com.flaco_music.ui.adapters.search.Artist -> searchHistoryRepository.insertArtist(searchItem)
        }
    }

    private suspend fun fetchTracks(query: String): List<Audio> = coroutineScope {
        return@coroutineScope try {
            audioRepositoryWithCaching.searchTracks(query).await().response.items
        } catch (e: Exception) {
            (errorMessageLiveData as MutableLiveData).postValue(context.getString(R.string.internet_connection_error_message))
            emptyList()
        }
    }

    private suspend fun fetchAlbums(query: String): List<Album> = coroutineScope {
        return@coroutineScope try {
            audioRepositoryWithCaching.searchAlbums(query).await().response.items
        } catch (e: Exception) {
            (errorMessageLiveData as MutableLiveData).postValue(context.getString(R.string.internet_connection_error_message))
            emptyList()
        }
    }

    private suspend fun fetchArtists(query: String): List<SearchArtist> = coroutineScope {
        return@coroutineScope try {
            audioRepositoryWithCaching.searchArtists(query).await().response.items
        } catch (e: Exception) {
            (errorMessageLiveData as MutableLiveData).postValue(context.getString(R.string.internet_connection_error_message))
            emptyList()
        }
    }

    private fun getArtistCoverUrl(artist: SearchArtist): String? {
        return try {
            artist.photo?.get(3)?.url
        } catch (e: java.lang.Exception) {
            null
        }
    }

    companion object {
        private const val TAG = "SearchViewModel"
        private const val PLAYLIST_NAME = "search"
    }
}