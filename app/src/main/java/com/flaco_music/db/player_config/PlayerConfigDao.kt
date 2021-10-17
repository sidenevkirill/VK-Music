package com.flaco_music.db.player_config

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PlayerConfigDao {

    @Query("SELECT repeat_mode FROM player_config")
    fun getRepeatMode(): Int

    @Query("SELECT is_shuffle_mode_enabled FROM player_config")
    fun getShuffleModeEnabled(): Boolean

    @Query("SELECT repeat_mode FROM player_config")
    fun getRepeatModeLiveData(): LiveData<Int>

    @Query("SELECT is_shuffle_mode_enabled FROM player_config")
    fun getShuffleModeEnabledLiveData(): LiveData<Boolean>

    @Insert
    fun insert(playerConfig: PlayerConfig)

    @Query("UPDATE player_config SET repeat_mode = CASE WHEN repeat_mode = 2 THEN 1 WHEN repeat_mode = 1 THEN 0 WHEN repeat_mode = 0 THEN 2 END")
    fun updateRepeatMode()

    @Query("UPDATE player_config SET is_shuffle_mode_enabled = CASE WHEN is_shuffle_mode_enabled = 0 THEN 1 ELSE 0 END")
    fun updateShuffleMode()

    @Query("UPDATE player_config SET is_shuffle_mode_enabled = :isShuffle")
    fun setShuffleMode(isShuffle: Boolean)

    @Query("DELETE FROM player_config")
    fun clear()
}