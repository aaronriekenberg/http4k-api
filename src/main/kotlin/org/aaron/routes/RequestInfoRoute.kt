package org.aaron.routes

import com.squareup.moshi.Json
import org.aaron.context.requestSharedStateKey
import org.aaron.json.jsonFormat
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.routing.bind
import se.ansman.kotshi.JsonSerializable
import java.util.*

@JsonSerializable
data class RequestSourceDTO(
    @Json(name = "address")
    val address: String,

    @Json(name = "port")
    val port: Int?,

    @Json(name = "scheme")
    val scheme: String?,
)

@JsonSerializable
data class RequestFieldsDTO(
    @Json(name = "request_id")
    val requestID: Long,

    @Json(name = "method")
    val method: String,

    @Json(name = "version")
    val version: String,

    @Json(name = "uri")
    val uri: String,

    @Json(name = "source")
    val source: RequestSourceDTO?,
)

@JsonSerializable
data class RequestInfoDTO(
    @Json(name = "request_fields")
    val requestFields: RequestFieldsDTO,

    @Json(name = "request_headers")
    val requestHeaders: Map<String, String?>,
)

val requestInfoDTOLens = jsonFormat.autoBody<RequestInfoDTO>().toLens()

object RequestInfoRoute {
    operator fun invoke() = "/request_info" bind GET to { request ->

        val requestInfoDTO = RequestInfoDTO(
            requestFields = RequestFieldsDTO(
                requestID = requestSharedStateKey(request).requestID,
                method = request.method.toString(),
                version = request.version,
                uri = request.uri.toString(),
                source = request.source?.let {
                    RequestSourceDTO(
                        address = it.address,
                        port = it.port,
                        scheme = it.scheme,
                    )
                }
            ),
            requestHeaders = request.headers
                .associateTo(TreeMap()) { it.first to it.second }
        )

        Response(OK).with(
            requestInfoDTOLens of requestInfoDTO
        )
    }
}