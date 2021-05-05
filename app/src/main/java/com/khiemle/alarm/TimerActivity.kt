package com.khiemle.alarm

import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.khiemle.alarm.databinding.ActivityTimerBinding
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import java.util.*

class TimerActivity : AppCompatActivity(), TimePickerDialog.OnTimeSetListener {
    private lateinit var binding: ActivityTimerBinding
    private val viewModel: TimerViewModel by viewModels {
        TimerViewModel.Factory(
            prefHelper =  PrefHelperImpl(context = applicationContext),
            timerBackground = TimerBackgroundImpl(context = applicationContext)
        )
    }
    private var countDownTimer: CountDownTimer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimerBinding.inflate(layoutInflater)
        applyInteractions()
        observeViewModel()
        setContentView(binding.root)
    }

    private fun updateButtons(state: CountdownTimerState) {
        binding.tvCountingDownTimer.isClickable = state == CountdownStopped
        when (state) {
            is CountdownIdle -> {
                with(binding) {
                    btnControl.text = getString(R.string.start)
                    btnControl.isEnabled = true
                    btnCancel.isEnabled = false
                    btnControl.setOnClickListener {
                        viewModel.startTimer(
                            timeLength = state.timeLength,
                            remainingTime = state.remainingTime,
                            now = Calendar.getInstance().timeInMillis
                        )
                    }
                }
            }
            is CountdownPaused -> {
                with(binding) {
                    btnControl.text = getString(R.string.resume)
                    btnCancel.isEnabled = true
                    btnControl.isEnabled = true
                    btnControl.setOnClickListener {
                        viewModel.resumeTimer(now = Calendar.getInstance().timeInMillis)
                    }
                    btnCancel.setOnClickListener {
                        viewModel.stopOrEndTimer()
                        countDownTimer?.cancel()
                    }
                }
            }
            is CountdownRunning -> {
                with(binding) {
                    btnControl.text = getString(R.string.pause)
                    btnCancel.isEnabled = true
                    btnControl.isEnabled = true
                    btnControl.setOnClickListener {
                        viewModel.pauseTimer()
                        countDownTimer?.cancel()
                    }
                    btnCancel.setOnClickListener {
                        viewModel.stopOrEndTimer()
                        countDownTimer?.cancel()
                    }
                }
            }
            CountdownStopped -> {
                with(binding) {
                    btnControl.text = getString(R.string.start)
                    btnControl.isEnabled = false
                    btnCancel.isEnabled = false
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.timerState.observe(this) { state ->
            updateButtons(state)
            displayCountdownText(state.remainingTime)
            updateProgressBar(
                millisUntilFinished = state.remainingTime,
                timeLength = state.timeLength
            )
            when (state) {
                is CountdownIdle -> {
                }
                is CountdownPaused -> {
                }
                is CountdownRunning -> {
                    startCountdownTimer(state.remainingTime, state.timeLength)
                }
                CountdownStopped -> {
                }
            }
        }
    }

    private fun applyInteractions() {
        with(binding) {
            tvCountingDownTimer.setOnClickListener {
                showTimePickerDialog();
            }
        }
    }

    private fun showTimePickerDialog() {
        val timePickerDialog: TimePickerDialog = TimePickerDialog.newInstance(
            this@TimerActivity,
            0, 0, 0, true
        )
        timePickerDialog.enableSeconds(true)
        timePickerDialog.show(supportFragmentManager, "TimePickerDialog")
    }

    private fun startCountdownTimer(remaining: Long, timeLength: Long) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(remaining, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                displayCountdownText(millisUntilFinished)
                updateProgressBar(millisUntilFinished, timeLength)
            }

            override fun onFinish() {
                updateProgressBar(0, timeLength)
                viewModel.stopOrEndTimer()
                showTimeUpDialog()
            }
        }
        countDownTimer?.start()
    }

    private fun updateProgressBar(millisUntilFinished: Long, timeLength: Long) {
        binding.timerProgressBar.progress = getPercentage(
            remainingTime = millisUntilFinished,
            timeLength = timeLength
        )
        viewModel.onTick(millisUntilFinished)
    }

    private fun displayCountdownText(millisUntilFinished: Long) {
        val (hours, minutes, seconds) = getDisplayValue(millisUntilFinished)
        binding.tvCountingDownTimer.text = getString(
            R.string.display_counting_timer,
            hours.twoDigitsWithZero(),
            minutes.twoDigitsWithZero(),
            seconds.twoDigitsWithZero()
        )
    }

    override fun onTimeSet(view: TimePickerDialog?, hourOfDay: Int, minute: Int, second: Int) {
        viewModel.onTimeSet(hourOfDay, minute, second)
    }


    private fun showTimeUpDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage("Time up")
            .setTitle("Alarm")
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
}

fun Int.twoDigitsWithZero(): String = "${this / 10}${this % 10}"
