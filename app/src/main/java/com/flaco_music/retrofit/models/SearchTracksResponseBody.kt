package com.flaco_music.retrofit.models

data class SearchTracksResponseBody(
    val count: Int,
    val items: List<Audio>
)