package parallel;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;


import util.LogLevel;
import util.Logger;

public class ParallelReviewDS2 {

    private final Queue<String> reviewQueue = new LinkedList<>();

    private final ThreadLocal<PipelineParallel> pipeline = ThreadLocal.withInitial(PipelineParallel::new);

    private final int maxThreads;

    private final Thread[] workers;

    private volatile boolean running = true;

    private final AtomicInteger totalProcessed = new AtomicInteger(0);

    private final long startTime = System.currentTimeMillis();


    public ParallelReviewDS2() {
        int cores = Runtime.getRuntime().availableProcessors();
        long maxMemoryMB = Runtime.getRuntime().maxMemory() / (1024 * 1024);

        int maxThreadsByMemory = (int)(maxMemoryMB / 500) - 4;
        int maxThreadsByCores = cores - 3;

        this.maxThreads = Math.max(1, Math.min(maxThreadsByMemory, maxThreadsByCores));
        this.workers = new Thread[maxThreads];

        Logger.log("Hardware: " + cores + " cores, " + maxMemoryMB + "MB RAM → "
                + maxThreads + " worker threads", LogLevel.Update);

        startWorkers();
    }

    private void startWorkers() {
        for (int i = 0; i < maxThreads; i++) {
            workers[i] = new Thread(() -> {
                while (running) {
                    String review = null;

                    synchronized (reviewQueue) {
                        while(reviewQueue.isEmpty() && running) {
                            try{
                                reviewQueue.wait();

                            }catch(InterruptedException e){
                                Thread.currentThread().interrupt();
                                return;
                            }
                        }
                        if(!running) return;
                        review = reviewQueue.poll();
                    }
                    if (review != null) {
                       processReview(review);
                    }
                }
            });

            workers[i].setName("Worker - " + i );
            workers[i].start();

        }
    }

    public void handleInput(String review) {

        /*if (review.startsWith("Review: ")) {
            Logger.log("Welcome message accepted", LogLevel.Update);
            return;
        }*/
        if (!review.startsWith("{")) return;

        synchronized (reviewQueue) {
            reviewQueue.add(review);
            reviewQueue.notify();
        }
    }


    private void processReview(String review) {

        String input;
        if(review.length() > 300){
            input = review.substring(0, 300);
        }else{
            input = review;
        }

        try{
            String sentiment = pipeline.get().analyzeSentiment(input);

            int total = totalProcessed.incrementAndGet();
            long elapsed = System.currentTimeMillis() - startTime;
            double throughput = total / (elapsed / 1000.0);

            Logger.log("[" + Thread.currentThread().getName() + "]"
                    + "\nReview: " + input
                    + "\nSentiment: " + sentiment
                    + "\nThroughput: " + String.format("%.2f", throughput) + " reviews/sec"
                    + " | Total: " + total, LogLevel.Success);
        }catch (OutOfMemoryError e){
            Logger.log("[" + Thread.currentThread().getName() + "] OOM on review... THIS REVIEW WILL BE SKIPPED.", LogLevel.Error);
            System.gc();
            try{
                Thread.sleep(2000);
            }catch (InterruptedException e2){
                Thread.currentThread().interrupt();
            }
        }

    }


    public void shutdown() {
        running = false;

        synchronized (reviewQueue) {
            reviewQueue.notifyAll();
        }

        for (Thread worker : workers) {
            try {
                worker.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        Logger.log("All workers finished. Final throughput: " +
                String.format("%.2f", totalProcessed.get() /
                        ((System.currentTimeMillis() - startTime) / 1000.0))
                + " reviews/sec | Total processed: " + totalProcessed.get(), LogLevel.Update);
    }
}