package io.lsdconsulting.lsd.distributed.generator.diagram

import com.lsd.core.IdGenerator
import com.lsd.core.domain.Message
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.connector.repository.InterceptedDocumentRepository
import io.lsdconsulting.lsd.distributed.generator.diagram.dto.EventContainer
import io.lsdconsulting.lsd.distributed.generator.diagram.event.EventBuilderMap
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.`is`
import org.jeasy.random.EasyRandom
import org.jeasy.random.EasyRandomParameters
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime

internal class InteractionGeneratorShould {
    private val easyRandom = EasyRandom(EasyRandomParameters().seed(System.currentTimeMillis()))
    private val interceptedDocumentRepository = mockk<InterceptedDocumentRepository>()
    private val eventBuilderMap = EventBuilderMap(IdGenerator(isDeterministic = true))

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
        val now = nowUTC()
        val interceptedInteractions = (1L..5).map {
            randomInteraction().copy(createdAt = now.minusSeconds(it))
        }
        every { interceptedDocumentRepository.findByTraceIds("traceId") } returns interceptedInteractions

        val result = underTest.generate(mapOf("traceId" to null))

        assertThat(result.startTime, `is`(now.minusSeconds(5L)))
    }

    @Test
    fun `calculate the right finishTime`() {
        val now = nowUTC()
        val interceptedInteractions = (1L..5).map {
            randomInteraction().copy(createdAt = now.minusSeconds(it))
        }
        every { interceptedDocumentRepository.findByTraceIds("traceId") } returns interceptedInteractions

        val result = underTest.generate(mapOf("traceId" to null))

        assertThat(result.finishTime, `is`(now.minusSeconds(1L)))
    }

    @Test
    fun `maps elapsed time to message duration`() {
        every { interceptedDocumentRepository.findByTraceIds("traceId") } returns listOf(
            randomInteraction().copy(elapsedTime = 5L)
        )

        val eventContainer = underTest.generate(mapOf("traceId" to null))
        val message = eventContainer.events.single() as Message

        assertThat(message.duration, equalTo(Duration.ofMillis(5)));
    }

    private fun nowUTC(): ZonedDateTime = ZonedDateTime.now(ZoneId.of("UTC"))

    private fun randomInteraction(): InterceptedInteraction =
        easyRandom.nextObject(InterceptedInteraction::class.java)
}
