package io.lsdconsulting.lsd.distributed.generator.config;

import com.lsd.IdGenerator;
import com.lsd.LsdContext;
import io.lsdconsulting.lsd.distributed.access.repository.InterceptedDocumentRepository;
import io.lsdconsulting.lsd.distributed.generator.diagram.InteractionGenerator;
import io.lsdconsulting.lsd.distributed.generator.diagram.LsdLogger;
import io.lsdconsulting.lsd.distributed.generator.diagram.data.InteractionDataGenerator;
import io.lsdconsulting.lsd.distributed.generator.diagram.event.ConsumeMessageBuilder;
import io.lsdconsulting.lsd.distributed.generator.diagram.event.EventBuilderMap;
import io.lsdconsulting.lsd.distributed.generator.diagram.event.MessageBuilder;
import io.lsdconsulting.lsd.distributed.generator.diagram.event.SynchronousResponseBuilder;
import io.lsdconsulting.lsd.distributed.generator.diagram.label.LabelGeneratorMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "lsd.dist.db.connectionString")
public class LibraryConfig {

    @Bean
    public IdGenerator idGenerator() {
        return new IdGenerator(false);
    }

    @Bean
    public MessageBuilder messageBuilder(IdGenerator idGenerator){
        return new MessageBuilder(idGenerator);
    }

    @Bean
    public ConsumeMessageBuilder consumeMessageBuilder(IdGenerator idGenerator){
        return new ConsumeMessageBuilder(idGenerator);
    }

    @Bean
    public SynchronousResponseBuilder synchronousResponseBuilder(IdGenerator idGenerator){
        return new SynchronousResponseBuilder(idGenerator);
    }

    @Bean
    public LabelGeneratorMap labelGeneratorMap() {
        return new LabelGeneratorMap();
    }

    @Bean
    public EventBuilderMap eventBuilderMap(MessageBuilder messageBuilder, SynchronousResponseBuilder synchronousResponseBuilder,
                                           ConsumeMessageBuilder consumeMessageBuilder) {
        return new EventBuilderMap(messageBuilder, synchronousResponseBuilder, consumeMessageBuilder);
    }

    @Bean
    public InteractionDataGenerator interactionDataGenerator() {
        return new InteractionDataGenerator();
    }

    @Bean
    public InteractionGenerator interactionGenerator(InterceptedDocumentRepository interceptedDocumentRepository,
                                                     EventBuilderMap eventBuilderMap, LabelGeneratorMap labelGeneratorMap,
                                                     InteractionDataGenerator interactionDataGenerator) {
        return new InteractionGenerator(interceptedDocumentRepository,eventBuilderMap, labelGeneratorMap, interactionDataGenerator);
    }

    @Bean
    public LsdLogger lsdLogger(InteractionGenerator interactionGenerator) {

        return new LsdLogger(interactionGenerator, LsdContext.getInstance());
    }
}
