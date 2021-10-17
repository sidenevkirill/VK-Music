package com.flaco_music.db.my_music_ids

import androidx.lifecycle.LiveData

class MyMusicIdsRepository(private val myMusicIdsDao: MyMusicIdsDao) {

    suspend fun getMyMusicIds(): List<MyMusicIdItem> = myMusicIdsDao.getMyMusicIds()

    suspend fun getMyMusicIdByUrl(url: String): Int = myMusicIdsDao.getMyMusicIdByUrl(url)

    fun isTrackInUserAudios(url: String): LiveData<Boolean> {
        return myMusicIdsDao.isTrackInUserAudios(url)
    }

    suspend fun insert(item: MyMusicIdItem) {
        myMusicIdsDao.insert(item)
    }

    suspend fun insert(items: List<MyMusicIdItem>) {
        myMusicIdsDao.clear()
        myMusicIdsDao.insert(items)
    }

    suspend fun delete(id: Int) {
        myMusicIdsDao.delete(id)
    }
}