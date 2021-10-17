package com.flaco_music.retrofit.models

data class GetAlbumsByArtistResponseBody(
    val count: Int,
    val items: List<Playlist>
)