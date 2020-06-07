package com.harada

import io.ktor.application.*
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.locations.Locations
import io.ktor.response.*
import io.ktor.request.*
import javax.xml.stream.Location

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(Locations) {

    }
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }
    install(CallLogging){}
}

