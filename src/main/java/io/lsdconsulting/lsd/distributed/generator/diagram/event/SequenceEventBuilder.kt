package io.lsdconsulting.lsd.distributed.generator.diagram.event

import com.lsd.core.IdGenerator
import com.lsd.core.domain.SequenceEvent

interface SequenceEventBuilder {
    fun build(idGenerator: IdGenerator, label: String, serviceName: String, target: String, colour: String, data: String): SequenceEvent
}