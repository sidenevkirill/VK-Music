package com.flaco_music.db.my_music_ids

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MyMusicIdsDao {

    @Query("SELECT * FROM my_music_ids")
    suspend fun getMyMusicIds(): List<MyMusicIdItem>

    @Query("SELECT id FROM my_music_ids WHERE url = :url")
    suspend fun getMyMusicIdByUrl(url: String): Int

    @Query("SELECT EXISTS(SELECT 1 FROM my_music_ids WHERE url = :url);")
    fun isTrackInUserAudios(url: String): LiveData<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(id: MyMusicIdItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ids: List<MyMusicIdItem>)

    @Query("DELETE FROM my_music_ids WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("DELETE FROM my_music_ids")
    suspend fun clear()
}