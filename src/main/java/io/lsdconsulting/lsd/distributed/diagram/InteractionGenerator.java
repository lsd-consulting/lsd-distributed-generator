package io.lsdconsulting.lsd.distributed.diagram;

import com.lsd.events.SequenceEvent;
import io.lsdconsulting.lsd.distributed.diagram.data.InteractionDataGenerator;
import io.lsdconsulting.lsd.distributed.diagram.event.EventBuilderMap;
import io.lsdconsulting.lsd.distributed.diagram.label.LabelGeneratorMap;
import io.lsdconsulting.lsd.distributed.model.Type;
import io.lsdconsulting.lsd.distributed.repository.InterceptedDocumentRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
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

    public List<SequenceEvent> generate(final Map<String, Optional<String>> traceIdToColourMap) {
        final var traceIds = traceIdToColourMap.keySet().toArray(new String[0]);
        return interceptedDocumentRepository.findByTraceIds(traceIds).stream().map(interceptedInteraction -> {
            String colour = ofNullable(traceIdToColourMap.get(interceptedInteraction.getTraceId())).flatMap(x -> x).orElse(NO_COLOUR);
            String data = prettyPrintJson(interactionDataGenerator.buildFrom(interceptedInteraction));
            String serviceName = interceptedInteraction.getServiceName();
            String target = interceptedInteraction.getTarget();
            Type type = interceptedInteraction.getType();
            String label = labelGeneratorMap.generate(interceptedInteraction);
            return eventBuilderMap.build(type, label, serviceName, target, colour, data);
        }).collect(Collectors.toList());
    }
}
