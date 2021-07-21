package com.example.grabby

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.coroutines.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

import kotlinx.coroutines.flow.*
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

data class TemperatureInfo(
    @JsonProperty("Name") val name: String,
    @JsonProperty("City") val city: String,
    @JsonProperty("State") val state: String,
    )

@Service
class AirportInfo(val restTemplate: RestTemplate) {
    val logger by lazy { LoggerFactory.getLogger(javaClass) }
    val webclient by lazy { WebClient.create() }

    fun getForAirportMono(airportCode: String): Mono<TemperatureInfo?> {
        logger.info("Getting airport info for $airportCode")
        return webclient.method(HttpMethod.GET)
            .uri(urlForAirport(airportCode)).retrieve()
            .bodyToMono(TemperatureInfo::class.java)
            .retry(3)
    }

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
        }.flattenMerge(concurrency = 10).flowOn(Dispatchers.IO).collect()

        return listOf()
    }

    suspend fun getForAirportsWebClient(airportcodes: List<String>): List<TemperatureInfo?> {
        val monos = airportcodes.map { getForAirportMono(it) }
        val flux = Flux.fromIterable(airportcodes).flatMap({
            getForAirportMono(it)
        }, 200)
        return flux.collectList().block()!!
    }

    companion object {
        fun urlForAirport(airportCode: String): String = "http://localhost:3000"
    }
}