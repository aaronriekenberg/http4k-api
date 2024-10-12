package org.aaron.routes

import com.squareup.moshi.Json
import org.aaron.environment.env
import org.aaron.environment.version
import org.http4k.core.Body
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.format.Moshi.auto
import org.http4k.routing.bind

data class VersionInfoDTO(
    @Json(name = "version")
    val version: String,
)

val versionInfoDTOLens = Body.auto<VersionInfoDTO>().toLens()

object VersionInfoRoute {
    operator fun invoke() = "/version_info" bind GET to { request ->

        val versionInfoDTO = VersionInfoDTO(
            version = version(env),
        )

        Response(OK).with(
            versionInfoDTOLens of versionInfoDTO
        )
    }
}
