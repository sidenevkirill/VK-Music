package com.flaco_music.utils.extentions

import java.util.stream.Collectors

val <T : Any, R : List<T>?> List<R>.flatten: List<T>
    get() {
        return stream().flatMap { it?.stream() }.collect(Collectors.toList())
    }