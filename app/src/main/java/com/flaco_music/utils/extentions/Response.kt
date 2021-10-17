package com.flaco_music.utils.extentions

import com.flaco_music.retrofit.models.ErrorResponse
import com.google.gson.Gson
import retrofit2.Response

val <T> Response<T>.errorResponseBody: ErrorResponse?
    get() {
        val errorBody = errorBody()
        errorBody?.let {
            return Gson().fromJson(it.string(), ErrorResponse::class.java)
        } ?: return null
    }