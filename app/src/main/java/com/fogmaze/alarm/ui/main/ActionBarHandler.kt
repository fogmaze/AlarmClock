package com.fogmaze.alarm.ui.main

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.core.view.MenuItemCompat
import com.azhon.appupdate.manager.DownloadManager
import com.azhon.appupdate.util.ApkUtil
import com.fogmaze.alarm.BuildConfig
import com.fogmaze.alarm.R
import com.fogmaze.alarm.ui.settings.SettingsActivity
import com.fogmaze.alarm.ui.state.BackPresses
import com.fogmaze.alarm.util.AutoUpdate
import com.fogmaze.alarm.util.versionIsNewerThan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * This class handles options menu and action bar
 *
 * @author Kate
 */
class ActionBarHandler(
    private val activity: Context,
    private val mainViewModel: MainViewModel,
    private val backPresses: BackPresses,
) {
  private var scope = CoroutineScope(Dispatchers.Unconfined)

  /**
   * Delegate [Activity.onCreateOptionsMenu]
   *
   * @param menu
   * @param inflater
   * @param actionBar
   * @return
   */
  fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater, actionBar: ActionBar): Boolean {
    inflater.inflate(R.menu.menu_action_bar, menu)

    val intent =
        Intent(Intent.ACTION_SEND).apply {
          type = "text/plain"

          addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)

          // Add data to the intent, the receiving app will decide what to do with
          // it.
          putExtra(
              Intent.EXTRA_SUBJECT,
              "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)
          putExtra(
              Intent.EXTRA_TEXT,
              "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)
        }

    val menuItem = menu.findItem(R.id.menu_share)
    val sp =
        MenuItemCompat.getActionProvider(menuItem) as androidx.appcompat.widget.ShareActionProvider
    sp.setShareIntent(intent)

    mainViewModel
        .editing()
        .onEach { edited ->
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            check(Looper.getMainLooper().isCurrentThread)
          }
          val showDelete = edited != null && !edited.isNew

          menu.findItem(R.id.set_alarm_menu_delete_alarm).isVisible = showDelete

          actionBar.setDisplayHomeAsUpEnabled(edited != null)
        }
        .launchIn(scope)

    return true
  }

  fun onDestroy() {
    scope.cancel()
  }

  /**
   * Delegate [Activity.onOptionsItemSelected]
   *
   * @param item
   * @return
   */
  fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.menu_item_settings ->
          activity.startActivity(Intent(activity, SettingsActivity::class.java))
      R.id.menu_review -> showSayThanks()
      R.id.menu_bugreport -> showBugreport()
      R.id.set_alarm_menu_delete_alarm -> deleteAlarm()
      R.id.menu_about -> showAbout()
      R.id.menu_check_update -> checkForUpdate()
      android.R.id.home -> backPresses.backPressed("ActionBar")
    }
    return true
  }

  private fun showAbout() {
    AlertDialog.Builder(activity)
        .apply {
          setTitle(activity.getString(R.string.menu_about_title))
          setView(
              View.inflate(activity, R.layout.dialog_about, null).apply {
                findViewById<TextView>(R.id.dialog_about_text).run {
                  setText(R.string.dialog_about_content)
                  movementMethod = LinkMovementMethod.getInstance()
                }
              })
          setPositiveButton(android.R.string.ok) { _, _ -> }
        }
        .create()
        .show()
  }

  private fun deleteAlarm() {
    AlertDialog.Builder(activity)
        .apply {
          setTitle(activity.getString(R.string.delete_alarm))
          setMessage(activity.getString(R.string.delete_alarm_confirm))
          setPositiveButton(android.R.string.ok) { _, _ -> mainViewModel.deleteEdited() }
          setNegativeButton(android.R.string.cancel, null)
        }
        .show()
  }

  private fun showSayThanks() {
    val inflator = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    val dialogView =
        inflator.inflate(R.layout.dialog_say_thanks, null).apply {
          findViewById<Button>(R.id.dialog_say_thanks_button_review).setOnClickListener {
            val appId = BuildConfig.APPLICATION_ID
            val uri = Uri.parse("market://details?id=$appId")
            val fallback = Uri.parse("https://play.google.com/store/apps/details?id=$appId")
            try {
              activity.startActivity(Intent(Intent.ACTION_VIEW, uri))
            } catch (anfe: ActivityNotFoundException) {
              activity.startActivity(Intent(Intent.ACTION_VIEW, fallback))
            }
          }

          findViewById<TextView>(R.id.dialog_say_thanks_text_as_button_donate_premium)
              .movementMethod = LinkMovementMethod.getInstance()
        }

    AlertDialog.Builder(activity)
        .apply {
          setPositiveButton(android.R.string.ok) { _, _ -> }
          setTitle(R.string.dialog_say_thanks_title)
          setView(dialogView)
          setCancelable(true)
        }
        .create()
        .show()
  }

  private fun showBugreport() {
    val inflator = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    val dialogView = inflator.inflate(R.layout.dialog_bugreport, null)

    dialogView.findViewById<TextView>(R.id.dialog_bugreport_textview).movementMethod =
        LinkMovementMethod.getInstance()

    AlertDialog.Builder(activity)
        .apply {
          setPositiveButton(android.R.string.ok) { _, _ -> mainViewModel.sendUserReport() }
          setTitle(R.string.menu_bugreport)
          setCancelable(true)
          setNegativeButton(android.R.string.cancel) { _, _ -> }
          setView(dialogView)
        }
        .create()
        .show()
  }

  private fun checkForUpdate() {
    AutoUpdate.getInstance().getOriginVersion { versionInfo ->
      if (versionInfo.name.isEmpty()) { // failed
        Handler(Looper.getMainLooper()).post {
          AlertDialog.Builder(activity)
            .apply {
              setPositiveButton(android.R.string.ok) { _, _ -> }
              setTitle(activity.getString(R.string.fail))
              setMessage(activity.getString(R.string.check_update_failed))
            }
            .create()
            .show()
        }
        return@getOriginVersion
      }
      if (versionInfo.name.versionIsNewerThan(BuildConfig.VERSION_NAME)) { // new version
        Handler(Looper.getMainLooper()).post {
          AlertDialog.Builder(activity)
            .apply {
              setPositiveButton(android.R.string.ok) { _, _ ->
                markNotInstalled(context)
                DownloadManager.Builder(activity as Activity).run {
                  apkUrl(versionInfo.downloadURL)
                  apkName(AutoUpdate.APK_NAME)
                  smallIcon(R.mipmap.ic_launcher)
                  build()
                }.download()
              }
              setTitle(activity.getString(R.string.new_version_found_title))
              setMessage(activity.getString(R.string.new_version_found_instruction))
              setCancelable(true)
              setNegativeButton(android.R.string.cancel) { _, _ -> }
            }
            .create()
            .show()
        }
      } else { // no new version
        Handler(Looper.getMainLooper()).post {
          AlertDialog.Builder(activity)
            .apply {
              setPositiveButton(android.R.string.ok) { _, _ -> }
              setTitle(activity.getString(R.string.no_new_version_found_title))
              setMessage(activity.getString(R.string.no_new_version_found_instruction))
            }
            .create()
            .show()
        }
      }
    }
  }
  private fun markNotInstalled(context: Context) {
    val sharedPref = context.getSharedPreferences("update", Context.MODE_PRIVATE)
    with(sharedPref.edit()) {
      putString("originalVersion", BuildConfig.VERSION_NAME)
      putBoolean("installChecked", false)
      apply()
    }
  }
}
