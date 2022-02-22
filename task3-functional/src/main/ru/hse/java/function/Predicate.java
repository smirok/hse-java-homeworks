package ru.hse.java.function;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface Predicate<T> extends Function1<T, Boolean> {

    Predicate<Object> ALWAYS_TRUE = arg -> true;

    Predicate<Object> ALWAYS_FALSE = arg -> false;

    default Predicate<T> or(@NotNull Predicate<? super T> other) {
        return arg -> apply(arg) || other.apply(arg);
    }

    default Predicate<T> and(@NotNull Predicate<? super T> other) {
        return arg -> apply(arg) && other.apply(arg);
    }

    default Predicate<T> not() {
        return arg -> !apply(arg);
    }
}
