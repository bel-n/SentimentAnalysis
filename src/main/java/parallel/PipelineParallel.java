package parallel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.util.*;



public class PipelineParallel {
    private final StanfordCoreNLP pipeline;

    public PipelineParallel() {
        Properties props = new Properties();
        props.setProperty("annotators","tokenize,ssplit,pos,parse,sentiment");
        //props.setProperty("annotators", "tokenize, ssplit, pos,lemma,ner,parse,sentiment");
        props.setProperty("threadsafe", "true");

        this.pipeline = new StanfordCoreNLP(props);
    }


    public String analyzeSentiment(String review) {
        try{
            Annotation annotation = new Annotation(review);
            this.pipeline.annotate(annotation);
            CoreDocument coreDocument  = new CoreDocument(annotation);

            if(!coreDocument.sentences().isEmpty()){
                CoreSentence sentence = coreDocument.sentences().get(0);
                return sentence.sentiment();
            }
            return "Unknown";
        }catch (Exception e){
            System.err.println("Error analyzing sentiment: " + e.getMessage());
            return "Error";
        }
    }
}
