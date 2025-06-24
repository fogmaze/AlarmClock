package com.fogmaze.alarm.services

import android.content.Context
import android.content.Intent
import com.fogmaze.alarm.domain.Store
import com.fogmaze.alarm.logger.Logger
import com.fogmaze.alarm.platform.WakeLockManager
import com.fogmaze.alarm.platform.oreo
import com.fogmaze.alarm.platform.preOreo
import com.fogmaze.alarm.receivers.Intents
import com.fogmaze.alarm.util.mapNotNull
import com.fogmaze.alarm.util.subscribeForever

class AlertServicePusher(store: Store, context: Context, wm: WakeLockManager, logger: Logger) {
  init {
    store.events
        .mapNotNull {
          when (it) {
            is Event.AlarmEvent ->
                Intent(Intents.ALARM_ALERT_ACTION).apply { putExtra(Intents.EXTRA_ID, it.id) }
            is Event.SnoozeAlarmEvent ->
                Intent(Intents.SNOOZE_ALARM_ALERT_ACTION).apply { putExtra(Intents.EXTRA_ID, it.id) }
            is Event.CheckAlarmEvent ->
                Intent(Intents.CHECK_ALARM_ALERT_ACTION).apply { putExtra(Intents.EXTRA_ID, it.id) }
            is Event.PrealarmEvent ->
                Intent(Intents.ALARM_PREALARM_ACTION).apply { putExtra(Intents.EXTRA_ID, it.id) }
            is Event.DismissEvent ->
                Intent(Intents.ALARM_DISMISS_ACTION).apply { putExtra(Intents.EXTRA_ID, it.id) }
            is Event.MuteEvent -> Intent(Intents.ACTION_MUTE)
            is Event.DemuteEvent -> Intent(Intents.ACTION_DEMUTE)
            is Event.PauseEvent -> Intent(Intents.ALARM_ALERT_PAUSE_ACTION)
            is Event.ResumeEvent -> Intent(Intents.ALARM_ALERT_RESUME_ACTION)
            is Event.StartWakingEvent -> Intent(Intents.ALARM_ALERT_START_WAKING_ACTION)
            is Event.MustWakeEvent -> Intent(Intents.ACTION_MUST_WAKE)
            is Event.SnoozedEvent -> null
            is Event.CheckEvent -> null
            is Event.Autosilenced -> null
            is Event.CancelSnoozedEvent -> null
            is Event.CancelCheckEvent -> null
            is Event.ShowSkip -> null
            is Event.HideSkip -> null
            is Event.NullEvent -> throw RuntimeException("NullEvent")
          }?.apply { setClass(context, AlertServiceWrapper::class.java) }
        }
        .subscribeForever { intent ->
          wm.acquireTransitionWakeLock(intent)
          oreo { context.startForegroundService(intent) }
          preOreo { context.startService(intent) }
          logger.debug { "pushed intent ${intent.action} to AlertServiceWrapper" }
        }
  }
}
