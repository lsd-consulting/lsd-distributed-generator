package io.lsdconsulting.lsd.distributed.generator.diagram.event

import com.lsd.core.domain.SequenceEvent

interface SequenceEventBuilder {
    fun build(id: String, label: String, serviceName: String, target: String, colour: String, data: String): SequenceEvent
}