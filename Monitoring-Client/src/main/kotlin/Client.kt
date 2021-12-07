import kotlinx.html.div
import kotlinx.html.dom.append
import org.w3c.dom.Node
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.w3c.dom.WebSocket

fun main() {
    window.onload = { document.body?.sayHello() }
    var ws = WebSocket("ws://localhost:1234/api/ws/data")
    ws.onclose = {
        ws = WebSocket("ws://localhost:1234/api/ws/data")
        null
    }
    ws.onmessage = {
        Json.decodeFromString<Data>(it.data as String)
    }
}

fun cpuInfo() {
}

fun Node.sayHello() {
    append {
        div {
            +"Hello from JS"
        }
    }
}
