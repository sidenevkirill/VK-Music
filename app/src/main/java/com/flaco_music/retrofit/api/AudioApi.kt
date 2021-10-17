package com.flaco_music.retrofit.api

import com.flaco_music.retrofit.models.*
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface AudioApi {

    @GET("audio.add")
    fun addTrack(
        @Query("owner_id")
        ownerId: Int,
        @Query("audio_id")
        audioId: Int
    ): Call<AddTrackResponse>

    @GET("audio.delete")
    fun deleteTrack(
        @Query("owner_id")
        ownerId: Int,
        @Query("audio_id")
        audioId: Int
    ): Call<DeleteTrackResponse>

    @GET("execute.getMusicPage")
    fun getMusicPage(
        @Query("owner_id")
        ownerId: Int,
        @Query("need_owner")
        needOwner: Int,
        @Query("need_playlists")
        needPlaylists: Int,
        @Query("audio_count")
        audioCount: Int,
        @Query("playlists_count")
        playlistsCount: Int
    ): Call<GetMusicPageResponse>

    @GET("execute.getPlaylist")
    fun getPlaylist(
        @Query("owner_id")
        ownerId: Int,
        @Query("id")
        playlistId: Int,
        @Query("need_playlist")
        needPlaylist: Int,
    ): Call<GetPlaylistResponse>

    @GET("audio.getPopular")
    fun getPopularByGenre(
        @Query("genre_id")
        genreId: Int,
        @Query("count")
        count: Int,
        @Query("auto_complete")
        autoComplete: Int
    ): Call<GetPopularAudiosByGenreResponse>

    @GET("audio.getCatalog")
    fun getCatalog(): Call<GetCatalogResponse>

    @GET("audio.search")
    fun searchTracks(
        @Query("q")
        query: String,
        @Query("count")
        count: Int,
        @Query("auto_complete")
        autoComplete: Int,
    ): Call<SearchTracksResponse>

    @GET("audio.searchAlbums")
    fun searchAlbums(
        @Query("q")
        query: String,
        @Query("count")
        count: Int,
    ): Call<SearchAlbumsResponse>

    @GET("audio.searchArtists")
    fun searchArtists(
        @Query("q")
        query: String,
        @Query("count")
        count: Int,
    ): Call<SearchArtistResponse>

    @GET("audio.getAlbumsByArtist")
    fun getAlbumsByArtist(
        @Query("artist_id")
        artistId: String,
        @Query("count")
        count: Int
    ): Call<GetAlbumsByArtistResponse>

    @GET("audio.getAudiosByArtist")
    fun getAudiosByArtist(
        @Query("artist_id")
        artistId: String,
        @Query("count")
        count: Int
    ): Call<GetAudiosByArtistResponse>

    @GET("catalog.getAudioArtist")
    fun getArtist(
        @Query("artist_id")
        artistId: String,
        @Query("need_blocks")
        needBlocks: Int
    ): Call<GetArtistResponse>

    @GET("audio.getById")
    fun getById(
        @Query("audios")
        audios: String
    ): Call<GetTrackByIdResponse>

    @GET("audio.getLyrics")
    fun getLyrics(
        @Query("lyrics_id")
        lyricsId: Int
    ): Call<GetLyricsByIdResponse>

    @GET("execute.addAudioToPlaylist")
    fun addAudioToPlaylist(
        @Query("playlist_id")
        playlistId: Int,
        @Query("owner_id")
        ownerId: Int,
        @Query("audio_ids")
        audioIds: String
    ): Call<AddAudioToPlaylistResponse>

    @GET("execute.removeAudioFromPlaylist")
    fun removeAudioFromPlaylist(
        @Query("playlist_id")
        playlistId: Int,
        @Query("owner_id")
        ownerId: Int,
        @Query("audio_ids")
        audioIds: String
    ): Call<RemoveAudioFromPlaylistResponse>

    @GET("audio.getPlaylists")
    fun getPlaylists(
        @Query("owner_id")
        ownerId: Int,
        @Query("count")
        count: Int
    ): Call<GetPlaylistsResponse>

    @GET("audio.followPlaylist")
    fun followPlaylist(
        @Query("owner_id")
        ownerId: Int,
        @Query("playlist_id")
        playlistId: Int
    ): Call<GetPlaylistResponseBody>

    @GET("audio.deletePlaylist")
    fun removePlaylist(
        @Query("owner_id")
        ownerId: Int,
        @Query("playlist_id")
        playlistId: Int
    ): Call<DeletePlaylistResponse>
}