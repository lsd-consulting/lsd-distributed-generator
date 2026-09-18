package io.lsdconsulting.lsd.distributed.generator.integration.testapp

import io.lsdconsulting.lsd.distributed.generator.integration.testapp.config.Jackson2ObjectMapperTestConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@SpringBootApplication
@Import(Jackson2ObjectMapperTestConfig::class)
open class TestApplication

fun main(args: Array<String>) {
    runApplication<TestApplication>(*args)
}
