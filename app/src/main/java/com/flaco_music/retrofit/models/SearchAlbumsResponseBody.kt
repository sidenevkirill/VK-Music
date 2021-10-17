package com.flaco_music.retrofit.models

data class SearchAlbumsResponseBody(
    val count: Int,
    val items: List<Album>
)