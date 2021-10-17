package com.flaco_music.retrofit.models

data class CatalogSection(
    val audios: List<Audio>?,
    val count: Int,
    val id: String,
    val items: List<CatalogSectionItem>?,
    val next_from: String,
    val playlists: List<CatalogSectionPlaylist>?,
    val source: String,
    val subtitle: String,
    val thumbs: List<Thumb>,
    val title: String,
    val type: String
)