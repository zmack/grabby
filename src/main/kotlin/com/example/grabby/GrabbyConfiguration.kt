package com.example.grabby

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class GrabbyConfiguration {

    @Bean
    fun restTemplate(): RestTemplate = RestTemplate()
}