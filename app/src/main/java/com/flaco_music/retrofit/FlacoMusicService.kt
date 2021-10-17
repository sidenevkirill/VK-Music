package com.flaco_music.retrofit

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.flaco_music.retrofit.api.AudioApi
import com.flaco_music.utils.constants.ApiConstants
import com.flaco_music.utils.preferences.Preferences
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit


@KoinApiExtension
class FlacoMusicService(private val context: Context, private val isCachingEnabled: Boolean = true) : KoinComponent {

    val audioApi: AudioApi
        get() = mRetrofit.create(AudioApi::class.java)

    private var mRetrofit: Retrofit

    private val preferences: Preferences by inject()

    private val loggingInterceptor: Interceptor by lazy {
        val loggingInterceptor = HttpLoggingInterceptor(ApiLogger())
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        loggingInterceptor
    }

    private val queryInterceptor: Interceptor by lazy {
        Interceptor { chain ->
            val original: Request = chain.request()
            val originalHttpUrl: HttpUrl = original.url

            val url = originalHttpUrl.newBuilder()
                .addQueryParameter("access_token", preferences.vkAccessToken)
                .addQueryParameter("v", ApiConstants.API_VERSION_CODE)
                .addQueryParameter("lang", Locale.getDefault().language)
                .build()

            val requestBuilder: Request.Builder = original.newBuilder()
                .url(url)
                .header(
                    "User-Agent",
                    "VKAndroidApp/5.52-4543 (Android 5.1.1; SDK 22; x86_64; unknown Android SDK built for x86_64; en; 320x240)"
                )

            val request: Request = requestBuilder.build()
            chain.proceed(request)
        }
    }

    private val networkInterceptor = Interceptor { chain ->
        val cacheControl: CacheControl = CacheControl.Builder()
            .maxAge(7, TimeUnit.SECONDS)
            .build()

        val response: Response = chain.proceed(chain.request())

        response.newBuilder()
            .removeHeader(HEADER_PRAGMA)
            .removeHeader(HEADER_CACHE_CONTROL)
            .header(HEADER_CACHE_CONTROL, cacheControl.toString())
            .build()
    }

    private val offlineInterceptor = Interceptor { chain ->
        var request: Request = chain.request()
        if (!isNetworkConnected) {
            val cacheControl: CacheControl = CacheControl.Builder()
                .maxStale(7, TimeUnit.DAYS)
                .build()

            request = request.newBuilder()
                .removeHeader(HEADER_PRAGMA)
                .removeHeader(HEADER_CACHE_CONTROL)
                .cacheControl(cacheControl)
                .build()
        }
        chain.proceed(request)
    }

    private val isNetworkConnected: Boolean
        get() {
            val type = getConnectionType(context)
            return type != 0
        }

    private val cache: Cache
        get() = Cache(context.cacheDir, 1000L * 1024L * 1024L) // 1GB

    init {
        mRetrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(createClient())
            .build()
    }

    private fun createClient(): OkHttpClient {
        val clientBuilder = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(queryInterceptor)

        if (isCachingEnabled) clientBuilder.enableCaching()

        return clientBuilder.build()
    }

    private fun OkHttpClient.Builder.enableCaching(): OkHttpClient.Builder {
        return cache(cache)
            .addNetworkInterceptor(networkInterceptor)
            .addInterceptor(offlineInterceptor)
    }

    private fun getConnectionType(context: Context): Int {
        var result = 0
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        cm?.getNetworkCapabilities(cm.activeNetwork)?.run {
            result = when {
                hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> 2
                hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> 1
                hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> 3
                else -> 0
            }
        }

        return result
    }

    class ApiLogger : HttpLoggingInterceptor.Logger {
        override fun log(message: String) {
            val logName = "ApiLogger"
            if (message.startsWith("{") || message.startsWith("[")) {
                try {
                    val prettyPrintJson = GsonBuilder().setPrettyPrinting()
                        .create().toJson(JsonParser().parse(message))
                    prettyPrintJson.lines().forEachIndexed { index, log ->
                        if (index == 0) {
                            Log.d(logName, log)
                        } else {
                            Log.d("", log)
                        }
                    }
                } catch (e: Exception) {
                    Log.d(logName, message)
                }
            } else {
                Log.d(logName, message)
                return
            }
        }
    }

    companion object {
        const val BASE_URL = "https://api.vk.com/method/"
        const val HEADER_CACHE_CONTROL = "Cache-Control"
        const val HEADER_PRAGMA = "Pragma"
    }
}