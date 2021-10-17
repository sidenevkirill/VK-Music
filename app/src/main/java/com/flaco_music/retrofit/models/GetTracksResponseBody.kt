package com.flaco_music.retrofit.models

data class GetTracksResponseBody(
    val count: Int,
    val items: List<Audio>
)