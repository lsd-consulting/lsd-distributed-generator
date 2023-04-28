package io.lsdconsulting.lsd.distributed.generator.diagram

import com.lsd.core.LsdContext

class LsdLogger(
    private val interactionGenerator: InteractionGenerator
) {
    fun captureInteractionsFromDatabase(lsdContext: LsdContext, vararg traceIds: String) {
        val traceIdToColourMap = mutableMapOf<String, String?>()
        traceIds.forEach { traceIdToColourMap[it] = null }
        captureInteractionsFromDatabase(lsdContext, traceIdToColourMap)
    }

    fun captureInteractionsFromDatabase(lsdContext: LsdContext, traceIdToColourMap: Map<String, String?>) {
        interactionGenerator.generate(traceIdToColourMap).events.forEach { interaction ->
            lsdContext.capture(interaction)
        }
    }
}
