package com.fogmaze.alarm.platform

interface Wakelocks {
  fun acquireServiceLock()

  fun releaseServiceLock()
}
