package io.lsdconsulting.lsd.distributed.generator.diagram.label;

import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction;
import org.junit.jupiter.api.Test;

import static io.lsdconsulting.lsd.distributed.access.model.Type.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class LabelGeneratorMapShould {

    private final LabelGeneratorMap labelGeneratorMap = new LabelGeneratorMap();

    @Test
    void generateRequestLabel() {
        InterceptedInteraction interaction = InterceptedInteraction.builder()
                .type(REQUEST)
                .httpMethod("GET")
                .path("path")
                .build();

        String result = labelGeneratorMap.generate(interaction);

        assertThat(result, is("GET path"));
    }

    @Test
    void generateResponseLabel() {
        InterceptedInteraction interaction = InterceptedInteraction.builder()
                .type(RESPONSE)
                .httpStatus("OK")
                .elapsedTime(20L)
                .build();

        String result = labelGeneratorMap.generate(interaction);

        assertThat(result, is("sync OK response (20 ms)"));
    }

    @Test
    void generatePublishLabel() {
        InterceptedInteraction interaction = InterceptedInteraction.builder().type(PUBLISH).build();

        String result = labelGeneratorMap.generate(interaction);

        assertThat(result, is("publish event"));
    }

    @Test
    void generateConsumeLabel() {
        InterceptedInteraction interaction = InterceptedInteraction.builder().type(CONSUME).build();

        String result = labelGeneratorMap.generate(interaction);

        assertThat(result, is("consume message"));
    }
}
