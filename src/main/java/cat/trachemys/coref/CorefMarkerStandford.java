/**
 * 
 */
package cat.trachemys.coref;

import java.util.Properties;

import org.json.JSONObject;

import cat.trachemys.generic.FileIO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.CorefChain.CorefMention;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;


/**
 * 
 * @author cristinae
 * @since 29.04.2017
 */
public class CorefMarkerStandford extends CorefererCommons implements Coreferer{
	

	public JSONObject annotateText(String text){
		
	    Annotation document = new Annotation(text);
	    
	    // Preparing the annotators
	    Properties props = new Properties();
	    props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,mention,coref");
	    props.setProperty("coref.algorithm", "neural");
	    props.put("ssplit.eolonly", "true");
	    
	    // Annotate
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	    pipeline.annotate(document);
	    
	    //coreference resolution begins here
	    Map<Integer, CorefChain> corefs = document.get(CorefCoreAnnotations.CorefChainAnnotation.class);
		JSONObject doc = new JSONObject();
	    for(Map.Entry<Integer, CorefChain> entry : corefs.entrySet()) {
	    	CorefChain corefChain = entry.getValue();
	    	
	    	//self references will not be considered 
	    	if(corefChain.getMentionMap().entrySet().size() <= 1) {
	    		continue;
	    	}
	    	//Customising the format of the chains
	    	String fullChain = "";
	    	for (CorefMention temp : corefChain.getMentionsInTextualOrder()) {
	    		// doing my best to avoid regexp!
				fullChain = fullChain + "<" + temp.toString().split("\"")[1] + "> ";
			}

	    	//Extracting the info for every mention
	    	for(CorefMention corefMention:corefChain.getMentionsInTextualOrder()){
				JSONObject info = new JSONObject();
				// General info
				info.put("start", corefMention.startIndex);
				info.put("end", corefMention.endIndex-1);
				info.put("tokens", corefMention.mentionSpan);
				info.put("type", corefMention.mentionType);

				// Head
				boolean isHead = false;
				if (corefMention.mentionID == corefChain.getRepresentativeMention().mentionID) {
					isHead = true;	
				}
				info.put("isHead", isHead);

				// Coreference chain, with and without the current word
   				info.put("chain", fullChain);
   				String restChain = fullChain.replace("<"+corefMention.mentionSpan+"> ","");
   				info.put("restChain", restChain);
   							
    			doc.accumulate(String.valueOf(corefMention.sentNum), info);
    		}
	    
	    }
	    //System.out.println(doc.toString(2));
		return doc;
	}
	
	  public static void main(String[] args) throws Exception {
		    String text = "Barack Obama was born in Hawaii and his wife in London. \n " 
	                      + "He is the president. She is noone. I know him. \n "
	                      + "Obama was elected in 2008 and she is tall.";
		    System.out.println("---");
		    System.out.println("coref chains");

		    Annotation document = new Annotation(text);
		    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		   // List<String> tokSentences;
		    List<String> tokSentences = new ArrayList<String>();
		    for(CoreMap sentence: sentences) {
		    	  // traversing the words in the current sentence
		    	String tokSentence = "";
		    	for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
		    		tokSentence = tokSentence + token.originalText() + " ";
		    	}	
		    	tokSentence.trim();
	    		tokSentences.add(tokSentence);
		    }


		    
		    //coreference resolution begins here
		    Map<Integer, CorefChain> corefs = document.get(CorefCoreAnnotations.CorefChainAnnotation.class);
    		JSONObject doc = new JSONObject();
		    
		    //https://svn.ipd.kit.edu/AliceNLP/StanfordCoreNlpAnalyzer/trunk/src/main/java/edu/kit/ipd/ggPlugins/stanfordCoreNlp/StanfordCoreNlpCoreferencer.java
		    for(Map.Entry<Integer, CorefChain> entry : corefs.entrySet()) {
		    	CorefChain corefChain = entry.getValue();
		    	
		    	//self references will not be considered 
		    	if(corefChain.getMentionMap().entrySet().size() <= 1) {
		    		continue;
		    	}
		    	
		    	String fullChain = "";
		    	for (CorefMention temp : corefChain.getMentionsInTextualOrder()) {
		    		// doing my best to avoid regexp!
					fullChain = fullChain + "<" + temp.toString().split("\"")[1] + "> ";
				}

		    	for(CorefMention corefMention:corefChain.getMentionsInTextualOrder()){
			    	JSONObject elems = new JSONObject();
    				JSONObject info = new JSONObject();
    				// General info
    				info.put("start", corefMention.startIndex);
    				info.put("end", corefMention.endIndex-1);
    				info.put("tokens", corefMention.mentionSpan);
    				info.put("type", corefMention.mentionType);

    				// Head
    				boolean isHead = false;
    				if (corefMention.mentionID == corefChain.getRepresentativeMention().mentionID) {
    					isHead = true;	
    				}
    				info.put("isHead", isHead);
 
    				// Coreference chain, with and without the current word
       				info.put("chain", fullChain);
       				String restChain = fullChain.replace("<"+corefMention.mentionSpan+"> ","");
       				info.put("restChain", restChain);
       				
       				if (!isHead){
       					String[] tokens = tokSentences.get(corefMention.sentNum).split("\\s+");
       					tokens[corefMention.startIndex-1] = restChain+"_"+tokens[corefMention.startIndex-1];
       					tokens[corefMention.startIndex-1] = tokens[corefMention.startIndex-1].replaceAll("\\s+", "_");
       				
       					tokSentences.add(corefMention.sentNum, String.join(" ",tokens));
       				}
       				// Let's add everything for this mention
					//elems.put(String.valueOf(corefMention.startIndex), info);
       				//String rawSentence = tokSentences.get(corefMention.sentNum-1);
       				//String annSentence = rawSentence.replace(corefMention.mentionSpan, restChain + corefMention.mentionSpan);
					elems.put("string", tokSentences.get(corefMention.sentNum));
					
	    			doc.accumulate(String.valueOf(corefMention.sentNum), info);
	    			doc.accumulate(String.valueOf(corefMention.sentNum), elems);

	    		}
		    
		    }
		    System.out.println(doc.toString(2));

		    		    
	  }


}


/*System.out.println(
//"ChainID: " + corefChain.getChainID()		    	

" position: " + corefMention.position
+ " sentNum: " + corefMention.sentNum 
+ " mentionID: " + corefMention.mentionID 
+ " menSpan: " + corefMention.mentionSpan 
+ " menType: " + corefMention.mentionType 
+ " repMention: " + corefChain.getRepresentativeMention().mentionSpan
//+ " repMentionID: " + corefChain.getRepresentativeMention().mentionID
+ " start: " + corefMention.startIndex 
+ " end: " + corefMention.endIndex
+ chain.toString());	*/

