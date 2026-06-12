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
package com.google.adk.kt.models

/** Config for authenticating to Vertex AI when constructing a [Gemini] model. */
data class VertexCredentials(
  /** Google Cloud project. */
  val project: String? = null,
  /** Google Cloud project location. */
  val location: String? = null,
  /** Credentials; defaults to `GoogleCredentials.getApplicationDefault()` when null. */
  val credentials: GoogleCredentials? = null,
)
