package org.alnet.allnetandroid

import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by docouto on 3/26/18.
 */

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}

fun Date.toDateString(): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm:ss", Locale.getDefault())
    return formatter.format(this)
}

fun String.toDate(): Date {
    val formatter = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm:ss", Locale.getDefault())
    return formatter.parse(this)
}