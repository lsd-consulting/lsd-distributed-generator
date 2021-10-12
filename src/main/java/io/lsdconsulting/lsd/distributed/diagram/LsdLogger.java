package io.lsdconsulting.lsd.distributed.diagram;

import com.lsd.LsdContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.empty;

@Slf4j
@RequiredArgsConstructor
public class LsdLogger {
    private final InteractionGenerator interactionGenerator;
    private final LsdContext lsdContext;

    public void captureInteractionsFromDatabase(final String... traceIds) {
        Map<String, Optional<String>> traceIdToColourMap = new HashMap<>();
        Arrays.stream(traceIds).forEach(x -> traceIdToColourMap.put(x, empty()));
        captureInteractionsFromDatabase(traceIdToColourMap);
    }

    public void captureInteractionsFromDatabase(final Map<String, Optional<String>> traceIdToColourMap) {
        for (var interaction : interactionGenerator.generate(traceIdToColourMap)) {
            lsdContext.capture(interaction);
        }
    }
}
