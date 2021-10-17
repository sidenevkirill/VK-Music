package com.flaco_music.ui.screens.explore

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.flaco_music.utils.constants.ApiConstants

class ExploreViewModel(application: Application) : AndroidViewModel(application) {

    val genresLiveData: MutableLiveData<List<ApiConstants.Genres.Genre>> = MutableLiveData()
    val isLoadingLiveData: MutableLiveData<Boolean> = MutableLiveData()

    fun loadGenres() {
        isLoadingLiveData.postValue(true)

        val genres = ApiConstants.Genres.get(getApplication<Application>().applicationContext)
        genresLiveData.postValue(genres)

        isLoadingLiveData.postValue(false)
    }
}