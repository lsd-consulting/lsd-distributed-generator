package io.lsdconsulting.lsd.distributed.generator.diagram.label

import io.lsdconsulting.lsd.distributed.access.model.InteractionType
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction
import java.util.function.Function

class LabelGeneratorMap {
    private val labelGenerators = mutableMapOf<InteractionType, Function<InterceptedInteraction, String>>()

    init {
        labelGenerators[InteractionType.RESPONSE] =
            Function { (_, _, _, _, _, _, _, httpStatus, _, _, _, elapsedTime): InterceptedInteraction -> "sync $httpStatus response ($elapsedTime ms)" }
        labelGenerators[InteractionType.REQUEST] =
            Function { (_, _, _, _, _, _, path, _, httpMethod): InterceptedInteraction -> "$httpMethod $path" }
        labelGenerators[InteractionType.PUBLISH] = Function { "publish event" }
        labelGenerators[InteractionType.CONSUME] = Function { "consume message" }
    }

    fun generate(interceptedInteraction: InterceptedInteraction) =
        labelGenerators[interceptedInteraction.interactionType]!!.apply(interceptedInteraction)
}