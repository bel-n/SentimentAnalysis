package com.sentiment.analysis;


import edu.stanford.nlp.pipeline.*;




import java.io.IOException;
import java.util.*;
//i need the properties class from here to store  and manage the
//config. settings that will influence the behaviour of the tools
//k-v pairs for specific settings

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
            System.out.println("Sentiment: " + sentence.sentiment());  // Debugging statement
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



    /*
    public static void main(String[] args) {
        //-> we need to hold the configuration
        Properties props = new Properties();


        //.setProperty(str key, str val) ->
        //for differentiating tasks to be run on the pipeline
        props.setProperty("annotators","tokenize,ssplit,pos,lemma,ner, parse, sentiment");

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        Annotation annotation;
        Annotation annotation2;
        Annotation annotation3;

       // if(args.length > 0 ){
           // annotation = new Annotation(IOUtils.slurpFileNoExceptions(args[0]));
        //}else{
          annotation = new Annotation("He was here yesterday and loved the place");
            annotation2 =new Annotation("I found the product broken. I'm not satisfied");
            annotation3 =new Annotation("It's kinda shit tho");




        //}

      pipeline.annotate(annotation);
            pipeline.annotate(annotation2);
            pipeline.annotate(annotation3);



        CoreDocument coreDocument = new CoreDocument(annotation);
        CoreDocument coreDocument2 = new CoreDocument(annotation2);
        CoreDocument coreDocument3 = new CoreDocument(annotation3);

        for (CoreSentence sentence : coreDocument.sentences()) {
            System.out.println("Sentiment:" + sentence.sentiment());
        }


        for (CoreSentence sentence : coreDocument2.sentences()) {
            System.out.println("Sentiment2:" + sentence.sentiment());
        }

        for (CoreSentence sentence : coreDocument3.sentences()) {
            System.out.println("Sentiment3:" + sentence.sentiment());
        }

        String text = "Stanford CoreNLP is amazing! It can perform sentiment analysis very easily.";

        CoreDocument document = new CoreDocument(text);
        pipeline.annotate(document);

        for(CoreSentence sentence : document.sentences()) {
            System.out.println("Sentiment:" +sentence.sentiment());
        }





    }



}*/

}
