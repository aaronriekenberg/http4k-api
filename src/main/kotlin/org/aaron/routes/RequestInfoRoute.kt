package org.aaron.routes

import com.squareup.moshi.Json
import org.aaron.context.requestSharedStateKey
import org.http4k.core.*
import org.http4k.core.Method.GET
import org.http4k.core.Status.Companion.OK
import org.http4k.format.Moshi.auto
import org.http4k.routing.bind
import java.util.*

data class RequestFieldsDTO(
    @Json(name = "request_id")
    val requestID: Long,

    @Json(name = "method")
    val method: Method,

    @Json(name = "version")
    val version: String,

    @Json(name = "uri")
    val uri: String,

    @Json(name = "source")
    val source: RequestSource?,
)

data class RequestInfoDTO(
    @Json(name = "request_fields")
    val requestFields: RequestFieldsDTO,

    @Json(name = "request_headers")
    val requestHeaders: Map<String, String?>,
)

val requestInfoDTOLens = Body.auto<RequestInfoDTO>().toLens()

object RequestInfoRoute {
    operator fun invoke() = "/request_info" bind GET to { request ->

        val requestInfoDTO = RequestInfoDTO(
            requestFields = RequestFieldsDTO(
                requestID = requestSharedStateKey(request).requestID,
                method = request.method,
                version = request.version,
                uri = request.uri.toString(),
                source = request.source,
            ),
            requestHeaders = request.headers
                .associateTo(TreeMap()) { it.first to it.second }
        )

        Response(OK).with(
            requestInfoDTOLens of requestInfoDTO
        )
    }
}