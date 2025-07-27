package parallel;

import java.nio.channels.Pipe;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import parallel.PipelineParallel;

import util.LogLevel;
import util.Logger;

public class ParallelReviewDS {
//Thread pool for executing sentiment analysis tasks
    private final ExecutorService executorService;

    private final PipelineParallel pipeline;

    //Queue that will hold incoming review before processing
    private final BlockingQueue<String> reviewQueue;

    private final AtomicInteger activeThreads;

    public ParallelReviewDS() {
        //get number of available cpu cores
       // int cores = Runtime.getRuntime().availableProcessors();
        /*
        Configurable thread pool:
        -corePoolSize : min threads to keep alive
        -maximumPoolSIze : max threads to create under load (2x cores)
        -keepAliveTime : how long excess threads wait for new work
        -workQueue : queue for holding tasks when all threads are busy
         */


        /*
            I am using 6 - 11 threads because i need some free cores for:
            -garbage collection, websocket thread, system processes...
            -This also prevents over saturation and
         */
        this.executorService = new ThreadPoolExecutor(
                6  ,
                11,
                30L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000)
        );
        //blocking queue indicates that the queue block the accessing
        //thread if it is full(when the queue is bounded) or becomes empty

        this.pipeline = new PipelineParallel();
        this.reviewQueue = new LinkedBlockingQueue<>();
        //this is the thread counter
        this.activeThreads = new AtomicInteger(0);

        startQueueWorker();
    }

    /*
    In the following code block we start a dedicated worker thread to monitor the review queue
    and dispatch processing tasks.
     */
    private void startQueueWorker() {
        //submit a continuous monitoring task to the thread pool
        executorService.submit(() -> {
            //it should run indefinitely until shutdown
            while (!executorService.isShutdown()) {
                try{

                    /*
                    take() - blocks until a review is available
                    -removes and returns the first review in queue
                    -waits if the queue is empty
                     */
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
            /*
            Adding a review to the processing queue
            put() will block if the queue is full(very unliekly radi linkedblockingqueue)
             */
            reviewQueue.put(review);

        }catch (InterruptedException e) {

            //restore the interrupted status
            Thread.currentThread().interrupt();

        }
    }


    //Actual review processing :
    private void processReview(String review) {
        //incrementing the active thread counter

        activeThreads.incrementAndGet();

        //submit processing tasks to thread pool

        executorService.submit(() -> {
            try{
            String sentiment = pipeline.analyzeSentiment(review);

            Logger.log("Review" + review + "\nSetiment:" + sentiment, LogLevel.Success);

            }finally {
                //even if the analysis fail the counter will get decremented
                activeThreads.decrementAndGet();
            }


        });
    }


    /*
    stops accepting new tasks but allows already submitted tasks to complete
    awaitTermination() - blocks until all tasks complete or timeout occurs or current thread is interrupted
     */

    public void manualCleanup() {
        try {
            // Clear queue
            reviewQueue.clear();

            // Reset active thread counter
            activeThreads.set(0);

            // Force GC (suggestion only)
            System.gc();

            // Optionally log memory usage
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






    /*
    interrupt() -  thread stopping mechanism
    -sets a flag on the target thread
     */

}
