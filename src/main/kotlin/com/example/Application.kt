package com.example

import com.example.plugins.*
import com.example.plugins.configureKoin
import io.ktor.server.application.*

fun main(args: Array<String>) {
    // comment
    io.ktor.server.netty.EngineMain.main(args)
}
@Suppress("unused")
fun Application.module() {
    configureKoin()
    configureSerialization()
    configureMonitoring()
    configureRouting()
    configureDefaultHeader()
    configureStatusPages()
}
