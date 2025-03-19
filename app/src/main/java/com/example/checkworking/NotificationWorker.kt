package com.example.checkworking

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class NotificationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    @SuppressLint("MissingPermission")
    override fun doWork(): Result {

        // Lấy dữ liệu từ InputData
        val workRequestId = inputData.getString("content") ?: "Error"

        // Gửi thông báo khi Worker thực hiện tác vụ
        val notification = NotificationCompat.Builder(applicationContext, "my_channel_id")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("SMM Work Time")
            .setContentText(workRequestId)
            .setOngoing(true) // Giữ thông báo luôn tồn tại
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Đảm bảo thông báo luôn hiển thị
            .build()

        val notificationManager = NotificationManagerCompat.from(applicationContext)
        notificationManager.notify(1, notification)

        return Result.success() // Hoàn thành tác vụ
    }
}