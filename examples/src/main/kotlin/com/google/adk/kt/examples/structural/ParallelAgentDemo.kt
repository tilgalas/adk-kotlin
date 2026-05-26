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
import com.google.adk.kt.agents.ParallelAgent
import com.google.adk.kt.events.Event
import com.google.adk.kt.ids.Uuid
import com.google.adk.kt.types.Content
import com.google.adk.kt.types.Part
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * A fun, quirky, family-friendly demo for ParallelAgent showcasing simultaneous actions by a team
 * of cats!
 */
object ParallelAgentDemo {

  private class CatAgent(name: String, private val actionReport: String) : BaseAgent(name = name) {
    override fun runAsyncImpl(context: InvocationContext): Flow<Event> = flow {
      emit(
        Event(
          id = Uuid.random(),
          invocationId = context.invocationId,
          author = name,
          content = Content(parts = listOf(Part(text = actionReport))),
        )
      )
    }
  }

  @JvmField
  val rootAgent =
    ParallelAgent(
      name = "CatTaskForce",
      subAgents =
        listOf(
          CatAgent("FoodDispenser", "Dispensing premium salmon kibble... Crunch crunch."),
          CatAgent("LitterPatrol", "Scooping the box. Pristine condition achieved."),
          CatAgent("LaserPointerOfficer", "Activating the Red Dot. Target acquired. Fast pounce!"),
        ),
    )
}
