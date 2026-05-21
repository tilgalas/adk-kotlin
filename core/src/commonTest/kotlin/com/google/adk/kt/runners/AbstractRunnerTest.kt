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

@file:OptIn(ExperimentalResumabilityFeature::class)

package com.google.adk.kt.runners

import com.google.adk.kt.agents.BaseAgent
import com.google.adk.kt.agents.InvocationContext
import com.google.adk.kt.agents.ResumabilityConfig
import com.google.adk.kt.annotations.ExperimentalResumabilityFeature
import com.google.adk.kt.events.Event
import com.google.adk.kt.events.EventActions
import com.google.adk.kt.sessions.SessionKey
import com.google.adk.kt.testing.DummyAgent
import com.google.adk.kt.testing.modelMessage
import com.google.adk.kt.testing.userFunctionResponse
import com.google.adk.kt.testing.userMessage
import com.google.adk.kt.types.Content
import com.google.adk.kt.types.FunctionCall
import com.google.adk.kt.types.Part
import com.google.adk.kt.types.Role
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest

class AbstractRunnerTest {

  class TestRunner(agent: BaseAgent) :
    InMemoryRunner(agent = agent, resumabilityConfig = ResumabilityConfig(isResumable = true)) {
    suspend fun callFindAgentToRun(context: InvocationContext, rootAgent: BaseAgent): BaseAgent {
      return findAgentToRun(context, rootAgent)
    }
  }

  @Test
  fun findAgentToRun_withFunctionResponse_returnsTargetAgent() = runTest {
    val subAgent = DummyAgent("sub")
    val rootAgent = DummyAgent("root", subAgents = listOf(subAgent))
    val runner = TestRunner(rootAgent)

    val callId = "call-123"
    val callEvent =
      Event(
        author = "sub",
        content =
          Content(
            Role.MODEL,
            listOf(Part(functionCall = FunctionCall("tool", emptyMap(), callId))),
          ),
        invocationId = "inv-1",
      )
    val responseEvent =
      Event(
        author = Role.USER,
        content = userFunctionResponse(name = "tool", id = callId),
        invocationId = "inv-1",
      )

    val session =
      runner.sessionService.createSession(SessionKey("InMemoryRunner", "user", "session"))
    val unused1 = runner.sessionService.appendEvent(session, callEvent)
    val unused2 = runner.sessionService.appendEvent(session, responseEvent)

    val updatedSession =
      runner.sessionService.getSession(SessionKey("InMemoryRunner", "user", "session"))!!
    val context =
      InvocationContext(
        session = updatedSession,
        runConfig = null,
        agent = rootAgent,
        invocationId = "inv-1",
        sessionService = runner.sessionService,
      )
    val result = runner.callFindAgentToRun(context, rootAgent)

    assertEquals("sub", result.name)
  }

  @Test
  fun findAgentToRun_noFunctionResponse_returnsMostRecentTransferableAgent() = runTest {
    val subAgent = DummyAgent("sub")
    val rootAgent = DummyAgent("root", subAgents = listOf(subAgent))
    val runner = TestRunner(rootAgent)

    val event1 = Event(author = "sub", content = modelMessage("Hello"), invocationId = "inv-1")
    val event2 = Event(author = Role.USER, content = userMessage("Hi"), invocationId = "inv-1")

    val session =
      runner.sessionService.createSession(SessionKey("InMemoryRunner", "user", "session"))
    val unused1 = runner.sessionService.appendEvent(session, event1)
    val unused2 = runner.sessionService.appendEvent(session, event2)

    val updatedSession =
      runner.sessionService.getSession(SessionKey("InMemoryRunner", "user", "session"))!!
    val context =
      InvocationContext(
        session = updatedSession,
        runConfig = null,
        agent = rootAgent,
        invocationId = "inv-1",
        sessionService = runner.sessionService,
      )
    val result = runner.callFindAgentToRun(context, rootAgent)

    assertEquals("sub", result.name)
  }

  @Test
  fun findAgentToRun_untransferableAgent_returnsRoot() = runTest {
    val subAgent = DummyAgent("sub", disallowTransferToParent = true)
    val rootAgent = DummyAgent("root", subAgents = listOf(subAgent))
    val runner = TestRunner(rootAgent)

    val event1 = Event(author = "sub", content = modelMessage("Hello"), invocationId = "inv-1")
    val event2 = Event(author = Role.USER, content = userMessage("Hi"), invocationId = "inv-1")

    val session =
      runner.sessionService.createSession(SessionKey("InMemoryRunner", "user", "session"))
    val unused1 = runner.sessionService.appendEvent(session, event1)
    val unused2 = runner.sessionService.appendEvent(session, event2)

    val updatedSession =
      runner.sessionService.getSession(SessionKey("InMemoryRunner", "user", "session"))!!
    val context =
      InvocationContext(
        session = updatedSession,
        runConfig = null,
        agent = rootAgent,
        invocationId = "inv-1",
        sessionService = runner.sessionService,
      )
    val result = runner.callFindAgentToRun(context, rootAgent)

    assertEquals("root", result.name)
  }

  @Test
  fun findAgentToRun_withStateEvents_skipsStateEvents() = runTest {
    val subAgent = DummyAgent("sub")
    val rootAgent = DummyAgent("root", subAgents = listOf(subAgent))
    val runner = TestRunner(rootAgent)

    val event1 = Event(author = "sub", content = modelMessage("Hello"), invocationId = "inv-1")
    val event2 =
      Event(author = "sub", actions = EventActions(endOfAgent = true), invocationId = "inv-1")
    val event3 = Event(author = Role.USER, content = userMessage("Hi"), invocationId = "inv-1")

    val session =
      runner.sessionService.createSession(SessionKey("InMemoryRunner", "user", "session"))
    val unused1 = runner.sessionService.appendEvent(session, event1)
    val unused2 = runner.sessionService.appendEvent(session, event2)
    val unused3 = runner.sessionService.appendEvent(session, event3)

    val updatedSession =
      runner.sessionService.getSession(SessionKey("InMemoryRunner", "user", "session"))!!
    val context =
      InvocationContext(
        session = updatedSession,
        runConfig = null,
        agent = rootAgent,
        invocationId = "inv-1",
        sessionService = runner.sessionService,
      )
    val result = runner.callFindAgentToRun(context, rootAgent)

    assertEquals("sub", result.name)
  }

  @Test
  fun findAgentToRun_withDisallowTransferToPeers_throwsException() = runTest {
    val sub1 = DummyAgent("sub1", disallowTransferToPeers = true)
    val sub2 = DummyAgent("sub2")
    val rootAgent = DummyAgent("root", subAgents = listOf(sub1, sub2))
    val runner = TestRunner(rootAgent)

    val event1 =
      Event(
        author = "sub1",
        actions = EventActions(transferToAgent = "sub2"),
        invocationId = "inv-1",
      )
    val event2 = Event(author = Role.USER, content = userMessage("Hi"), invocationId = "inv-1")

    val session =
      runner.sessionService.createSession(SessionKey("InMemoryRunner", "user", "session"))
    val unused1 = runner.sessionService.appendEvent(session, event1)
    val unused2 = runner.sessionService.appendEvent(session, event2)

    val updatedSession =
      runner.sessionService.getSession(SessionKey("InMemoryRunner", "user", "session"))!!
    val context =
      InvocationContext(
        session = updatedSession,
        runConfig = null,
        agent = rootAgent,
        invocationId = "inv-1",
        sessionService = runner.sessionService,
      )

    assertFailsWith<IllegalArgumentException> { runner.callFindAgentToRun(context, rootAgent) }
      .also { assertEquals("Agent 'sub1' is not allowed to transfer to peer 'sub2'.", it.message) }
  }
}
