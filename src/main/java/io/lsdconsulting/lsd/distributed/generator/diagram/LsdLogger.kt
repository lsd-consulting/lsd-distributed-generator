package io.lsdconsulting.lsd.distributed.generator.diagram

import com.lsd.core.LsdContext
import java.util.*

class LsdLogger(
    private val interactionGenerator: InteractionGenerator,
    private val lsdContext: LsdContext
) {
    fun captureInteractionsFromDatabase(vararg traceIds: String) {
        val traceIdToColourMap = mutableMapOf<String, Optional<String>>()
        traceIds.forEach { traceIdToColourMap[it] = Optional.empty() }
        captureInteractionsFromDatabase(traceIdToColourMap)
    }

    fun captureInteractionsFromDatabase(traceIdToColourMap: Map<String, Optional<String>>) {
        interactionGenerator.generate(traceIdToColourMap).events.forEach { interaction ->
            lsdContext.capture(interaction)
        }
    }
}
