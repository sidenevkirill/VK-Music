package com.flaco_music.retrofit.models

import com.google.gson.annotations.SerializedName

data class AudioAlbum(
    @SerializedName("access_key")
    val accessKey: String,
    val id: Int,
    @SerializedName("owner_id")
    val ownerId: Int,
    val thumb: Thumb?,
    val title: String
)