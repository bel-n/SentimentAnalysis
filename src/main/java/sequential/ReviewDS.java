package sequential;

import util.LogLevel;
import util.Logger;
import java.util.ArrayList;
import java.util.List;

public class ReviewDS {
    private final List<String> reviews;

    public ReviewDS() {
        this.reviews = new ArrayList<>();
    }

    public synchronized void addReview(String review) {
        this.reviews.add(review);
    }

    public synchronized void processReviewsSequentially(Pipeline pipeline) {
        // Process everything currently in the list
        while (!reviews.isEmpty()) {
            String review = reviews.remove(0);
            String sentiment = pipeline.analyzeSentiment(review);
            Logger.log("Processed review: [" + sentiment + "] " + review, LogLevel.Success);
        }
    }
}