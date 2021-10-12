package io.lsdconsulting.lsd.distributed.diagram.event;

@FunctionalInterface
public interface QuintFunction<S, T, U, V, W, R> {
    R apply(S s, T t, U u, V v, W w);
}
