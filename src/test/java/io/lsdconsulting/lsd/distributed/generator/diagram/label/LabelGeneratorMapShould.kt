package io.lsdconsulting.lsd.distributed.generator.diagram.label

import io.lsdconsulting.lsd.distributed.access.model.InteractionType
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.ZonedDateTime

internal class LabelGeneratorMapShould {

    private val underTest = LabelGeneratorMap()

    @Test
    fun `generate request label`() {
        val interaction = InterceptedInteraction(traceId = "", interactionType = InteractionType.REQUEST, httpMethod = "GET", path = "path", elapsedTime = 0, createdAt = ZonedDateTime.now(ZoneId.of("UTC")))
        val result = underTest.generate(interaction)
        assertThat(result, `is`("GET path"))
    }

    @Test
    fun `generate response label`() {
        val interaction = InterceptedInteraction(traceId = "", interactionType = InteractionType.RESPONSE, httpStatus = "OK", elapsedTime = 20L, createdAt = ZonedDateTime.now(ZoneId.of("UTC")))
        val result = underTest.generate(interaction)
        assertThat(result, `is`("sync OK response (20 ms)"))
    }

    @Test
    fun `generate publish label`() {
        val interaction = InterceptedInteraction(traceId = "", interactionType = InteractionType.PUBLISH, elapsedTime = 0, createdAt = ZonedDateTime.now(ZoneId.of("UTC")))
        val result = underTest.generate(interaction)
        assertThat(result, `is`("publish event"))
    }

    @Test
    fun `generate consume label`() {
        val interaction = InterceptedInteraction(traceId = "", interactionType = InteractionType.CONSUME, elapsedTime = 0, createdAt = ZonedDateTime.now(ZoneId.of("UTC")))
        val result = underTest.generate(interaction)
        assertThat(result, `is`("consume message"))
    }
}
