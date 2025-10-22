package com.rojassac.canchaya.utils

import android.content.Context
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Long.toDateString(format: String = Constants.DATE_FORMAT): String {
    val sdf = SimpleDateFormat(format, Locale("es", "PE"))
    return sdf.format(Date(this))
}

fun String.isValidEmail(): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.isValidPhone(): Boolean {
    return this.length == 9 && this.all { it.isDigit() }
}

fun generateRandomCode(length: Int = Constants.CODIGO_LENGTH): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return (1..length)
        .map { chars.random() }
        .joinToString("")
}
