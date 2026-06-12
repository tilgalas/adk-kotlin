# Changelog

## [0.2.0](https://github.com/google/adk-kotlin/compare/v0.1.1...v0.2.0) (2026-05-26)


### Features

* Added structural agent demos: LoopAgentDemo, ParallelAgentDemo, and SequentialAgentDemo ([2a93053](https://github.com/google/adk-kotlin/commit/2a9305317766b094563cee13f7c3823c4738c62e))
* Add a new example agent demonstrating callbacks ([9183250](https://github.com/google/adk-kotlin/commit/918325061412ef62f32c1d958a5927328b9c9bfa))
* Add a new ReportGeneratorAgent example ([c6ea965](https://github.com/google/adk-kotlin/commit/c6ea96552ca259ff047a63cd04eac177ea8daac4))
* Add input schema validation to AgentTool ([3df572f](https://github.com/google/adk-kotlin/commit/3df572f86ef795682b92461e774dca5e8b837f5b))


### Bug Fixes

* exclude protobuf-java transitive dependency in android ([3b2100c](https://github.com/google/adk-kotlin/commit/3b2100c2efd67489f372ad1336cefd5ae7ee0ec0))
* **processor:** support Map&lt;String, Any&gt; and List&lt;Any&gt; as @Tool return types ([26c3f9d](https://github.com/google/adk-kotlin/commit/26c3f9d8231828c323e0329cb0d969f3670e322b))

## 0.1.0 (2026-05-19)

ADK Kotlin 0.1 release. Provides core features for building AI agents on JVM and Android, including:
* LLM agents, custom agents
* Multi - agent orchestration
* Function tools, Agent Skills, and long-running operations
* In-memory session and memory services
* Model integrations: 
  * Gemini on JVM/Android (Google GenAI SDK, Firebase AI),
  * On-device Gemini Nano and Gemma (ML Kit)
* ADK web UI interface

For full details, please visit the official documentation at https://adk.dev.

**Full Changelog**: https://github.com/google/adk-kotlin/commits/v0.1.0