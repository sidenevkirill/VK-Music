package com.flaco_music.retrofit.models

data class GetAudiosByArtistResponseBody(
    val count: Int,
    val items: List<Audio>
)