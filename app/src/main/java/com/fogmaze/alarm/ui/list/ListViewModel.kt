package com.fogmaze.alarm.ui.list

import androidx.lifecycle.ViewModel
import com.fogmaze.alarm.data.AlarmValue
import com.fogmaze.alarm.ui.state.UiStore

class ListViewModel(
    private val uiStore: UiStore,
) : ViewModel() {
  @Deprecated("Use state flow instead") var openDrawerOnCreate: Boolean = false

  fun edit(alarmValue: AlarmValue) {
    uiStore.edit(alarmValue)
  }

  fun createNewAlarm() {
    uiStore.createNewAlarm()
  }
}
