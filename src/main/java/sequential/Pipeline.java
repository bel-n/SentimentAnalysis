package sequential;

import edu.stanford.nlp.pipeline.*;
import java.util.*;

public class Pipeline {

    private final StanfordCoreNLP pipeline;

    public Pipeline() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, parse, sentiment");
        this.pipeline = new StanfordCoreNLP(props);
    }

    public String analyzeSentiment(String review) {
        if (review == null || review.isEmpty()) return "Unknown";

        Annotation annotation = new Annotation(review);
        this.pipeline.annotate(annotation);
        CoreDocument coreDocument = new CoreDocument(annotation);

        if (!coreDocument.sentences().isEmpty()) {
            return coreDocument.sentences().get(0).sentiment();
        }
        return "Unknown";
    }

    public Map<String, String> analyzeSentiments(List<String> reviews) {
        Map<String, String> results = new HashMap<>();
        for (String review : reviews) {
            results.put(review, analyzeSentiment(review));
        }
        return results;
    }
}