package ru.hse.java.threadpool;

import org.jetbrains.annotations.NotNull;
import ru.hse.java.threadpool.exceptions.LightExecutionException;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Supplier;

public class ThreadPoolImpl implements ThreadPool {
    private final Thread[] threadPool;
    private final Lock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    private final Queue<LightFutureTask<?>> queue = new ArrayDeque<>();
    private volatile boolean isShutdowned = false;

    public ThreadPoolImpl(int threads) {
        threadPool = new Thread[threads];
        for (int i = 0; i < threads; i++) {
            Thread thread = new Thread(() -> {
                while (!Thread.interrupted()) {
                    lock.lock();
                    LightFutureTask<?> task;
                    try {
                        while (queue.isEmpty()) {
                            notEmpty.await();
                        }
                        task = queue.poll();
                    } catch (InterruptedException interruptedException) {
                        break;
                    } finally {
                        lock.unlock();
                    }
                    task.call();
                }
            });
            threadPool[i] = thread;
            threadPool[i].start();
        }
    }


    @Override
    public @NotNull <R> LightFuture<R> submit(Supplier<@NotNull R> supplier) {
        if (isShutdowned) {
            throw new RejectedExecutionException(supplier.toString());
        }

        LightFutureTask<R> task = new LightFutureTask<>(supplier);
        lock.lock();
        try {
            queue.add(task);
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
        return task;
    }

    @Override
    public void shutdown() {
        isShutdowned = true;

        for (int i = 0; i < getNumberOfThreads(); i++) {
            threadPool[i].interrupt();
        }
    }

    @Override
    public int getNumberOfThreads() {
        return threadPool.length;
    }

    private class LightFutureTask<R> implements LightFuture<R> {
        private final Supplier<R> supplier;
        private R result = null;
        private boolean isReady = false;
        private final Condition isDone = lock.newCondition();
        private LightExecutionException innerException = null;

        private void call() {
            try {
                result = supplier.get();
            } catch (Exception e) {
                innerException = new LightExecutionException(e);
            }
            lock.lock();
            try {
                isDone.signalAll();
                isReady = true;
            } finally {
                lock.unlock();
            }
        }

        public LightFutureTask(Supplier<R> supplier) {
            this.supplier = supplier;
        }

        @Override
        public boolean isReady() {
            return isReady;
        }

        @Override
        public @NotNull R get() throws LightExecutionException {
            lock.lock();
            try {
                waitResult();
                if (innerException != null) {
                    throw innerException;
                }
            } finally {
                lock.unlock();
            }

            return result;
        }

        @Override
        public @NotNull <R1> LightFuture<R1> thenApply(Function<R, @NotNull R1> function) {
            return submit(() -> {
                lock.lock();
                try {
                    waitResult();
                } finally {
                    lock.unlock();
                }
                return function.apply(result);
            });
        }

        private void waitResult() {
            while (!isReady) {
                isDone.awaitUninterruptibly();
            }
        }
    }
}
