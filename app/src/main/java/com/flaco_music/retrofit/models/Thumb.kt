package com.flaco_music.retrofit.models

import com.google.gson.annotations.SerializedName

data class Thumb(
    val height: Int,
    @SerializedName("photo_1200")
    val photo1200: String,
    @SerializedName("photo_135")
    val photo135: String,
    @SerializedName("photo_270")
    val photo270: String,
    @SerializedName("photo_300")
    val photo300: String,
    @SerializedName("photo_34")
    val photo34: String,
    @SerializedName("photo_600")
    val photo600: String,
    @SerializedName("photo_68")
    val photo68: String,
    val width: Int
)