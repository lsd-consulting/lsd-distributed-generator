package io.lsdconsulting.lsd.distributed.generator.diagram

import com.lsd.core.LsdContext
import java.util.*

class LsdLogger(
    private val interactionGenerator: InteractionGenerator
) {
    fun captureInteractionsFromDatabase(lsdContext: LsdContext, vararg traceIds: String) {
        val traceIdToColourMap = mutableMapOf<String, Optional<String>>()
        traceIds.forEach { traceIdToColourMap[it] = Optional.empty() }
        captureInteractionsFromDatabase(lsdContext, traceIdToColourMap)
    }

    fun captureInteractionsFromDatabase(lsdContext: LsdContext, traceIdToColourMap: Map<String, Optional<String>>) {
        interactionGenerator.generate(traceIdToColourMap).events.forEach { interaction ->
            lsdContext.capture(interaction)
        }
    }
}
