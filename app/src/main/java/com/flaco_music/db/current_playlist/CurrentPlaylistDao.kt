package com.flaco_music.db.current_playlist

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CurrentPlaylistDao {

    @Query("SELECT * FROM current_playlist ORDER BY position")
    fun getCurrentPlaylist(): List<CurrentPlaylistItem>

    @Query("SELECT * FROM current_playlist ORDER BY position")
    fun getCurrentPlaylistLiveData(): LiveData<List<CurrentPlaylistItem>>

    @Query("SELECT * FROM current_playlist WHERE isPlaying IS 1")
    fun getCurrentPlayingTrack(): CurrentPlaylistItem

    @Query("SELECT * FROM current_playlist WHERE isPlaying IS 1")
    fun getCurrentPlayingTrackLiveData(): LiveData<CurrentPlaylistItem?>

    @Insert
    fun insert(currentPlaylistItem: CurrentPlaylistItem)

    @Insert
    fun insert(currentPlaylistItems: List<CurrentPlaylistItem>)

    @Query("UPDATE current_playlist SET isPlaying = CASE WHEN position = :positionId THEN 1 ELSE 0 END")
    fun setCurrentPlayingTrack(positionId: Int)

    @Query("SELECT * FROM current_playlist WHERE position = :position")
    fun getTrackByPosition(position: Int): CurrentPlaylistItem

    @Query("DELETE FROM current_playlist")
    fun clear()
}