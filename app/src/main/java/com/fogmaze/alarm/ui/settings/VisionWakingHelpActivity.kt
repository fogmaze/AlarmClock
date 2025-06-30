/***
 * MIT License
 * Copyright (c) 2025 Andy Chen
 * See LICENSE file for full license text.
 */
package com.fogmaze.alarm.ui.settings

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.fogmaze.alarm.R
import com.fogmaze.alarm.bootstrap.AlarmApplication
import com.fogmaze.alarm.ui.themes.DynamicThemeHandler
import org.koin.android.ext.android.inject

class VisionWakingHelpActivity : AppCompatActivity() {
  private val dynamicThemeHandler: DynamicThemeHandler by inject()
    override fun onCreate(savedInstanceState: Bundle?) {
      AlarmApplication.startOnce(application)
      setTheme(dynamicThemeHandler.defaultTheme())
      super.onCreate(savedInstanceState)
      setContentView(R.layout.activity_vision_waking_help)

      val actionBar = supportActionBar
      actionBar?.setDisplayHomeAsUpEnabled(true)
    }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      finish()
      return true
    }

    return super.onOptionsItemSelected(item)
  }
}
