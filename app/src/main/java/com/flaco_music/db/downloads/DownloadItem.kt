package com.flaco_music.db.downloads

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadItem(
    @PrimaryKey
    val trackId: Int,
    val trackName: String,
    val artistName: String,
    val artistId: Int,
    val downloadTime: Long,
    val url: String,
    val coverUrl34: String?,
    val coverUrl68: String?,
    val coverUrl135: String?,
    val coverUrl270: String?,
    val coverUrl300: String?,
    val coverUrl600: String?,
    val coverUrl1200: String?
)