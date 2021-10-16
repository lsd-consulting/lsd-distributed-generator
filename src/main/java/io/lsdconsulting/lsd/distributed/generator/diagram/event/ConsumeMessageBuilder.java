package io.lsdconsulting.lsd.distributed.generator.diagram.event;

import com.lsd.IdGenerator;
import com.lsd.events.Message;
import com.lsd.events.SequenceEvent;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ConsumeMessageBuilder implements SequenceEventBuilder {
    private final IdGenerator idGenerator;

    @Override
    public SequenceEvent build(String label, String serviceName, String target, String colour, String data) {
        return Message.builder()
                .id(idGenerator.next())
                .label(label)
                .from(target)
                .to(serviceName)
                .colour(colour)
                .data(data)
                .build();
    }
}
