package net.redstonecraft.systemmonitoring.plugins

import io.ktor.server.engine.*
import io.ktor.application.*

fun Application.configureAdministration() {
    install(
        ShutDownUrl.ApplicationCallFeature
    ) { // The URL that will be intercepted (you can also use the application.conf's ktor.deployment.shutdown.url key)
        shutDownUrl =
            "/shutdown" // A function that will be executed to get the exit code of the process
        exitCodeSupplier = { 0 } // ApplicationCall.() -> Int
    }

}
