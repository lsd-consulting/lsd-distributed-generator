package io.lsdconsulting.lsd.distributed.generator.diagram

import com.lsd.core.IdGenerator
import com.lsd.core.LsdContext
import com.lsd.core.properties.LsdProperties
import io.lsdconsulting.lsd.distributed.connector.repository.InterceptedDocumentRepository
import io.lsdconsulting.lsd.distributed.generator.diagram.event.EventBuilderMap
import io.lsdconsulting.lsd.distributed.http.config.CONNECTION_TIMEOUT_MILLIS_DEFAULT
import io.lsdconsulting.lsd.distributed.http.repository.InterceptedDocumentHttpRepository
import io.lsdconsulting.lsd.distributed.mongo.repository.DEFAULT_COLLECTION_SIZE_LIMIT_MBS
import io.lsdconsulting.lsd.distributed.mongo.repository.DEFAULT_TIMEOUT_MILLIS
import io.lsdconsulting.lsd.distributed.mongo.repository.InterceptedDocumentMongoRepository
import io.lsdconsulting.lsd.distributed.mongo.repository.InterceptedInteractionCollectionBuilder
import io.lsdconsulting.lsd.distributed.postgres.config.*
import io.lsdconsulting.lsd.distributed.postgres.repository.InterceptedDocumentPostgresRepository
import lsd.format.json.createObjectMapper
import java.lang.Boolean.FALSE

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
        fun instance(): LsdLogger {
            val connectionString = LsdProperties["lsd.dist.connectionString"]
            val repository = buildInterceptedDocumentRepository(connectionString)
            val idGenerator = IdGenerator(LsdProperties.getBoolean("lsd.core.ids.deterministic", FALSE))
            val eventBuilderMap = EventBuilderMap(idGenerator)
            val interactionGenerator = InteractionGenerator(repository, eventBuilderMap)
            return LsdLogger(interactionGenerator)
        }

        private fun buildInterceptedDocumentRepository(connectionString: String): InterceptedDocumentRepository {
            val repository: InterceptedDocumentRepository = when {
                connectionString.startsWith("jdbc:postgresql://") -> InterceptedDocumentPostgresRepository(
                    connectionString,
                    createObjectMapper(),
                    false,1L,
                    LsdProperties.getInt("lsd.dist.db.traceIdMaxLength", DEFAULT_TRACE_ID_MAX_LENGTH),
                    LsdProperties.getInt("lsd.dist.db.bodyMaxLength", DEFAULT_BODY_MAX_LENGTH),
                    LsdProperties.getInt("lsd.dist.db.requestHeadersMaxLength", DEFAULT_REQUEST_HEADERS_MAX_LENGTH),
                    LsdProperties.getInt("lsd.dist.db.responseHeadersMaxLength", DEFAULT_RESPONSE_HEADERS_MAX_LENGTH),
                    LsdProperties.getInt("lsd.dist.db.serviceNameMaxLength", DEFAULT_SERVICE_NAME_MAX_LENGTH),
                    LsdProperties.getInt("lsd.dist.db.targetMaxLength", DEFAULT_TARGET_MAX_LENGTH),
                    LsdProperties.getInt("lsd.dist.db.pathMaxLength", DEFAULT_PATH_MAX_LENGTH),
                    LsdProperties.getInt("lsd.dist.db.httpStatusMaxLength", DEFAULT_HTTP_STATUS_MAX_LENGTH),
                    LsdProperties.getInt("lsd.dist.db.httpMethodMaxLength", DEFAULT_HTTP_METHOD_MAX_LENGTH),
                    LsdProperties.getInt("lsd.dist.db.profileMaxLength", DEFAULT_PROFILE_MAX_LENGTH),
                )

                connectionString.startsWith("mongodb://") -> InterceptedDocumentMongoRepository(
                    InterceptedInteractionCollectionBuilder(
                        connectionString,
                        null,
                        null,
                        LsdProperties.getInt("lsd.dist.db.connectionTimeout.millis", DEFAULT_TIMEOUT_MILLIS),
                        LsdProperties.getLong("lsd.dist.db.collectionSizeLimit.megabytes", DEFAULT_COLLECTION_SIZE_LIMIT_MBS),
                    )
                )

                connectionString.startsWith("http") -> InterceptedDocumentHttpRepository(
                    connectionString,
                    LsdProperties.getInt("lsd.dist.http.connectionTimeout.millis", CONNECTION_TIMEOUT_MILLIS_DEFAULT),
                    createObjectMapper()
                )

                else -> throw IllegalArgumentException("Wrong connectionString value!")
            }
            return repository
        }
    }
}
