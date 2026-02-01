package parallel;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.eclipse.jetty.util.log.Log;
import util.LogLevel;
import util.Logger;

import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ParallelReviewDS2 {

    private final ExecutorService executor;
    private final PipelineParallel pipeline;

    private final AtomicInteger activeTasks = new AtomicInteger(0);

    public ParallelReviewDS2() {
        int cores = Runtime.getRuntime().availableProcessors();
        int threads = Math.max(1, cores - 1);

        this.executor = new ThreadPoolExecutor(threads, threads,0L, TimeUnit.MILLISECONDS,new ArrayBlockingQueue<>(50), new ThreadPoolExecutor.CallerRunsPolicy());

        this.pipeline = new PipelineParallel();

        Logger.log("ParallelReviewDS started with" + threads + "threads", LogLevel.Update);
    }

    public void handleInput(String review){
        executor.execute(() -> processReview(review));
    }

    public void processReview (String review){
        activeTasks.incrementAndGet();

        try {
            String sentiment = pipeline.analyzeSentiment(review);
            Logger.log("Review:" + review +"\nSentiment:" + sentiment, LogLevel.Success);
        }
        finally {
            activeTasks.decrementAndGet();
        }
    }

    public void shutdown() {
        Logger.log("Shutting down executor...", LogLevel.Update);
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}