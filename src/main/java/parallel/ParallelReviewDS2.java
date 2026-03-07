package parallel;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


import util.LogLevel;
import util.Logger;

public class ParallelReviewDS2 {

    private final Queue<String> reviewQueue = new LinkedList<>();

    private final ThreadLocal<PipelineParallel> pipleline = ThreadLocal.withInitial(() -> new PipelineParallel());

    private final int maxThreads;

    private volatile boolean running = true;

    private final AtomicInteger totalProcessed = new AtomicInteger(0);

    private final long startTime = System.currentTimeMillis();





public ParallelReviewDS2(){
    int cores = Runtime.getRuntime().availableProcessors();
    long maxMemoryMB = Runtime.getRuntime().maxMemory() / (1024 * 1024);

    int maxThreadsByMemory = (int)(maxMemoryMB / 500);
    int maxThreadsByCores = cores - 1;

    this.maxThreads = Math.max(1, Math.min(maxThreadsByCores, maxThreadsByMemory));

    Logger.log("Hardware:" + cores + " cores," + maxMemoryMB + "MB RAM ->" + maxThreads + "threads", LogLevel.Update);

    // startWorkers();
}
}