package org.aaron.routes

import com.fasterxml.jackson.annotation.JsonProperty
import org.aaron.context.requestSharedStateKey
import org.http4k.core.Body
import org.http4k.core.Method
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.format.Jackson.auto
import org.http4k.routing.bind

data class RequestFieldsDTO(
    @JsonProperty("request_id")
    val requestID: Long,

    @JsonProperty("method")
    val method: Method,

    @JsonProperty("version")
    val version: String,

    @JsonProperty("uri")
    val uri: String,
)

data class RequestInfoDTO(
    @JsonProperty("request_fields")
    val requestFields: RequestFieldsDTO,

    @JsonProperty("request_headers")
    val requestHeaders: Map<String, String?>,
)

val requestInfoDTOLens = Body.auto<RequestInfoDTO>().toLens()

object RequestInfoRoute {
    operator fun invoke() = "/api/v1/request_info" bind GET to { request ->

        val requestInfoDTO = RequestInfoDTO(
            requestFields = RequestFieldsDTO(
                requestID = requestSharedStateKey(request).requestID,
                method = request.method,
                version = request.version,
                uri = request.uri.toString(),
            ),
            requestHeaders = request.headers.associate { it.first to it.second },
        )

        Response(OK).with(
            requestInfoDTOLens of requestInfoDTO
        )
    }
}