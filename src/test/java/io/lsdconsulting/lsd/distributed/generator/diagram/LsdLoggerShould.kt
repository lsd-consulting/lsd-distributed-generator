package io.lsdconsulting.lsd.distributed.generator.diagram

import com.lsd.core.LsdContext
import com.lsd.core.builders.MessageBuilder
import com.lsd.core.domain.Message
import com.lsd.core.domain.SequenceEvent
import io.lsdconsulting.lsd.distributed.access.model.InteractionType.RESPONSE
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.access.repository.InterceptedDocumentRepository
import io.lsdconsulting.lsd.distributed.generator.diagram.dto.EventContainer
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class LsdLoggerShould {
    private val interceptedDocumentRepository = mockk<InterceptedDocumentRepository>()
    private val interactionGenerator = mockk<InteractionGenerator>()
    private val lsdContext = spyk(LsdContext())
    private val traceId = randomAlphanumeric(8)
    private val secondaryTraceId = randomAlphanumeric(8)

    private val underTest = LsdLogger(interactionGenerator, lsdContext)

    @Test
    fun `capture interaction name`() {
        val interceptedInteraction = InterceptedInteraction(traceId = "", interactionType = RESPONSE, elapsedTime = 0, createdAt = ZonedDateTime.now(ZoneId.of("UTC")))
        every { interceptedDocumentRepository.findByTraceIds(traceId) } returns listOf(interceptedInteraction)
        val message: Message = MessageBuilder.messageBuilder().label("interactionName").data("body").build()
        every {interactionGenerator.generate(any())} returns
            EventContainer.builder().events(listOf<SequenceEvent>(message)).build()

        underTest.captureInteractionsFromDatabase(traceId)

        verify { interactionGenerator.generate(mapOf(traceId to Optional.empty())) }
        verify { lsdContext.capture(message) }
    }

    @Test
    fun `capture interaction names with colour`() {
        val interceptedInteraction1 = InterceptedInteraction(traceId = "", interactionType = RESPONSE, elapsedTime = 0, createdAt = ZonedDateTime.now(ZoneId.of("UTC")))
        val interceptedInteraction2 = InterceptedInteraction(traceId = "", interactionType = RESPONSE, elapsedTime = 0, createdAt = ZonedDateTime.now(ZoneId.of("UTC")))
        every { interceptedDocumentRepository.findByTraceIds(traceId) } returns listOf(interceptedInteraction1)
        every { interceptedDocumentRepository.findByTraceIds(secondaryTraceId) } returns listOf(interceptedInteraction2)
        val message: Message = MessageBuilder.messageBuilder().label("interactionName").data("body").build()
        every { interactionGenerator.generate(any()) } returns
            EventContainer.builder().events(
                listOf<SequenceEvent>(message, message)
            ).build()


        underTest.captureInteractionsFromDatabase(
            mapOf(
                traceId to Optional.of("red"),
                secondaryTraceId to Optional.of("green")
            )
        )

        verify {  interactionGenerator.generate(mapOf(traceId to Optional.of("red"), secondaryTraceId to Optional.of("green"))) }
        verify(exactly = 2) {lsdContext.capture(message) }
    }
}
