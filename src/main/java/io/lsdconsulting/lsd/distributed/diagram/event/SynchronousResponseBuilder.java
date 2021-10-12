package io.lsdconsulting.lsd.distributed.diagram.event;

import com.lsd.IdGenerator;
import com.lsd.events.SequenceEvent;
import com.lsd.events.SynchronousResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class SynchronousResponseBuilder implements SequenceEventBuilder {
    private final IdGenerator idGenerator;

    @Override
    public SequenceEvent build(String label, String serviceName, String target, String colour, String data) {
        return SynchronousResponse.builder()
                .id(idGenerator.next())
                .label(label)
                .from(target)
                .to(serviceName)
                .colour(colour)
                .data(data)
                .build();
    }
}
