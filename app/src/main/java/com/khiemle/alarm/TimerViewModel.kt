package com.khiemle.alarm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TimerViewModel(
    private val prefHelper: PrefHelper,
    private val timerBackground: TimerBackground
) : ViewModel() {

    class Factory(
        private val prefHelper: PrefHelper,
        private val timerBackground: TimerBackground
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TimerViewModel(
                prefHelper = prefHelper,
                timerBackground = timerBackground
            ) as T
        }
    }

    private val _timerState = MutableLiveData<CountdownTimerState>()
    val timerState: LiveData<CountdownTimerState>
        get() = _timerState

    init {
        initialize()
        val state = prefHelper.getTimerState()
        if (state == TimerState.Paused || state == TimerState.Running) {
            val timeLength = prefHelper.getTimeLength()
            val remainingTime = prefHelper.getTimeRemaining()
            val startedTime = prefHelper.getStartedTime()
            val current = prefHelper.getCurrentTime()
            if (startedTime + timeLength > current) {
                if (state == TimerState.Paused) {
                    _timerState.postValue(CountdownPaused(timeLength, remainingTime - (current - startedTime)))
                } else if (state == TimerState.Running) {
                    _timerState.postValue(CountdownRunning(timeLength, remainingTime - (current - startedTime)))
                }
            }
        }
    }

    fun onTimeSet(hour: Int, minute: Int, second: Int) {
        _timerState.postValue(CountdownIdle(timeLength = getMillis(hour, minute, second)))
    }

    private fun initialize() {
        _timerState.postValue(CountdownStopped)
    }

    fun startTimer(timeLength: Long, remainingTime: Long, now: Long) {
        _timerState.postValue(
            CountdownRunning(
                timeLength = timeLength,
                remainingTime = remainingTime
            )
        )
        prefHelper.setStartedTime(now = now)
        _timerState.value?.let {
            prefHelper.setTimeLength(timeLength = it.timeLength)
            prefHelper.setTimeRemaining(timeRemaining = it.remainingTime)
            prefHelper.setTimerState(state = TimerState.Running)

            timerBackground.startTimer(
                timeLength = it.timeLength, remainingTime = it.remainingTime,
                now = now
            )
        }

    }

    fun onTick(remainingTime: Long) {
        val countdownRunning = _timerState.value as? CountdownRunning
        countdownRunning?.remainingTime = remainingTime
    }

    fun pauseTimer() {
        val countdownRunning = _timerState.value as? CountdownRunning
        countdownRunning?.let {
            _timerState.postValue(
                CountdownPaused(
                    countdownRunning.timeLength,
                    countdownRunning.remainingTime
                )
            )
            prefHelper.setTimeLength(timeLength = it.timeLength)
            prefHelper.setTimeRemaining(timeRemaining = it.remainingTime)
            prefHelper.setTimerState(state = TimerState.Paused)

            timerBackground.stopTimer()
        }
    }

    fun resumeTimer(now: Long) {
        val countdownPaused = _timerState.value as? CountdownPaused
        countdownPaused?.let {
            _timerState.postValue(
                CountdownRunning(
                    timeLength = it.timeLength,
                    remainingTime = it.remainingTime
                )
            )
            prefHelper.setStartedTime(now = now)
            prefHelper.setTimeLength(timeLength = it.timeLength)
            prefHelper.setTimeRemaining(timeRemaining = it.remainingTime)
            prefHelper.setTimerState(state = TimerState.Running)
            timerBackground.startTimer(
                timeLength = it.timeLength, remainingTime = it.remainingTime,
                now = now
            )
        }
    }

    fun stopOrEndTimer() {
        _timerState.postValue(CountdownStopped)
        _timerState.value?.let {
            prefHelper.setTimeLength(timeLength = 0)
            prefHelper.setTimeRemaining(timeRemaining = 0)
            prefHelper.setTimerState(state = TimerState.Stopped)
        }
        timerBackground.stopTimer()
    }
}

sealed class CountdownTimerState(val timeLength: Long, var remainingTime: Long)
class CountdownIdle(timeLength: Long) : CountdownTimerState(timeLength, timeLength)
class CountdownPaused(timeLength: Long, remainingTime: Long) :
    CountdownTimerState(timeLength, remainingTime)

class CountdownRunning(timeLength: Long, remainingTime: Long) :
    CountdownTimerState(timeLength, remainingTime)

object CountdownStopped : CountdownTimerState(0, 0)