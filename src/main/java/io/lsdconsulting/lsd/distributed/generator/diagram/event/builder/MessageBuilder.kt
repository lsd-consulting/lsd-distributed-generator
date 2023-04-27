package io.lsdconsulting.lsd.distributed.generator.diagram.event.builder

import com.lsd.core.builders.MessageBuilder.Companion.messageBuilder
import com.lsd.core.domain.MessageType

fun buildMessage(
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
    .from(serviceName)
    .to(target)
    .colour(colour)
    .data(data)
    .build()
