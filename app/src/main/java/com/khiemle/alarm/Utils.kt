package com.khiemle.alarm


const val ONE_SECOND = 1000
const val ONE_MINUTE = 60 * 1000
const val ONE_HOUR = 60 * 60 * 1000

fun getMillis(hour: Int, minute: Int, second: Int) =
    (((hour * 60 + minute) * 60 + second) * 1000).toLong()

fun getDisplayValue(timeInMillis: Long): Triple<Int, Int, Int> {
    val hours = timeInMillis / ONE_HOUR
    var remaining = timeInMillis % ONE_HOUR
    val minute = remaining / ONE_MINUTE
    remaining %= ONE_MINUTE
    val second = remaining / ONE_SECOND
    return Triple(hours.toInt(), minute.toInt(), second.toInt())
}

fun getPercentage(remainingTime: Long, timeLength: Long): Int =
    (100 * (remainingTime.toFloat() / timeLength)).toInt()