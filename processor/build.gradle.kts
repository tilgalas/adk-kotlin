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

plugins {
  kotlin("jvm")
  alias(libs.plugins.vanniktech.maven.publish)
  alias(libs.plugins.ksp)
}

sourceSets {
  main {
    kotlin.setSrcDirs(listOf("src/jvmMain/kotlin"))
    resources.setSrcDirs(listOf("src/jvmMain/resources"))
  }
  test {
    kotlin.setSrcDirs(listOf("src/jvmTest/kotlin"))
    resources.setSrcDirs(listOf("src/jvmTest/resources"))
  }
}

mavenPublishing {
  // `KotlinJvm` tells the vanniktech plugin to publish the Kotlin/JVM
  // component with a sources jar and a `-javadoc.jar` containing Dokka's HTML
  // output. The plugin defaults the javadoc jar to an empty one for Kotlin
  // targets because Gradle's `javadoc` task does not understand `.kt` sources;
  // wiring it to Dokka instead lets Central ship real API docs. Shared POM
  // metadata and signing are configured in the root `build.gradle.kts`.
  configure(
    com.vanniktech.maven.publish.KotlinJvm(
      javadocJar = com.vanniktech.maven.publish.JavadocJar.Dokka("dokkaHtml"),
      sourcesJar = true,
    )
  )
  coordinates(artifactId = "google-adk-kotlin-processor")
}

dependencies {
  implementation(project(":google-adk-kotlin-core"))
  implementation(libs.kotlinx.coroutines.core)
  implementation("com.squareup:kotlinpoet:1.16.0")
  implementation("com.squareup:kotlinpoet-ksp:1.16.0")
  implementation("com.google.devtools.ksp:symbol-processing-api:1.9.23-1.0.19")

  kspTest(project(":google-adk-kotlin-processor"))

  testImplementation(libs.google.truth)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
}
