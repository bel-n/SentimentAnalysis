package parallel;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import sequential.Pipeline; //because nothing changes

public class ParallelReviewDS {
private final ExecutorService executorService;

private final Pipeline pipeline;

public ParallelReviewDS(){
    this.executorService = Executors.newCachedThreadPool();
    this.pipeline = new Pipeline();
}

public void processReview (String review){
    executorService.submit(() -> {
        String sentiment = pipeline.analyzeSentiment(review);
        System.out.println("Sentiment:" + sentiment);
    });
}

public void shutdown(){
    executorService.shutdown();
}

}
