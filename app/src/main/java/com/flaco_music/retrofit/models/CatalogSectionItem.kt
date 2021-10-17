package com.flaco_music.retrofit.models

data class CatalogSectionItem(
    val image: List<Image>?,
    val meta: CatalogSectionMeta,
    val subtitle: String,
    val title: String,
    val url: String
)