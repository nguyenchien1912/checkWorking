package com.example.checkworking

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkRequest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object NotifyUtil {
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "my_channel_id"
            val channelName = "My Notification Channel"
            val description = "Mô tả kênh thông báo"
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(channelId, channelName, importance).apply {
                this.description = description
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleNotificationWithWorkManager(context: Context, delayInMillis: Long, content: String) {
        val inputData = Data.Builder()
            .putString("content", content) // Dữ liệu nhận dạng WorkRequest
            .build()
        val workRequest: WorkRequest = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
            .setInputData(inputData)
            .setInitialDelay(delayInMillis, TimeUnit.SECONDS)
            .build()

        // Đặt tác vụ vào WorkManager
        WorkManager.getInstance(context).enqueue(workRequest)
    }


    @SuppressLint("ScheduleExactAlarm")
    fun setExactAlarm(context: Context,id: Int, time: Long, message: String) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("key", message) // Truyền dữ liệu vào Intent
            action = id.toString()
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )



        // Đặt báo thức chính xác
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            time,
            pendingIntent
        )
    }

    fun cancelAlarm(context: Context,id: Int) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)

        val intent = Intent(context, AlarmReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Đặt báo thức chính xác
        alarmManager.cancel(pendingIntent)
    }


    @SuppressLint("MissingPermission")
    fun createNotification(context: Context, content: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "my_channel_id")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("SMM Work Time")
            .setContentText(content)
            .setOngoing(true) // Giữ thông báo luôn tồn tại
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Đảm bảo thông báo luôn hiển thị
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(1, notification)
    }

}