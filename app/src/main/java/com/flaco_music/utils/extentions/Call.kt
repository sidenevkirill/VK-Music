package com.flaco_music.utils.extentions

import com.flaco_music.retrofit.models.ErrorResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun <T> Call<T>.invoke(
    onResponseSuccessful: (response: Response<T>) -> Unit = { _ -> },
    onResponseNotSuccessful: (response: ErrorResponse) -> Unit = { },
    onFailure: () -> Unit = {},
    anyway: () -> Unit = {}
) {
    enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            if (response.isSuccessful) {
                onResponseSuccessful(response)
            } else {
                val errorBody = response.errorResponseBody ?: throw Exception("Error response body is wrong.")
                onResponseNotSuccessful(errorBody)
            }
            anyway()
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            onFailure()
            anyway()
        }
    })
}