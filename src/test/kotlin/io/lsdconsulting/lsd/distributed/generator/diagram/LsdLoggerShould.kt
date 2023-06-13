package io.lsdconsulting.lsd.distributed.generator.diagram

import com.lsd.core.LsdContext
import com.lsd.core.builders.MessageBuilder
import com.lsd.core.domain.Message
import com.lsd.core.domain.SequenceEvent
import io.lsdconsulting.lsd.distributed.connector.model.InteractionType.RESPONSE
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
import java.time.ZoneId
import java.time.ZonedDateTime

class LsdLoggerShould {
    private val easyRandom = EasyRandom(EasyRandomParameters().seed(System.currentTimeMillis()))
    private val interceptedDocumentRepository = mockk<InterceptedDocumentRepository>()
    private val interactionGenerator = mockk<InteractionGenerator>()
    private val lsdContext = spyk(LsdContext())
    private val traceId = randomAlphanumeric(8)
    private val secondaryTraceId = randomAlphanumeric(8)

    private val underTest = LsdLogger(interactionGenerator)

    @Test
    fun `capture interaction name`() {
        val interceptedInteraction = easyRandom.nextObject(InterceptedInteraction::class.java).copy(traceId = "", interactionType = RESPONSE, elapsedTime = 0, createdAt = ZonedDateTime.now(ZoneId.of("UTC")))
        every { interceptedDocumentRepository.findByTraceIds(traceId) } returns listOf(interceptedInteraction)
        val message: Message = MessageBuilder.messageBuilder().label("interactionName").data("body").build()
        every {interactionGenerator.generate(any())} returns
            EventContainer(events = listOf<SequenceEvent>(message))

        underTest.captureInteractionsFromDatabase(lsdContext, traceId)

        verify { interactionGenerator.generate(mapOf(traceId to null)) }
        verify { lsdContext.capture(message) }
    }

    @Test
    fun `capture interaction names with colour`() {
        val interceptedInteraction1 = easyRandom.nextObject(InterceptedInteraction::class.java).copy(traceId = "", interactionType = RESPONSE, elapsedTime = 0, createdAt = ZonedDateTime.now(ZoneId.of("UTC")))
        val interceptedInteraction2 = easyRandom.nextObject(InterceptedInteraction::class.java).copy(traceId = "", interactionType = RESPONSE, elapsedTime = 0, createdAt = ZonedDateTime.now(ZoneId.of("UTC")))
        every { interceptedDocumentRepository.findByTraceIds(traceId) } returns listOf(interceptedInteraction1)
        every { interceptedDocumentRepository.findByTraceIds(secondaryTraceId) } returns listOf(interceptedInteraction2)
        val message: Message = MessageBuilder.messageBuilder().label("interactionName").data("body").build()
        every { interactionGenerator.generate(any()) } returns
            EventContainer(events = listOf<SequenceEvent>(message, message))


        underTest.captureInteractionsFromDatabase(lsdContext,
            mapOf(
                traceId to "red",
                secondaryTraceId to "green"
            )
        )

        verify {  interactionGenerator.generate(mapOf(traceId to "red", secondaryTraceId to "green")) }
        verify(exactly = 2) {lsdContext.capture(message) }
    }
}
