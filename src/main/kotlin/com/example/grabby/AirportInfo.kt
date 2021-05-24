package com.example.grabby

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.coroutines.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

import kotlinx.coroutines.flow.*
import org.apache.catalina.util.IOTools.flow
import org.slf4j.LoggerFactory

data class TemperatureInfo(
    @JsonProperty("Name") val name: String,
    @JsonProperty("City") val city: String,
    @JsonProperty("State") val state: String,
    )

@Service
class AirportInfo(val restTemplate: RestTemplate) {
    val logger by lazy { LoggerFactory.getLogger(javaClass) }

    fun getForAirport(airportCode: String): TemperatureInfo? {
        logger.info("Getting airport info for $airportCode")
        val result = restTemplate.getForEntity(urlForAirport(airportCode), TemperatureInfo::class.java)
        return result.body
    }

    suspend fun getForAirports(airportcodes: List<String>): List<TemperatureInfo?> = coroutineScope {
        airportcodes.map {
            async(Dispatchers.IO) {
                getForAirport(it)
            }
        }.awaitAll()
    }

    @FlowPreview
    suspend fun getForAirportsFlow(airportcodes: List<String>): List<TemperatureInfo?> {
        airportcodes.asFlow().map {
            flow {
                emit(getForAirport(it))
            }
        }.flattenMerge(concurrency = 10).collect()

        return listOf()
    }

    companion object {
        fun urlForAirport(airportCode: String): String = "http://localhost:3000"
    }
}