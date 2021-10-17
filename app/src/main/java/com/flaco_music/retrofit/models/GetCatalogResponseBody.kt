package com.flaco_music.retrofit.models

data class GetCatalogResponseBody(
    val groups: List<Group>,
    val items: List<CatalogSection>,
    val profiles: List<Profile>
)