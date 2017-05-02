/**
 * 
 */
package cat.trachemys.coref;

import java.util.Collection;
import java.util.Properties;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;


/**
 * 
 * @author cristinae
 * @since 29.04.2017
 */
public class CorefMarker {
	
	  public static void main(String[] args) throws Exception {
		    String text = "Barack Obama was born in Hawaii and his wife in London. \n " +
	                      "He is the president. She is noone. I know him. \n Obama was elected in 2008 and she is tall.";
		    Annotation document = new Annotation(text);
		    Properties props = new Properties();
		    props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,mention,coref");
		    props.setProperty("coref.algorithm", "neural");
		    props.put("ssplit.eolonly", "true");
		    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		    pipeline.annotate(document);
		    System.out.println("---");
		    System.out.println("coref chains");
		    Collection<CorefChain> chains = document.get(CorefCoreAnnotations.CorefChainAnnotation.class).values();
		    for (CorefChain cc : document.get(CorefCoreAnnotations.CorefChainAnnotation.class).values()) {
		      System.out.println("\t" + cc);
		    }
		    
		    

		  }

}
