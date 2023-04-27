package io.lsdconsulting.lsd.distributed.generator.diagram.event;

import com.lsd.core.IdGenerator;
import com.lsd.core.domain.SequenceEvent;
import io.lsdconsulting.lsd.distributed.access.model.InteractionType;
import io.lsdconsulting.lsd.distributed.generator.diagram.event.builder.ConsumeMessageBuilder;
import io.lsdconsulting.lsd.distributed.generator.diagram.event.builder.MessageBuilder;
import io.lsdconsulting.lsd.distributed.generator.diagram.event.builder.SynchronousResponseBuilder;

import java.util.HashMap;
import java.util.Map;

import static io.lsdconsulting.lsd.distributed.access.model.InteractionType.*;

public class EventBuilderMap {
    private final IdGenerator idGenerator;
    private final Map<InteractionType, QuintFunction<String, String, String, String, String, String, SequenceEvent>> eventBuilders = new HashMap<>();

    public EventBuilderMap(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
        eventBuilders.put(RESPONSE, new SynchronousResponseBuilder()::build);
        eventBuilders.put(REQUEST, new MessageBuilder()::build);
        eventBuilders.put(PUBLISH, new MessageBuilder()::build);
        eventBuilders.put(CONSUME, new ConsumeMessageBuilder()::build);
    }

    public SequenceEvent build(InteractionType type, String label, String serviceName, String target, String colour, String data) {
        return eventBuilders.get(type).apply(idGenerator.next(), label, serviceName, target, colour, data);
    }
}
