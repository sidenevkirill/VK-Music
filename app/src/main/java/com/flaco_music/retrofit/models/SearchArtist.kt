package com.flaco_music.retrofit.models

data class SearchArtist(
    val domain: String,
    val id: String,
    val name: String,
    val photo: List<PhotoListItem>?
)