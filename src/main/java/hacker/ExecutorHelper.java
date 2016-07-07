
package hacker;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * Created by sarath on 7/5/16.
 */

public class ExecutorHelper {

    private static final ExecutorService executorService = new ThreadPoolExecutor(100, Integer.MAX_VALUE,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>());

    public static void execute(Runnable runnable) {
        executorService.execute(runnable);
    }

    public static <T> Future<T> submit(Callable<T> callable) {
        return executorService.submit(callable);
    }

    @Override
    protected void finalize() throws Throwable {
        executorService.shutdown();
    }
}

