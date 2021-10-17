package com.flaco_music.utils

import java.util.*

object RandomUtils {

    fun getSaltString(saltChars: String = "0123456789abcdef", length: Int = 16): String {
        val salt = StringBuilder()
        val rnd = Random()
        while (salt.length < length) {
            val index = (rnd.nextFloat() * saltChars.length).toInt()
            salt.append(saltChars[index])
        }
        return salt.toString()
    }

    fun getSaltNumbersString(saltNumbers: String = "0123456789", length: Int = 16): String {
        return getSaltString(saltNumbers, length)
    }

    fun getRandomNumber(start: Int, end: Int): Int {
        return (start..end).random()
    }
}