package io.lsdconsulting.lsd.distributed.generator.integration

import com.lsd.core.LsdContext
import com.lsd.core.domain.ComponentName
import com.lsd.core.domain.SequenceEvent
import io.lsdconsulting.lsd.distributed.access.model.InteractionType
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.generator.diagram.InteractionGenerator
import io.lsdconsulting.lsd.distributed.generator.diagram.LsdLogger
import io.lsdconsulting.lsd.distributed.generator.integration.testapp.TestApplication
import io.lsdconsulting.lsd.distributed.generator.integration.testapp.config.RepositoryConfig
import io.lsdconsulting.lsd.distributed.generator.integration.testapp.repository.TestRepository
import io.mockk.*
import org.apache.commons.lang3.RandomStringUtils
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

@Import(RepositoryConfig::class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = [TestApplication::class])
@ActiveProfiles("test")
class LsdLoggerIT {
    @Autowired
    private lateinit var testRepository: TestRepository

    @Autowired
    private lateinit var interactionGenerator: InteractionGenerator

    private val setupTraceId = RandomStringUtils.randomAlphanumeric(10)
    private val mainTraceId = RandomStringUtils.randomAlphanumeric(10)
    private val sourceName = RandomStringUtils.randomAlphanumeric(10).uppercase(Locale.getDefault())
    private val targetName = RandomStringUtils.randomAlphanumeric(10).uppercase(Locale.getDefault())
    private val sequenceEventSlot = ArrayList<SequenceEvent>()
    private val lsdContext = spyk(LsdContext())

    private lateinit var underTest: LsdLogger

    @BeforeEach
    fun setup() {
        underTest = LsdLogger(interactionGenerator, lsdContext)
        every { lsdContext.capture(capture(sequenceEventSlot)) } just Runs
    }

    @Test
    fun shouldLogInteractionsInLsdContextWithSuppliedMultipleTraceIds() {
        val interceptedInteraction1 = InterceptedInteraction(
            traceId = mainTraceId,
            httpMethod = "GET",
            path = "/api-listener?message=from_test",
            serviceName = sourceName,
            target = targetName,
            interactionType = InteractionType.REQUEST,
            elapsedTime = 0,
            createdAt = ZonedDateTime.now(ZoneId.of("UTC"))
        )
        testRepository.save(interceptedInteraction1)

        val interceptedInteraction2 = InterceptedInteraction(
            traceId = setupTraceId,
            target = "SomethingDoneEvent",
            serviceName = "TestApp",
            interactionType = InteractionType.PUBLISH,
            elapsedTime = 0,
            createdAt = ZonedDateTime.now(ZoneId.of("UTC"))
        )
        testRepository.save(interceptedInteraction2)

        val interceptedInteraction3 = InterceptedInteraction(
            traceId = mainTraceId,
            serviceName = sourceName,
            target = targetName,
            interactionType = InteractionType.RESPONSE,
            elapsedTime = 10L,
            httpStatus = "200 OK",
            createdAt = ZonedDateTime.now(ZoneId.of("UTC"))
        )
        testRepository.save(interceptedInteraction3)

        val interceptedInteraction4 = InterceptedInteraction(
            traceId = setupTraceId,
            serviceName = "TestApp",
            target = "SomethingDoneEvent",
            interactionType = InteractionType.CONSUME,
            elapsedTime = 0,
            createdAt = ZonedDateTime.now(ZoneId.of("UTC"))
        )
        testRepository.save(interceptedInteraction4)

        val interceptedInteraction5 = InterceptedInteraction(
            traceId = mainTraceId,
            httpMethod = "POST",
            path = "/external-api?message=from_feign",
            serviceName = "TestApp",
            target = "UNKNOWN_TARGET",
            interactionType = InteractionType.REQUEST,
            elapsedTime = 0,
            createdAt = ZonedDateTime.now(ZoneId.of("UTC"))
        )
        testRepository.save(interceptedInteraction5)

        val interceptedInteraction6 = InterceptedInteraction(
            traceId = mainTraceId,
            serviceName = "TestApp",
            target = "UNKNOWN_TARGET",
            interactionType = InteractionType.RESPONSE,
            elapsedTime = 20L,
            httpStatus = "200 OK",
            createdAt = ZonedDateTime.now(ZoneId.of("UTC"))
        )
        testRepository.save(interceptedInteraction6)

        underTest.captureInteractionsFromDatabase(
            linkedMapOf(
                mainTraceId to Optional.of("blue"),
                setupTraceId to Optional.of("green")
            )
        )

        verify(exactly = 6) { lsdContext.capture(any<SequenceEvent>()) }
        assertThat(
            sequenceEventSlot, contains(
                allOf(
                    hasProperty("label", `is`("GET /api-listener?message=from_test")),
                    hasProperty("from", `is`(ComponentName(sourceName))),
                    hasProperty("to", `is`(ComponentName(targetName))),
                    hasProperty("colour", `is`("blue"))
                ), allOf(
                    hasProperty("label", `is`("publish event")),
                    hasProperty("from", `is`(ComponentName("TestApp"))),
                    hasProperty("to", `is`(ComponentName("SomethingDoneEvent"))),
                    hasProperty("colour", `is`("green"))
                ), allOf(
                    hasProperty("label", `is`("sync 200 OK response (10 ms)")),
                    hasProperty("from", `is`(ComponentName(targetName))),
                    hasProperty("to", `is`(ComponentName(sourceName))),
                    hasProperty("colour", `is`("blue"))
                ),allOf(
                    hasProperty("label", `is`("consume message")),
                    hasProperty("from", `is`(ComponentName("SomethingDoneEvent"))),
                    hasProperty("to", `is`(ComponentName("TestApp"))),
                    hasProperty("colour", `is`("green"))
                ),allOf(
                    hasProperty("label", `is`("POST /external-api?message=from_feign")),
                    hasProperty("from", `is`(ComponentName("TestApp"))),
                    hasProperty("to", `is`(ComponentName("UNKNOWN_TARGET"))),
                    hasProperty("colour", `is`("blue"))
                ),allOf(
                    hasProperty("label", `is`("sync 200 OK response (20 ms)")),
                    hasProperty("from", `is`(ComponentName("UNKNOWN_TARGET"))),
                    hasProperty("to", `is`(ComponentName("TestApp"))),
                    hasProperty("colour", `is`("blue"))
                )
            )
        )
    }

    companion object {
        @JvmStatic
        @AfterAll
        fun tearDown() {
            TestRepository.tearDownDatabase()
        }
    }
}
