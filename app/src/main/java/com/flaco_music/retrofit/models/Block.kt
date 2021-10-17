package com.flaco_music.retrofit.models

import com.google.gson.annotations.SerializedName

data class Block(
    @SerializedName("artist_videos_ids")
    val artistVideosIds: List<String>,
    @SerializedName("artists_ids")
    val artistsIds: List<String>,
    @SerializedName("audios_ids")
    val audiosIds: List<String>,
    val buttons: List<Button>,
    @SerializedName("data_type")
    val dataType: String,
    val id: String,
    val layout: Layout,
    @SerializedName("links_ids")
    val linksIds: List<String>,
    @SerializedName("next_from")
    val nextFrom: String,
    @SerializedName("playlists_ids")
    val playlistsIds: List<String>,
    val url: String
)