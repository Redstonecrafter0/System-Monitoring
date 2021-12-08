import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.html.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.w3c.dom.WebSocket
import org.w3c.dom.asList
import kotlin.math.roundToInt

var lastChange = currentTimeMillis()

fun main() {
    var ws = WebSocket("ws://localhost:1234/api/ws/data")
    ws.onclose = {
        ws = WebSocket("ws://localhost:1234/api/ws/data")
        null
    }
    var animate: (Double) -> Unit = {}
    animate = { _: Double ->
        document.getElementsByClassName("core").asList().forEach {
            val delta = (currentTimeMillis() - lastChange) / 2000.0
            it.setAttribute("style", "--value: ${(lerp(delta, ((it.getAttribute("style")?.substring(9)?.toDoubleOrNull() ?: .0)), ((it.getAttribute("to")?.toDoubleOrNull() ?: .0) * 100))).roundToInt()}")
        }
        window.requestAnimationFrame(animate)
    }
    window.requestAnimationFrame(animate)
    ws.onmessage = {
        val data = Json.decodeFromString<Data>(it.data as String)
        lastChange = currentTimeMillis()
        data.cpu.forEach { updateCpuInfo(it) }
        updateMemory(data.memory)
        data.memoryInfo.forEachIndexed { index, memoryInfo -> updateMemoryInfo(memoryInfo, index) }

        document.getElementById("os")?.textContent = data.os
        document.getElementById("uptime")?.textContent = data.uptime.formatTime()
        null
    }
}
