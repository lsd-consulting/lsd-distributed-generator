package io.lsdconsulting.lsd.distributed.generator.diagram.event;

import com.lsd.core.IdGenerator;
import com.lsd.core.domain.ComponentName;
import com.lsd.core.domain.Message;
import com.lsd.core.domain.MessageType;
import com.lsd.core.domain.SequenceEvent;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class MessageBuilder implements SequenceEventBuilder {
    private final IdGenerator idGenerator;

    @Override
    public SequenceEvent build(String label, String serviceName, String target, String colour, String data) {
        return new Message(
                idGenerator.next(),
                new ComponentName(serviceName),
                new ComponentName(target),
                label,
                MessageType.SYNCHRONOUS,
                colour,
                data);
    }
}
