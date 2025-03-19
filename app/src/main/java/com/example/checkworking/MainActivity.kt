package com.example.checkworking

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.checkworking.MyNotificationListenerService.Companion.CHANNEL_ID
import com.example.checkworking.MyNotificationListenerService.Companion.LOCK
import com.example.checkworking.MyNotificationListenerService.Companion.UNLOCK
import com.example.checkworking.NotifyUtil.cancelAlarm
import com.example.checkworking.NotifyUtil.createNotificationChannel
import com.example.checkworking.NotifyUtil.setExactAlarm
import com.example.checkworking.TimeUtils.ListIdNotify
import com.example.checkworking.TimeUtils.MAX_PROGRESS
import com.example.checkworking.TimeUtils.OFF_HALF
import com.example.checkworking.TimeUtils.OVER_TIME
import com.example.checkworking.TimeUtils.TIME_BEFORE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.example.checkworking.TimeUtils.TIME_IN
import com.example.checkworking.TimeUtils.TIME_OFF
import com.example.checkworking.TimeUtils.TIME_OUT_ON_WORK
import com.example.checkworking.TimeUtils.TIME_WORKING
import com.example.checkworking.TimeUtils.TOTAL_TIME_OUT
import com.example.checkworking.TimeUtils.convertMillisToTimeNoSecond
import com.example.checkworking.TimeUtils.convertTimeCount
import com.example.checkworking.TimeUtils.getDate
import com.example.checkworking.TimeUtils.getTimeOff
import com.example.checkworking.TimeUtils.getTimeOffFirst
import com.example.checkworking.databinding.ActivityMainBinding
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import java.util.Calendar


class MainActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityMainBinding
    private val multiplePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions -> }
    private var scope: CoroutineScope? = null
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        askNotificationPermission()
        createNotificationChannel(this)
        actionDayWork()
        actionCheckPermission()
        actionSetTime()
    }

    private fun actionSetTime() {
        mBinding.btnEditTimeIn.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = SharedPreferencesUtil.getLong(this, TIME_IN)
            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)  // 12H hoặc 24H
                .setHour(calendar.get(Calendar.HOUR_OF_DAY))
                .setMinute(calendar.get(Calendar.MINUTE))
                .setTitleText("Chọn giờ")
                .build()

            picker.show(this.supportFragmentManager, "TimePicker")

            picker.addOnPositiveButtonClickListener {
                val selectedHour = picker.hour
                val selectedMinute = picker.minute
                Toast.makeText(this, "Bạn đã chọn: $selectedHour:$selectedMinute", Toast.LENGTH_SHORT).show()
            }
        }

        mBinding.btnEditTotalTimeOut.setOnClickListener {
            val time = SharedPreferencesUtil.getLong(this, TOTAL_TIME_OUT)
            val hour = time.toFloat() / 3600000
            val minute = (time.toFloat() % 3600000) / 60000
            val picker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(hour.toInt())
                .setMinute(minute.toInt())
                .setTitleText("Chọn giờ")
                .build()

            picker.show(this.supportFragmentManager, "TimePicker")

            picker.addOnPositiveButtonClickListener {
                val selectedHour = picker.hour
                val selectedMinute = picker.minute
                Toast.makeText(this, "Bạn đã chọn: $selectedHour:$selectedMinute", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun actionCheckPermission() {
        if (!hasExactAlarmPermission(this)) {
            mBinding.btnPermissionAlarm.visibility = View.VISIBLE
            mBinding.btnPermissionAlarm.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    startActivity(intent)
                }
            }
        } else {
            mBinding.btnPermissionAlarm.visibility = View.GONE
        }

        if (!isNotificationServiceEnable(this)) {
            mBinding.btnPermissionNotification.setOnClickListener {
                mBinding.btnPermissionNotification.visibility = View.VISIBLE
                startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
            }
        } else {
            mBinding.btnPermissionNotification.visibility = View.GONE
        }
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun initView() {
        if (hasExactAlarmPermission(this)) {
            mBinding.tvPermissionGrantedAlarm.visibility = View.VISIBLE
            mBinding.btnPermissionAlarm.visibility = View.GONE
        }
        if (isNotificationServiceEnable(this)) {
            mBinding.tvPermissionGrantedNotification.visibility = View.VISIBLE
            mBinding.btnPermissionNotification.visibility = View.GONE
        }
        bindView()
    }

    fun actionDayWork() {
        mBinding.dayWork.setOnCheckedChangeListener { _, checkedId ->
            val timeIn = SharedPreferencesUtil.getLong(this, TIME_IN)

            when(checkedId) {
                R.id.normal -> {
                    val timeOff = getTimeOff(timeIn + TIME_WORKING)
                    SharedPreferencesUtil.putLong(this, TIME_OFF, timeOff)
                }
                R.id.offFirst -> {
                    SharedPreferencesUtil.putLong(this, TIME_OFF, getTimeOffFirst(timeIn))
                }
                R.id.offLast -> {
                    SharedPreferencesUtil.putLong(this, TIME_OFF, timeIn + OFF_HALF)
                }
            }
            val timeOff = SharedPreferencesUtil.getLong(this, TIME_OFF)
            cancelAlarm(this, ListIdNotify[1])
            cancelAlarm(this, ListIdNotify[2])
            setExactAlarm(this, ListIdNotify[1], timeOff - TIME_BEFORE, "Còn 3 Phút")
            setExactAlarm(this, ListIdNotify[2], timeOff, "Tan làm")
            bindView()
        }
    }

    fun bindView() {
        val totalTimeOut = SharedPreferencesUtil.getLong(this, TOTAL_TIME_OUT)
        val timeIn = SharedPreferencesUtil.getLong(this, TIME_IN)
        val timeOff = SharedPreferencesUtil.getLong(this, TIME_OFF)
        val timeOutOnWork = SharedPreferencesUtil.getLong(this, TIME_OUT_ON_WORK)

        mBinding.tvDate.text = getDate(System.currentTimeMillis())

        mBinding.tvTimeIn.text = "Time in: ${convertMillisToTimeNoSecond(timeIn)} - ${convertMillisToTimeNoSecond(timeOff)}"
        if (totalTimeOut > 0) {
            mBinding.tvTotalTimeout.visibility = View.VISIBLE
            mBinding.tvTotalTimeout.text = "Total time out: ${convertTimeCount(totalTimeOut)}"
        } else {
            mBinding.tvTotalTimeout.visibility = View.GONE
        }


        val channelId = SharedPreferencesUtil.getString(this, CHANNEL_ID, UNLOCK)
        if (channelId == LOCK) {
            stopTimer()
            startTimer(mBinding.tvTimeRemain, mBinding.circularProgress, mBinding.tvProgressPercent)
        } else {
            if (timeOff - timeOutOnWork > 0) {
                mBinding.tvTimeRemain.text = "Time Remain: ${convertTimeCount(timeOff - timeOutOnWork)}"
            } else {
                val ot =  SharedPreferencesUtil.getLong(this, OVER_TIME)
                if (ot > 0) {
                    mBinding.tvTimeRemain.text = "OT: ${convertTimeCount(ot)}"
                }
            }

            val progress = ((timeOutOnWork - timeIn - totalTimeOut).toFloat()/(timeOff - timeIn - totalTimeOut + 1).toFloat())*MAX_PROGRESS
            if (progress < MAX_PROGRESS) {
                mBinding.circularProgress.setProgress(progress.toInt(), true)
                mBinding.tvProgressPercent.text = "${String.format("%.3f", 100 * progress / MAX_PROGRESS)}%"
            } else {
                mBinding.circularProgress.setProgress(MAX_PROGRESS)
                mBinding.tvProgressPercent.text = "${100}%"
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun startTimer(
        timeRemain: TextView,
        circularProgress: CircularProgressIndicator,
        tvProgressPercent: TextView
    ) {
        val totalTimeOut = SharedPreferencesUtil.getLong(this@MainActivity, TOTAL_TIME_OUT)
        val timeIn = SharedPreferencesUtil.getLong(this@MainActivity, TIME_IN)
        val timeOff = SharedPreferencesUtil.getLong(this@MainActivity, TIME_OFF)
        scope = CoroutineScope(Dispatchers.Main)
        scope?.launch {
            flow {
                while (true) {
                    val currentTime = System.currentTimeMillis()
                    emit(currentTime)
                    delay(200)
                }
            }.collect{ time ->
                if (time < timeOff) {
                    timeRemain.text = "Time Remain: ${convertTimeCount(timeOff - time)}"
                    val progress = (time - timeIn - totalTimeOut).toFloat()/(timeOff - timeIn - totalTimeOut).toFloat() * MAX_PROGRESS
                    circularProgress.setProgress(progress.toInt(), true)
                    tvProgressPercent.text = "${String.format("%.3f", 100*progress/MAX_PROGRESS)}%"
                } else {
                    timeRemain.text = "OT: ${convertTimeCount(time - timeOff)}"
                    mBinding.circularProgress.setProgress(MAX_PROGRESS)
                    mBinding.tvProgressPercent.text = "${100}%"
                }

                if (time - timeIn >= 13*60*60*1000 || time - timeOff > (3*60 + 10)*60+1000) {
                    stopTimer()
                }
            }
        }
    }

    private fun stopTimer() {
        scope?.cancel()
    }

    override fun onResume() {
        super.onResume()
        initView()
    }

    override fun onStop() {
        super.onStop()
        stopTimer()
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                multiplePermissionLauncher.launch(
                    arrayOf(
                        android.Manifest.permission.POST_NOTIFICATIONS
                    )
                )
            }
        }
    }

    fun getPackageName(context: Context) {
        if (!hasUsageAccessPermission(this)) {
            openUsageAccessSettings(this)
        }
        GlobalScope.launch(Dispatchers.IO) {
            callHandle(this@MainActivity)
        }
    }

    fun getForegroundApp(context: Context): String? {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()
        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time)

        if (stats != null && stats.isNotEmpty()) {
            val sortedStats = stats.sortedByDescending { it.lastTimeUsed }
            return sortedStats[0].packageName // Ứng dụng đang chạy
        }
        return null
    }
    private val handler = Handler() // Chạy trên Main Thread

    fun callHandle(context: Context) {
        handler.postDelayed({
            Log.d("namepackage", getForegroundApp(context).toString())
            callHandle(context)
        }, 5000)
    }


    fun openUsageAccessSettings(context: Context) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.data = Uri.parse("package:" + context.packageName)
        context.startActivity(intent)
    }

    fun hasUsageAccessPermission(context: Context): Boolean {
        val appOps = context.getSystemService(APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )

        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun isNotificationServiceEnable(context: Context): Boolean {
        val cn = ComponentName(context, MyNotificationListenerService::class.java)
        val enabledListeners = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        return enabledListeners?.contains(cn.flattenToString()) == true
    }

    @SuppressLint("ServiceCast")
    fun requestBatteryOptimization(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        context.startActivity(intent)
    }

    fun hasExactAlarmPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            alarmManager?.canScheduleExactAlarms() == true
        } else {
            true
        }
    }

    private fun showCustomTimeIn(onTimeSelected: (Int, Int) -> Unit) {
        val dialogView = layoutInflater.inflate(R.layout.time_picker_dialog, null)
        val npHour = dialogView.findViewById<NumberPicker>(R.id.npHour)
        val npMinute = dialogView.findViewById<NumberPicker>(R.id.npMinute)

        // Cấu hình NumberPicker cho giờ, phút, giây
        npHour.minValue = 0
        npHour.maxValue = 18

        npMinute.minValue = 0
        npMinute.maxValue = 59

        // Tạo AlertDialog
        AlertDialog.Builder(this)
            .setTitle("Chọn thời gian")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                val selectedHour = npHour.value
                val selectedMinute = npMinute.value
                onTimeSelected(selectedHour, selectedMinute)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCustomTotalTimeOut(onTimeSelected: (Int, Int) -> Unit) {
        val dialogView = layoutInflater.inflate(R.layout.time_picker_dialog, null)
        val npHour = dialogView.findViewById<NumberPicker>(R.id.npHour)
        val npMinute = dialogView.findViewById<NumberPicker>(R.id.npMinute)

        // Cấu hình NumberPicker cho giờ, phút, giây
        npHour.minValue = 0
        npHour.maxValue = 9

        npMinute.minValue = 0
        npMinute.maxValue = 59

        // Tạo AlertDialog
        AlertDialog.Builder(this)
            .setTitle("Chọn thời gian")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                val selectedHour = npHour.value
                val selectedMinute = npMinute.value
                onTimeSelected(selectedHour, selectedMinute)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

}
