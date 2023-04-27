package io.lsdconsulting.lsd.distributed.generator.diagram.event.builder

import com.lsd.core.IdGenerator
import com.lsd.core.builders.MessageBuilder.Companion.messageBuilder
import com.lsd.core.domain.MessageType
import io.lsdconsulting.lsd.distributed.generator.diagram.event.SequenceEventBuilder
import org.springframework.stereotype.Component

@Component
class ConsumeMessageBuilder : SequenceEventBuilder {
    override fun build(
        idGenerator: IdGenerator,
        label: String,
        serviceName: String,
        target: String,
        colour: String,
        data: String
    ) = messageBuilder()
        .id(idGenerator.next())
        .type(MessageType.SYNCHRONOUS)
        .label(label)
        .from(target)
        .to(serviceName)
        .colour(colour)
        .data(data)
        .build()
}