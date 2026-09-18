package io.lsdconsulting.lsd.distributed.generator.integration.testapp.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

/**
 * Boot 4 no longer auto-exposes a Jackson 2 ObjectMapper. The published
 * postgres connector still requires one; provide it for ITs until that
 * connector ships its own ConditionalOnMissingBean fallback.
 */
@TestConfiguration
open class Jackson2ObjectMapperTestConfig {
    @Bean
    @ConditionalOnMissingBean(ObjectMapper::class)
    fun jackson2ObjectMapper(): ObjectMapper = ObjectMapper().findAndRegisterModules()
}
