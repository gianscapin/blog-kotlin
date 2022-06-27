package com.example.blogapp.core

import java.util.concurrent.TimeUnit

private const val SECOND_MILLIS = 1
private const val MINUTE_MILLIS = 60
private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
private const val DAY_MILLIS = 24 * HOUR_MILLIS

object TimeAgo {

    fun getTime(time: Int): String {

        val now = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())
        if(time>now || time<=0){
            return "in the future"
        }

        val diff = now - time
        return when{
            diff < MINUTE_MILLIS -> "Just now"
            diff < HOUR_MILLIS -> "${diff/ MINUTE_MILLIS} minutes ago"
            diff < 2 * HOUR_MILLIS -> "an hour ago"
            diff < DAY_MILLIS -> "${diff/ HOUR_MILLIS} hours ago"
            diff < 2 * DAY_MILLIS -> "a day ago"
            else -> {"${diff / DAY_MILLIS} days ago"}
        }
    }
}