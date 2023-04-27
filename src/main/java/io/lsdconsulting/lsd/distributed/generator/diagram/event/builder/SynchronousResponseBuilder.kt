package io.lsdconsulting.lsd.distributed.generator.diagram.event.builder

import com.lsd.core.builders.MessageBuilder.Companion.messageBuilder
import com.lsd.core.domain.MessageType

fun buildSynchronousResponse(
    id: String,
    label: String,
    serviceName: String,
    target: String,
    colour: String,
    data: String
) = messageBuilder()
    .id(id)
    .type(MessageType.SYNCHRONOUS_RESPONSE)
    .label(label)
    .from(target)
    .to(serviceName)
    .colour(colour)
    .data(data)
    .build()
