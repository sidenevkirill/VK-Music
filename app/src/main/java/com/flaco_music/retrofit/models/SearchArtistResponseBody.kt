package com.flaco_music.retrofit.models

data class SearchArtistResponseBody(
    val count: Int,
    val items: List<SearchArtist>
)