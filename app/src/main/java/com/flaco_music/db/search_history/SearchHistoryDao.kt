package com.flaco_music.db.search_history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.flaco_music.ui.adapters.search.Album
import com.flaco_music.ui.adapters.search.Artist
import com.flaco_music.ui.adapters.search.Audio

@Dao
interface SearchHistoryDao {

    @Query("SELECT * FROM audios_history")
    fun getAudiosHistory(): List<Audio>

    @Query("SELECT * FROM albums_history")
    fun getAlbumsHistory(): List<Album>

    @Query("SELECT * FROM artists_history")
    fun getArtistsHistory(): List<Artist>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAudio(track: Audio)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAlbum(album: Album)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertArtist(artist: Artist)
}