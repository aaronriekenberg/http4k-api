package org.aaron.config

import org.http4k.config.Environment
import org.http4k.config.EnvironmentKey
import org.http4k.lens.Lens
import org.http4k.lens.boolean
import org.http4k.lens.int

private val env = Environment.ENV

data class Port(val value: Int)

private val portLens: Lens<Environment, Port> =
    EnvironmentKey.int().map(::Port).defaulted("PORT", Port(8080))

val port: Port = portLens(env)

data class Http2Enabled(val value: Boolean)

private val http2EnabledLens: Lens<Environment, Http2Enabled> =
    EnvironmentKey.boolean().map(::Http2Enabled).defaulted("HTTP2_ENABLED", Http2Enabled(false))

val http2Enabled: Http2Enabled = http2EnabledLens(env)

data class RequestRecordingEnabled(val value: Boolean)

private val requestRecordingEnabledLens: Lens<Environment, RequestRecordingEnabled> =
    EnvironmentKey.boolean().map(::RequestRecordingEnabled)
        .defaulted("REQUEST_RECORDING_ENABLED", RequestRecordingEnabled(false))

val requestRecordingEnabled: RequestRecordingEnabled = requestRecordingEnabledLens(env)

