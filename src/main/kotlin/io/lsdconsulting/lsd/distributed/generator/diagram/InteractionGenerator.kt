package io.lsdconsulting.lsd.distributed.generator.diagram

import com.lsd.core.domain.SequenceEvent
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.connector.repository.InterceptedDocumentRepository
import io.lsdconsulting.lsd.distributed.generator.diagram.dto.EventContainer
import io.lsdconsulting.lsd.distributed.generator.diagram.event.CapturedData
import io.lsdconsulting.lsd.distributed.generator.diagram.event.EventBuilderMap
import io.lsdconsulting.lsd.distributed.generator.diagram.label.generateLabel
import io.lsdconsulting.lsd.distributed.generator.diagram.render.renderHtmlFor
import lsd.format.prettyPrint
import java.time.Duration


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
    ): List<SequenceEvent> = interactions.map { interaction: InterceptedInteraction ->
        eventBuilderMap.build(
            CapturedData(
                created = interaction.createdAt.toInstant(),
                type = interaction.interactionType,
                label = generateLabel(interaction),
                serviceName = interaction.serviceName,
                target = interaction.target,
                colour = traceIdToColourMap[interaction.traceId] ?: NO_COLOUR,
                data = renderHtmlFor(
                    path = interaction.path,
                    requestHeaders = interaction.requestHeaders,
                    responseHeaders = interaction.responseHeaders,
                    prettyBody = prettyPrint(interaction.body),
                    duration = interaction.elapsedTime,
                    type = interaction.interactionType
                ),
                duration = Duration.ofMillis(interaction.elapsedTime)
            )
        )
    }

    companion object {
        const val NO_COLOUR = ""
    }
}
