package io.lsdconsulting.lsd.distributed.generator.diagram.event;

import com.lsd.core.domain.SequenceEvent;

public interface SequenceEventBuilder {
    SequenceEvent build(String label, String serviceName, String target, String colour, String data);
}
