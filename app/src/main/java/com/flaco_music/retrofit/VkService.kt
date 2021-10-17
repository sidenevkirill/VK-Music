package com.flaco_music.retrofit

import com.flaco_music.retrofit.api.VkTokenApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class VkService {

    private var mRetrofit: Retrofit

    private val httpClient: OkHttpClient
        get() {
            return OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()
        }

    private val loggingInterceptor: Interceptor
        get() {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            return loggingInterceptor
        }

    val vkTokenApi: VkTokenApi
        get() = mRetrofit.create(VkTokenApi::class.java)

    init {
        mRetrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
    }

    companion object {
        const val BASE_URL = "https://oauth.vk.com"
    }
}