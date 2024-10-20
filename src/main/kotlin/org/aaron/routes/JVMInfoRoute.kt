package org.aaron.routes

import com.squareup.moshi.Json
import org.aaron.json.jsonFormat
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.routing.bind
import se.ansman.kotshi.JsonSerializable
import java.lang.management.*
import kotlin.math.round

@JsonSerializable
data class GCDTO(

    @Json(name = "name")
    val name: String,

    @Json(name = "collection_count")
    val collectionCount: Long,

    @Json(name = "collection_time_milliseconds")
    val collectionTimeMilliseconds: Long,

    @Json(name = "memory_pool_names")
    val memoryPoolNames: List<String>,
)

@JsonSerializable
data class GCInfoDTO(

    @Json(name = "gcs")
    val gcDTOs: List<GCDTO>,
)

private fun GarbageCollectorMXBean.toGCDTO() =
    GCDTO(
        name = name,
        collectionCount = collectionCount,
        collectionTimeMilliseconds = collectionTime,
        memoryPoolNames = memoryPoolNames.toList()
    )

fun buildGCInfoDTO(): GCInfoDTO =
    GCInfoDTO(
        gcDTOs = ManagementFactory.getGarbageCollectorMXBeans()
            .map { it.toGCDTO() }
    )

@JsonSerializable
data class MemoryUsageDTO(

    @Json(name = "committed")
    val committed: String,

    @Json(name = "init")
    val init: String,

    @Json(name = "max")
    val max: String,

    @Json(name = "used")
    val used: String,
)

private fun Long.bytesToMiB(): String {
    return if (this < 0) {
        "$this"
    } else {
        var mb = this / 1024.0 / 1024.0
        mb = round(mb * 100) / 100.0
        "$mb MiB"
    }
}

fun MemoryUsage.toMemoryUsageDTO(): MemoryUsageDTO =
    MemoryUsageDTO(
        committed = committed.bytesToMiB(),
        init = init.bytesToMiB(),
        max = max.bytesToMiB(),
        used = used.bytesToMiB(),
    )

@JsonSerializable
data class MemoryInfoDTO(

    @Json(name = "heap_memory_usage")
    val heapMemoryUsage: MemoryUsageDTO,

    @Json(name = "non_heap_memory_usage")
    val nonHeapMemoryUsage: MemoryUsageDTO,
)

fun buildMemoryInfoDTO(): MemoryInfoDTO {
    val memoryMXBean = ManagementFactory.getMemoryMXBean()
    return MemoryInfoDTO(
        heapMemoryUsage = memoryMXBean.heapMemoryUsage.toMemoryUsageDTO(),
        nonHeapMemoryUsage = memoryMXBean.nonHeapMemoryUsage.toMemoryUsageDTO(),
    )
}

@JsonSerializable
data class OSInfoDTO(

    @Json(name = "name")
    val name: String,

    @Json(name = "arch")
    val arch: String,

    @Json(name = "version")
    val version: String,

    @Json(name = "available_processors")
    val availableProcessors: Int,

    @Json(name = "load_average")
    val loadAverage: Double,
)

private fun OperatingSystemMXBean.toOSInfoDTO() = OSInfoDTO(
    name = name,
    arch = arch,
    version = version,
    availableProcessors = availableProcessors,
    loadAverage = systemLoadAverage,
)

@JsonSerializable
data class ThreadDTO(
    @Json(name = "id")
    val id: Long,

    @Json(name = "name")
    val name: String,

    @Json(name = "state")
    val state: Thread.State,
)

private fun ThreadInfo.toThreadDTO() = ThreadDTO(
    id = threadId,
    name = threadName,
    state = threadState,
)

@JsonSerializable
data class ThreadInfoDTO(

    @Json(name = "current_thread_is_virtual")
    val currentThreadIsVirtual: Boolean,

    @Json(name = "thread_count")
    val threadCount: Int,

    @Json(name = "peak_thread_count")
    val peakThreadCount: Int,

    @Json(name = "total_started_thread_count")
    val totalStartedThreadCount: Long,

    @Json(name = "threads")
    val threads: List<ThreadDTO>,
)

private fun ThreadMXBean.toThreadInfoDTO() =
    ThreadInfoDTO(
        currentThreadIsVirtual = Thread.currentThread().isVirtual,
        threadCount = threadCount,
        peakThreadCount = peakThreadCount,
        totalStartedThreadCount = totalStartedThreadCount,
        threads = getThreadInfo(allThreadIds)
            .filterNotNull()
            .map { it.toThreadDTO() }
            .sortedBy { it.id }
    )

@JsonSerializable
data class JVMInfoDTO(
    @Json(name = "gc_info")
    val gcInfo: GCInfoDTO,

    @Json(name = "memory_info")
    val memoryInfo: MemoryInfoDTO,

    @Json(name = "os_info")
    val osInfo: OSInfoDTO,

    @Json(name = "thread_info")
    val threadInfoDTO: ThreadInfoDTO,
)

val jvmInfoDTOLens = jsonFormat.autoBody<JVMInfoDTO>().toLens()

object JVMInfoRoute {
    operator fun invoke() =
        "/jvm_info" bind GET to {

            val jvmInfoDTO = JVMInfoDTO(
                gcInfo = buildGCInfoDTO(),
                memoryInfo = buildMemoryInfoDTO(),
                osInfo = ManagementFactory.getOperatingSystemMXBean().toOSInfoDTO(),
                threadInfoDTO = ManagementFactory.getThreadMXBean().toThreadInfoDTO(),
            )

            Response(OK).with(
                jvmInfoDTOLens of jvmInfoDTO
            )
        }
}