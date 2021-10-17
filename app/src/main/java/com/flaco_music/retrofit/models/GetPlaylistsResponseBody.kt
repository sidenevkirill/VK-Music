package com.flaco_music.retrofit.models

data class GetPlaylistsResponseBody(
    val count: Int,
    val items: List<Playlist>
)