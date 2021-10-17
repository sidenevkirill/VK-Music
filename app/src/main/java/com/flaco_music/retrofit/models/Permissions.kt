package com.flaco_music.retrofit.models

data class Permissions(
    val boom_download: Boolean,
    val delete: Boolean,
    val edit: Boolean,
    val follow: Boolean,
    val play: Boolean,
    val share: Boolean
)