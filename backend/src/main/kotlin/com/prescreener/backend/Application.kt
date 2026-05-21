package com.prescreener.backend

import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import com.prescreener.backend.plugins.configureSerialization
import com.prescreener.backend.plugins.configureRouting
import java.io.File

fun main() {
    val port = 8080
    
    // This message will confirm you are running the LATEST code version
    println("************************************************")
    println("        PATIENT PRE-SCREENER BACKEND v2.0       ")
    println("        Working Dir: ${File(".").absolutePath}  ")
    println("************************************************")

    embeddedServer(Netty, port = port, host = "0.0.0.0") {
        configureSerialization()
        configureRouting()
    }.start(wait = true)
}
