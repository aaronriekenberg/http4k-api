package org.aaron.routes

import org.aaron.environment.version
import org.aaron.json.jsonFormat
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.routing.bind
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class VersionInfoDTO(
    val version: String,
)

val versionInfoDTOLens = jsonFormat.autoBody<VersionInfoDTO>().toLens()

object VersionInfoRoute {
    operator fun invoke() = "/version_info" bind GET to {

        val versionInfoDTO = VersionInfoDTO(
            version = version,
        )

        Response(OK).with(
            versionInfoDTOLens of versionInfoDTO
        )
    }
}
