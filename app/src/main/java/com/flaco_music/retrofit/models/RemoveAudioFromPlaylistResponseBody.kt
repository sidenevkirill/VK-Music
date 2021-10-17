package com.flaco_music.retrofit.models

data class RemoveAudioFromPlaylistResponseBody(
    val playlist: Playlist,
    val result: Int
)