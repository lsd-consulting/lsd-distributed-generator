package io.lsdconsulting.lsd.distributed.generator.diagram.event.builder

import com.lsd.core.builders.MessageBuilder.Companion.messageBuilder
import com.lsd.core.domain.MessageType
import io.lsdconsulting.lsd.distributed.generator.diagram.event.SequenceEventBuilder

class ConsumeMessageBuilder : SequenceEventBuilder {
    override fun build(
        id: String,
        label: String,
        serviceName: String,
        target: String,
        colour: String,
        data: String
    ) = messageBuilder()
        .id(id)
        .type(MessageType.SYNCHRONOUS)
        .label(label)
        .from(target)
        .to(serviceName)
        .colour(colour)
        .data(data)
        .build()
}