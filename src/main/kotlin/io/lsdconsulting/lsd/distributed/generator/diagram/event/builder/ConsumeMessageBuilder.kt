package io.lsdconsulting.lsd.distributed.generator.diagram.event.builder

import com.lsd.core.builders.MessageBuilder.Companion.messageBuilder
import com.lsd.core.domain.Message
import com.lsd.core.domain.MessageType
import io.lsdconsulting.lsd.distributed.generator.diagram.event.MessageData

fun buildConsumeMessage(request: MessageData): Message =
    with(request) {
        messageBuilder()
            .id(id)
            .type(MessageType.SYNCHRONOUS)
            .label(captured.label)
            .from(captured.target)
            .to(captured.serviceName)
            .colour(captured.colour)
            .data(captured.data)
            .duration(captured.duration)
            .build()
    }
