package com.flaco_music.retrofit.models

import com.google.gson.annotations.SerializedName

data class Artist(
    val domain: String,
    val id: String,
    @SerializedName("is_album_cover")
    val isAlbumCover: Boolean,
    val name: String,
    val photo: List<PhotoListItem>
)