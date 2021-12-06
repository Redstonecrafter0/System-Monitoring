package net.redstonecraft.systemmonitoring

import io.ktor.application.*
import net.redstonecraft.systemmonitoring.plugins.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    configureRouting()
    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureSockets()
    configureAdministration()
}
