package io.lsdconsulting.lsd.distributed.generator.integration

import com.lsd.core.IdGenerator
import com.lsd.core.adapter.puml.*
import com.lsd.core.domain.Message
import io.lsdconsulting.lsd.distributed.access.model.InteractionType
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.access.repository.InterceptedDocumentRepository
import io.lsdconsulting.lsd.distributed.generator.diagram.InteractionGenerator
import io.lsdconsulting.lsd.distributed.generator.diagram.event.EventBuilderMap
import io.lsdconsulting.lsd.distributed.generator.diagram.label.LabelGeneratorMap
import org.apache.commons.lang3.RandomStringUtils.randomAlphabetic
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class InteractionGeneratorIT {
    private val interceptedDocumentRepository = Mockito.mock(
        InterceptedDocumentRepository::class.java
    )

    private val labelGeneratorMap = LabelGeneratorMap()

    private lateinit var underTest:InteractionGenerator

    @BeforeEach
    fun setUp() {
        val idGenerator = IdGenerator(true)

        val eventBuilderMap = EventBuilderMap(
            idGenerator
        )
        underTest = InteractionGenerator(
            interceptedDocumentRepository,
            eventBuilderMap,
            labelGeneratorMap,
        )
    }

    @ParameterizedTest
    @MethodSource("provideInterceptedInteractions")
    fun `generate interactions`(
        interceptedInteraction: InterceptedInteraction,
        expectedInteractionName: String,
        expectedBody: String?
    ) {
        given(interceptedDocumentRepository.findByTraceIds(TRACE_ID))
            .willReturn(listOf(interceptedInteraction))

        val result = underTest.generate(mapOf(TRACE_ID to Optional.of("grey"))).events

        assertThat(result, hasSize(1))
        val sequenceEvent = result[0] as Message
        assertThat(sequenceEvent.data, `is`(expectedBody))
        assertThat(sequenceEvent.toPumlMarkup(), `is`(expectedInteractionName))
    }

    @Test
    fun `attach timing to correct synchronous responses`() {
        val interceptedInteractions: List<InterceptedInteraction> = listOf(
            InterceptedInteraction(traceId = TRACE_ID, path = "/abc/def1", target = "target1", serviceName = "service",
                interactionType = InteractionType.REQUEST, httpMethod = "POST", body = "key1=value1;key2=value2", elapsedTime = 0, createdAt = ZonedDateTime.now(
                    ZoneId.of("UTC"))),
            InterceptedInteraction(traceId = TRACE_ID, path = "/abc/def2", target = "target2", serviceName = "service",
                interactionType = InteractionType.REQUEST, httpMethod = "POST", body = "key1=value1;key2=value2", elapsedTime = 0, createdAt = ZonedDateTime.now(
                    ZoneId.of("UTC"))),
            InterceptedInteraction(traceId = TRACE_ID, target = "exchange", serviceName = "service",
                interactionType = InteractionType.PUBLISH, body = "{\"key1\":\"value1\",\"key2\":\"value2\"}", elapsedTime = 0, createdAt = ZonedDateTime.now(
                    ZoneId.of("UTC"))),
            InterceptedInteraction(traceId = TRACE_ID, target = "exchange", serviceName = "service",
                interactionType = InteractionType.CONSUME, body = "", elapsedTime = 0, createdAt = ZonedDateTime.now(
                    ZoneId.of("UTC"))),
            InterceptedInteraction(traceId = TRACE_ID, target = "target", serviceName = "service",
                interactionType = InteractionType.RESPONSE, httpStatus = "200", body = "someValue", elapsedTime = 25L, createdAt = ZonedDateTime.now(
                    ZoneId.of("UTC"))),
            InterceptedInteraction(traceId = TRACE_ID, target = "target", serviceName = "service",
                interactionType = InteractionType.RESPONSE, httpStatus = "200", body = "someValue", elapsedTime = 35L, createdAt = ZonedDateTime.now(
                    ZoneId.of("UTC")))
        )
        given(interceptedDocumentRepository.findByTraceIds(TRACE_ID)).willReturn(interceptedInteractions)

        val result = underTest.generate(mapOf(TRACE_ID to Optional.of("[#grey]"))).events

        val interactions = result.stream().map {it.toPumlMarkup() }.collect(Collectors.toList())
        assertThat(interactions, hasSize(6))
        assertThat(
            interactions, hasItems(
                not(containsString("ms)")),
                not(containsString("ms)")),
                not(containsString("ms)")),
                not(containsString("ms)")),
                containsString("(25 ms)"),
                containsString("(35 ms)")
            )
        )
    }

    @Test
    fun `generate start time of captured flow`() {
        val utc = ZoneId.of("UTC")
        val interceptedInteractions: List<InterceptedInteraction> = listOf(
            InterceptedInteraction(traceId = TRACE_ID, createdAt = ZonedDateTime.ofInstant(Instant.EPOCH, utc).plusMinutes(1),
                path = "/abc/def1", target = "target1", serviceName = "service", interactionType = InteractionType.REQUEST,
                httpMethod = "POST", body = "key1=value1;key2=value2", elapsedTime = 0),
            InterceptedInteraction(traceId = TRACE_ID, createdAt = ZonedDateTime.ofInstant(Instant.EPOCH, utc).plusMinutes(2),
                path = "/abc/def2", target = "target2", serviceName = "service", interactionType = InteractionType.REQUEST,
                httpMethod = "POST", body = "key1=value1;key2=value2", elapsedTime = 0),
            InterceptedInteraction(traceId = TRACE_ID, createdAt = ZonedDateTime.ofInstant(Instant.EPOCH, utc).plusMinutes(3),
                target = "exchange", serviceName = "service", interactionType = InteractionType.PUBLISH,
                body = "{\"key1\":\"value1\",\"key2\":\"value2\"}", elapsedTime = 0),
            InterceptedInteraction(traceId = TRACE_ID, createdAt = ZonedDateTime.ofInstant(Instant.EPOCH, utc).plusMinutes(6),
                target = "exchange", serviceName = "service", interactionType = InteractionType.CONSUME, body = "", elapsedTime = 0),
            InterceptedInteraction(traceId = TRACE_ID, createdAt = ZonedDateTime.ofInstant(Instant.EPOCH, utc).plusMinutes(5),
                target = "target", serviceName = "service", interactionType = InteractionType.RESPONSE, httpStatus = "200",
                body = "someValue", elapsedTime = 25L),
            InterceptedInteraction(traceId = TRACE_ID, createdAt = ZonedDateTime.ofInstant(Instant.EPOCH, utc).plusMinutes(4),
                target = "target", serviceName = "service", interactionType = InteractionType.RESPONSE, httpStatus = "200",
                body = "someValue", elapsedTime = 35L)
        )
        given(interceptedDocumentRepository.findByTraceIds(TRACE_ID)).willReturn(interceptedInteractions)

        val result = underTest.generate(mapOf(TRACE_ID to Optional.of("[#grey]")))

        assertThat(
            result.startTime,
            `is`(ZonedDateTime.ofInstant(Instant.EPOCH, utc).plusMinutes(1))
        )
    }

    @Test
    fun `generate finish time of captured flow`() {
        val utc = ZoneId.of("UTC")
        val interceptedInteractions: List<InterceptedInteraction> = listOf(
            InterceptedInteraction(traceId = TRACE_ID, createdAt = ZonedDateTime.ofInstant(Instant.EPOCH, utc).plusMinutes(3),
                path = "/abc/def1", target = "target1", serviceName = "service", interactionType = InteractionType.REQUEST, httpMethod = "POST",
                body = "key1=value1;key2=value2", elapsedTime = 0),
            InterceptedInteraction(traceId = TRACE_ID, createdAt = ZonedDateTime.ofInstant(Instant.EPOCH, utc).plusMinutes(2),
                path = "/abc/def2", target = "target2", serviceName = "service", interactionType = InteractionType.REQUEST, httpMethod = "POST",
                body = "key1=value1;key2=value2", elapsedTime = 0),
            InterceptedInteraction(traceId = TRACE_ID, createdAt = ZonedDateTime.ofInstant(Instant.EPOCH, utc).plusMinutes(1),
                target = "exchange", serviceName = "service", interactionType = InteractionType.PUBLISH,
                body = "{\"key1\":\"value1\",\"key2\":\"value2\"}", elapsedTime = 0),
            InterceptedInteraction(traceId = TRACE_ID, createdAt = ZonedDateTime.ofInstant(Instant.EPOCH, utc).plusMinutes(4),
                target = "exchange", serviceName = "service", interactionType = InteractionType.CONSUME, body = "", elapsedTime = 0),
            InterceptedInteraction(traceId = TRACE_ID, createdAt = ZonedDateTime.ofInstant(Instant.EPOCH, utc).plusMinutes(5),
                target = "target", serviceName = "service", interactionType = InteractionType.RESPONSE, httpStatus = "200",
                 body = "someValue", elapsedTime = 25L),
            InterceptedInteraction(traceId = TRACE_ID, createdAt = ZonedDateTime.ofInstant(Instant.EPOCH, utc).plusMinutes(6),
                target = "target", serviceName = "service", interactionType = InteractionType.RESPONSE, httpStatus = "200",
                 body = "someValue", elapsedTime = 35L)
        )
        given(interceptedDocumentRepository.findByTraceIds(TRACE_ID)).willReturn(interceptedInteractions)

        val result = underTest.generate(mapOf(TRACE_ID to Optional.of("[#grey]")))

        assertThat(
            result.finishTime,
            `is`(ZonedDateTime.ofInstant(Instant.EPOCH, utc).plusMinutes(6))
        )
    }

    @Test
    fun `generate request headers in body`() {
        val interceptedInteraction = buildInterceptedInteraction(
            InteractionType.REQUEST,
            mutableMapOf("header" to listOf("value")),
            mapOf()
        )
        given(interceptedDocumentRepository.findByTraceIds(TRACE_ID))
            .willReturn(listOf(interceptedInteraction))

        val interactionNames = underTest.generate(mapOf(TRACE_ID to Optional.empty())).events

        val body = (interactionNames[0] as Message).data as String
        assertThat(body, containsString("requestHeaders"))
        assertThat(body, not(containsString("responseHeaders")))
        assertThat(body, not(containsString("headers")))
    }

    @Test
    fun `generate response headers in body`() {
        val interceptedInteraction = buildInterceptedInteraction(
            InteractionType.RESPONSE,
            mutableMapOf(),
            mapOf("header" to listOf("value"))
        )
        given(interceptedDocumentRepository.findByTraceIds(TRACE_ID))
            .willReturn(listOf(interceptedInteraction))

        val interactionNames = underTest.generate(mapOf(TRACE_ID to Optional.empty())).events

        val body = (interactionNames[0] as Message).data as String
        assertThat(body, containsString("responseHeaders"))
        assertThat(body, not(containsString("requestHeaders")))
        assertThat(body, not(containsString("headers")))
    }

    @Test
    fun `generate headers in body`() {
        val interceptedInteraction = buildInterceptedInteraction(
            InteractionType.PUBLISH,
            mutableMapOf("header" to listOf("value")),
            mapOf()
        )
        given(interceptedDocumentRepository.findByTraceIds(TRACE_ID))
            .willReturn(listOf(interceptedInteraction))

        val interactionNames = underTest.generate(mapOf(TRACE_ID to Optional.empty())).events

        val body = (interactionNames[0] as Message).data as String
        assertThat(body, containsString("headers"))
        assertThat(body, not(containsString("responseHeaders")))
        assertThat(body, not(containsString("requestHeaders")))
    }

    private fun buildInterceptedInteraction(
        type: InteractionType,
        requestHeaders: MutableMap<String, Collection<String>>,
        responseHeaders: Map<String, List<String>>
    ): InterceptedInteraction {
        return InterceptedInteraction(
            traceId = TRACE_ID,
            requestHeaders = requestHeaders,
            responseHeaders = responseHeaders,
            serviceName = "service",
            target = "target",
            interactionType = type,
            elapsedTime = 0,
            createdAt = ZonedDateTime.now(ZoneId.of("UTC"))
        )
    }

    companion object {
        private val TRACE_ID = randomAlphabetic(10)

        @JvmStatic
        private fun provideInterceptedInteractions(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    InterceptedInteraction(traceId = TRACE_ID, path = "/abc/def", target = "target", serviceName = "service",
                        interactionType = InteractionType.REQUEST, httpMethod = "POST", body = "key1=value1;key2=value2", elapsedTime = 0, createdAt = ZonedDateTime.now(
                            ZoneId.of("UTC"))),
                    "Service -[#grey]> Target: <text fill=\"grey\">[[#1 {POST /abc/def} POST /abc/def]]</text>",
                    "{\n  \"body\": \"key1=value1;key2=value2\"\n}"
                ),
                Arguments.of(
                    InterceptedInteraction(traceId = TRACE_ID, path = "/abc/def", target = "target", serviceName = "service",
                        interactionType = InteractionType.REQUEST, httpMethod = "POST",
                        body = "{\"key1\":\"value1\",\"key2\":\"value2\"}", elapsedTime = 0, createdAt = ZonedDateTime.now(
                            ZoneId.of("UTC"))),
                    "Service -[#grey]> Target: <text fill=\"grey\">[[#1 {POST /abc/def} POST /abc/def]]</text>",
                    "{\n  \"body\": {\n    \"key1\": \"value1\",\n    \"key2\": \"value2\"\n  }\n}"
                ),
                Arguments.of(
                    InterceptedInteraction(traceId = TRACE_ID, path = "/abc/defghi", target = "target", serviceName = "service",
                        interactionType = InteractionType.RESPONSE, httpStatus = "200", body = "someValue", elapsedTime = 2L, createdAt = ZonedDateTime.now(
                            ZoneId.of("UTC"))),
                    "Target --[#grey]> Service: <text fill=\"grey\">[[#1 {sync 200 response (2 ms)} sync 200 response (2 ms)]]</text>",
                    "{\n  \"body\": \"someValue\"\n}"
                ),
                Arguments.of(
                    InterceptedInteraction(traceId = TRACE_ID, target = "exchange", serviceName = "service",
                        interactionType = InteractionType.PUBLISH, body = "{\"key1\":\"value1\",\"key2\":\"value2\"}", elapsedTime = 0, createdAt = ZonedDateTime.now(
                            ZoneId.of("UTC"))),
                    "Service -[#grey]> Exchange: <text fill=\"grey\">[[#1 {publish event} publish event]]</text>",
                    "{\n  \"body\": {\n    \"key1\": \"value1\",\n    \"key2\": \"value2\"\n  }\n}"
                ),
                Arguments.of(
                    InterceptedInteraction(traceId = TRACE_ID, target = "exchange", serviceName = "service",
                        interactionType = InteractionType.CONSUME, body = "",  elapsedTime = 0, createdAt = ZonedDateTime.now(
                            ZoneId.of("UTC"))),
                    "Exchange -[#grey]> Service: <text fill=\"grey\">[[#1 {consume message} consume message]]</text>",
                    "{\n  \"body\": \"\"\n}"
                )
            )
        }
    }
}
