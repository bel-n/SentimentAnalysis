package parallel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.util.*;
import java.util.stream.Collectors;


public class PipelineParallel {
    private final StanfordCoreNLP pipeline;

    public PipelineParallel() {
        Properties props = new Properties();
        props.setProperty("annotators","tokenize,ssplit,pos,parse,sentiment");
        //props.setProperty("annotators", "tokenize, ssplit, pos,lemma,ner,parse,sentiment");
        //too much properties
        props.setProperty("threadsafe", "true");

        this.pipeline = new StanfordCoreNLP(props);
    }


    public String analyzeSentiment(String review) {
        try{
            Annotation annotation = new Annotation(review);
            this.pipeline.annotate(annotation);
            //document object
            CoreDocument coreDocument  = new CoreDocument(annotation);
            List<CoreSentence> sentences = coreDocument.sentences();

            if(sentences.isEmpty()){
                return "Unknown";
            }

            Map<String,Long> sentimentCounts =
                    sentences.stream()
                            .map(CoreSentence::sentiment)
                            .collect(Collectors.groupingBy(
                                    s -> s,Collectors.counting()
                            ));

            return sentimentCounts.entrySet()
                    .stream()
                    .max(Map.Entry.comparingByValue())
                    .get()
                    .getKey();
           /* if(!coreDocument.sentences().isEmpty()){
                CoreSentence sentence = coreDocument.sentences().get(0);
                return sentence.sentiment();
            }*/

        }catch (Exception e){
            System.err.println("Error analyzing sentiment: " + e.getMessage());
            return "Error";
        }
    }
}
