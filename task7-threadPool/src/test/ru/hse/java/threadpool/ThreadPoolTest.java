package ru.hse.java.threadpool;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.hse.java.threadpool.exceptions.LightExecutionException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.*;

public class ThreadPoolTest {
    @Test
    @SuppressWarnings("unused")
    public void testCreatingThreadPool() {
        ThreadPool threadPool = ThreadPool.create(5);
    }

    @Test
    public void testShutdown() {
        ThreadPool threadPool = ThreadPool.create(6);

        threadPool.shutdown();
        Assertions.assertThrows(RejectedExecutionException.class, () -> threadPool.submit(() -> 1));
    }

    @Test
    public void testThreadCount() {
        final int THREAD_POOL_SIZE = 8;
        int countThreadsBefore = Thread.activeCount();

        ThreadPool threadPool = ThreadPool.create(THREAD_POOL_SIZE);

        int countThreadsAfter = Thread.activeCount();

        Assertions.assertTrue(countThreadsAfter - countThreadsBefore >= THREAD_POOL_SIZE);
        threadPool.shutdown();
    }

    @Test
    @SuppressWarnings("StatementWithEmptyBody")
    public void testRunOneThread() throws LightExecutionException {
        ThreadPool threadPool = ThreadPool.create(1);
        ArrayList<LightFuture<Integer>> arrayList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            arrayList.add(threadPool.submit(() -> {
                for (int j = 0; j < 1e8; ++j) ;
                return finalI;
            }));
        }

        for (int i = 0; i < 10; i++) {
            Assertions.assertEquals(arrayList.get(i).get(), i);
        }
    }

    @Test
    @SuppressWarnings("all")
    public void testExceptions() {
        ThreadPool threadPool = ThreadPool.create(1);
        LightFuture<Integer> lightFuture = threadPool.submit(() -> 1 / 0);
        Assertions.assertThrows(LightExecutionException.class, lightFuture::get);
    }

    @Test
    public void testThenApply() throws LightExecutionException {
        ThreadPool threadPool = ThreadPool.create(1);
        LightFuture<Integer> lightFuture = threadPool.submit(() -> 1);
        Assertions.assertEquals(lightFuture.thenApply(x -> x + 1).get(), 2);
    }

    @Test
    @SuppressWarnings("all")
    public void testThenApplyExceptions() {
        ThreadPool threadPool = ThreadPool.create(1);
        LightFuture<Integer> lightFuture = threadPool.submit(() -> 1);
        Assertions.assertThrows(LightExecutionException.class,
                () -> lightFuture.thenApply(x -> x / 0).get());
    }

    @Test
    public void testStress() throws LightExecutionException {
        ThreadPool threadPool = ThreadPool.create(8);
        ArrayList<LightFuture<Integer>> futures = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            futures.add(threadPool.submit(() -> {
                int cnt = 0;
                while (cnt < 1e9) {
                    cnt++;
                }
                return 1;
            }));
        }

        Assertions.assertTimeout(Duration.ofMinutes(1), () -> {
            for (int i = 0; i < 100; i++) {
                futures.get(i).get();
            }
        });
    }

    @Test
    public void testThenApplyAll() throws LightExecutionException {
        ThreadPool threadPool = ThreadPool.create(8);
        LightFuture<Integer> lightFuture = threadPool.submit(() -> 0);
        for (int i = 0; i < 1e5; i++) {
            lightFuture = lightFuture.thenApply(x -> x + 1);
        }

        Assertions.assertEquals(lightFuture.get(), (int) 1e5);
    }
}
