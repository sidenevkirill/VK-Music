package com.flaco_music.retrofit.repository

import com.flaco_music.retrofit.api.VkTokenApi
import com.flaco_music.retrofit.models.GetVkTokenResponse
import com.flaco_music.utils.RandomUtils
import retrofit2.Call

class VkTokenRepository(private val vkTokenApi: VkTokenApi) {

    private val randomDeviceId: String
        get() = RandomUtils.getSaltString("0123456789abcdef", 16)

    fun getVkToken(
        login: String,
        password: String,
    ): Call<GetVkTokenResponse> {
        return vkTokenApi.getVkToken(login, password, randomDeviceId)
    }

    fun getVkToken2Fa(
        login: String,
        password: String,
        code: String,
    ): Call<GetVkTokenResponse> {
        return vkTokenApi.getVkToken2Fa(login, password, code, randomDeviceId)
    }
}