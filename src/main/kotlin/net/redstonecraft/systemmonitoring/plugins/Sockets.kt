package net.redstonecraft.systemmonitoring.plugins

import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import java.time.*
import io.ktor.application.*
import io.ktor.routing.*
import net.redstonecraft.systemmonitoring.SystemMonitoring

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        webSocket("/api/ws/data") { // websocketSession
            SystemMonitoring.ws.add(this)

            for (frame in incoming) { }

            SystemMonitoring.ws.remove(this)
        }
    }
}
