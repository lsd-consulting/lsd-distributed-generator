package io.lsdconsulting.lsd.distributed.generator.integration.testapp.config;

import io.lsdconsulting.lsd.distributed.generator.integration.testapp.repository.TestRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class RepositoryConfig {

    // This is because the configs in spring.factories run always before any test configs.
    static {
        TestRepository.setupDatabase();
    }

    @Bean
    public TestRepository testRepository() {
        return new TestRepository();
    }
}
