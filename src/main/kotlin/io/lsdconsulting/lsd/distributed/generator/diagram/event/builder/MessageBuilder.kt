package io.lsdconsulting.lsd.distributed.generator.diagram.event.builder

import com.lsd.core.builders.MessageBuilder.Companion.messageBuilder
import com.lsd.core.domain.Message
import com.lsd.core.domain.MessageType
import io.lsdconsulting.lsd.distributed.generator.diagram.event.MessageData

fun buildSynchronousMessage(request: MessageData): Message =
    with(request) {
        messageBuilder()
            .id(id)
            .created(captured.created)
            .type(MessageType.SYNCHRONOUS)
            .label(captured.label)
            .from(captured.serviceName)
            .to(captured.target)
            .colour(captured.colour)
            .data(captured.data)
            .duration(captured.duration)
            .build()
    }
