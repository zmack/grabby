package com.example.grabby

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.intellij.lang.annotations.Flow
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate

@RestController
class RootController(val airportInfo: AirportInfo) {
    val logger: Logger by lazy { LoggerFactory.getLogger(javaClass) }

    @GetMapping("/")
    fun index(@RequestParam(defaultValue = "SFO", name = "airport_code") airportCode: String): ResponseEntity<TemperatureInfo?> {
        logger.info("Param is $airportCode")
        return ResponseEntity.ok(
            airportInfo.getForAirport(airportCode)
        )
    }

    @GetMapping("/airports")
    fun list(@RequestParam(defaultValue = "10", name = "airports") airports: Int):
            ResponseEntity<List<TemperatureInfo?>> {
        logger.info("Param is $airports")
        val airportCodes = List(airports) { "SFO" }
        val airportInfo = runBlocking { airportInfo.getForAirportsFlow(airportCodes) }

        return ResponseEntity.ok(airportInfo)
    }
}