package io.lsdconsulting.lsd.distributed.generator.diagram.event

//import io.lsdconsulting.lsd.distributed.generator.diagram.event.builder.MessageBuilder
//import io.lsdconsulting.lsd.distributed.generator.diagram.event.builder.SynchronousResponseBuilder
import com.lsd.core.IdGenerator
import com.lsd.core.domain.Message
import io.lsdconsulting.lsd.distributed.access.model.InteractionType
import io.lsdconsulting.lsd.distributed.access.model.InteractionType.*
import io.lsdconsulting.lsd.distributed.generator.diagram.event.builder.buildConsumeMessage
import io.lsdconsulting.lsd.distributed.generator.diagram.event.builder.buildMessage
import io.lsdconsulting.lsd.distributed.generator.diagram.event.builder.buildSynchronousResponse

class EventBuilderMap(private val idGenerator: IdGenerator) {
    fun build(
        type: InteractionType,
        label: String,
        serviceName: String,
        target: String,
        colour: String,
        data: String
    ) = getFunction(type).invoke(idGenerator.next(), label, serviceName, target, colour, data)

    private fun getFunction(type: InteractionType): (String, String, String, String, String, String) -> Message {
        return when (type) {
            REQUEST, PUBLISH -> ::buildMessage
            RESPONSE -> ::buildSynchronousResponse
            CONSUME -> ::buildConsumeMessage
        }
    }
}
