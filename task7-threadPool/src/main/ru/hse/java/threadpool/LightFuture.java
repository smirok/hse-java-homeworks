package ru.hse.java.threadpool;

import org.jetbrains.annotations.NotNull;
import ru.hse.java.threadpool.exceptions.LightExecutionException;

import java.util.function.Function;

public interface LightFuture<R> {
    boolean isReady();

    @NotNull R get() throws LightExecutionException;

    <R1> @NotNull LightFuture<R1> thenApply(Function<R, @NotNull R1> function);
}