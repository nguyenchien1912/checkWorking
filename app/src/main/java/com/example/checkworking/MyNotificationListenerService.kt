package com.example.checkworking

import android.content.ComponentName
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.checkworking.NotifyUtil.cancelAlarm
import com.example.checkworking.NotifyUtil.setExactAlarm
import com.example.checkworking.TimeUtils.IS_OUT_WORK
import com.example.checkworking.TimeUtils.ListIdNotify
import com.example.checkworking.TimeUtils.OVER_TIME
import com.example.checkworking.TimeUtils.TIME_BEFORE
import com.example.checkworking.TimeUtils.TIME_IN
import com.example.checkworking.TimeUtils.TIME_OFF
import com.example.checkworking.TimeUtils.TIME_OUT_ON_WORK
import com.example.checkworking.TimeUtils.TIME_WORKING
import com.example.checkworking.TimeUtils.TOTAL_TIME_OUT
import com.example.checkworking.TimeUtils.convertMillisToTime
import com.example.checkworking.TimeUtils.getTimeOff
import com.example.checkworking.TimeUtils.roundTime
import com.example.checkworking.TimeUtils.roundTimeUp
import java.util.Calendar

class MyNotificationListenerService: NotificationListenerService() {
    companion object {
        const val CHANNEL_ID = "channel_id"
        const val UNLOCK = "UNLOCK"
        const val LOCK = "LOCK"
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn?.let {
            val packageName = it.packageName
            if (packageName ==  "com.samsung.svmc.smm" || packageName == "com.example.testnitification") {
                val channelId = it.notification.channelId
                if (channelId != LOCK && channelId != UNLOCK) return@let
                val oldChannelId = SharedPreferencesUtil.getString(this, CHANNEL_ID, UNLOCK)
                if (oldChannelId == channelId) return@let
                SharedPreferencesUtil.putString(this, CHANNEL_ID, channelId)
                val calendar = Calendar.getInstance()
                val time = System.currentTimeMillis()
                val newTime = roundTime(time)
                if (channelId == UNLOCK) {
                    val timeOff = SharedPreferencesUtil.getLong(this, TIME_OFF)
                    if (time > timeOff) {
                        Log.d("chien.nguyen", "da ve")
                        setExactAlarm(this, ListIdNotify[3], time + 1000, "Da ve")
                        SharedPreferencesUtil.putLong(this, OVER_TIME, newTime - timeOff)
                    } else {
                        Log.d("chien.nguyen", "ra ngoai")
                        cancelAlarm(this, ListIdNotify[1])
                        cancelAlarm(this, ListIdNotify[2])
                    }

                    SharedPreferencesUtil.putLong(this, TIME_OUT_ON_WORK, newTime)
                    SharedPreferencesUtil.putBoolean(this, IS_OUT_WORK, true)

                } else {
                    val isOut = SharedPreferencesUtil.getBoolean(this, IS_OUT_WORK, false)
                    val timeIn = SharedPreferencesUtil.getLong(this, TIME_IN)
                    calendar.timeInMillis = timeIn
                    val dayIn = calendar.get(Calendar.DAY_OF_MONTH)
                    calendar.timeInMillis = time
                    val dayInNow = calendar.get(Calendar.DAY_OF_MONTH)

                    if (isOut && dayIn == dayInNow) {
                        Log.d("chien.nguyen", "da vao")
                        val totalOut = SharedPreferencesUtil.getLong(this, TOTAL_TIME_OUT)
                        val timeOutOnWork = SharedPreferencesUtil.getLong(this, TIME_OUT_ON_WORK)
                        val timeOut = SharedPreferencesUtil.getLong(this, TIME_OFF)
                        val resultTimeOut = timeOut + roundTimeUp(time) - timeOutOnWork
                        SharedPreferencesUtil.putLong(this, TOTAL_TIME_OUT, totalOut + roundTimeUp(time) - timeOutOnWork)
                        SharedPreferencesUtil.putLong(this, TIME_OFF, resultTimeOut)
                        if (newTime < resultTimeOut) {
                            setExactAlarm(this, ListIdNotify[1], resultTimeOut - TIME_BEFORE, "Còn 3 Phút")
                            setExactAlarm(this, ListIdNotify[2], resultTimeOut, "Tan làm")
                        }
                    } else {
                        Log.d("chien.nguyen", "vao lam")
                        val timeOff = getTimeOff(newTime + TIME_WORKING)
                        SharedPreferencesUtil.putLong(this, TIME_IN, newTime)
                        SharedPreferencesUtil.putLong(this, TIME_OFF, timeOff)
                        SharedPreferencesUtil.putLong(this, OVER_TIME, 0L)
                        setExactAlarm(this, ListIdNotify[0], time , "Vào làm: ${convertMillisToTime(time)}")
                        setExactAlarm(this, ListIdNotify[1], timeOff - TIME_BEFORE, "Còn 3 Phút")
                        setExactAlarm(this, ListIdNotify[2], timeOff, "Tan làm")
                    }
                    SharedPreferencesUtil.putBoolean(this, IS_OUT_WORK, false)
                }
            }
        }
    }

    override fun onListenerDisconnected() {
        requestRebind(ComponentName(this, MyNotificationListenerService::class.java))
    }
}