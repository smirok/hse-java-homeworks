package ru.hse.java.threadpool;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public interface ThreadPool {
    static @NotNull ThreadPool create(int threads) {
        return new ThreadPoolImpl(threads);
    }

    <R> @NotNull LightFuture<R> submit(Supplier<@NotNull R> supplier);

    void shutdown();

    int getNumberOfThreads();
}
