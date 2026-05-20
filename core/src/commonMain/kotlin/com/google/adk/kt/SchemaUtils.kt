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

package com.google.adk.kt

import com.google.adk.kt.types.Schema
import com.google.adk.kt.types.Type

/** Utility class for validating schemas. */
object SchemaUtils {

  /**
   * Matches a value against a schema type.
   *
   * @param value The value to match.
   * @param schema The schema to match against.
   * @param argsName The name of the arguments being validated (e.g., "Input" or "Output").
   * @return [Result.success] if the value matches the schema type, [Result.failure] wrapping an
   *   [IllegalArgumentException] otherwise.
   */
  private fun matchType(value: Any?, schema: Schema, argsName: String): Result<Unit> {
    val type = schema.type ?: return Result.success(Unit) // If type is not specified, assume match.

    val matches =
      when (type) {
        Type.STRING -> value is String
        Type.INTEGER -> value is Int || value is Long
        Type.BOOLEAN -> value is Boolean
        Type.NUMBER -> value is Number
        Type.ARRAY -> {
          if (value !is List<*>) {
            return Result.failure(IllegalArgumentException("$argsName value is not a list: $value"))
          }
          val itemSchema = schema.items ?: return Result.success(Unit)
          for (item in value) {
            matchType(item, itemSchema, argsName).onFailure {
              return Result.failure(it)
            }
          }
          true
        }
        Type.OBJECT -> {
          if (value !is Map<*, *>) {
            return Result.failure(IllegalArgumentException("$argsName value is not a map: $value"))
          }
          @Suppress("UNCHECKED_CAST")
          return validateMapOnSchema(value as Map<String, Any?>, schema, argsName)
        }
        Type.NULL -> value == null
        Type.TYPE_UNSPECIFIED ->
          return Result.failure(
            IllegalArgumentException("Unsupported type: $type is not a Open API data type.")
          )
      }

    return if (matches) {
      Result.success(Unit)
    } else {
      Result.failure(IllegalArgumentException("$argsName value $value does not match type $type"))
    }
  }

  /**
   * Validates a map against a schema.
   *
   * @param args The map to validate.
   * @param schema The schema to validate against.
   * @param argsName The name of the arguments being validated (e.g., "Input" or "Output").
   * @return [Result.success] if the map matches the schema, [Result.failure] wrapping an
   *   [IllegalArgumentException] describing the first validation error otherwise.
   */
  fun validateMapOnSchema(args: Map<String, Any?>, schema: Schema, argsName: String): Result<Unit> {
    val properties = schema.properties ?: emptyMap()
    for ((key, value) in args) {
      // Check if the argument is in the schema.
      if (!properties.containsKey(key)) {
        return Result.failure(
          IllegalArgumentException(
            "$argsName arg: $key doesn't exist in ${argsName.lowercase()} schema: $schema"
          )
        )
      }
      // Check if the argument type matches the schema type.
      matchType(value, properties[key]!!, argsName).onFailure {
        return Result.failure(
          IllegalArgumentException(
            "$argsName arg: $key type does not match ${argsName.lowercase()} schema: $schema",
            it,
          )
        )
      }
    }
    // Check if all required arguments are present.
    schema.required?.forEach { required ->
      if (!args.containsKey(required)) {
        return Result.failure(
          IllegalArgumentException("$argsName args does not contain required $required")
        )
      }
    }
    return Result.success(Unit)
  }
}
