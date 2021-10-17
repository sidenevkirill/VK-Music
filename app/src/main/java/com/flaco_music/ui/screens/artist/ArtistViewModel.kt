package com.flaco_music.ui.screens.artist

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.flaco_music.R
import com.flaco_music.retrofit.models.Audio
import com.flaco_music.retrofit.models.GetArtistResponseBody
import com.flaco_music.retrofit.models.Playlist
import com.flaco_music.ui.screens.player.PlayerViewModel
import com.flaco_music.utils.extentions.coroutineIO
import kotlinx.coroutines.coroutineScope
import retrofit2.await

class ArtistViewModel(application: Application) : PlayerViewModel(application) {

    override var playlistTypeName: String = ""

    val artistInfoLiveData: LiveData<ArtistInfo> = MutableLiveData()
    val albumsLiveData: LiveData<List<Playlist>> = MutableLiveData()
    val tracksLiveData: LiveData<List<Audio>> = MutableLiveData()
    val isLoadingLiveData: LiveData<Boolean> = MutableLiveData()
    val artistNotFoundLiveData: LiveData<Boolean> = MutableLiveData()
    val noAlbumsLiveDataLiveData: LiveData<Boolean> = MutableLiveData()
    val errorMessageLiveData: LiveData<String> = MutableLiveData()

    fun loadArtistData(artistId: String) {
        coroutineIO {
            (isLoadingLiveData as MutableLiveData).postValue(true)

            playlistTypeName = "${PLAYLIST_NAME}_$artistId"

            val artist = fetchArtist(artistId) ?: return@coroutineIO

            if (artist.placeholders != null) {
                (artistNotFoundLiveData as MutableLiveData).postValue(true)
                return@coroutineIO
            } else {
                (artistNotFoundLiveData as MutableLiveData).postValue(false)
                val tracks = fetchAudios(artistId)
                setupCurrentPlaylist(tracks)

                val albums = fetchAlbums(artistId)

                (noAlbumsLiveDataLiveData as MutableLiveData).postValue(albums.isEmpty())

                val artistInfo = ArtistInfo(
                    name = artist.artists[0].name,
                    numberOfTracks = tracks.size,
                    photoUrl = artist.artists[0].photo.maxByOrNull { it.width }?.url
                )

                (artistInfoLiveData as MutableLiveData).postValue(artistInfo)
                (albumsLiveData as MutableLiveData).postValue(albums)
                (tracksLiveData as MutableLiveData).postValue(tracks)

                isLoadingLiveData.postValue(false)
            }
        }
    }

    private suspend fun fetchArtist(artistId: String): GetArtistResponseBody? = coroutineScope {
        return@coroutineScope try {
            audioRepositoryWithCaching.getArtist(artistId).await().response
        } catch (e: Exception) {
            (errorMessageLiveData as MutableLiveData).postValue(context.getString(R.string.internet_connection_error_message))
            return@coroutineScope null
        }
    }

    private suspend fun fetchAlbums(artistId: String): List<Playlist> = coroutineScope {
        return@coroutineScope audioRepositoryWithCaching.getAlbumsByArtist(artistId).await().response.items
    }

    private suspend fun fetchAudios(artistId: String): List<Audio> = coroutineScope {
        return@coroutineScope audioRepositoryWithCaching.getAudiosByArtist(artistId).await().response.items
    }

    companion object {
        private const val TAG = "ArtistViewModel"
        private const val PLAYLIST_NAME = "artist"
    }
}