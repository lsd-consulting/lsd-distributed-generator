package io.lsdconsulting.lsd.distributed.generator.integration;

import com.lsd.IdGenerator;
import com.lsd.events.Message;
import com.lsd.events.SequenceEvent;
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction;
import io.lsdconsulting.lsd.distributed.access.model.Type;
import io.lsdconsulting.lsd.distributed.access.repository.InterceptedDocumentRepository;
import io.lsdconsulting.lsd.distributed.generator.diagram.InteractionGenerator;
import io.lsdconsulting.lsd.distributed.generator.diagram.data.InteractionDataGenerator;
import io.lsdconsulting.lsd.distributed.generator.diagram.event.ConsumeMessageBuilder;
import io.lsdconsulting.lsd.distributed.generator.diagram.event.EventBuilderMap;
import io.lsdconsulting.lsd.distributed.generator.diagram.event.MessageBuilder;
import io.lsdconsulting.lsd.distributed.generator.diagram.event.SynchronousResponseBuilder;
import io.lsdconsulting.lsd.distributed.generator.diagram.label.LabelGeneratorMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static io.lsdconsulting.lsd.distributed.access.model.Type.*;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class InteractionGeneratorIT {

    private static final String TRACE_ID = randomAlphabetic(10);

    private final InterceptedDocumentRepository interceptedDocumentRepository = mock(InterceptedDocumentRepository.class);
    private final IdGenerator idGenerator = new IdGenerator(true);

    private final EventBuilderMap eventBuilderMap = new EventBuilderMap(new MessageBuilder(idGenerator), new SynchronousResponseBuilder(idGenerator), new ConsumeMessageBuilder(idGenerator));
    private final LabelGeneratorMap labelGeneratorMap = new LabelGeneratorMap();
    private final InteractionDataGenerator interactionDataGenerator = new InteractionDataGenerator();

    private final InteractionGenerator underTest = new InteractionGenerator(interceptedDocumentRepository,eventBuilderMap, labelGeneratorMap, interactionDataGenerator);

    @ParameterizedTest
    @MethodSource("provideInterceptedInteractions")
    void generateInteractions(final InterceptedInteraction interceptedInteraction, final String expectedInteractionName, final String expectedBody) {
        given(interceptedDocumentRepository.findByTraceIds(TRACE_ID)).willReturn(List.of(interceptedInteraction));

        final List<SequenceEvent> result = underTest.generate(Map.of(TRACE_ID, Optional.of("[#grey]")));

        assertThat(result, hasSize(1));
        Message sequenceEvent = (Message) result.get(0);
        assertThat(sequenceEvent.getData(), is(expectedBody));
        assertThat(sequenceEvent.toMarkup(), is(expectedInteractionName));
    }

    private static Stream<Arguments> provideInterceptedInteractions() {
        return Stream.of(
                of(InterceptedInteraction.builder().traceId(TRACE_ID).path("/abc/def").target("target").serviceName("service").type(REQUEST).httpMethod("POST").body("key1=value1;key2=value2").build(), "Service -[#[#grey]]> Target: <text fill=\"[#grey]\">[[#1 {POST /abc/def} POST /abc/def]]</text>", "{\n  \"body\": \"key1=value1;key2=value2\"\n}"),
                of(InterceptedInteraction.builder().traceId(TRACE_ID).path("/abc/defghi").target("target").serviceName("service").type(RESPONSE).httpStatus("200").body("someValue").elapsedTime(2L).build(), "Target --[#[#grey]]>> Service: <text fill=\"[#grey]\">[[#1 {sync 200 response (2 ms)} sync 200 response (2 ms)]]</text>", "{\n  \"body\": \"someValue\"\n}"),
                of(InterceptedInteraction.builder().traceId(TRACE_ID).target("exchange").serviceName("service").type(PUBLISH).body("{\"key1\":\"value1\",\"key2\":\"value2\"}").build(), "Service -[#[#grey]]> Exchange: <text fill=\"[#grey]\">[[#1 {publish event} publish event]]</text>", "{\n  \"body\": \"{\\\"key1\\\":\\\"value1\\\",\\\"key2\\\":\\\"value2\\\"}\"\n}"),
                of(InterceptedInteraction.builder().traceId(TRACE_ID).target("exchange").serviceName("service").type(CONSUME).body("").build(), "Exchange -[#[#grey]]> Service: <text fill=\"[#grey]\">[[#1 {consume message} consume message]]</text>", "{\n  \"body\": \"\"\n}")
        );
    }

    @Test
    void attachTimingToCorrectSynchronousResponses() {
        List<InterceptedInteraction> interceptedInteractions = List.of(
                InterceptedInteraction.builder().traceId(TRACE_ID).path("/abc/def1").target("target1").serviceName("service").type(REQUEST).httpMethod("POST").body("key1=value1;key2=value2").build(),
                InterceptedInteraction.builder().traceId(TRACE_ID).path("/abc/def2").target("target2").serviceName("service").type(REQUEST).httpMethod("POST").body("key1=value1;key2=value2").build(),
                InterceptedInteraction.builder().traceId(TRACE_ID).target("exchange").serviceName("service").type(PUBLISH).body("{\"key1\":\"value1\",\"key2\":\"value2\"}").build(),
                InterceptedInteraction.builder().traceId(TRACE_ID).target("exchange").serviceName("service").type(CONSUME).body("").build(),
                InterceptedInteraction.builder().traceId(TRACE_ID).target("target").serviceName("service").type(RESPONSE).httpStatus("200").body("someValue").elapsedTime(25L).build(),
                InterceptedInteraction.builder().traceId(TRACE_ID).target("target").serviceName("service").type(RESPONSE).httpStatus("200").body("someValue").elapsedTime(35L).build()
        );

        given(interceptedDocumentRepository.findByTraceIds(TRACE_ID)).willReturn(interceptedInteractions);

        List<SequenceEvent> result = underTest.generate(Map.of(TRACE_ID, Optional.of("[#grey]")));

        List<String> interactions = result.stream().map(SequenceEvent::toMarkup).collect(toList());
        assertThat(interactions, hasSize(6));
        assertThat(interactions, hasItems(
                not(containsString("ms)")),
                not(containsString("ms)")),
                not(containsString("ms)")),
                not(containsString("ms)")),
                containsString("(25 ms)"),
                containsString("(35 ms)")));
    }

    @Test
    void generateRequestHeadersInBody() {
        InterceptedInteraction interceptedInteraction = buildInterceptedInteraction(REQUEST)
                .requestHeaders(Map.of("header", List.of("value")))
                .build();

        given(interceptedDocumentRepository.findByTraceIds(TRACE_ID)).willReturn(List.of(interceptedInteraction));

        final List<SequenceEvent> interactionNames = underTest.generate(Map.of(TRACE_ID, Optional.empty()));

        String body = (String) ((Message) interactionNames.get(0)).getData();

        assertThat(body, containsString("requestHeaders"));
        assertThat(body, not(containsString("responseHeaders")));
        assertThat(body, not(containsString("headers")));
    }

    @Test
    void generateResponseHeadersInBody() {
        InterceptedInteraction interceptedInteraction = buildInterceptedInteraction(RESPONSE)
                .responseHeaders(Map.of("header", List.of("value")))
                .build();
        given(interceptedDocumentRepository.findByTraceIds(TRACE_ID)).willReturn(List.of(interceptedInteraction));

        final List<SequenceEvent> interactionNames = underTest.generate(Map.of(TRACE_ID, Optional.empty()));

        String body = (String) ((Message) interactionNames.get(0)).getData();

        assertThat(body, containsString("responseHeaders"));
        assertThat(body, not(containsString("requestHeaders")));
        assertThat(body, not(containsString("headers")));
    }

    @Test
    void generateHeadersInBody() {
        InterceptedInteraction interceptedInteraction = buildInterceptedInteraction(PUBLISH)
                .requestHeaders(Map.of("header", List.of("value")))
                .build();
        given(interceptedDocumentRepository.findByTraceIds(TRACE_ID)).willReturn(List.of(interceptedInteraction));

        final List<SequenceEvent> interactionNames = underTest.generate(Map.of(TRACE_ID, Optional.empty()));

        String body = (String) ((Message) interactionNames.get(0)).getData();

        assertThat(body, containsString("headers"));
        assertThat(body, not(containsString("responseHeaders")));
        assertThat(body, not(containsString("requestHeaders")));
    }

    private InterceptedInteraction.InterceptedInteractionBuilder buildInterceptedInteraction(Type type) {
        return InterceptedInteraction.builder()
                .traceId(TRACE_ID)
                .type(type)
                .target("target")
                .serviceName("service");
    }
}
