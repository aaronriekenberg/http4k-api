package org.aaron.environment

import org.http4k.config.Environment
import org.http4k.config.EnvironmentKey
import org.http4k.lens.Lens
import org.http4k.lens.boolean
import org.http4k.lens.int
import org.http4k.lens.string

private val env = Environment.ENV

data class Port(val value: Int)

private val portLens: Lens<Environment, Port> =
    EnvironmentKey.int().map(::Port).defaulted("PORT", Port(8080))

val port: Port = portLens(env)

data class RequestRecordingEnabled(val value: Boolean)

private val requestRecordingEnabledLens: Lens<Environment, RequestRecordingEnabled> =
    EnvironmentKey.boolean().map(::RequestRecordingEnabled)
        .defaulted("REQUEST_RECORDING_ENABLED", RequestRecordingEnabled(false))

val requestRecordingEnabled: RequestRecordingEnabled = requestRecordingEnabledLens(env)

private val versionResourceEnv = Environment.fromResource("appversion.properties")

data class Version(val version: String)

private val versionLens: Lens<Environment, Version> =
    EnvironmentKey.string().map(::Version).defaulted("version", Version("UNKNOWN"))

val version: Version = versionLens(versionResourceEnv)
