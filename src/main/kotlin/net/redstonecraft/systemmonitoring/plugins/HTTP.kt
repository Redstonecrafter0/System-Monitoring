package net.redstonecraft.systemmonitoring.plugins

import io.ktor.features.*
import io.ktor.application.*

fun Application.configureHTTP() {
    install(Compression) {
        gzip {
            priority = 1.0
        }
        deflate {
            priority = 10.0
            minimumSize(1024) // condition
        }
    }

}
