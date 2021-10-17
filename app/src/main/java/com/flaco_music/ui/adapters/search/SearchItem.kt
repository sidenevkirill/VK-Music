package com.flaco_music.ui.adapters.search

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.flaco_music.db.search_history.SearchTypeConverter

@Entity
sealed class SearchItem(
    val title: String,
    var coverUrl: String?,
    var type: SearchItemType
)

@Entity(tableName = "audios_history")
@TypeConverters(SearchTypeConverter::class)
class Audio(
    @PrimaryKey
    val id: Int,
    val ownerId: Int,
    title: String,
    val artist: String,
    val url: String,
    val duration: Int,
    val coverUrl34: String?,
    val coverUrl68: String?,
    val coverUrl135: String?,
    val coverUrl270: String?,
    val coverUrl300: String?,
    val coverUrl600: String?,
    val coverUrl1200: String?
) : SearchItem(title, coverUrl135 ?: coverUrl68 ?: coverUrl270 ?: coverUrl300 ?: coverUrl600, SearchItemType.AUDIO)

@Entity(tableName = "albums_history")
@TypeConverters(SearchTypeConverter::class)
class Album(
    @PrimaryKey
    val id: Int,
    val ownerId: Int,
    title: String,
    val artist: String,
    coverUrl: String?
) : SearchItem(title, coverUrl, SearchItemType.ALBUM)

@Entity(tableName = "artists_history")
@TypeConverters(SearchTypeConverter::class)
class Artist(
    @PrimaryKey
    val id: String,
    title: String,
    coverUrl: String,
) : SearchItem(title, coverUrl, SearchItemType.ARTIST)

enum class SearchItemType {
    ARTIST, ALBUM, AUDIO
}