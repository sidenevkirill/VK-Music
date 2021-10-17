package com.flaco_music.retrofit.models

data class Section(
    val blocks: List<Block>,
    val buttons: List<Any>,
    val id: String,
    val title: String,
    val url: String
)