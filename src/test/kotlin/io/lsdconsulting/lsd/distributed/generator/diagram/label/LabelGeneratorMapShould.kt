package io.lsdconsulting.lsd.distributed.generator.diagram.label

import io.lsdconsulting.lsd.distributed.access.model.InteractionType
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.jeasy.random.EasyRandom
import org.jeasy.random.EasyRandomParameters
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.ZonedDateTime

internal class LabelGeneratorMapShould {
    private val easyRandom = EasyRandom(EasyRandomParameters().seed(System.currentTimeMillis()))

    @Test
    fun `generate request label`() {
        val interaction = easyRandom.nextObject(InterceptedInteraction::class.java).copy(traceId = "", interactionType = InteractionType.REQUEST, httpMethod = "GET", path = "path", elapsedTime = 0, createdAt = ZonedDateTime.now(ZoneId.of("UTC")))
        val result = generateLabel(interaction)
        assertThat(result, `is`("GET path"))
    }

    @Test
    fun `generate response label`() {
        val interaction = easyRandom.nextObject(InterceptedInteraction::class.java).copy(traceId = "", interactionType = InteractionType.RESPONSE, httpStatus = "OK", elapsedTime = 20L, createdAt = ZonedDateTime.now(ZoneId.of("UTC")))
        val result = generateLabel(interaction)
        assertThat(result, `is`("sync OK response (20 ms)"))
    }

    @Test
    fun `generate publish label`() {
        val interaction = easyRandom.nextObject(InterceptedInteraction::class.java).copy(traceId = "", interactionType = InteractionType.PUBLISH, elapsedTime = 0, createdAt = ZonedDateTime.now(ZoneId.of("UTC")))
        val result = generateLabel(interaction)
        assertThat(result, `is`("publish event"))
    }

    @Test
    fun `generate consume label`() {
        val interaction = easyRandom.nextObject(InterceptedInteraction::class.java).copy(traceId = "", interactionType = InteractionType.CONSUME, elapsedTime = 0, createdAt = ZonedDateTime.now(ZoneId.of("UTC")))
        val result = generateLabel(interaction)
        assertThat(result, `is`("consume message"))
    }
}
