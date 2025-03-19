package com.example.checkworking

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.checkworking.NotifyUtil.createNotification
import com.example.checkworking.TimeUtils.convertMillisToTime

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        // Khi đến thời gian hẹn, hiển thị thông báo
        val content = intent?.getStringExtra("key")
        createNotification(context, content.toString())
    }


}
