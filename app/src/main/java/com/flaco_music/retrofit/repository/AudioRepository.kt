package com.flaco_music.retrofit.repository

import com.flaco_music.retrofit.api.AudioApi
import com.flaco_music.retrofit.models.*
import retrofit2.Call

class AudioRepository(private val audioApi: AudioApi) {

    fun addTrack(ownerId: Int, audioId: Int): Call<AddTrackResponse> {
        return audioApi.addTrack(ownerId, audioId)
    }

    fun deleteTrack(ownerId: Int, audioId: Int): Call<DeleteTrackResponse> {
        return audioApi.deleteTrack(ownerId, audioId)
    }

    fun getMusicPage(
        ownerId: Int,
        needOwner: Int = 1,
        needPlaylists: Int = 1,
        audioCount: Int = 100000,
        playlistsCount: Int = 200
    ): Call<GetMusicPageResponse> {
        return audioApi.getMusicPage(
            ownerId,
            needOwner,
            needPlaylists,
            audioCount,
            playlistsCount,
        )
    }

    fun getPlaylist(
        ownerId: Int,
        playlistId: Int,
        needPlaylist: Int = 1,
    ): Call<GetPlaylistResponse> {
        return audioApi.getPlaylist(ownerId, playlistId, needPlaylist)
    }

    fun getPopularByGenre(
        genreId: Int,
        count: Int = 1000,
        autoComplete: Int = 1 // fix errors
    ): Call<GetPopularAudiosByGenreResponse> {
        return audioApi.getPopularByGenre(genreId, count, autoComplete)
    }

    fun getCatalog(): Call<GetCatalogResponse> {
        return audioApi.getCatalog()
    }

    fun searchTracks(
        query: String,
        count: Int = 10,
        autoComplete: Int = 1,
    ): Call<SearchTracksResponse> {
        return audioApi.searchTracks(query, count, autoComplete)
    }

    fun searchAlbums(
        query: String,
        count: Int = 3,
    ): Call<SearchAlbumsResponse> {
        return audioApi.searchAlbums(query, count)
    }

    fun searchArtists(
        query: String,
        count: Int = 3,
    ): Call<SearchArtistResponse> {
        return audioApi.searchArtists(query, count)
    }

    fun getAlbumsByArtist(
        artistId: String,
        count: Int = 200
    ): Call<GetAlbumsByArtistResponse> {
        return audioApi.getAlbumsByArtist(artistId, count)
    }

    fun getAudiosByArtist(
        artistId: String,
        count: Int = 1000
    ): Call<GetAudiosByArtistResponse> {
        return audioApi.getAudiosByArtist(artistId, count)
    }

    fun getArtist(
        artistId: String,
        needBlocks: Int = 1
    ): Call<GetArtistResponse> {
        return audioApi.getArtist(artistId, needBlocks)
    }

    fun getById(
        vararg audios: String
    ): Call<GetTrackByIdResponse> {
        return audioApi.getById(audios.joinToString(","))
    }

    fun getLyrics(
        lyricsId: Int
    ): Call<GetLyricsByIdResponse> {
        return audioApi.getLyrics(lyricsId)
    }

    fun addAudioToPlaylist(
        playlistId: Int,
        ownerId: Int,
        vararg audioIds: String
    ): Call<AddAudioToPlaylistResponse> {
        return audioApi.addAudioToPlaylist(
            playlistId,
            ownerId,
            audioIds.joinToString(",")
        )
    }

    fun removeAudioFromPlaylist(
        playlistId: Int,
        ownerId: Int,
        vararg audioIds: String
    ): Call<RemoveAudioFromPlaylistResponse> {
        return audioApi.removeAudioFromPlaylist(
            playlistId,
            ownerId,
            audioIds.joinToString(",")
        )
    }

    fun getPlaylists(
        ownerId: Int,
        count: Int = 200
    ): Call<GetPlaylistsResponse> {
        return audioApi.getPlaylists(ownerId, count)
    }

    fun followPlaylist(ownerId: Int, playlistId: Int): Call<GetPlaylistResponseBody> {
        return audioApi.followPlaylist(ownerId, playlistId)
    }

    fun removePlaylist(ownerId: Int, playlistId: Int): Call<DeletePlaylistResponse> {
        return audioApi.removePlaylist(ownerId, playlistId)
    }

    fun findPlaylistInMyMusic(userId: Int, playlistOriginalId: Int): Playlist? {
        return audioApi.getPlaylists(userId, 200).execute().body()?.response?.items?.find {
            it.original?.playlistId == playlistOriginalId
        }
    }
}