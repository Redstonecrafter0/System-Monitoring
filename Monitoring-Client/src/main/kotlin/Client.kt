import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.browser.*
import kotlinx.html.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.w3c.dom.*
import org.w3c.dom.url.URLSearchParams
import kotlin.math.roundToInt

var lastChange = currentTimeMillis()

suspend fun main() {
    var animate: (Double) -> Unit = {}
    val background = Background(canvas = document.getElementById("background") as HTMLCanvasElement)
    val overlay = Overlay(canvas = document.getElementById("overlay") as HTMLCanvasElement)
    animate = { _ ->
        document.getElementsByClassName("core").asList().forEach {
            val delta = (currentTimeMillis() - lastChange) / 2000.0
            it.setAttribute("style", "--value: ${(lerp(delta, ((it.getAttribute("style")?.substring(9)?.toDoubleOrNull() ?: .0)), ((it.getAttribute("to")?.toDoubleOrNull() ?: .0) * 100))).roundToInt()}")
        }
        background.tick()
        overlay.tick()
        window.requestAnimationFrame(animate)
    }
    window.requestAnimationFrame(animate)
    val client = HttpClient {
        install(WebSockets)
    }
    while (true) {
        try {
            client.webSocket(host = window.location.host, port = if (window.location.port == "") DEFAULT_PORT else window.location.port.toInt() , path = "/api/ws/data") {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val data = Json.decodeFromString<Data>(frame.readText())
                        lastChange = currentTimeMillis()
                        data.cpu.forEach { updateCpuInfo(it) }
                        updateMemory(data.memory)
                        data.memoryInfo.forEachIndexed { index, memoryInfo -> updateMemoryInfo(memoryInfo, index) }
                        data.gpu.forEachIndexed { index, gpuData -> updateGpuData(gpuData, index) }
                        updateMedia(data.media)
                        document.getElementById("cpu-temp")?.textContent = ((data.cpu[0].temp * 10).roundToInt() / 10F).toString()
                        data.storage.disks.forEachIndexed { index, disk -> updateDisk(disk, index) }
                        for (i in document.getElementsByClassName("drive").toList()) {
                            if (i.id.split("_")[1].toInt() >= data.storage.disks.size) {
                                i.remove()
                            }
                        }

                        document.getElementById("os")?.textContent = data.os
                        document.getElementById("uptime")?.textContent = data.uptime.formatTime()
                        document.getElementById("hide")?.id = "unhide"
                    }
                }
            }
        } catch (e: Throwable) {
        }
    }
}
