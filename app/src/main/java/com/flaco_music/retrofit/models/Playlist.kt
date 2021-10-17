package com.flaco_music.retrofit.models

import com.google.gson.annotations.SerializedName

data class Playlist(
    @SerializedName("access_key")
    val accessKey: String,
    @SerializedName("album_type")
    val albumType: String,
    val count: Int,
    @SerializedName("create_time")
    val createTime: Int,
    val description: String,
    val followers: Int,
    val genres: List<Genre>,
    val id: Int,
    @SerializedName("is_blocked")
    val isBlocked: Boolean,
    @SerializedName("is_explicit")
    val isExplicit: Boolean,
    @SerializedName("is_following")
    val isFollowing: Boolean,
    @SerializedName("main_artists")
    val mainArtists: List<MainArtist>?,
    val meta: Meta,
    val original: Original?,
    @SerializedName("owner_id")
    val ownerId: Int,
    val photo: Photo?,
    val plays: Int,
    val restriction: Restriction,
    val title: String,
    val type: Int,
    @SerializedName("update_time")
    val updateTime: Int,
    val year: Int,
    val permissions: Permissions
) {
    val properPlaylistId: Int
        get() = original?.playlistId ?: id

    val properOwnerId: Int
        get() = original?.ownerId ?: ownerId
}