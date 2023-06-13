package io.lsdconsulting.lsd.distributed.generator.diagram

import com.lsd.core.domain.SequenceEvent
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.connector.repository.InterceptedDocumentRepository
import io.lsdconsulting.lsd.distributed.generator.diagram.data.buildDataFrom
import io.lsdconsulting.lsd.distributed.generator.diagram.dto.EventContainer
import io.lsdconsulting.lsd.distributed.generator.diagram.event.EventBuilderMap
import io.lsdconsulting.lsd.distributed.generator.diagram.label.generateLabel
import lsd.format.PrettyPrinter

class InteractionGenerator(
    private val interceptedDocumentRepository: InterceptedDocumentRepository,
    private val eventBuilderMap: EventBuilderMap,
) {
    fun generate(traceIdToColourMap: Map<String, String?>): EventContainer {
        val traceIds = traceIdToColourMap.keys.toTypedArray()
        val interactions = interceptedDocumentRepository.findByTraceIds(*traceIds)
        return EventContainer(
            events = getEvents(traceIdToColourMap, interactions),
            startTime = interactions.minOfOrNull { it.createdAt },
            finishTime = interactions.maxOfOrNull { it.createdAt }
        )
    }

    private fun getEvents(
        traceIdToColourMap: Map<String, String?>,
        interactions: List<InterceptedInteraction>
    ): List<SequenceEvent> = interactions.map { interceptedInteraction: InterceptedInteraction ->
        val colour = traceIdToColourMap[interceptedInteraction.traceId] ?: NO_COLOUR
        val data = PrettyPrinter.prettyPrintJson(buildDataFrom(interceptedInteraction))
        val serviceName = interceptedInteraction.serviceName
        val target = interceptedInteraction.target
        val type = interceptedInteraction.interactionType
        val label = generateLabel(interceptedInteraction)
        eventBuilderMap.build(type, label, serviceName, target, colour, data)
    }

    companion object {
        const val NO_COLOUR = ""
    }
}
