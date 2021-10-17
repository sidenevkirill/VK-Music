package com.flaco_music.db.current_playlist

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "current_playlist")
data class CurrentPlaylistItem(
    @PrimaryKey
    val position: Int,
    val id: Int,
    val ownerId: Int,
    val name: String,
    val artist: String,
    val url: String,
    val coverUrl34: String?,
    val coverUrl68: String?,
    val coverUrl135: String?,
    val coverUrl270: String?,
    val coverUrl300: String?,
    val coverUrl600: String?,
    val coverUrl1200: String?,
    val isContentRestricted: Boolean,
    val isPlaying: Boolean = false
)