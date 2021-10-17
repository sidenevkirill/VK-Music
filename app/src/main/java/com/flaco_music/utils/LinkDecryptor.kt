package com.flaco_music.utils

import java.util.regex.Pattern

object LinkDecryptor {
    fun getMp3FromM3u8(url: String): String {
        if ("index.m3u8?" !in url) return url

        val regexPattern = if ("/audios/" in url) {
            """^(.+?)/[^/]+?/audios/([^/]+)/.+$"""
        } else {
            """^(.+?)/(p[0-9]+)/[^/]+?/([^/]+)/.+$"""
        }

        val pattern = Pattern.compile(regexPattern)
        val matcher = pattern.matcher(url)

        return if (matcher.matches()) {
            if ("/audios/" in url) {
                "${matcher.group(1)}/audios/${matcher.group(2)}.mp3"
            } else {
                "${matcher.group(1)}/${matcher.group(2)}/${matcher.group(3)}.mp3"
            }
        } else ""
    }
}