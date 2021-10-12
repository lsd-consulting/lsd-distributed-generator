package io.lsdconsulting.lsd.distributed.diagram.event;

import com.lsd.events.SequenceEvent;
import io.lsdconsulting.lsd.distributed.model.Type;

import java.util.HashMap;
import java.util.Map;

import static io.lsdconsulting.lsd.distributed.model.Type.*;

public class EventBuilderMap {
    private final Map<Type, QuintFunction<String, String, String, String, String, SequenceEvent>> eventBuilders = new HashMap<>();

    public EventBuilderMap(MessageBuilder messageBuilder, SynchronousResponseBuilder synchronousResponseBuilder, ConsumeMessageBuilder consumeMessageBuilder) {
        eventBuilders.put(RESPONSE, synchronousResponseBuilder::build);
        eventBuilders.put(REQUEST, messageBuilder::build);
        eventBuilders.put(PUBLISH, messageBuilder::build);
        eventBuilders.put(CONSUME, consumeMessageBuilder::build);
    }

    public SequenceEvent build(Type type, String label, String serviceName, String target, String colour, String data) {
        return eventBuilders.get(type).apply(label, serviceName, target, colour, data);
    }
}
