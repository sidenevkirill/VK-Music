package com.flaco_music.db.my_music_ids

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "my_music_ids")
data class MyMusicIdItem(
    @PrimaryKey
    val id: Int,
    val url: String
)