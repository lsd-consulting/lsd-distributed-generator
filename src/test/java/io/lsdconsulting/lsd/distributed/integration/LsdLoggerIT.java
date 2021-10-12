package io.lsdconsulting.lsd.distributed.integration;

import com.lsd.LsdContext;
import com.lsd.events.SequenceEvent;
import io.lsdconsulting.lsd.distributed.diagram.InteractionGenerator;
import io.lsdconsulting.lsd.distributed.diagram.LsdLogger;
import io.lsdconsulting.lsd.distributed.integration.testapp.TestApplication;
import io.lsdconsulting.lsd.distributed.integration.testapp.config.RepositoryConfig;
import io.lsdconsulting.lsd.distributed.integration.testapp.repository.TestRepository;
import io.lsdconsulting.lsd.distributed.model.InterceptedInteraction;
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

import static io.lsdconsulting.lsd.distributed.integration.testapp.repository.TestRepository.tearDownDatabase;
import static io.lsdconsulting.lsd.distributed.model.Type.*;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
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
    private final ArgumentCaptor<SequenceEvent> argumentCaptor = ArgumentCaptor.forClass(SequenceEvent.class);

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
                .type(REQUEST)
                .build());
        testRepository.save(InterceptedInteraction.builder()
                .traceId(setupTraceId)
                .target("SomethingDoneEvent")
                .serviceName("TestApp")
                .type(PUBLISH)
                .build());
        testRepository.save(InterceptedInteraction.builder()
                .traceId(mainTraceId)
                .serviceName(sourceName)
                .target(targetName)
                .type(RESPONSE)
                .elapsedTime(10L)
                .httpStatus("200 OK")
                .build());
        testRepository.save(InterceptedInteraction.builder()
                .traceId(setupTraceId)
                .serviceName("TestApp")
                .target("SomethingDoneEvent")
                .type(CONSUME)
                .build());
        testRepository.save(InterceptedInteraction.builder()
                .traceId(mainTraceId)
                .httpMethod("POST")
                .path("/external-api?message=from_feign")
                .serviceName("TestApp")
                .target("UNKNOWN_TARGET")
                .type(REQUEST)
                .build());
        testRepository.save(InterceptedInteraction.builder()
                .traceId(mainTraceId)
                .serviceName("TestApp")
                .target("UNKNOWN_TARGET")
                .type(RESPONSE)
                .elapsedTime(20L)
                .httpStatus("200 OK")
                .build());

        doNothing().when(lsdContext).capture(argumentCaptor.capture());

        lsdLogger.captureInteractionsFromDatabase(Map.of(mainTraceId, Optional.of("[#blue]"), setupTraceId, Optional.of("[#green]")));

        List<SequenceEvent> capturedValues = argumentCaptor.getAllValues();
        assertThat(capturedValues, hasSize(6));
        assertThat(capturedValues.get(0), allOf(
                hasProperty("label", is("GET /api-listener?message=from_test")),
                hasProperty("from", is(sourceName)),
                hasProperty("to", is(targetName)),
                hasProperty("colour", is("[#blue]"))
        ));
        assertThat(capturedValues.get(1), allOf(
                hasProperty("label", is("publish event")),
                hasProperty("from", is("TestApp")),
                hasProperty("to", is("SomethingDoneEvent")),
                hasProperty("colour", is("[#green]"))
        ));
        assertThat(capturedValues.get(2), allOf(
                hasProperty("label", is("sync 200 OK response (10 ms)")),
                hasProperty("from", is(targetName)),
                hasProperty("to", is(sourceName)),
                hasProperty("colour", is("[#blue]"))
        ));
        assertThat(capturedValues, hasItem(allOf(
                hasProperty("label", is("consume message")),
                hasProperty("from", is("SomethingDoneEvent")),
                hasProperty("to", is("TestApp")),
                hasProperty("colour", is("[#green]"))
        )));
        assertThat(capturedValues, hasItem(allOf(
                hasProperty("label", is("POST /external-api?message=from_feign")),
                hasProperty("from", is("TestApp")),
                hasProperty("to", is("UNKNOWN_TARGET")),
                hasProperty("colour", is("[#blue]"))
        )));
        assertThat(capturedValues, hasItem(allOf(
                hasProperty("label", is("sync 200 OK response (20 ms)")),
                hasProperty("from", is("UNKNOWN_TARGET")),
                hasProperty("to", is("TestApp")),
                hasProperty("colour", is("[#blue]"))
        )));
    }
}
