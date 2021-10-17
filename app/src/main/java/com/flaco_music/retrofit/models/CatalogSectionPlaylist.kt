package com.flaco_music.retrofit.models

import com.google.gson.annotations.SerializedName

data class CatalogSectionPlaylist(
    @SerializedName("access_key")
    @JvmField val accessKey: String,
    @SerializedName("album_type")
    @JvmField val albumType: String,
    val count: Int,
    @SerializedName("create_time")
    @JvmField val createTime: Int,
    val description: String,
    @SerializedName("featured_artists")
    @JvmField val featuredArtists: List<FeaturedArtist>,
    val followers: Int,
    val genres: List<Genre>,
    val id: Int,
    @SerializedName("is_explicit")
    @JvmField val isExplicit: Boolean,
    @SerializedName("is_following")
    @JvmField val isFollowing: Boolean,
    @SerializedName("main_artists")
    @JvmField val mainArtists: List<MainArtist>?,
    val original: Original,
    @SerializedName("owner_id")
    @JvmField val ownerId: Int,
    val permissions: Permissions,
    val photo: Photo?,
    @SerializedName("play_button")
    @JvmField val playButton: Boolean,
    val plays: Int,
    val subtitle: String,
    @SerializedName("subtitle_badge")
    @JvmField val subtitleBadge: Boolean,
    val title: String,
    val type: Int,
    @SerializedName("update_time")
    @JvmField val updateTime: Int,
    val year: Int
)