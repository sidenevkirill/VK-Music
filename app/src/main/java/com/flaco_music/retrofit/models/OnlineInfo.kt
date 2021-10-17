package com.flaco_music.retrofit.models

import com.google.gson.annotations.SerializedName

data class OnlineInfo(
    @SerializedName("is_mobile")
    val isMobile: Boolean,
    @SerializedName("is_online")
    val isOnline: Boolean,
    @SerializedName("last_seen")
    val lastSeen: Int,
    val visible: Boolean
)