package io.lsdconsulting.lsd.distributed.generator.diagram

import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.access.repository.InterceptedDocumentRepository
import io.lsdconsulting.lsd.distributed.generator.diagram.dto.EventContainer
import io.lsdconsulting.lsd.distributed.generator.diagram.event.EventBuilderMap
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.jeasy.random.EasyRandom
import org.jeasy.random.EasyRandomParameters
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.ZonedDateTime

internal class InteractionGeneratorShould {
    private val easyRandom = EasyRandom(EasyRandomParameters().seed(System.currentTimeMillis()))
    private val interceptedDocumentRepository = mockk<InterceptedDocumentRepository>()
    private val eventBuilderMap = mockk<EventBuilderMap>(relaxed = true)

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

    @Test
    fun `calculate the right startTime`() {
        val now = ZonedDateTime.now(ZoneId.of("UTC"))
        val interceptedInteractions = (1L..5).map {
            easyRandom.nextObject(InterceptedInteraction::class.java).copy(createdAt = now.minusSeconds(it))
        }
        every { interceptedDocumentRepository.findByTraceIds("traceId") } returns interceptedInteractions

        val result = underTest.generate(mapOf("traceId" to null))

        assertThat(result.startTime, `is`(now.minusSeconds(5L)))
    }

    @Test
    fun `calculate the right finishTime`() {
        val now = ZonedDateTime.now(ZoneId.of("UTC"))
        val interceptedInteractions = (1L..5).map {
            easyRandom.nextObject(InterceptedInteraction::class.java).copy(createdAt = now.minusSeconds(it))
        }
        every { interceptedDocumentRepository.findByTraceIds("traceId") } returns interceptedInteractions

        val result = underTest.generate(mapOf("traceId" to null))

        assertThat(result.finishTime, `is`(now.minusSeconds(1L)))
    }
}
