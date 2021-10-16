package io.lsdconsulting.lsd.distributed.generator.integration.testapp.repository;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction;
import io.lsdconsulting.lsd.distributed.mongo.repository.codec.TypeCodec;
import io.lsdconsulting.lsd.distributed.mongo.repository.codec.ZonedDateTimeCodec;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.types.ObjectId;

import java.io.IOException;

import static com.mongodb.MongoClientSettings.builder;
import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static de.flapdoodle.embed.mongo.distribution.Version.Main.PRODUCTION;
import static de.flapdoodle.embed.process.runtime.Network.localhostIsIPv6;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Slf4j
public class TestRepository {
    public static final String MONGODB_HOST = "localhost";
    public static final int MONGODB_PORT = 27017;

    private static final String DATABASE_NAME = "lsd";
    private static final String COLLECTION_NAME = "interceptedInteraction";

    private static MongoClient mongoClient;
    private static MongodExecutable mongodExecutable;

    private final CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), CodecRegistries.fromCodecs(new ZonedDateTimeCodec(), new TypeCodec()),
            fromProviders(PojoCodecProvider.builder().automatic(true).build()));

    public static void setupDatabase() {
        try {
            final IMongodConfig mongodConfig = new MongodConfigBuilder()
                    .version(PRODUCTION)
                    .net(new Net(MONGODB_HOST, MONGODB_PORT, localhostIsIPv6()))
                    .build();

            mongodExecutable = MongodStarter.getDefaultInstance().prepare(mongodConfig);
            mongodExecutable.start();


            mongoClient = MongoClients.create(builder()
                    .applyConnectionString(new ConnectionString("mongodb://" + MONGODB_HOST + ":" + MONGODB_PORT))
                    .retryWrites(true)
                    .build());
        } catch (final IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public static void tearDownDatabase() {
        mongoClient.close();
        mongodExecutable.stop();
    }

    public void save(InterceptedInteraction interceptedInteraction) {
        final MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
        final MongoCollection<Document> collection = database.getCollection(COLLECTION_NAME).withCodecRegistry(pojoCodecRegistry);
        Document document = new Document("_id", new ObjectId());
        document.append("traceId", interceptedInteraction.getTraceId())
                .append("body", interceptedInteraction.getBody())
                .append("requestHeaders", interceptedInteraction.getRequestHeaders())
                .append("responseHeaders", interceptedInteraction.getResponseHeaders())
                .append("serviceName", interceptedInteraction.getServiceName())
                .append("target", interceptedInteraction.getTarget())
                .append("path", interceptedInteraction.getPath())
                .append("httpStatus", interceptedInteraction.getHttpStatus())
                .append("httpMethod", interceptedInteraction.getHttpMethod())
                .append("type", interceptedInteraction.getType())
                .append("profile", interceptedInteraction.getProfile())
                .append("elapsedTime", interceptedInteraction.getElapsedTime())
                .append("createdAt", interceptedInteraction.getCreatedAt());
        collection.insertOne(document);
    }
}
