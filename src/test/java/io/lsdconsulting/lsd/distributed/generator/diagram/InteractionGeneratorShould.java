package io.lsdconsulting.lsd.distributed.generator.diagram;

import io.lsdconsulting.lsd.distributed.access.repository.InterceptedDocumentRepository;
import io.lsdconsulting.lsd.distributed.generator.diagram.data.InteractionDataGenerator;
import io.lsdconsulting.lsd.distributed.generator.diagram.dto.EventContainer;
import io.lsdconsulting.lsd.distributed.generator.diagram.event.EventBuilderMap;
import io.lsdconsulting.lsd.distributed.generator.diagram.label.LabelGeneratorMap;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class InteractionGeneratorShould {

    private final InterceptedDocumentRepository interceptedDocumentRepository = mock(InterceptedDocumentRepository.class);
    private final EventBuilderMap eventBuilderMap = mock(EventBuilderMap.class);
    private final LabelGeneratorMap labelGeneratorMap = mock(LabelGeneratorMap.class);
    private final InteractionDataGenerator interactionDataGenerator = mock(InteractionDataGenerator.class);

    private final InteractionGenerator underTest = new InteractionGenerator(interceptedDocumentRepository, eventBuilderMap, labelGeneratorMap, interactionDataGenerator);

    @Test
    void handleInactiveRepository() {
        given(interceptedDocumentRepository.findByTraceIds("traceId")).willReturn(emptyList());

        EventContainer result = underTest.generate(Map.of("traceId", Optional.empty()));

        assertThat(result, is(EventContainer.builder().events(emptyList()).build()));
    }
}