package io.lsdconsulting.lsd.distributed.generator.diagram;

import com.lsd.LsdContext;
import com.lsd.events.Message;
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction;
import io.lsdconsulting.lsd.distributed.access.repository.InterceptedDocumentRepository;
import io.lsdconsulting.lsd.distributed.generator.diagram.dto.EventContainer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class LsdLoggerShould {

    private final InterceptedDocumentRepository interceptedDocumentRepository = mock(InterceptedDocumentRepository.class);
    private final InteractionGenerator interactionGenerator = mock(InteractionGenerator.class);
    private final LsdContext lsdContext = mock(LsdContext.class);

    private final String traceId = randomAlphanumeric(8);
    private final String secondaryTraceId = randomAlphanumeric(8);

    private final LsdLogger underTest = new LsdLogger(interactionGenerator, lsdContext);

    @Test
    public void captureInteractionName() {
        final InterceptedInteraction interceptedInteraction = InterceptedInteraction.builder().build();
        given(interceptedDocumentRepository.findByTraceIds(traceId)).willReturn(singletonList(interceptedInteraction));
        Message message = Message.builder().label("interactionName").data("body").build();
        given(interactionGenerator.generate(any())).willReturn(EventContainer.builder().events(singletonList(message)).build());

        underTest.captureInteractionsFromDatabase(traceId);

        verify(interactionGenerator).generate(Map.of(traceId, Optional.empty()));
        verify(lsdContext).capture(message);
    }

    @Test
    public void captureInteractionNamesWithColour() {
        final InterceptedInteraction interceptedInteraction1 = InterceptedInteraction.builder().build();
        final InterceptedInteraction interceptedInteraction2 = InterceptedInteraction.builder().build();
        given(interceptedDocumentRepository.findByTraceIds(traceId)).willReturn(singletonList(interceptedInteraction1));
        given(interceptedDocumentRepository.findByTraceIds(secondaryTraceId)).willReturn(singletonList(interceptedInteraction2));
        Message message = Message.builder().label("interactionName").data("body").build();
        given(interactionGenerator.generate(any())).willReturn(EventContainer.builder().events(List.of(message, message)).build());

        underTest.captureInteractionsFromDatabase(Map.of(traceId, Optional.of("red"), secondaryTraceId, Optional.of("green")));

        verify(interactionGenerator).generate(Map.of(traceId, Optional.of("red"), secondaryTraceId, Optional.of("green")));
        verify(lsdContext, times(2)).capture(message);
    }
}
