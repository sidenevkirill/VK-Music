package com.flaco_music.retrofit.models

data class AddAudioToPlaylistResponseBody(
    val playlist: Playlist,
    val result: List<Result>
)