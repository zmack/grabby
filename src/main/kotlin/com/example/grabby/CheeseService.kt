package com.example.grabby

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.util.*

data class Envelope<T>(
    val tx_result: String,
    val tx_code: Int,
    val properties: T?,
    @JsonProperty("Status") val status: List<String>,
    val created_at: Date?
)

sealed class Cizis

data class Cizu(
    val id: Int,
    val name: String,
    val created_at: Date?
): Cizis()

data class Brie(
    val id: Int,
    val flavor: String,
    val list: List<String>,
    val pancake_recipe: Map<String, Int>,
): Cizis()

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = EnvelopedCizu::class, name = "cizu"),
    JsonSubTypes.Type(value = EnvelopedBrie::class, name = "brie")
)
open class Enveloped(
    val tx_result: String,
    val tx_code: Int,
    val type: String?,
    @JsonProperty("Status") val status: List<String>?,
    val created_at: Date?
)

@JsonTypeName("brie")
class EnvelopedBrie(
    tx_result: String,
    tx_code: Int,
    type: String?,
    @JsonProperty("Status") status: List<String>?,
    created_at: Date?,
    val properties: Brie?,
): Enveloped(tx_result, tx_code, type, status, created_at)

@JsonTypeName("cizu")
class EnvelopedCizu(
    tx_result: String,
    tx_code: Int,
    type: String?,
    @JsonProperty("Status") status: List<String>?,
    created_at: Date?,
    val properties: Cizu?,
): Enveloped(tx_result, tx_code, type, status, created_at)

@Service
class CheeseService {
    private val logger by lazy { LoggerFactory.getLogger(javaClass) }
    val webclient by lazy { WebClient.create() }

    final inline fun <reified T> getCizu(param: String): Mono<Envelope<T>?> {
        return webclient.method(HttpMethod.GET)
            .uri(urlForCizu(param)).retrieve()
            .bodyToMono(object: ParameterizedTypeReference<Envelope<T>>(){})
            .retry(3)
    }

    fun getEnvelopedCizu(param: String): Mono<Enveloped?> {
        return webclient.method(HttpMethod.GET)
            .uri(urlForCizu(param)).retrieve()
            .bodyToMono(Enveloped::class.java)
            .retry(3)
    }

    companion object {
        fun urlForCizu(params: String): String = "http://localhost:3000/?$params"
    }
}