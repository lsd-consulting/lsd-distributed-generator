package io.lsdconsulting.lsd.distributed.generator.diagram.dto

import com.lsd.core.domain.SequenceEvent
import java.time.ZonedDateTime

data class EventContainer(
    var events: List<SequenceEvent>,
    var startTime: ZonedDateTime? = null,
    var finishTime: ZonedDateTime? = null,
)