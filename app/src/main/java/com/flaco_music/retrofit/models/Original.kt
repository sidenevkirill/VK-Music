package com.flaco_music.retrofit.models

import com.google.gson.annotations.SerializedName

data class Original(
    @SerializedName("access_key")
    val accessKey: String,
    @SerializedName("owner_id")
    val ownerId: Int,
    @SerializedName("playlist_id")
    val playlistId: Int
)