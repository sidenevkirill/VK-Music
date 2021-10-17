package com.flaco_music.retrofit.models

data class Link(
    val id: String,
    val image: List<Image>,
    val meta: Meta,
    val subtitle: String,
    val title: String,
    val url: String
)