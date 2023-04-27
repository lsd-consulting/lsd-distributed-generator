package io.lsdconsulting.lsd.distributed.generator.diagram.event

fun interface QuintFunction<S, T, U, V, W, P, R> {
    fun apply(s: S, t: T, u: U, v: V, w: W, p: P): R
}