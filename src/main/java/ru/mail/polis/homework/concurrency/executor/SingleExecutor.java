package ru.mail.polis.homework.concurrency.executor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;

/**
 * Нужно сделать свой executor с одним вечным потоком. Пока не вызовут shutdown или shutdownNow
 *
 * Задачи должны выполняться в порядке FIFO
 *
 * Max 6 тугриков
 */
public class SingleExecutor implements Executor {

    private volatile boolean isTerminated;
    private final CustomThread thread;
    private final BlockingQueue<Runnable> tasks;

    public SingleExecutor() {
        tasks = new LinkedBlockingQueue<>();
        thread = new CustomThread();
        thread.start();
    }

    /**
     * Метод ставит задачу в очередь на исполнение.
     * 3 балла за метод
     */
    @Override
    public void execute(Runnable command) {
        if (command == null) {
            throw new IllegalArgumentException();
        }
        if (isTerminated) {
            throw new RejectedExecutionException();
        }
        synchronized (this) {
            tasks.add(command);
        }
    }

    /**
     * Дает текущим задачам выполниться. Добавление новых - бросает RejectedExecutionException
     * 1 балл за метод
     */
    public void shutdown() {
        isTerminated = true;
    }

    /**
     * Прерывает текущие задачи. При добавлении новых - бросает RejectedExecutionException
     * 2 балла за метод
     */
    public void shutdownNow() {
        isTerminated = true;
        thread.interrupt();
    }

    private class CustomThread extends Thread {
        @Override
        public void run() {
            try {
                while ((!isTerminated || !tasks.isEmpty()) && !isInterrupted()) {
                    tasks.take().run();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
