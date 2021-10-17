package com.flaco_music.db.player_config

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.exoplayer2.ExoPlayer

@Entity(tableName = "player_config")
class PlayerConfig(
    @PrimaryKey
    @ColumnInfo(name = "repeat_mode")
    val repeatMode: Int = ExoPlayer.REPEAT_MODE_ALL,
    @ColumnInfo(name = "is_shuffle_mode_enabled")
    val isShuffleModeEnabled: Boolean = false
)