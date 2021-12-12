package net.redstonecraft.systemmonitoring.plugins

import io.ktor.http.cio.websocket.*
import io.ktor.websocket.*
import java.time.*
import io.ktor.application.*
import io.ktor.routing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.redstonecraft.systemmonitoring.MediaInfo
import net.redstonecraft.systemmonitoring.MediaState
import net.redstonecraft.systemmonitoring.SystemMonitoring
import net.redstonecraft.systemmonitoring.UIMediaInfo

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
        webSocket("/api/ws/media") {
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val info = Json.decodeFromString<MediaInfo>(frame.readText())
                    SystemMonitoring.mediaState = UIMediaInfo(
                        on = info.state != MediaState.NONE,
                        playing = info.state == MediaState.PLAYING,
                        album = info.album,
                        title = info.title,
                        artist = info.artist,
                        duration = formatTime(info.duration?.toLong() ?: 0),
                        currentTime = formatTime(info.currentTime.toLong()),
                        img = info.artwork.minByOrNull {
                            val h = it.sizes.split("x")[1].toInt()
                            if (h < 64) { h + 8192 } else { h }
                        }?.src,
                        progress = if (info.duration == null || info.duration == .0) { "100%" } else { "${(info.currentTime / info.duration) * 100}%" }
                    )
                    SystemMonitoring.lastMediaState = System.currentTimeMillis()
                }
            }
        }
    }
}

private fun formatTime(totalSecs: Long): String {
    val h = totalSecs / 3600
    val m = (totalSecs % 3600) / 60
    val s = totalSecs % 60
    return if (h != 0L) "${nts(h)}:${nts(m)}:${nts(s)}" else "${nts(m)}:${nts(s)}"
}

private fun nts(num: Long): String = num.toString().padStart(2, '0')
