package com.flaco_music.retrofit.models

data class Audios(
    val count: Int,
    val groups: List<Any>,
    val items: List<Audio>,
    val profiles: List<Profile>
)