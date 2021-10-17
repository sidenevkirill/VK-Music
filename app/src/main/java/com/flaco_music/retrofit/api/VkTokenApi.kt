package com.flaco_music.retrofit.api

import com.flaco_music.retrofit.models.GetVkTokenResponse
import com.flaco_music.utils.constants.ApiConstants
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


interface VkTokenApi {
    @GET(
        "token" +
                "?grant_type=password" +
                "&client_id=${ApiConstants.Token.CLIENT_ID}" +
                "&client_secret=${ApiConstants.Token.CLIENT_SECRET}" +
                "&v=${ApiConstants.API_VERSION_CODE}" +
                "&2fa_supported=1" +
                "&lang=en" +
                "&scope=all"
    )
    fun getVkToken(
        @Query("username")
        login: String,
        @Query("password")
        password: String,
        @Query("device_id")
        deviceId: String
    ): Call<GetVkTokenResponse>

    @GET(
        "token" +
                "?grant_type=password" +
                "&client_id=${ApiConstants.Token.CLIENT_ID}" +
                "&client_secret=${ApiConstants.Token.CLIENT_SECRET}" +
                "&v=${ApiConstants.API_VERSION_CODE}" +
                "&2fa_supported=1" +
                "&lang=en" +
                "&scope=all"
    )
    fun getVkToken2Fa(
        @Query("username")
        login: String,
        @Query("password")
        password: String,
        @Query("code")
        code: String,
        @Query("device_id")
        deviceId: String
    ): Call<GetVkTokenResponse>
}