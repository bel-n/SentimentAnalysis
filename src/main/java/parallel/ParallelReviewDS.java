package parallel;

import java.util.concurrent.Executor; //executor is interface for executing tasks
import java.util.concurrent.ExecutorService; //manages a pool of threads
import java.util.concurrent.Executors;

import sequential.Pipeline; //because nothing changes

public class ParallelReviewDS {
private final ExecutorService executorService; //for pool of threads

private final Pipeline pipeline;

public ParallelReviewDS(){
    this.executorService = Executors.newCachedThreadPool();
    //a thread pool that can dynamically grow as needed.
    //it will reuse idle threads when possible.
    this.pipeline = new Pipeline();
    //handles sentiment analysis
}

public void processReview (String review){
    executorService.submit(() -> { //submits a task to be run in a separate thread
        //every thread performs the following  :
        String sentiment = pipeline.analyzeSentiment(review);
        System.out.println("Sentiment:" + sentiment);
    });
}


}
