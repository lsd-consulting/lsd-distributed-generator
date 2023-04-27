package io.lsdconsulting.lsd.distributed.generator.diagram;

import com.lsd.core.domain.SequenceEvent;
import io.lsdconsulting.lsd.distributed.access.model.InteractionType;
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction;
import io.lsdconsulting.lsd.distributed.access.repository.InterceptedDocumentRepository;
import io.lsdconsulting.lsd.distributed.generator.diagram.data.InteractionDataGenerator;
import io.lsdconsulting.lsd.distributed.generator.diagram.dto.EventContainer;
import io.lsdconsulting.lsd.distributed.generator.diagram.event.EventBuilderMap;
import io.lsdconsulting.lsd.distributed.generator.diagram.label.LabelGeneratorMap;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static lsd.format.PrettyPrinter.prettyPrintJson;

@Slf4j
@AllArgsConstructor
public class InteractionGenerator {
    public static final String NO_COLOUR = "";

    private final InterceptedDocumentRepository interceptedDocumentRepository;
    private final EventBuilderMap eventBuilderMap;
    private final LabelGeneratorMap labelGeneratorMap;
    private final InteractionDataGenerator interactionDataGenerator;

    public EventContainer generate(final Map<String, Optional<String>> traceIdToColourMap) {
        final var traceIds = traceIdToColourMap.keySet().toArray(new String[0]);
        List<InterceptedInteraction> interactions = interceptedDocumentRepository.findByTraceIds(traceIds);

        return new EventContainer(
                getEvents(traceIdToColourMap, interactions),
                getStartTime(interactions),
                getFinishTime(interactions));
    }

    private ZonedDateTime getFinishTime(List<InterceptedInteraction> interactions) {
        return interactions.stream().map(InterceptedInteraction::getCreatedAt).filter(Objects::nonNull).max(ZonedDateTime::compareTo).orElse(null);
    }

    private ZonedDateTime getStartTime(List<InterceptedInteraction> interactions) {
        return interactions.stream().map(InterceptedInteraction::getCreatedAt).filter(Objects::nonNull).min(ZonedDateTime::compareTo).orElse(null);
    }

    private List<SequenceEvent> getEvents(Map<String, Optional<String>> traceIdToColourMap, List<InterceptedInteraction> interactions) {
        return interactions.stream().map(interceptedInteraction -> {
            String colour = ofNullable(traceIdToColourMap.get(interceptedInteraction.getTraceId())).flatMap(x -> x).orElse(NO_COLOUR);
            String data = prettyPrintJson(interactionDataGenerator.buildFrom(interceptedInteraction));
            String serviceName = interceptedInteraction.getServiceName();
            String target = interceptedInteraction.getTarget();
            InteractionType type = interceptedInteraction.getInteractionType();
            String label = labelGeneratorMap.generate(interceptedInteraction);
            return eventBuilderMap.build(type, label, serviceName, target, colour, data);
        }).collect(Collectors.toList());
    }
}
