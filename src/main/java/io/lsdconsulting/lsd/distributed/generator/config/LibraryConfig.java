package io.lsdconsulting.lsd.distributed.generator.config;

import com.lsd.core.IdGenerator;
import com.lsd.core.LsdContext;
import io.lsdconsulting.lsd.distributed.access.repository.InterceptedDocumentRepository;
import io.lsdconsulting.lsd.distributed.generator.diagram.InteractionGenerator;
import io.lsdconsulting.lsd.distributed.generator.diagram.LsdLogger;
import io.lsdconsulting.lsd.distributed.generator.diagram.data.InteractionDataGenerator;
import io.lsdconsulting.lsd.distributed.generator.diagram.event.EventBuilderMap;
import io.lsdconsulting.lsd.distributed.generator.diagram.event.builder.ConsumeMessageBuilder;
import io.lsdconsulting.lsd.distributed.generator.diagram.event.builder.MessageBuilder;
import io.lsdconsulting.lsd.distributed.generator.diagram.event.builder.SynchronousResponseBuilder;
import io.lsdconsulting.lsd.distributed.generator.diagram.label.LabelGeneratorMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "lsd.dist.db.connectionString")
public class LibraryConfig {

    @Bean
    public IdGenerator idGenerator(@Value("${lsd.core.ids.deterministic:false}") boolean isDeterministic) {
        return new IdGenerator(isDeterministic);
    }

    @Bean
    public MessageBuilder messageBuilder(){
        return new MessageBuilder();
    }

    @Bean
    public ConsumeMessageBuilder consumeMessageBuilder(){
        return new ConsumeMessageBuilder();
    }

    @Bean
    public SynchronousResponseBuilder synchronousResponseBuilder(){
        return new SynchronousResponseBuilder();
    }

    @Bean
    public LabelGeneratorMap labelGeneratorMap() {
        return new LabelGeneratorMap();
    }

    @Bean
    public EventBuilderMap eventBuilderMap(IdGenerator idGenerator,
                                           MessageBuilder messageBuilder,
                                           SynchronousResponseBuilder synchronousResponseBuilder,
                                           ConsumeMessageBuilder consumeMessageBuilder) {
        return new EventBuilderMap(idGenerator, messageBuilder, synchronousResponseBuilder, consumeMessageBuilder);
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
