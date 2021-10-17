package com.flaco_music.utils.extentions

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import com.flaco_music.R
import java.util.*


inline fun <reified T : View> T.onClick(crossinline function: (T) -> Unit) {
    return setOnClickListener {
        if (it is T) {
            function(it)
        }
    }
}

inline fun <reified T : View> T.onLongClick(crossinline function: (T) -> Unit) {
    return setOnLongClickListener {
        if (it is T) {
            function(it)
        }
        true
    }
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.visibleWithFading() {
    val animation = AnimationUtils.loadAnimation(context, R.anim.fade_in)
    startAnimation(animation)
    visibility = View.VISIBLE
}

fun View.invisibleWithFading() {
    val animation = AnimationUtils.loadAnimation(context, R.anim.fade_out)
    animation.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation?) {
        }

        override fun onAnimationEnd(animation: Animation?) {
            visibility = View.INVISIBLE
        }

        override fun onAnimationRepeat(animation: Animation?) {
        }
    })
    startAnimation(animation)
}

fun View.goneWithFading() {
    val animation = AnimationUtils.loadAnimation(context, R.anim.fade_out)
    animation.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation?) {
        }

        override fun onAnimationEnd(animation: Animation?) {
            visibility = View.GONE
        }

        override fun onAnimationRepeat(animation: Animation?) {
        }
    })
    startAnimation(animation)
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun View.showKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    imm!!.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
}

fun View.setRandomBgColor(): Int {
    val random = Random()
    val color: Int = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))
    backgroundTintList = ColorStateList.valueOf(color)
    return color
}