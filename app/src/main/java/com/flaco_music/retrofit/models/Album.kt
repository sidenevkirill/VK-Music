package com.flaco_music.retrofit.models

import com.google.gson.annotations.SerializedName

data class Album(
    @SerializedName("access_key")
    val accessKey: String,
    @SerializedName("album_type")
    val albumType: String,
    val count: Int,
    @SerializedName("create_time")
    val createTime: Int,
    val description: String,
    @SerializedName("featured_artists")
    val featuredArtists: List<FeaturedArtist>,
    val followers: Int,
    val genres: List<Genre>,
    val id: Int,
    @SerializedName("is_explicit")
    val isExplicit: Boolean,
    @SerializedName("is_following")
    val isFollowing: Boolean,
    @SerializedName("main_artists")
    val mainArtists: List<MainArtist>,
    @SerializedName("owner_id")
    val ownerId: Int,
    val photo: Photo,
    val plays: Int,
    val title: String,
    val type: Int,
    @SerializedName("update_time")
    val updateTime: Int,
    val year: Int
)