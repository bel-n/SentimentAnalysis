package parallel;

import java.nio.channels.Pipe;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import parallel.PipelineParallel;

import util.LogLevel;
import util.Logger;

public class ParallelReviewDS {
    private final ExecutorService executorService;

    private final PipelineParallel pipeline;

    private final BlockingQueue<String> reviewQueue;

    private final AtomicInteger activeThreads;

    public ParallelReviewDS() {

        this.executorService = new ThreadPoolExecutor(
                6  ,
                11,
                30L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000)
        );


        this.pipeline = new PipelineParallel();
        this.reviewQueue = new LinkedBlockingQueue<>();
        this.activeThreads = new AtomicInteger(0);

        startQueueWorker();
    }


    private void startQueueWorker() {
        //submit a continuous monitoring task to the thread pool
        executorService.submit(() -> {
            //it should run indefinitely until shutdown
            while (!executorService.isShutdown()) {
                try{

                    String review = reviewQueue.take();
                    processReview(review);
                }catch (InterruptedException e){
                    //restore interrupt status and exit
                    Thread.currentThread().interrupt();
                    break;
                }
            }

        });
    }

    public void handleInput(String review) {
        try{

            reviewQueue.put(review);

        }catch (InterruptedException e) {


            Thread.currentThread().interrupt();

        }
    }


    private void processReview(String review) {

        activeThreads.incrementAndGet();


        executorService.submit(() -> {
            try{
            String sentiment = pipeline.analyzeSentiment(review);

            Logger.log("Review" + review + "\nSetiment:" + sentiment, LogLevel.Success);

            }finally {
                activeThreads.decrementAndGet();
            }


        });
    }




    public void manualCleanup() {
        try {
            reviewQueue.clear();

            activeThreads.set(0);

            System.gc();

            long used = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            Logger.log("Heap used (MB): " + (used / 1024 / 1024), LogLevel.Update);
        } catch (Exception e) {
            Logger.log("Cleanup failed: " + e.getMessage(), LogLevel.Error);
        }
    }


    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        manualCleanup();
    }



}
