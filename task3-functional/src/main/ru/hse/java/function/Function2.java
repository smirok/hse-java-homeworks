package ru.hse.java.function;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface Function2<T, U, R> {

    R apply(T arg1, U arg2);

    default <V> Function2<T, U, V> compose(@NotNull Function1<? super R, ? extends V> g) {
        return (arg1, arg2) -> g.apply(apply(arg1, arg2));
    }

    default Function1<U, R> bind1(T arg1) {
        return arg2 -> apply(arg1, arg2);
    }

    default Function1<T, R> bind2(U arg2) {
        return arg1 -> apply(arg1, arg2);
    }

    default Function1<T, Function1<U, R>> curry() {
        return this::bind1;
    }
}
