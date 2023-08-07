package io.lsdconsulting.lsd.distributed.generator.diagram

import com.lsd.core.IdGenerator
import com.lsd.core.adapter.puml.*
import com.lsd.core.domain.Message
import io.lsdconsulting.lsd.distributed.connector.model.InteractionType
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.connector.repository.InterceptedDocumentRepository
import io.lsdconsulting.lsd.distributed.generator.diagram.event.EventBuilderMap
import io.mockk.every
import io.mockk.mockk
import org.apache.commons.lang3.RandomStringUtils.randomAlphabetic
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class InteractionGeneratorIT {
    private val interceptedDocumentRepository = mockk<InterceptedDocumentRepository>()

    private lateinit var underTest: InteractionGenerator

    @BeforeEach
    fun setUp() {
        val idGenerator = IdGenerator(true)

        val eventBuilderMap = EventBuilderMap(
            idGenerator
        )
        underTest = InteractionGenerator(
            interceptedDocumentRepository,
            eventBuilderMap,
        )
    }

    @ParameterizedTest
    @MethodSource("provideInterceptedInteractions")
    fun `generate interactions`(
        interceptedInteraction: InterceptedInteraction,
        expectedInteractionName: String,
        expectedBody: String?
    ) {
        every { interceptedDocumentRepository.findByTraceIds(TRACE_ID) } returns (listOf(interceptedInteraction))

        val result = underTest.generate(mapOf(TRACE_ID to "grey")).events

        assertThat(result, hasSize(1))
        val sequenceEvent = result[0] as Message
        assertThat(sequenceEvent.data, `is`(expectedBody))
        assertThat(sequenceEvent.toPumlMarkup(), `is`(expectedInteractionName))
    }

    @Test
    fun `attach timing to correct synchronous responses`() {
        val interceptedInteractions: List<InterceptedInteraction> = listOf(
            InterceptedInteraction(
                traceId = TRACE_ID,
                path = "/abc/def1",
                target = "target1",
                serviceName = "service",
                interactionType = InteractionType.REQUEST,
                httpMethod = "POST",
                body = "key1=value1;key2=value2",
                elapsedTime = 0,
                createdAt = nowUtc()
            ),
            InterceptedInteraction(
                traceId = TRACE_ID,
                path = "/abc/def2",
                target = "target2",
                serviceName = "service",
                interactionType = InteractionType.REQUEST,
                httpMethod = "POST",
                body = "key1=value1;key2=value2",
                elapsedTime = 0,
                createdAt = nowUtc()
            ),
            InterceptedInteraction(
                traceId = TRACE_ID,
                target = "exchange",
                path = "exchange",
                serviceName = "service",
                interactionType = InteractionType.PUBLISH,
                body = "{\"key1\":\"value1\",\"key2\":\"value2\"}",
                elapsedTime = 0,
                createdAt = nowUtc()
            ),
            InterceptedInteraction(
                traceId = TRACE_ID, target = "exchange", path = "exchange", serviceName = "service",
                interactionType = InteractionType.CONSUME, body = "", elapsedTime = 0, createdAt = nowUtc()
            ),
            InterceptedInteraction(
                traceId = TRACE_ID,
                target = "target",
                path = "target",
                serviceName = "service",
                interactionType = InteractionType.RESPONSE,
                httpStatus = "200",
                body = "someValue",
                elapsedTime = 25L,
                createdAt = nowUtc()
            ),
            InterceptedInteraction(
                traceId = TRACE_ID,
                target = "target",
                path = "target",
                serviceName = "service",
                interactionType = InteractionType.RESPONSE,
                httpStatus = "200",
                body = "someValue",
                elapsedTime = 35L,
                createdAt = nowUtc()
            )
        )
        every { interceptedDocumentRepository.findByTraceIds(TRACE_ID) } returns interceptedInteractions

        val result = underTest.generate(mapOf(TRACE_ID to "[grey]")).events

        val interactions = result.stream().map { it.toPumlMarkup() }.collect(Collectors.toList())
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

    @ParameterizedTest
    @EnumSource(InteractionType::class)
    fun `map createdAt timestamp to created instant`(type: InteractionType) {
        val createdAt = nowUtc()
        every { interceptedDocumentRepository.findByTraceIds(TRACE_ID) } returns listOf(
            InterceptedInteraction(
                traceId = TRACE_ID,
                interactionType = type,
                createdAt = createdAt,
                path = randomAlphabetic(5),
                target = randomAlphabetic(5),
                serviceName = randomAlphabetic(5),
                elapsedTime = 0,
            ),
        )

        val sequenceEvent = underTest.generate(mapOf(TRACE_ID to null)).events.single()

        assertThat(sequenceEvent.created, equalTo(createdAt.toInstant()))
    }

    @Test
    fun `generate start time of captured flow`() {
        val utc = ZoneId.of("UTC")
        val interceptedInteractions: List<InterceptedInteraction> = listOf(
            InterceptedInteraction(
                traceId = TRACE_ID,
                createdAt = ZonedDateTime.ofInstant(Instant.EPOCH, utc).plusMinutes(1),
                path = "/abc/def1",
                target = "target1",
                serviceName = "service",
                interactionType = InteractionType.REQUEST,
                httpMethod = "POST",
                body = "key1=value1;key2=value2",
                elapsedTime = 0
            ),
            InterceptedInteraction(
                traceId = TRACE_ID,
                createdAt = ZonedDateTime.ofInstant(Instant.EPOCH, utc).plusMinutes(2),
                path = "/abc/def2",
                target = "target2",
                serviceName = "service",
                interactionType = InteractionType.REQUEST,
                httpMethod = "POST",
                body = "key1=value1;key2=value2",
                elapsedTime = 0
            ),
            InterceptedInteraction(
                traceId = TRACE_ID,
                createdAt = ZonedDateTime.ofInstant(Instant.EPOCH, utc).plusMinutes(3),
                target = "exchange",
                path = "exchange",
                serviceName = "service",
                interactionType = InteractionType.PUBLISH,
                body = "{\"key1\":\"value1\",\"key2\":\"value2\"}",
                elapsedTime = 0
            ),
            InterceptedInteraction(
                traceId = TRACE_ID,
                createdAt = ZonedDateTime.ofInstant(Instant.EPOCH, utc).plusMinutes(6),
                target = "exchange",
                path = "exchange",
                serviceName = "service",
                interactionType = InteractionType.CONSUME,
                body = "",
                elapsedTime = 0
            ),
            InterceptedInteraction(
                traceId = TRACE_ID,
                createdAt = ZonedDateTime.ofInstant(Instant.EPOCH, utc).plusMinutes(5),
                target = "target",
                path = "target",
                serviceName = "service",
                interactionType = InteractionType.RESPONSE,
                httpStatus = "200",
                body = "someValue",
                elapsedTime = 25L
            ),
            InterceptedInteraction(
                traceId = TRACE_ID,
                createdAt = ZonedDateTime.ofInstant(Instant.EPOCH, utc).plusMinutes(4),
                target = "target",
                path = "target",
                serviceName = "service",
                interactionType = InteractionType.RESPONSE,
                httpStatus = "200",
                body = "someValue",
                elapsedTime = 35L
            )
        )
        every { interceptedDocumentRepository.findByTraceIds(TRACE_ID) } returns interceptedInteractions

        val result = underTest.generate(mapOf(TRACE_ID to "[grey]"))

        assertThat(
            result.startTime,
            `is`(ZonedDateTime.ofInstant(Instant.EPOCH, utc).plusMinutes(1))
        )
    }

    @Test
    fun `generate finish time of captured flow`() {
        val utc = ZoneId.of("UTC")
        val interceptedInteractions: List<InterceptedInteraction> = listOf(
            InterceptedInteraction(
                traceId = TRACE_ID,
                createdAt = ZonedDateTime.ofInstant(Instant.EPOCH, utc).plusMinutes(3),
                path = "/abc/def1",
                target = "target1",
                serviceName = "service",
                interactionType = InteractionType.REQUEST,
                httpMethod = "POST",
                body = "key1=value1;key2=value2",
                elapsedTime = 0
            ),
            InterceptedInteraction(
                traceId = TRACE_ID,
                createdAt = ZonedDateTime.ofInstant(Instant.EPOCH, utc).plusMinutes(2),
                path = "/abc/def2",
                target = "target2",
                serviceName = "service",
                interactionType = InteractionType.REQUEST,
                httpMethod = "POST",
                body = "key1=value1;key2=value2",
                elapsedTime = 0
            ),
            InterceptedInteraction(
                traceId = TRACE_ID,
                createdAt = ZonedDateTime.ofInstant(Instant.EPOCH, utc).plusMinutes(1),
                target = "exchange",
                path = "exchange",
                serviceName = "service",
                interactionType = InteractionType.PUBLISH,
                body = "{\"key1\":\"value1\",\"key2\":\"value2\"}",
                elapsedTime = 0
            ),
            InterceptedInteraction(
                traceId = TRACE_ID,
                createdAt = ZonedDateTime.ofInstant(Instant.EPOCH, utc).plusMinutes(4),
                target = "exchange",
                path = "exchange",
                serviceName = "service",
                interactionType = InteractionType.CONSUME,
                body = "",
                elapsedTime = 0
            ),
            InterceptedInteraction(
                traceId = TRACE_ID,
                createdAt = ZonedDateTime.ofInstant(Instant.EPOCH, utc).plusMinutes(5),
                target = "target",
                path = "target",
                serviceName = "service",
                interactionType = InteractionType.RESPONSE,
                httpStatus = "200",
                body = "someValue",
                elapsedTime = 25L
            ),
            InterceptedInteraction(
                traceId = TRACE_ID,
                createdAt = ZonedDateTime.ofInstant(Instant.EPOCH, utc).plusMinutes(6),
                target = "target",
                path = "target",
                serviceName = "service",
                interactionType = InteractionType.RESPONSE,
                httpStatus = "200",
                body = "someValue",
                elapsedTime = 35L
            )
        )
        every {interceptedDocumentRepository.findByTraceIds(TRACE_ID) } returns interceptedInteractions

        val result = underTest.generate(mapOf(TRACE_ID to "[grey]"))

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
        every {interceptedDocumentRepository.findByTraceIds(TRACE_ID) } returns listOf(interceptedInteraction)

        val interactionNames = underTest.generate(mapOf(TRACE_ID to null)).events

        val body = (interactionNames[0] as Message).data as String
        assertThat(body, containsString("Request Headers"))
        assertThat(body, not(containsString("Response Headers")))
    }

    @Test
    fun `generate response headers in body`() {
        val interceptedInteraction = buildInterceptedInteraction(
            InteractionType.RESPONSE,
            mutableMapOf(),
            mapOf("header" to listOf("value"))
        )
        every {interceptedDocumentRepository.findByTraceIds(TRACE_ID) } returns listOf(interceptedInteraction)

        val interactionNames = underTest.generate(mapOf(TRACE_ID to null)).events

        val body = (interactionNames[0] as Message).data as String
        assertThat(body, containsString("Response Headers"))
        assertThat(body, not(containsString("Request Headers")))
    }

    @Test
    fun `generate headers in body`() {
        val interceptedInteraction = buildInterceptedInteraction(
            InteractionType.PUBLISH,
            mutableMapOf("header" to listOf("value")),
            mapOf()
        )
        every {interceptedDocumentRepository.findByTraceIds(TRACE_ID) } returns listOf(interceptedInteraction)

        val interactionNames = underTest.generate(mapOf(TRACE_ID to null)).events

        val body = (interactionNames[0] as Message).data as String
        assertThat(body, containsString("Headers"))
        assertThat(body, not(containsString("Response Headers")))
        assertThat(body, not(containsString("Request Headers")))
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
            path = "target",
            interactionType = type,
            elapsedTime = 0,
            createdAt = nowUtc()
        )
    }

    companion object {
        private val TRACE_ID = randomAlphabetic(10)

        private fun nowUtc(): ZonedDateTime = ZonedDateTime.now(ZoneId.of("UTC"))

        @JvmStatic
        private fun provideInterceptedInteractions(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    InterceptedInteraction(
                        traceId = TRACE_ID,
                        path = "/abc/def",
                        target = "target",
                        serviceName = "service",
                        interactionType = InteractionType.REQUEST,
                        httpMethod = "POST",
                        body = "key1=value1;key2=value2",
                        elapsedTime = 0,
                        createdAt = nowUtc()
                    ),
                    "Service -[#grey]> Target: <text fill=\"grey\">[[#1 {POST /abc/def} POST /abc/def]]</text>",
                    "<div><section><h3>Full Path</h3><span>/abc/def</span></section><p></p><p></p><p></p><section><h3>Body</h3><p>key1=value1;key2=value2</p></section><p></p></div>"
                ),
                Arguments.of(
                    InterceptedInteraction(
                        traceId = TRACE_ID, path = "/abc/def", target = "target", serviceName = "service",
                        interactionType = InteractionType.REQUEST, httpMethod = "POST",
                        body = "{\"key1\":\"value1\",\"key2\":\"value2\"}", elapsedTime = 0, createdAt = nowUtc()
                    ),
                    "Service -[#grey]> Target: <text fill=\"grey\">[[#1 {POST /abc/def} POST /abc/def]]</text>",
                    "<div><section><h3>Full Path</h3><span>/abc/def</span></section><p></p><p></p><p></p><section><h3>Body</h3><p>{\n  &quot;key1&quot;: &quot;value1&quot;,\n  &quot;key2&quot;: &quot;value2&quot;\n}</p></section><p></p></div>"
                ),
                Arguments.of(
                    InterceptedInteraction(
                        traceId = TRACE_ID,
                        path = "/abc/defghi",
                        target = "target",
                        serviceName = "service",
                        interactionType = InteractionType.RESPONSE,
                        httpStatus = "200",
                        body = "someValue",
                        elapsedTime = 2L,
                        createdAt = nowUtc()
                    ),
                    "Target --[#grey]> Service: <text fill=\"grey\">[[#1 {sync 200 response (2 ms)} sync 200 response (2 ms)]]</text>",
                    "<div><section><h3>Full Path</h3><span>/abc/defghi</span></section><p></p><p></p><p></p><section><h3>Body</h3><p>someValue</p></section><section><h3>Duration</h3><p>2ms</p></section></div>"
                ),
                Arguments.of(
                    InterceptedInteraction(
                        traceId = TRACE_ID,
                        target = "exchange",
                        path = "exchange",
                        serviceName = "service",
                        interactionType = InteractionType.PUBLISH,
                        body = "{\"key1\":\"value1\",\"key2\":\"value2\"}",
                        elapsedTime = 0,
                        createdAt = nowUtc()
                    ),
                    "Service -[#grey]> Exchange: <text fill=\"grey\">[[#1 {publish event} publish event]]</text>",
                    "<div><section><h3>Full Path</h3><span>exchange</span></section><p></p><p></p><p></p><section><h3>Body</h3><p>{\n  &quot;key1&quot;: &quot;value1&quot;,\n  &quot;key2&quot;: &quot;value2&quot;\n}</p></section><p></p></div>"
                ),
                Arguments.of(
                    InterceptedInteraction(
                        traceId = TRACE_ID, target = "exchange", path = "exchange", serviceName = "service",
                        interactionType = InteractionType.CONSUME, body = "", elapsedTime = 0, createdAt = nowUtc()
                    ),
                    "Exchange -[#grey]> Service: <text fill=\"grey\">[[#1 {consume message} consume message]]</text>",
                    "<div><section><h3>Full Path</h3><span>exchange</span></section><p></p><p></p><p></p><p></p><p></p></div>"
                )
            )
        }
    }

}
