package org.aaron.routes

import com.fasterxml.jackson.annotation.JsonProperty
import org.aaron.environment.version
import org.http4k.core.Body
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.format.Jackson.auto
import org.http4k.routing.bind

data class VersionInfoDTO(
    @JsonProperty("version")
    val version: String,
)

val versionInfoDTOLens = Body.auto<VersionInfoDTO>().toLens()

object VersionInfoRoute {
    operator fun invoke() = "/version_info" bind GET to { request ->

        val versionInfoDTO = VersionInfoDTO(
            version = version.version,
        )

        Response(OK).with(
            versionInfoDTOLens of versionInfoDTO
        )
    }
}
