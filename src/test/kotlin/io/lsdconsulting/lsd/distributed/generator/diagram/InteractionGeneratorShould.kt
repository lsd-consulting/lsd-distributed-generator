package io.lsdconsulting.lsd.distributed.generator.diagram

import io.lsdconsulting.lsd.distributed.access.repository.InterceptedDocumentRepository
import io.lsdconsulting.lsd.distributed.generator.diagram.dto.EventContainer
import io.lsdconsulting.lsd.distributed.generator.diagram.event.EventBuilderMap
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test

internal class InteractionGeneratorShould {
    private val interceptedDocumentRepository = mockk<InterceptedDocumentRepository>()
    private val eventBuilderMap = mockk<EventBuilderMap>()

    private val underTest = InteractionGenerator(
        interceptedDocumentRepository,
        eventBuilderMap,
    )

    @Test
    fun `handle inactive repository`() {
        every { interceptedDocumentRepository.findByTraceIds("traceId") } returns listOf()

        val result = underTest.generate(mapOf("traceId" to null))

        assertThat(result, `is`(EventContainer(events = emptyList())))
    }
}
