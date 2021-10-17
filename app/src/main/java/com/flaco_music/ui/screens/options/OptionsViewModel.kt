package com.flaco_music.ui.screens.options

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.flaco_music.utils.CoverResolutionHelper
import com.flaco_music.utils.preferences.Preferences
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class OptionsViewModel : ViewModel(), KoinComponent {

    private val preferences: Preferences by inject()

    val isDarkModeEnabledLiveData: LiveData<Boolean> = MutableLiveData(preferences.isDarkModeEnabled)
    val isAutoCachingEnabledLiveData: LiveData<Boolean> = MutableLiveData(preferences.isAutoCachingEnabled)

    fun onEnableDarkModeSwitchChecked(isChecked: Boolean) {
        (isDarkModeEnabledLiveData as MutableLiveData).postValue(isChecked)
        preferences.isDarkModeEnabled = isChecked
    }

    fun onEnableAutoCachingSwitchChecked(isChecked: Boolean) {
        (isAutoCachingEnabledLiveData as MutableLiveData).postValue(isChecked)
        preferences.isAutoCachingEnabled = isChecked
    }

    fun changeCoverResolution(coverResolutionIndex: Int) {
        CoverResolutionHelper.coverResolutionIndex = coverResolutionIndex
    }

    fun logout() {
        preferences.clear()
    }
}