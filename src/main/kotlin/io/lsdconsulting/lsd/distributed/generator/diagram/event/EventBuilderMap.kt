package io.lsdconsulting.lsd.distributed.generator.diagram.event

import com.lsd.core.IdGenerator
import com.lsd.core.domain.Message
import io.lsdconsulting.lsd.distributed.connector.model.InteractionType
import io.lsdconsulting.lsd.distributed.connector.model.InteractionType.*
import io.lsdconsulting.lsd.distributed.generator.diagram.event.builder.buildConsumeMessage
import io.lsdconsulting.lsd.distributed.generator.diagram.event.builder.buildSynchronousMessage
import io.lsdconsulting.lsd.distributed.generator.diagram.event.builder.buildSynchronousResponse
import java.time.Duration


data class CapturedData(
    val type: InteractionType,
    val label: String,
    val serviceName: String,
    val target: String,
    val colour: String,
    val data: String,
    val duration: Duration
)

data class MessageData(val id: String, val captured: CapturedData)

typealias MessageMapper = (MessageData) -> Message

class EventBuilderMap(private val idGenerator: IdGenerator) {
    fun build(capturedData: CapturedData): Message {
        val messageBuilder = messageBuilderFor(interactionType = capturedData.type)
        return messageBuilder(
            MessageData(
                id = idGenerator.next(),
                captured = capturedData
            )
        )
    }

    private fun messageBuilderFor(interactionType: InteractionType): MessageMapper =
        when (interactionType) {
            REQUEST, PUBLISH -> ::buildSynchronousMessage
            RESPONSE -> ::buildSynchronousResponse
            CONSUME -> ::buildConsumeMessage
        }
}
