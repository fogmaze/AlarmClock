/***
 * MIT License
 * Copyright (c) 2025 Andy Chen
 * See LICENSE file for full license text.
 */
package com.fogmaze.alarm.vision

import kotlinx.serialization.Serializable

object Behavior {
  @Serializable
  data class BehaviorStoreItem(val gesture: Gesture, val operation: String)
  @Serializable
  data class BehaviorsStoreValue(val items: List<Behavior.BehaviorStoreItem>)
}

