package io.lsdconsulting.lsd.distributed.generator.diagram

import com.lsd.core.domain.SequenceEvent
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.access.repository.InterceptedDocumentRepository
import io.lsdconsulting.lsd.distributed.generator.diagram.data.buildDataFrom
import io.lsdconsulting.lsd.distributed.generator.diagram.dto.EventContainer
import io.lsdconsulting.lsd.distributed.generator.diagram.event.EventBuilderMap
import io.lsdconsulting.lsd.distributed.generator.diagram.label.LabelGeneratorMap
import lsd.format.PrettyPrinter
import java.util.*

class InteractionGenerator(
    private val interceptedDocumentRepository: InterceptedDocumentRepository,
    private val eventBuilderMap: EventBuilderMap,
    private val labelGeneratorMap: LabelGeneratorMap,
) {
    fun generate(traceIdToColourMap: Map<String, Optional<String>>): EventContainer {
        val traceIds = traceIdToColourMap.keys.toTypedArray()
        val interactions = interceptedDocumentRepository.findByTraceIds(*traceIds)
        return EventContainer(
            events = getEvents(traceIdToColourMap, interactions),
            startTime = interactions.minOfOrNull { it.createdAt },
            finishTime = interactions.maxOfOrNull { it.createdAt }
        )
    }

    private fun getEvents(
        traceIdToColourMap: Map<String, Optional<String>>,
        interactions: List<InterceptedInteraction>
    ): List<SequenceEvent> = interactions.map { interceptedInteraction: InterceptedInteraction ->
        val colour = Optional.ofNullable(
            traceIdToColourMap[interceptedInteraction.traceId]
        ).flatMap { x: Optional<String>? -> x }.orElse(NO_COLOUR)
        val data = PrettyPrinter.prettyPrintJson(buildDataFrom(interceptedInteraction))
        val serviceName = interceptedInteraction.serviceName
        val target = interceptedInteraction.target
        val type = interceptedInteraction.interactionType
        val label = labelGeneratorMap.generate(interceptedInteraction)
        eventBuilderMap.build(type, label, serviceName!!, target!!, colour, data)
    }

    companion object {
        const val NO_COLOUR = ""
    }
}
