package io.lsdconsulting.lsd.distributed.generator.diagram

import io.lsdconsulting.lsd.distributed.access.repository.InterceptedDocumentRepository
import io.lsdconsulting.lsd.distributed.generator.diagram.data.InteractionDataGenerator
import io.lsdconsulting.lsd.distributed.generator.diagram.dto.EventContainer
import io.lsdconsulting.lsd.distributed.generator.diagram.event.EventBuilderMap
import io.lsdconsulting.lsd.distributed.generator.diagram.label.LabelGeneratorMap
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import java.util.*

internal class InteractionGeneratorShould {
    private val interceptedDocumentRepository = mockk<InterceptedDocumentRepository>()
    private val eventBuilderMap = mockk<EventBuilderMap>()
    private val labelGeneratorMap = mockk<LabelGeneratorMap>()
    private val interactionDataGenerator = mockk<InteractionDataGenerator>()

    private val underTest = InteractionGenerator(
        interceptedDocumentRepository,
        eventBuilderMap,
        labelGeneratorMap,
        interactionDataGenerator
    )

    @Test
    fun handleInactiveRepository() {
        every { interceptedDocumentRepository.findByTraceIds("traceId") } returns listOf()

        val result = underTest.generate(mapOf("traceId" to Optional.empty()))

        assertThat(result, `is`(EventContainer.builder().events(emptyList()).build()))
    }
}
