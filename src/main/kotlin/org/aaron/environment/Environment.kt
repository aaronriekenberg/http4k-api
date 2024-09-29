package org.aaron.environment

import org.http4k.config.Environment
import org.http4k.config.EnvironmentKey
import org.http4k.config.Port
import org.http4k.lens.boolean
import org.http4k.lens.port
import org.http4k.lens.string

val port = EnvironmentKey.port().required("port")
val requestRecordingEnabled = EnvironmentKey.boolean().required("request_recording_enabled")
val version = EnvironmentKey.string().required("version")

private val defaultConfig = Environment.defaults(
    port of Port(8080),
    requestRecordingEnabled of false,
    version of "UNKNOWN",
)

val env = Environment.fromResource("appversion.properties") overrides
        Environment.JVM_PROPERTIES overrides
        Environment.ENV overrides
        defaultConfig
