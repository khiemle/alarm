package com.khiemle.alarm

import android.content.Context
import android.preference.PreferenceManager
import java.util.*

enum class TimerState {
    Paused,
    Running,
    Stopped
}

interface PrefHelper {
    fun getTimeLength() : Long
    fun setTimeLength(timeLength: Long)
    fun getStartedTime() : Long
    fun setStartedTime(now: Long)
    fun getTimeRemaining() : Long
    fun setTimeRemaining(timeRemaining: Long)
    fun getTimerState() : TimerState
    fun setTimerState(state: TimerState)
    fun getCurrentTime(): Long
}

class PrefHelperImpl(private val context: Context) : PrefHelper {

    companion object {
        private const val ALARM_TIME_LENGTH = "com.khiemle.alarm.time_length"
        private const val ALARM_REMAINING_TIME = "com.khiemle.alarm.remaining_time"
        private const val ALARM_TIMER_STATE = "com.khiemle.alarm.timer_state"
        private const val ALARM_STARTED_TIME = "com.khiemle.alarm.started_time"
    }

    override fun getTimeLength() : Long {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        return pref.getLong(ALARM_TIME_LENGTH, 0L)
    }

    override fun setTimeLength(timeLength: Long) {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putLong(ALARM_TIME_LENGTH, timeLength)
        editor.apply()
    }
    override fun getStartedTime() : Long {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        return pref.getLong(ALARM_STARTED_TIME, 0L)
    }

    override fun setStartedTime(now: Long) {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putLong(ALARM_STARTED_TIME, now)
        editor.apply()
    }

    override fun getTimeRemaining() : Long {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        return pref.getLong(ALARM_REMAINING_TIME, 0L)
    }

    override fun setTimeRemaining(timeRemaining: Long) {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putLong(ALARM_REMAINING_TIME, timeRemaining)
        editor.apply()
    }

    override fun getTimerState() : TimerState {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        return TimerState.values()[pref.getInt(ALARM_TIMER_STATE, 2)]
    }

    override fun setTimerState(state: TimerState) {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putInt(ALARM_TIMER_STATE, TimerState.values().indexOf(state))
        editor.apply()
    }

    override fun getCurrentTime(): Long = Calendar.getInstance().timeInMillis
}