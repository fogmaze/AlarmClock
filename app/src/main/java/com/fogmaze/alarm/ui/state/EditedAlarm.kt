package com.fogmaze.alarm.ui.state

import com.fogmaze.alarm.data.AlarmValue

/** Created by Yuriy on 09.08.2017. */
data class EditedAlarm(
    val isNew: Boolean = false,
    val value: AlarmValue,
)
