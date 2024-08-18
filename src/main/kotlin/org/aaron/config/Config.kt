package org.aaron.config

import org.http4k.config.Environment
import org.http4k.config.EnvironmentKey
import org.http4k.lens.Lens
import org.http4k.lens.boolean
import org.http4k.lens.int

data class Port(val value: Int)

data class RequestRecordingEnabled(val value: Boolean)

private val env = Environment.ENV

private val portLens: Lens<Environment, Port> =
    EnvironmentKey.int().map(::Port).defaulted("PORT", Port(8080))

val port: Port = portLens(env)

private val requestRecordingEnabledLens: Lens<Environment, RequestRecordingEnabled> =
    EnvironmentKey.boolean().map(::RequestRecordingEnabled)
        .defaulted("REQUEST_RECORDING_ENABLED", RequestRecordingEnabled(false))

val requestRecordingEnabled: RequestRecordingEnabled = requestRecordingEnabledLens(env)
