package ru.hse.java.function;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface Function1<T, R> {

    R apply(T arg);

    default <V> Function1<T, V> compose(@NotNull Function1<? super R, ? extends V> g) {
        return arg -> g.apply(apply(arg));
    }
}
