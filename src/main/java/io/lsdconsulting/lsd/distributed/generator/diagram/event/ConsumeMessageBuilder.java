package io.lsdconsulting.lsd.distributed.generator.diagram.event;

import com.lsd.core.IdGenerator;
import com.lsd.core.domain.MessageType;
import com.lsd.core.domain.SequenceEvent;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import static com.lsd.core.builders.MessageBuilder.messageBuilder;

@Component
@AllArgsConstructor
public class ConsumeMessageBuilder implements SequenceEventBuilder {
    private final IdGenerator idGenerator;

    @Override
    public SequenceEvent build(String label, String serviceName, String target, String colour, String data) {
        return messageBuilder()
                .id(idGenerator.next())
                .label(label)
                .from(target)
                .to(serviceName)
                .colour(colour)
                .data(data)
                .type(MessageType.SYNCHRONOUS)
                .build();
    }
}
