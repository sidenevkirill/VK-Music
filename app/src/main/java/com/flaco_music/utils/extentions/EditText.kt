package com.flaco_music.utils.extentions

import android.widget.EditText

val EditText.string: String
    get() {
        return text.toString()
    }