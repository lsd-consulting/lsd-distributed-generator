package io.lsdconsulting.lsd.distributed.generator.diagram.event.builder;

import com.lsd.core.IdGenerator;
import com.lsd.core.domain.SequenceEvent;
import io.lsdconsulting.lsd.distributed.generator.diagram.event.SequenceEventBuilder;
import org.springframework.stereotype.Component;

import static com.lsd.core.builders.MessageBuilder.messageBuilder;
import static com.lsd.core.domain.MessageType.SYNCHRONOUS;

@Component
public class ConsumeMessageBuilder implements SequenceEventBuilder {
    @Override
    public SequenceEvent build(IdGenerator idGenerator, String label, String serviceName, String target, String colour, String data) {
        return messageBuilder()
                .id(idGenerator.next())
                .type(SYNCHRONOUS)
                .label(label)
                .from(target)
                .to(serviceName)
                .colour(colour)
                .data(data)
                .build();
    }
}
