package com.flaco_music.retrofit.models

import com.google.gson.annotations.SerializedName


data class Ads(
    @SerializedName("account_age_type")
    val accountAgeType: String,
    @SerializedName("content_id")
    val contentId: String,
    val duration: String,
    val puid1: String,
    val puid22: String
)