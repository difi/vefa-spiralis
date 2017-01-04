package no.balder.spiralis;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author steinar
 *         Date: 01.01.2017
 *         Time: 19.11
 */
public class Transaction {

    List<? extends Task> tasks;
    private ExecutorService executorService;

    public List<Future<?>> futures = new ArrayList<>();


    public Transaction(List<? extends Task> tasks) {
        this.tasks = tasks;
    }

    public List<? extends Task> getTasks() {
        return tasks;
    }

    public List<Future<?>> getFutures() {
        return futures;
    }

    public void start() {
        executorService = Executors.newFixedThreadPool(tasks.size());
        for (Task task : tasks) {
            Future<?> future = executorService.submit(task);
            futures.add(future);
        }
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }


    /**
     * Returns the number of items processed by the tasks in this {@link Transaction}
     *
     * @return number of items processed so far.
     */
    public long getProcessCount() {
        long result = 0;

        for (Task task : tasks) {
            result += task.getProcessCount();
        }
        return result;
    }
}
