package io.lsdconsulting.lsd.distributed.generator.diagram

import com.lsd.core.IdGenerator
import com.lsd.core.LsdContext
import com.lsd.core.properties.LsdProperties
import io.lsdconsulting.lsd.distributed.connector.repository.InterceptedDocumentRepository
import io.lsdconsulting.lsd.distributed.generator.diagram.event.EventBuilderMap
import io.lsdconsulting.lsd.distributed.http.repository.InterceptedDocumentHttpRepository
import io.lsdconsulting.lsd.distributed.mongo.repository.InterceptedDocumentMongoRepository
import io.lsdconsulting.lsd.distributed.mongo.repository.InterceptedInteractionCollectionBuilder
import io.lsdconsulting.lsd.distributed.postgres.repository.InterceptedDocumentPostgresRepository
import lsd.format.json.createObjectMapper

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

    companion object {
        fun instance() : LsdLogger {
            val connectionString = LsdProperties["lsd.dist.connectionString"]
            val repository: InterceptedDocumentRepository = when {
                connectionString.startsWith("jdbc:postgresql://") -> InterceptedDocumentPostgresRepository(connectionString, createObjectMapper())
                connectionString.startsWith("mongodb://") -> InterceptedDocumentMongoRepository(
                    InterceptedInteractionCollectionBuilder(connectionString, null, null, LsdProperties.getInt("lsd.dist.db.connectionTimeout.millis"),
                        LsdProperties.getInt("lsd.dist.db.collectionSizeLimit.megabytes").toLong()
                    )
                )
                connectionString.startsWith("http") -> InterceptedDocumentHttpRepository(connectionString, LsdProperties.getInt("lsd.dist.http.connectionTimeout.millis"), createObjectMapper())
                else -> throw IllegalArgumentException("Wrong connectionString value!")
            }
            val idGenerator = IdGenerator(LsdProperties.getBoolean("lsd.core.ids.deterministic"))
            val eventBuilderMap = EventBuilderMap(idGenerator)
            val interactionGenerator = InteractionGenerator(repository, eventBuilderMap)
            return LsdLogger(interactionGenerator)
        }
    }
}
