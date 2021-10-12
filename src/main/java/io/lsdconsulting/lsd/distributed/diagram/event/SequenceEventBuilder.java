package io.lsdconsulting.lsd.distributed.diagram.event;

import com.lsd.events.SequenceEvent;

public interface SequenceEventBuilder {
    SequenceEvent build(String label, String serviceName, String target, String colour, String data);
}
