package com.fogmaze.alarm.util

import com.fogmaze.alarm.bootstrap.globalLogger
import com.fogmaze.alarm.logger.Logger
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.Future

data class VersionInfo(val name: String, val downloadURL: String)

class AutoUpdate {
  private val logger by globalLogger("ActionBarHandler")
  companion object {
    const val UPDATE_URL = "https://api.github.com/repos/fogmaze/AlarmClock/releases/latest"
    const val APK_NAME = "lazyclock-release.apk"
    @Volatile private var instance: AutoUpdate? = null

    fun getInstance(): AutoUpdate =
      instance ?: synchronized(this) {
        instance ?: AutoUpdate().also { instance = it }
      }
  }
  fun getOriginVersion(onResultCallback: (VersionInfo) -> Unit) {
    val client = OkHttpClient()
    val request = Request.Builder()
      .url(UPDATE_URL)
      .build()

    client.newCall(request).enqueue(object : okhttp3.Callback {
      override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
        onResultCallback(VersionInfo("", ""))
      }
      override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
        response.use {
          try {
            if (!it.isSuccessful) {
              logger.error {"Unexpected code $it" }
              return
            }

            val json = JSONObject(it.body!!.string())
            val versionName = json.getString("tag_name") // or json.getString("name")
            logger.debug{"Latest version: $versionName"}
            val downloadURL = json.getJSONArray("assets").getJSONObject(0).getString("browser_download_url")
            onResultCallback(VersionInfo(versionName, downloadURL))
          } catch (e: Exception) {
            logger.error { e.toString() }
            onResultCallback(VersionInfo("", ""))
          }
        }
      }
    })
  }
}
fun String.versionIsNewerThan(other: String): Boolean {
  val thisVersion = this.split(".").map { it.replace("v", "").toInt() }
  val otherVersion = other.split(".").map { it.replace("v", "").toInt() }

  thisVersion.zip(otherVersion).forEach {
    if (it.first > it.second) {
      return true
    } else if (it.first < it.second) {
      return false
    }
  }
  return false
}
