package io.lsdconsulting.lsd.distributed.diagram.data;

import io.lsdconsulting.lsd.distributed.model.InterceptedInteraction;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static io.lsdconsulting.lsd.distributed.model.Type.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

class InteractionDataGeneratorShould {

    private final InteractionDataGenerator underTest = new InteractionDataGenerator();

    @Test
    void buildWithRequestHeadersOnly() {
        Map<String, Collection<String>> requestHeaders = Map.of("name", List.of("value"));
        InterceptedInteraction interaction = InterceptedInteraction.builder()
                .type(REQUEST)
                .requestHeaders(requestHeaders)
                .responseHeaders(Map.of("name1", List.of("value2")))
                .body("someBody")
                .build();

        InteractionDataGenerator.InteractionData result = underTest.buildFrom(interaction);

        assertThat(result.getRequestHeaders(), is(requestHeaders));
        assertThat(result.getResponseHeaders(), is(nullValue()));
        assertThat(result.getHeaders(), is(nullValue()));
        assertThat(result.getBody(), is("someBody"));
    }

    @Test
    void buildWithResponseHeadersOnly() {
        Map<String, Collection<String>> responseHeaders = Map.of("name", List.of("value"));
        InterceptedInteraction interaction = InterceptedInteraction.builder()
                .type(RESPONSE)
                .requestHeaders(Map.of("name1", List.of("value2")))
                .responseHeaders(responseHeaders)
                .body("someBody")
                .build();

        InteractionDataGenerator.InteractionData result = underTest.buildFrom(interaction);

        assertThat(result.getRequestHeaders(), is(nullValue()));
        assertThat(result.getResponseHeaders(), is(responseHeaders));
        assertThat(result.getHeaders(), is(nullValue()));
        assertThat(result.getBody(), is("someBody"));
    }

    @Test
    void buildWithHeadersOnly() {
        Map<String, Collection<String>> headers = Map.of("name", List.of("value"));
        InterceptedInteraction interaction = InterceptedInteraction.builder()
                .type(PUBLISH)
                .requestHeaders(headers)
                .responseHeaders(Map.of("name1", List.of("value2")))
                .body("someBody")
                .build();

        InteractionDataGenerator.InteractionData result = underTest.buildFrom(interaction);

        assertThat(result.getRequestHeaders(), is(nullValue()));
        assertThat(result.getResponseHeaders(), is(nullValue()));
        assertThat(result.getHeaders(), is(headers));
        assertThat(result.getBody(), is("someBody"));
    }
}
