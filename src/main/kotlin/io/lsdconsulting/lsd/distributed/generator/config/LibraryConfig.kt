package io.lsdconsulting.lsd.distributed.generator.config

import com.lsd.core.IdGenerator
import io.lsdconsulting.lsd.distributed.connector.repository.InterceptedDocumentRepository
import io.lsdconsulting.lsd.distributed.generator.diagram.InteractionGenerator
import io.lsdconsulting.lsd.distributed.generator.diagram.LsdLogger
import io.lsdconsulting.lsd.distributed.generator.diagram.event.EventBuilderMap
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(name = ["lsd.dist.connectionString"])
open class LibraryConfig {
    @Bean
    open fun idGenerator(@Value("\${lsd.core.ids.deterministic:false}") isDeterministic: Boolean) = IdGenerator(isDeterministic)

    @Bean
    open fun eventBuilderMap(idGenerator: IdGenerator) = EventBuilderMap(idGenerator)

    @Bean
    open fun interactionGenerator(
        interceptedDocumentRepository: InterceptedDocumentRepository,
        eventBuilderMap: EventBuilderMap
    ) = InteractionGenerator(interceptedDocumentRepository, eventBuilderMap)

    @Bean
    open fun lsdLogger(interactionGenerator: InteractionGenerator) = LsdLogger(interactionGenerator)
}
