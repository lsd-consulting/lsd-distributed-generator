package io.lsdconsulting.lsd.distributed.generator.diagram.label

import io.lsdconsulting.lsd.distributed.connector.model.InteractionType.*
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction

fun generateLabel(interceptedInteraction: InterceptedInteraction) =
    when (interceptedInteraction.interactionType) {
        RESPONSE -> "sync " + interceptedInteraction.httpStatus + " response (" + interceptedInteraction.elapsedTime + " ms)"
        REQUEST -> interceptedInteraction.httpMethod + " " + interceptedInteraction.path
        PUBLISH -> "publish event"
        CONSUME -> "consume message"
    }

