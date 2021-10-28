package io.lsdconsulting.lsd.distributed.generator.diagram.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.lsdconsulting.lsd.distributed.access.model.InterceptedInteraction;
import lombok.Builder;
import lombok.Value;
import lsd.format.Parser;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static io.lsdconsulting.lsd.distributed.access.model.Type.*;

public class InteractionDataGenerator {

    public InteractionData buildFrom(InterceptedInteraction interceptedInteraction) {
        return InteractionData.builder()
                .requestHeaders(interceptedInteraction.getType().equals(REQUEST) ? interceptedInteraction.getRequestHeaders() : null)
                .responseHeaders(interceptedInteraction.getType().equals(RESPONSE) ? interceptedInteraction.getResponseHeaders() : null)
                .headers(List.of(PUBLISH, CONSUME).contains(interceptedInteraction.getType()) ? interceptedInteraction.getRequestHeaders() : null)
                .body(generateBody(interceptedInteraction.getBody()))
                .build();
    }

    private Object generateBody(String body) {
        Map<String, Object> bodyMap = Parser.parse(body);
        return bodyMap.isEmpty() ? body : bodyMap;
    }

    @Value
    @Builder
    static class InteractionData {
        @JsonInclude(NON_EMPTY)
        Map<String, Collection<String>> requestHeaders;

        @JsonInclude(NON_EMPTY)
        Map<String, Collection<String>> responseHeaders;

        @JsonInclude(NON_EMPTY)
        Map<String, Collection<String>> headers;

        Object body;
    }
}
