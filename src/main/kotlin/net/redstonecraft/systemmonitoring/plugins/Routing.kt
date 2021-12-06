package net.redstonecraft.systemmonitoring.plugins

import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.features.*
import io.ktor.application.*
import io.ktor.response.*
import net.redstonecraft.systemmonitoring.SystemMonitoring

fun Application.configureRouting() {
    routing {
        get("/api/data") {
            call.respond(SystemMonitoring.lastData)
        }
        static("/static") {
            resources("static")
        }
        install(StatusPages) {
            exception<Throwable> { cause ->
                call.respond(HttpStatusCode.InternalServerError, cause.stackTraceToString())
            }
        }
    }
}
