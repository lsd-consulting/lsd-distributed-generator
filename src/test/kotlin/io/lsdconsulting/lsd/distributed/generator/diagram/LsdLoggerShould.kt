package io.lsdconsulting.lsd.distributed.generator.diagram

import com.lsd.core.LsdContext
import com.lsd.core.builders.MessageBuilder.Companion.messageBuilder
import com.lsd.core.domain.Message
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.connector.repository.InterceptedDocumentRepository
import io.lsdconsulting.lsd.distributed.generator.diagram.dto.EventContainer
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric
import org.jeasy.random.EasyRandom
import org.jeasy.random.EasyRandomParameters
import org.junit.jupiter.api.Test

class LsdLoggerShould {
    private val easyRandom = EasyRandom(EasyRandomParameters().seed(System.currentTimeMillis()))
    private val interceptedDocumentRepository = mockk<InterceptedDocumentRepository>()
    private val interactionGenerator = mockk<InteractionGenerator>()
    private val lsdContext = spyk(LsdContext())
    private val traceId = randomAlphanumeric(8)
    private val secondaryTraceId = randomAlphanumeric(8)
    private val message: Message = messageBuilder().build()

    private val underTest = LsdLogger(interactionGenerator)

    @Test
    fun `capture interaction name`() {
        every { interceptedDocumentRepository.findByTraceIds(traceId) } returns listOf(randomInteraction())
        every { interactionGenerator.generate(any()) } returns EventContainer(events = listOf(message))

        underTest.captureInteractionsFromDatabase(lsdContext, traceId)

        verify { interactionGenerator.generate(mapOf(traceId to null)) }
        verify { lsdContext.capture(message) }
    }

    @Test
    fun `capture interaction names with colour`() {
        every { interceptedDocumentRepository.findByTraceIds(traceId) } returns listOf(randomInteraction())
        every { interceptedDocumentRepository.findByTraceIds(secondaryTraceId) } returns listOf(randomInteraction())
        every { interactionGenerator.generate(any()) } returns EventContainer(events = listOf(message, message))

        underTest.captureInteractionsFromDatabase(
            lsdContext = lsdContext,
            traceIdToColourMap = mapOf(
                traceId to "red",
                secondaryTraceId to "green"
            )
        )

        verify { interactionGenerator.generate(mapOf(traceId to "red", secondaryTraceId to "green")) }
        verify(exactly = 2) { lsdContext.capture(message) }
    }

    private fun randomInteraction(): InterceptedInteraction =
        easyRandom.nextObject(InterceptedInteraction::class.java)
}
