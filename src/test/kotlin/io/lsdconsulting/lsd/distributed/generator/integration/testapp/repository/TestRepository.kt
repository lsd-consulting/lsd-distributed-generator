package io.lsdconsulting.lsd.distributed.generator.integration.testapp.repository

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import de.flapdoodle.embed.mongo.MongodExecutable
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.MongodConfig
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.process.runtime.Network
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.generator.integration.testapp.config.log
import io.lsdconsulting.lsd.distributed.mongo.repository.codec.InteractionTypeCodec
import io.lsdconsulting.lsd.distributed.mongo.repository.codec.ZonedDateTimeCodec
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import org.bson.types.ObjectId
import java.io.IOException

class TestRepository {
    private val pojoCodecRegistry = CodecRegistries.fromRegistries(
        MongoClientSettings.getDefaultCodecRegistry(),
        CodecRegistries.fromCodecs(ZonedDateTimeCodec(), InteractionTypeCodec()),
        CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
    )

    fun save(interceptedInteraction: InterceptedInteraction) {
        val database = mongoClient.getDatabase(DATABASE_NAME)
        val collection = database.getCollection(COLLECTION_NAME).withCodecRegistry(pojoCodecRegistry)
        val document = Document("_id", ObjectId())
        document.append("traceId", interceptedInteraction.traceId)
            .append("body", interceptedInteraction.body)
            .append("requestHeaders", interceptedInteraction.requestHeaders)
            .append("responseHeaders", interceptedInteraction.responseHeaders)
            .append("serviceName", interceptedInteraction.serviceName)
            .append("target", interceptedInteraction.target)
            .append("path", interceptedInteraction.path)
            .append("httpStatus", interceptedInteraction.httpStatus)
            .append("httpMethod", interceptedInteraction.httpMethod)
            .append("interactionType", interceptedInteraction.interactionType)
            .append("profile", interceptedInteraction.profile)
            .append("elapsedTime", interceptedInteraction.elapsedTime)
            .append("createdAt", interceptedInteraction.createdAt)
        collection.insertOne(document)
    }

    companion object {
        const val MONGODB_HOST = "localhost"
        const val MONGODB_PORT = 27017
        private const val DATABASE_NAME = "lsd"
        private const val COLLECTION_NAME = "interceptedInteraction"
        private lateinit var mongoClient: MongoClient
        private lateinit var mongodExecutable: MongodExecutable

        fun setupDatabase() {
            try {
                val mongodConfig: MongodConfig = MongodConfig.builder()
                    .version(Version.Main.V5_0)
                    .net(Net(MONGODB_HOST, MONGODB_PORT, Network.localhostIsIPv6()))
                    .build()
                mongodExecutable = MongodStarter.getDefaultInstance().prepare(mongodConfig)
                mongodExecutable.start()
                mongoClient = MongoClients.create(
                    MongoClientSettings.builder()
                        .applyConnectionString(ConnectionString("mongodb://$MONGODB_HOST:$MONGODB_PORT"))
                        .retryWrites(true)
                        .build()
                )
            } catch (e: IOException) {
                log().error(e.message, e)
            }
        }

        fun tearDownDatabase() {
            mongoClient.close()
            mongodExecutable.stop()
        }
    }
}