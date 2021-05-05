package com.khiemle.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

interface TimerBackground {
    fun startTimer(timeLength: Long, remainingTime: Long, now: Long)
    fun stopTimer()
}

class TimerBackgroundImpl(private val context: Context): TimerBackground {
    override fun startTimer(timeLength: Long, remainingTime: Long, now: Long) {
        val wakeUpTime = (now + remainingTime)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TimerExpiredReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeUpTime, pendingIntent)
    }

    override fun stopTimer() {
        val intent = Intent(context, TimerExpiredReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }
}