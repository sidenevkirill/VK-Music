package com.flaco_music.retrofit.models

data class GetPlaylistResponseBody(
    val audios: List<Audio>,
    val playlist: Playlist
)