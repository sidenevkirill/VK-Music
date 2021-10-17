package com.flaco_music.ui.screens.login

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.flaco_music.R
import com.flaco_music.retrofit.repository.VkTokenRepository
import com.flaco_music.utils.extentions.coroutineIO
import com.flaco_music.utils.extentions.invoke
import com.flaco_music.utils.preferences.Preferences
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class LoginViewModel(application: Application) : AndroidViewModel(application), KoinComponent {

    val isLoggingProcessingLiveData: LiveData<Boolean> = MutableLiveData()

    val errorMessageLiveData: LiveData<String> = MutableLiveData()

    val isCodeNeededLiveData: LiveData<Boolean> = MutableLiveData(false)

    private val preferences: Preferences by inject()

    private val vkTokenRepository: VkTokenRepository by inject()

    private val context: Context by lazy { getApplication<Application>().applicationContext }

    fun obtainVkToken(
        login: String,
        password: String,
        code: String,
        onSuccess: () -> Unit
    ) {
        (isLoggingProcessingLiveData as MutableLiveData).postValue(true)

        if (preferences.vkAccessToken.isNotEmpty()) {
            onSuccess()
            isLoggingProcessingLiveData.postValue(false)
            return
        }

        val call = if (isCodeNeededLiveData.value == false) {
            vkTokenRepository.getVkToken(login, password)
        } else {
            vkTokenRepository.getVkToken2Fa(login, password, code)
        }

        call.invoke(
            onResponseSuccessful = { response ->
                coroutineIO {
                    response.body()?.let { body ->
                        preferences.vkAccessToken = body.accessToken
                        preferences.userId = body.userId
                        onSuccess()
                    }
                }
            },
            onResponseNotSuccessful = {
                if (it.error == "invalid_client") {
                    val errorMessage = context.getString(R.string.error_auth_invalid_credentials)
                    (errorMessageLiveData as MutableLiveData).postValue(errorMessage)
                } else if (it.error == "need_validation") {
                    val errorMessage = context.getString(R.string.error_auth_code_needed)
                    (errorMessageLiveData as MutableLiveData).postValue(errorMessage)
                    (isCodeNeededLiveData as MutableLiveData).postValue(true)
                }
            },
            onFailure = {
                (errorMessageLiveData as MutableLiveData).postValue("Failed.")
            },
            anyway = {
                isLoggingProcessingLiveData.postValue(false)
            }
        )
    }

    companion object {
        private const val TAG = "LoginViewModel"
    }
}