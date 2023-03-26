package io.lsdconsulting.lsd.distributed.generator.diagram.dto;

import com.lsd.core.domain.SequenceEvent;
import lombok.Builder;
import lombok.Value;

import java.time.ZonedDateTime;
import java.util.List;

@Value
@Builder
public class EventContainer {
    List<SequenceEvent> events;
    ZonedDateTime startTime;
    ZonedDateTime finishTime;
}
