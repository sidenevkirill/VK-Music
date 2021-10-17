package com.flaco_music.utils.extentions

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

suspend fun URL.getBitmap(): Bitmap? = withContext(Dispatchers.IO) {
    try {
        val connection: HttpURLConnection = this@getBitmap.openConnection() as HttpURLConnection
        connection.doInput = true
        connection.connect()
        val input: InputStream = connection.inputStream
        val bitmap = BitmapFactory.decodeStream(input)
        bitmap
    } catch (e: IOException) {
        null
    }
}

suspend fun URL.getBytes(): ByteArray? = withContext(Dispatchers.IO) {
    try {
        val connection: HttpURLConnection = this@getBytes.openConnection() as HttpURLConnection
        connection.doInput = true
        connection.connect()
        val input: ByteArrayOutputStream = connection.outputStream as ByteArrayOutputStream
        val bytes = input.toByteArray()
        bytes
    } catch (e: IOException) {
        null
    }
}