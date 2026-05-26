/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.adk.kt.examples.structural

import com.google.adk.kt.agents.BaseAgent
import com.google.adk.kt.agents.InvocationContext
import com.google.adk.kt.agents.LoopAgent
import com.google.adk.kt.events.Event
import com.google.adk.kt.events.EventActions
import com.google.adk.kt.ids.Uuid
import com.google.adk.kt.types.Content
import com.google.adk.kt.types.Part
import kotlin.random.Random
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** A fun, nerdy demo for LoopAgent simulating an RPG loot grinder. */
object LoopAgentDemo {

  private class MonsterFightAgent(name: String) : BaseAgent(name = name) {
    val loots = listOf("Gold x10", "Health Potion", "Rusty Dagger", "Legendary Sword of Gemini")

    override fun runAsyncImpl(context: InvocationContext): Flow<Event> = flow {
      val roll = Random.nextInt(loots.size)
      val loot = loots[roll]
      val isLegendary = loot == "Legendary Sword of Gemini"

      emit(
        Event(
          id = Uuid.random(),
          invocationId = context.invocationId,
          author = name,
          content = Content(parts = listOf(Part(text = "Defeated a Goblin! Found loot: $loot"))),
          actions = EventActions(escalate = isLegendary),
        )
      )
    }
  }

  @JvmField
  val rootAgent =
    LoopAgent(
      name = "LootGrinder",
      subAgents = listOf(MonsterFightAgent("GoblinSlayer")),
      maxIterations = 10, // Prevent infinite loops if unlucky
    )
}
