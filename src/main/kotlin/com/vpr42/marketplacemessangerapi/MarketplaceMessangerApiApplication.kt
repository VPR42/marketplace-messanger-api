package com.vpr42.marketplacemessangerapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.web.socket.config.annotation.EnableWebSocket

@SpringBootApplication
@EnableWebSocket
@EnableDiscoveryClient
class MarketplaceMessangerApiApplication

fun main(args: Array<String>) {
    runApplication<MarketplaceMessangerApiApplication>(*args)
}
