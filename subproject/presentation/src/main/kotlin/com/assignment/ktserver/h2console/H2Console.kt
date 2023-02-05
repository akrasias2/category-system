package com.assignment.ktserver.h2console

import org.h2.tools.Server
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.sql.SQLException

@Component
@Profile("local")
class H2Console {
    // IDE "local" 환경 변수 변경해야 동작
    // console : localhost:8078
    // url : jdbc:h2:mem:testdb
    // user : sa
    private val log = LoggerFactory.getLogger(this::class.java)
    private var webServer: Server? = null
    @EventListener(ContextRefreshedEvent::class)
    @Throws(SQLException::class)
    fun start() {
        log.info("starting h2 console at port 8078")
        webServer = Server.createWebServer("-webPort", "8078", "-tcpAllowOthers").start()
    }

    @EventListener(ContextClosedEvent::class)
    fun stop() {
        log.info("stopping h2 console at port 8078")
        webServer?.stop()
    }
}