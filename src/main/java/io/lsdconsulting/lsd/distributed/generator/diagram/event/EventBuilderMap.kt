package io.lsdconsulting.lsd.distributed.generator.diagram.event

import com.lsd.core.IdGenerator
import com.lsd.core.domain.SequenceEvent
import io.lsdconsulting.lsd.distributed.access.model.InteractionType
import io.lsdconsulting.lsd.distributed.access.model.InteractionType.*
import io.lsdconsulting.lsd.distributed.generator.diagram.event.builder.ConsumeMessageBuilder
import io.lsdconsulting.lsd.distributed.generator.diagram.event.builder.MessageBuilder
import io.lsdconsulting.lsd.distributed.generator.diagram.event.builder.SynchronousResponseBuilder

class EventBuilderMap(private val idGenerator: IdGenerator) {
    private val eventBuilders = mutableMapOf<InteractionType, QuintFunction<String, String, String, String, String, String, SequenceEvent>>()

    init {
        eventBuilders[RESPONSE] =
            QuintFunction { id: String, label: String, serviceName: String, target: String, colour: String, data: String ->
                SynchronousResponseBuilder().build(id, label, serviceName, target, colour, data)
            }
        eventBuilders[REQUEST] =
            QuintFunction { id: String, label: String, serviceName: String, target: String, colour: String, data: String ->
                MessageBuilder().build(id, label, serviceName, target, colour, data)
            }
        eventBuilders[PUBLISH] =
            QuintFunction { id: String, label: String, serviceName: String, target: String, colour: String, data: String ->
                MessageBuilder().build(id, label, serviceName, target, colour, data)
            }
        eventBuilders[CONSUME] =
            QuintFunction { id: String, label: String, serviceName: String, target: String, colour: String, data: String ->
                ConsumeMessageBuilder().build(id, label, serviceName, target, colour, data)
            }
    }

    fun build(
        type: InteractionType,
        label: String,
        serviceName: String,
        target: String,
        colour: String,
        data: String
    ): SequenceEvent {
        return eventBuilders[type]!!.apply(idGenerator.next(), label, serviceName, target, colour, data)
    }
}
