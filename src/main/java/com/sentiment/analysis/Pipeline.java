
package com.sentiment.analysis;


import edu.stanford.nlp.pipeline.*;

import java.io.IOException;
import java.util.*;

public class Pipeline {

    private final StanfordCoreNLP pipeline;

    public Pipeline(){
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner, parse, sentiment");

        this.pipeline = new StanfordCoreNLP(props);
    }
    public String analyzeSentiment(String review) {
        Annotation annotation = new Annotation(review);
        this.pipeline.annotate(annotation);
        CoreDocument coreDocument = new CoreDocument(annotation);

        if (!coreDocument.sentences().isEmpty()) {
            CoreSentence sentence = coreDocument.sentences().get(0);
            return sentence.sentiment();
        }
        return "Unknown";
    }

    public Map<String,String> analyzeSentiments(List<String> reviews){
        Map<String,String> results = new HashMap<>();
        for (String review : reviews){
            results.put(analyzeSentiment(review),analyzeSentiment(review));
        }
        return results;
    }



}
