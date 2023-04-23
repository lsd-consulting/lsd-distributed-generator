package io.lsdconsulting.lsd.distributed.generator.integration.testapp.config

import io.lsdconsulting.lsd.distributed.generator.integration.testapp.repository.TestRepository
import io.lsdconsulting.lsd.distributed.generator.integration.testapp.repository.TestRepository.Companion.setupDatabase
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
open class RepositoryConfig {

    @Bean
    open fun testRepository(): TestRepository {
        return TestRepository()
    }

    companion object {
        // This is because the configs in spring.factories run always before any test configs.
        init {
            setupDatabase()
        }
    }
}
