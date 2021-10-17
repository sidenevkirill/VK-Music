package com.flaco_music.db.downloads

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DownloadsDao {

    @Query("SELECT * FROM downloads ORDER BY downloadTime")
    fun getDownloads(): List<DownloadItem>

    @Query("SELECT * FROM downloads ORDER BY downloadTime")
    fun getDownloadsLiveData(): LiveData<List<DownloadItem>>

    @Query("SELECT * FROM downloads WHERE url = :trackUrl")
    fun getDownloadByTrackUrl(trackUrl: String): DownloadItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(downloadItem: DownloadItem)

    @Query("DELETE FROM downloads WHERE trackId = :trackId")
    suspend fun deleteByTrackId(trackId: Int)

    @Query("DELETE FROM downloads")
    suspend fun clear()
}