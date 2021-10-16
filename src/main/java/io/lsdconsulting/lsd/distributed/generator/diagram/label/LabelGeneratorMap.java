package io.lsdconsulting.lsd.distributed.generator.diagram.label;

import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction;
import io.lsdconsulting.lsd.distributed.access.model.Type;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static io.lsdconsulting.lsd.distributed.access.model.Type.*;

public class LabelGeneratorMap {
    private final Map<Type, Function<InterceptedInteraction, String>> labelGenerators = new HashMap<>();

    public LabelGeneratorMap() {
        labelGenerators.put(RESPONSE, x -> "sync " + x.getHttpStatus() + " response (" + x.getElapsedTime() + " ms)");
        labelGenerators.put(REQUEST, x -> x.getHttpMethod() + " " + x.getPath());
        labelGenerators.put(PUBLISH, x -> "publish event");
        labelGenerators.put(CONSUME, x -> "consume message");
    }

    public String generate(InterceptedInteraction interceptedInteraction) {
        return labelGenerators.get(interceptedInteraction.getType()).apply(interceptedInteraction);
    }
}
