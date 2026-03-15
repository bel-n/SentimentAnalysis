package parallel;

import org.eclipse.jetty.util.log.Log;
import util.LogLevel;
import util.Logger;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ParallelReviewDS3 {

    private final Queue<String> reviewQueue = new LinkedList<>();
    private final BlockingQueue<PipelineParallel> pipelinePool;
    private final int maxThreads;
    private final Thread[] workers;
    private volatile boolean running = true;
    private final AtomicInteger totalProcessed = new AtomicInteger(0);
  //  private final int numOfPipelines = 6;
    private final long startTime = System.currentTimeMillis();

    public ParallelReviewDS3() {
        int cores = Runtime.getRuntime().availableProcessors();
        long maxMemoryMB = Runtime.getRuntime().maxMemory() / (1024 * 1024);

        int maxThreadsByMemory = (int)(maxMemoryMB / 500) - 4;
        int maxThreadsByCores = cores - 3;
        this.maxThreads = Math.max(1, Math.min(maxThreadsByMemory, maxThreadsByCores));
        this.workers = new Thread[maxThreads] ;


        long available = maxMemoryMB - 500;
        int maxByMemory = Math.max(1, (int)((available - 205) / 2));
        int numOfPipelines = Math.min(maxByMemory, maxThreads - 2);

        this.pipelinePool = new LinkedBlockingQueue<>(numOfPipelines);
        for (int i = 0; i < numOfPipelines; i++) {
            pipelinePool.add(new PipelineParallel());
        }

        Logger.log("Hardware: " + cores + " cores, " + maxMemoryMB + "MB RAM → "
                + maxThreads + " worker threads, " + numOfPipelines + " shared pipelines", LogLevel.Update);

        startWorkers();


        //long available = maxMemoryMB - 500;
        //int numOfPipelines = Math.max(1, (int)((available - 205) / 2));

        //long available = maxMemoryMB - 1500;
        //int numOfPipelines = Math.max(1, 1 + (int)((available - 2000) / 400));
        // long availableForPipeline = maxMemoryMB - 1500;
        // int numOfPipelines = Math.max(1,(int)(availableForPipeline / 2000));


    }

    private void startWorkers() {
        for (int i = 0; i < maxThreads; i++){
            workers[i] = new Thread(() -> {
                while (running) {
                    String review;

                    synchronized ( (reviewQueue)) {
                        while(reviewQueue.isEmpty() && running) {
                            try{
                                reviewQueue.wait();
                            }catch (InterruptedException e){
                                Thread.currentThread().interrupt();
                                return;
                            }
                        }
                        if (!running)return;
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
        //Logger.log("handleInput received: " + review.substring(0, Math.min(50, review.length())), LogLevel.Update);

        if(!review.startsWith("{")) return;

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

        PipelineParallel pipeline = null;



        try{
            pipeline = pipelinePool.take();
            String sentiment = pipeline.analyzeSentiment(input);

            int total = totalProcessed.incrementAndGet();
            long elapsed = System.currentTimeMillis() - startTime;
            double throughput = total / (elapsed / 1000.0);

            Logger.log("[" + Thread.currentThread().getName() + "]"
                    + "\nReview: " + input
                    + "\nSentiment: " + sentiment
                    + "\nThroughput: " + String.format("%.2f", throughput) + " reviews/sec"
                    + " | Total: " + total, LogLevel.Success);
        }catch (OutOfMemoryError e){
            Logger.log("[" + Thread.currentThread().getName() + "] OOM, skipping review");

            try{
                Thread.sleep(2000);
            }catch (InterruptedException ie){
                Thread.currentThread().interrupt();
            }
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }finally {
            if(pipeline != null){
                pipelinePool.add(pipeline);
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

    //get the total memory used by init of a pipeline
    public static void main(String[] args) {
        Runtime runtime = Runtime.getRuntime();
        for (int i = 0; i < 6; i++) {
            System.gc();
            long before = runtime.totalMemory() - runtime.freeMemory();
            new PipelineParallel();
            long after = runtime.totalMemory() - runtime.freeMemory();
            long cost = (after - before) / (1024 * 1024);
            System.out.println("Pipeline " + (i + 1) + " cost: " + cost + "MB");
        }
    }


}
