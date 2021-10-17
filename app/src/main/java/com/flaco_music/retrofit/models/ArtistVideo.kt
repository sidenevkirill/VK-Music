package com.flaco_music.retrofit.models

import com.google.gson.annotations.SerializedName

data class ArtistVideo(
    val added: Int,
    val ads: Ads,
    @SerializedName("can_add")
    val canAdd: Int,
    @SerializedName("can_add_to_faves")
    val canAddToFaves: Int,
    @SerializedName("can_comment")
    val canComment: Int,
    @SerializedName("can_like")
    val canLike: Int,
    @SerializedName("can_repost")
    val canRepost: Int,
    @SerializedName("can_subscribe")
    val canSubscribe: Int,
    val comments: Int,
    val date: Int,
    val description: String,
    val duration: Int,
    @SerializedName("featured_artists")
    val featuredArtists: List<FeaturedArtist>,
    val files: Files,
    @SerializedName("first_frame")
    val firstFrame: List<FirstFrame>,
    val genres: List<Genre>,
    val height: Int,
    val id: Int,
    val image: List<Image>,
    @SerializedName("is_explicit")
    val isExplicit: Int,
    @SerializedName("is_favorite")
    val isFavorite: Boolean,
    @SerializedName("is_subscribed")
    val isSubscribed: Int,
    val likes: Likes,
    val mainArtists: List<MainArtist>,
    @SerializedName("ov_id")
    val ovId: String,
    @SerializedName("owner_id")
    val ownerId: Int,
    val player: String,
    @SerializedName("release_date")
    val releaseDate: Int,
    val reposts: Reposts,
    @SerializedName("timeline_thumbs")
    val timelineThumbs: TimelineThumbs,
    val title: String,
    @SerializedName("track_code")
    val trackCode: String,
    val type: String,
    @SerializedName("user_id")
    val userId: Int,
    val views: Int,
    val width: Int
)