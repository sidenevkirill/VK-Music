package com.flaco_music.retrofit.models

data class Catalog(
    val default_section: String,
    val sections: List<Section>
)