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
    private final Map<InteractionType, QuintFunction<IdGenerator, String, String, String, String, String, SequenceEvent>> eventBuilders = new HashMap<>();

    public EventBuilderMap(IdGenerator idGenerator, MessageBuilder messageBuilder, SynchronousResponseBuilder synchronousResponseBuilder, ConsumeMessageBuilder consumeMessageBuilder) {
        this.idGenerator = idGenerator;
        eventBuilders.put(RESPONSE, synchronousResponseBuilder::build);
        eventBuilders.put(REQUEST, messageBuilder::build);
        eventBuilders.put(PUBLISH, messageBuilder::build);
        eventBuilders.put(CONSUME, consumeMessageBuilder::build);
    }

    public SequenceEvent build(InteractionType type, String label, String serviceName, String target, String colour, String data) {
        return eventBuilders.get(type).apply(idGenerator, label, serviceName, target, colour, data);
    }
}
