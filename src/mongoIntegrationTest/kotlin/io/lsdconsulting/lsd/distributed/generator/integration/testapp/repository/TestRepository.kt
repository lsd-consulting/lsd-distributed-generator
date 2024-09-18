package io.lsdconsulting.lsd.distributed.generator.integration.testapp.repository

import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClients
import io.lsdconsulting.lsd.distributed.connector.model.InterceptedInteraction
import io.lsdconsulting.lsd.distributed.mongo.repository.codec.InteractionTypeCodec
import io.lsdconsulting.lsd.distributed.mongo.repository.codec.ZonedDateTimeCodec
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import org.bson.types.ObjectId


private const val DATABASE_NAME = "lsd"
private const val COLLECTION_NAME = "interceptedInteraction"

class TestRepository {

    private val pojoCodecRegistry = CodecRegistries.fromRegistries(
        MongoClientSettings.getDefaultCodecRegistry(),
        CodecRegistries.fromCodecs(ZonedDateTimeCodec(), InteractionTypeCodec()),
        CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
    )

    fun save(interceptedInteraction: InterceptedInteraction) {
        val uri = TestContainersMongoTest.mongoDBContainer.connectionString
        val mongoClient = MongoClients.create(uri)
        val database = mongoClient.getDatabase(DATABASE_NAME)
        val collection = database.getCollection(COLLECTION_NAME).withCodecRegistry(pojoCodecRegistry)
        val document = Document("_id", ObjectId()).apply {
            append("traceId", interceptedInteraction.traceId)
            append("body", interceptedInteraction.body)
            append("requestHeaders", interceptedInteraction.requestHeaders)
            append("responseHeaders", interceptedInteraction.responseHeaders)
            append("serviceName", interceptedInteraction.serviceName)
            append("target", interceptedInteraction.target)
            append("path", interceptedInteraction.path)
            append("httpStatus", interceptedInteraction.httpStatus)
            append("httpMethod", interceptedInteraction.httpMethod)
            append("interactionType", interceptedInteraction.interactionType)
            append("profile", interceptedInteraction.profile)
            append("elapsedTime", interceptedInteraction.elapsedTime)
            append("createdAt", interceptedInteraction.createdAt)
        }
        collection.insertOne(document)
    }

    fun deleteAll() {
        val uri = TestContainersMongoTest.mongoDBContainer.connectionString
        val mongoClient = MongoClients.create(uri)
        val database = mongoClient.getDatabase(DATABASE_NAME)
        val collection = database.getCollection(COLLECTION_NAME).withCodecRegistry(pojoCodecRegistry)
        collection.drop()
    }
}
