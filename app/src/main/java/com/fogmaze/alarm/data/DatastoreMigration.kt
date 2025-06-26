package com.fogmaze.alarm.data

interface DatastoreMigration {
  fun drop()

  fun insertDefaultAlarms()

  fun migrateDatabase()
}
