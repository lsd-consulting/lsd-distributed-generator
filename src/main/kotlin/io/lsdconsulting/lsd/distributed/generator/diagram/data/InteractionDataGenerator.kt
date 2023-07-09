package io.lsdconsulting.lsd.distributed.generator.diagram.data

import com.fasterxml.jackson.annotation.JsonInclude
import io.lsdconsulting.lsd.distributed.connector.model.InteractionType.*
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import lsd.format.convertJsonStringToMap

fun buildDataFrom(interceptedInteraction: InterceptedInteraction) =
    InteractionData(
        requestHeaders = (if (interceptedInteraction.interactionType == REQUEST) interceptedInteraction.requestHeaders else null),
        responseHeaders = (if (interceptedInteraction.interactionType == RESPONSE) interceptedInteraction.responseHeaders else null),
        headers = (
                if (listOf(PUBLISH, CONSUME)
                        .contains(interceptedInteraction.interactionType)
                ) interceptedInteraction.requestHeaders else null
                ),
        body = (generateBody(interceptedInteraction.body))
    )

private fun generateBody(body: String?): Any? = convertJsonStringToMap(body).ifEmpty { body }

data class InteractionData(
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    var requestHeaders: Map<String, Collection<String>>?,

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    var responseHeaders: Map<String, Collection<String>>?,

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    var headers: Map<String, Collection<String>>?,
    var body: Any? = null
)
