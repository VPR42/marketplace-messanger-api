package com.vpr42.marketplacemessangerapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@SpringBootApplication
@EnableDiscoveryClient
class MarketplaceMessangerApiApplication

fun main(args: Array<String>) {
    runApplication<MarketplaceMessangerApiApplication>(*args)
}
