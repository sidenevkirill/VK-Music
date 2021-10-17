package com.flaco_music.utils.extentions

import kotlinx.coroutines.*

fun coroutineDefault(block: suspend CoroutineScope.() -> Unit): Job {
    return GlobalScope.launch {
        block(this)
    }
}

fun coroutineMain(block: suspend CoroutineScope.() -> Unit): Job {
    return GlobalScope.launch(Dispatchers.Main) {
        block(this)
    }
}

fun coroutineIO(block: suspend CoroutineScope.() -> Unit): Job {
    return GlobalScope.launch(Dispatchers.IO) {
        block(this)
    }
}
