import kotlinx.browser.document
import kotlinx.html.*
import kotlinx.html.dom.create
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.w3c.dom.*
import kotlin.math.roundToInt

fun main() {
    var ws = WebSocket("ws://localhost:1234/api/ws/data")
    ws.onclose = {
        ws = WebSocket("ws://localhost:1234/api/ws/data")
        null
    }
    document.body?.append(createCpuInfo(CpuData(
        0,
        .0,
        213123132,
        124124141,
        .4,
        1.5
    )))
    ws.onmessage = {
        val data = Json.decodeFromString<Data>(it.data as String)
        data.cpu.forEach {
            document.body?.append(createCpuInfo(it))
        }
    }
}

fun createCpuInfo(data: CpuData): HTMLElement =
    document.create.div {
        div {
            p(classes = "mhz") {
                + (data.clock / 1000000F).toString()
            }
            div(classes = "circle-wrap") {
                style = "--rot: ${data.usage * 180}deg"
                div(classes = "circle") {
                    div(classes = "mask full") {
                        div(classes = "fill")
                    }
                    div(classes = "mask half") {
                        div(classes = "fill")
                    }
                    div(classes = "inside-circle") {
                        + "${(data.usage * 1000).roundToInt() / 10F}%"
                    }
                }
            }
        }
    }
