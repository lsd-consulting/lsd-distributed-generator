package io.lsdconsulting.lsd.distributed.generator.diagram.data

import io.lsdconsulting.lsd.distributed.access.model.InteractionType
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import java.time.ZoneId
import java.time.ZonedDateTime

internal class InteractionDataGeneratorShould {

    @Test
    fun `build with request headers only`() {
        val requestHeaders = mutableMapOf<String, Collection<String>>("name" to listOf("value"))
        val interaction = InterceptedInteraction(
            traceId = "traceId",
            interactionType = InteractionType.REQUEST,
            requestHeaders = requestHeaders,
            responseHeaders = mapOf("name1" to listOf("value2")),
            body = "someBody",
            serviceName = "serviceName",
            target = "target",
            path = "path",
            elapsedTime = 0,
            createdAt = ZonedDateTime.now(ZoneId.of("UTC"))
        )
        val result = buildDataFrom(interaction)
        assertThat(result.requestHeaders, `is`(requestHeaders))
        assertThat(result.responseHeaders, `is`(Matchers.nullValue()))
        assertThat(result.headers, `is`(Matchers.nullValue()))
        assertThat(result.body, `is`("someBody"))
    }

    @Test
    fun `build with response headers only`() {
        val responseHeaders = mutableMapOf<String, Collection<String>>("name" to listOf("value"))
        val interaction = InterceptedInteraction(
            traceId = "traceId",
            interactionType = InteractionType.RESPONSE,
            requestHeaders = mutableMapOf("name1" to listOf("value2")),
            responseHeaders = responseHeaders,
            body = "someBody",
            serviceName = "serviceName",
            target = "target",
            path = "path",
            elapsedTime = 0,
            createdAt = ZonedDateTime.now(ZoneId.of("UTC"))
        )
        val result = buildDataFrom(interaction)
        assertThat(result.requestHeaders, `is`(Matchers.nullValue()))
        assertThat(result.responseHeaders, `is`(responseHeaders))
        assertThat(result.headers, `is`(Matchers.nullValue()))
        assertThat(result.body, `is`("someBody"))
    }

    @Test
    fun `build with headers only`() {
        val headers = mutableMapOf<String, Collection<String>>("name" to listOf("value"))
        val interaction = InterceptedInteraction(
            traceId = "traceId",
            interactionType = InteractionType.PUBLISH,
            requestHeaders = headers,
            responseHeaders = mapOf("name1" to listOf("value2")),
            body = "someBody",
            serviceName = "serviceName",
            target = "target",
            path = "path",
            elapsedTime = 0,
            createdAt = ZonedDateTime.now(ZoneId.of("UTC"))
        )
        val result = buildDataFrom(interaction)
        assertThat(result.requestHeaders, `is`(Matchers.nullValue()))
        assertThat(result.responseHeaders, `is`(Matchers.nullValue()))
        assertThat(result.headers, `is`(headers))
        assertThat(result.body, `is`("someBody"))
    }
}
