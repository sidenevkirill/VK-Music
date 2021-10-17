package com.flaco_music.retrofit.models

data class GetMusicPageResponseBody(
    val audios: Audios,
    val owner: Owner,
    val playlists: Playlists
)