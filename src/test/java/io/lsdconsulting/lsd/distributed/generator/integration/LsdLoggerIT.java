package io.lsdconsulting.lsd.distributed.generator.integration;

import com.lsd.core.LsdContext;
import com.lsd.core.domain.ComponentName;
import com.lsd.core.domain.SequenceEvent;
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction;
import io.lsdconsulting.lsd.distributed.generator.diagram.InteractionGenerator;
import io.lsdconsulting.lsd.distributed.generator.diagram.LsdLogger;
import io.lsdconsulting.lsd.distributed.generator.integration.testapp.TestApplication;
import io.lsdconsulting.lsd.distributed.generator.integration.testapp.config.RepositoryConfig;
import io.lsdconsulting.lsd.distributed.generator.integration.testapp.repository.TestRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.lsdconsulting.lsd.distributed.access.model.InteractionType.*;
import static io.lsdconsulting.lsd.distributed.generator.integration.testapp.repository.TestRepository.tearDownDatabase;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Slf4j
@Import(RepositoryConfig.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = TestApplication.class)
@TestPropertySource("classpath:application-test.properties")
public class LsdLoggerIT {

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private InteractionGenerator interactionGenerator;

    private final String setupTraceId = randomAlphanumeric(10);
    private final String mainTraceId = randomAlphanumeric(10);
    private final String sourceName = randomAlphanumeric(10).toUpperCase();
    private final String targetName = randomAlphanumeric(10).toUpperCase();

    private final LsdContext realContext = new LsdContext();
    private final LsdContext lsdContext = spy(realContext);
    private final ArgumentCaptor<SequenceEvent> eventCaptor = ArgumentCaptor.forClass(SequenceEvent.class);

    private LsdLogger lsdLogger;

    @BeforeEach
    public void setup() {
        lsdLogger = new LsdLogger(interactionGenerator, lsdContext);
    }

    @AfterAll
    static void tearDown() {
        tearDownDatabase();
    }

    @Test
    void shouldLogInteractionsInLsdContextWithSuppliedMultipleTraceIds() {
        testRepository.save(InterceptedInteraction.builder()
                .traceId(mainTraceId)
                .httpMethod("GET")
                .path("/api-listener?message=from_test")
                .serviceName(sourceName)
                .target(targetName)
                .interactionType(REQUEST)
                .build());
        testRepository.save(InterceptedInteraction.builder()
                .traceId(setupTraceId)
                .target("SomethingDoneEvent")
                .serviceName("TestApp")
                .interactionType(PUBLISH)
                .build());
        testRepository.save(InterceptedInteraction.builder()
                .traceId(mainTraceId)
                .serviceName(sourceName)
                .target(targetName)
                .interactionType(RESPONSE)
                .elapsedTime(10L)
                .httpStatus("200 OK")
                .build());
        testRepository.save(InterceptedInteraction.builder()
                .traceId(setupTraceId)
                .serviceName("TestApp")
                .target("SomethingDoneEvent")
                .interactionType(CONSUME)
                .build());
        testRepository.save(InterceptedInteraction.builder()
                .traceId(mainTraceId)
                .httpMethod("POST")
                .path("/external-api?message=from_feign")
                .serviceName("TestApp")
                .target("UNKNOWN_TARGET")
                .interactionType(REQUEST)
                .build());
        testRepository.save(InterceptedInteraction.builder()
                .traceId(mainTraceId)
                .serviceName("TestApp")
                .target("UNKNOWN_TARGET")
                .interactionType(RESPONSE)
                .elapsedTime(20L)
                .httpStatus("200 OK")
                .build());

        lsdLogger.captureInteractionsFromDatabase(Map.of(mainTraceId, Optional.of("blue"), setupTraceId, Optional.of("green")));
        
        verify(lsdContext, times(6)).capture(eventCaptor.capture());

        List<SequenceEvent> capturedValues = eventCaptor.getAllValues();
        assertThat(capturedValues, hasSize(6));
        assertThat(capturedValues.get(0), allOf(
                hasProperty("label", is("GET /api-listener?message=from_test")),
                hasProperty("from", is(new ComponentName(sourceName))),
                hasProperty("to", is(new ComponentName(targetName))),
                hasProperty("colour", is("blue"))
        ));
        assertThat(capturedValues.get(1), allOf(
                hasProperty("label", is("publish event")),
                hasProperty("from", is(new ComponentName("TestApp"))),
                hasProperty("to", is(new ComponentName("SomethingDoneEvent"))),
                hasProperty("colour", is("green"))
        ));
        assertThat(capturedValues.get(2), allOf(
                hasProperty("label", is("sync 200 OK response (10 ms)")),
                hasProperty("from", is(new ComponentName(targetName))),
                hasProperty("to", is(new ComponentName(sourceName))),
                hasProperty("colour", is("blue"))
        ));
        assertThat(capturedValues, hasItem(allOf(
                hasProperty("label", is("consume message")),
                hasProperty("from", is(new ComponentName("SomethingDoneEvent"))),
                hasProperty("to", is(new ComponentName("TestApp"))),
                hasProperty("colour", is("green"))
        )));
        assertThat(capturedValues, hasItem(allOf(
                hasProperty("label", is("POST /external-api?message=from_feign")),
                hasProperty("from", is(new ComponentName("TestApp"))),
                hasProperty("to", is(new ComponentName("UNKNOWN_TARGET"))),
                hasProperty("colour", is("blue"))
        )));
        assertThat(capturedValues, hasItem(allOf(
                hasProperty("label", is("sync 200 OK response (20 ms)")),
                hasProperty("from", is(new ComponentName("UNKNOWN_TARGET"))),
                hasProperty("to", is(new ComponentName("TestApp"))),
                hasProperty("colour", is("blue"))
        )));
    }
}
