package com.example.checkworking

import android.util.Log
import java.util.Calendar
import java.util.concurrent.TimeUnit

object TimeUtils {
    const val TIME_IN = "time_in"
    const val TIME_IN_ORIGIN = "time_in_origin"
    const val TIME_OFF = "time_off"
    const val TIME_OUT_ON_WORK = "time_out_on_work"
    const val TOTAL_TIME_OUT = "total_time_out"
    const val TOTAL_TIME_OUT_ORIGIN = "total_time_out_origin"
    const val IS_OUT_WORK = "is_out_work"
    const val OVER_TIME = "over_time"
    const val TIME_WORKING = ((9*60*60 + 48*60) * 1000).toLong()
    const val OFF_HALF = ((4*60*60 + 24*60) * 1000).toLong()
    const val TIME_BEFORE = 3*60*1000L
    const val MAX_PROGRESS = 100000
    val ListIdNotify = arrayOf(1,2,3,4)

    fun convertMillisToTime(time: Long): String {
        val calander = Calendar.getInstance()
        calander.timeInMillis = time

        if (time <= 0L) return "00:00:00"

        val hours = calander.get(Calendar.HOUR_OF_DAY)
        val minutes = calander.get(Calendar.MINUTE)
        val seconds = calander.get(Calendar.SECOND)

        val strHours = String.format("%02d", hours)
        val strMinutes = String.format("%02d", minutes)
        val strSeconds = String.format("%02d", seconds)

        return "$strHours:$strMinutes:$strSeconds"
    }

    fun convertMillisToTimeNoSecond(time: Long): String {
        val calander = Calendar.getInstance()
        calander.timeInMillis = time

        if (time <= 0L) return "00:00"

        val hours = calander.get(Calendar.HOUR_OF_DAY)
        val minutes = calander.get(Calendar.MINUTE)

        val strHours = String.format("%02d", hours)
        val strMinutes = String.format("%02d", minutes)

        return "$strHours:$strMinutes"
    }

    fun convertTimeCount(time: Long): String {
        if (time <= 0L) return "00:00:00"
        val hours = TimeUnit.MILLISECONDS.toHours(time) // Giờ
        val minutes = TimeUnit.MILLISECONDS.toMinutes(time) % 60 // Phút
        val seconds = TimeUnit.MILLISECONDS.toSeconds(time) % 60 // Giây

        val strHours = String.format("%02d", hours)
        val strMinutes = String.format("%02d", minutes)
        val strSeconds = String.format("%02d", seconds)

        return "$strHours:$strMinutes:$strSeconds"
    }

    fun getTimeOff(time: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val date = calendar.get(Calendar.DATE)
        var hour = calendar.get(Calendar.HOUR_OF_DAY)
        var minute = calendar.get(Calendar.MINUTE)
        var second = calendar.get(Calendar.SECOND)
        if (hour > 18 || hour == 18 && minute > 48) {
            hour = 18
            minute = 48
            second = 0
            calendar.set(year, month, date,hour,minute,second)
            return calendar.timeInMillis
        }
        return time
    }

    fun getDate(time: Long): String {
        val calander = Calendar.getInstance()
        calander.timeInMillis = time

        if (time <= 0L) return ""

        val day = calander.get(Calendar.DAY_OF_MONTH)
        val month = calander.get(Calendar.MONTH)
        val year = calander.get(Calendar.YEAR)

        val strDay = String.format("%02d", day)
        val strMonth = String.format("%02d", month+1)
        val strYear = String.format("%04d", year)

        return "$strDay/$strMonth/$strYear"
    }

    fun roundTimeUp(time: Long): Long {
        return (time/60000 + 1) * 60000
    }

    fun roundTime(time: Long): Long {
        return (time/60000) * 60000
    }

    fun getTimeOffFirst(time: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val date = calendar.get(Calendar.DATE)
        var hour = 17
        var minute = 24
        calendar.set(year, month, date,hour,minute)
        return calendar.timeInMillis
    }
}