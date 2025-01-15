
package com.sentiment.analysis;

import util.LogLevel;
import util.Logger;

import java.util.ArrayList;
import java.util.List;

public class ReviewDS {
    private List<String> reviews;

    public ReviewDS() {
        this.reviews = new ArrayList<>();
    }

    public void addReview(String review) {
        this.reviews.add(review);
    }

    public List<String> getReviews() {
        return reviews;
    }

    public void processReviewsSequentially(Pipeline pipeline) {

        while(!reviews.isEmpty()) {
            String review = reviews.remove(0); //take the first review
            String sentiment = pipeline.analyzeSentiment(review);
            Logger.log("Sentiment of the review: " + sentiment, LogLevel.Success);
        }
        /*for (String review : reviews) {
            String sentiment = pipeline.analyzeSentiment(review);
            Logger.log("Sentiment of the review: " + sentiment, LogLevel.Success);

        }*/
    }
}
