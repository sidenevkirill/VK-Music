package com.flaco_music.retrofit.models

import com.flaco_music.utils.LinkDecryptor
import com.google.gson.annotations.SerializedName

data class Audio(
    @SerializedName("access_key")
    val accessKey: String,
    val ads: Ads,
    val album: AudioAlbum?,
    val artist: String,
    @SerializedName("content_restricted")
    val contentRestricted: Int,
    val original: Original?,
    val date: Int,
    val duration: Int,
    @SerializedName("featured_artists")
    val featuredArtists: List<FeaturedArtist>,
    @SerializedName("genre_id")
    val genreId: Int,
    val id: Int,
    @SerializedName("is_explicit")
    val isExplicit: Boolean,
    @SerializedName("is_focus_track")
    val isFocusTrack: Boolean,
    @SerializedName("is_licensed")
    val isLicensed: Boolean,
    @SerializedName("lyrics_id")
    val lyricsId: Int?,
    @SerializedName("main_artists")
    val mainArtists: List<MainArtist>?,
    @SerializedName("no_search")
    val noSearch: Int,
    @SerializedName("owner_id")
    val ownerId: Int,
    @SerializedName("short_videos_allowed")
    val shortVideosAllowed: Boolean,
    @SerializedName("stories_allowed")
    val storiesAllowed: Boolean,
    @SerializedName("stories_cover_allowed")
    val storiesCoverAllowed: Boolean,
    val subtitle: String,
    val title: String,
    @SerializedName("track_code")
    val trackCode: String,
    val url: String
) {
    val decryptedUrl: String
        get() = LinkDecryptor.getMp3FromM3u8(url)
}