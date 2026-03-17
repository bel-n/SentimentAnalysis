package sequential;

import util.LogLevel;
import util.Logger;
import java.util.ArrayList;
import java.util.List;
import util.CleanReviews;

public class ReviewDS {
    private int totalProcessed = 0;
    private final long startTime = System.currentTimeMillis();
    private final List<String> reviews;


    public ReviewDS() {
        this.reviews = new ArrayList<>();
    }

    public synchronized void addReview(String review) {

        this.reviews.add(review);
    }


    public synchronized void processReviewsSequentially(Pipeline pipeline) {
        if(reviews.isEmpty()) return;

        String review = reviews.remove(0);

        String reviewText = CleanReviews.extractReviewText(review);
        String topic = CleanReviews.extractTopic(review);
        String reviewerID = CleanReviews.extractField(review, "reviewerID");
        String reviewerName = CleanReviews.extractField(review, "reviewerName");
        String asin = CleanReviews.extractField(review, "asin");
        // Process everything currently in the list
        String input;
        if(reviewText.length() > 300){
            input = reviewText.substring(0, 300);
        }else{
            input = reviewText;
        }
        String sentiment = pipeline.analyzeSentiment(input);

        Logger.log("[" + Thread.currentThread().getName() + "]"
                + "\nTopic: " + topic + " | Product (ASIN): " + asin
                + " | Reviewer: " + reviewerName + " (" + reviewerID + ")"
                + "\nReview: " + input, LogLevel.Success);

        Logger.log("Sentiment: " + sentiment, LogLevel.Sentiment);

        totalProcessed++;
        long elapsed = System.currentTimeMillis() - startTime;
        double throughput = totalProcessed / (elapsed / 1000.0);

        Logger.log("Throughput: " + String.format("%.2f", throughput) + " reviews/sec"
                + " | Total: " + totalProcessed, LogLevel.Success);


    }
}