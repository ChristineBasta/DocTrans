/**
 * 
 */
package cat.trachemys.coref;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
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
 * Main class to apply the correference annotation with Stanford's CoreNLP for English
 * 
 * @author cristinae
 * @since 29.04.2017
 */
public class CorefMarkerStandford extends CorefererCommons implements Coreferer{
	

	/**
	 * Does the actual correference annotation of a text
	 * 
	 * @param text
	 * @return CorefDocs object: json object with the coreference chains and the complete tokenised document 
	 */
	public CorefDocs annotateText(String text){
		
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
	    		System.out.println("map:  "+corefChain.toString());
	    		continue;
	    	}
	    	// The previous one is not working?
	    	if(corefChain.getMentionsInTextualOrder().size() == 1) {
	    		System.out.println("mentions:  "+corefChain.toString());
	    		continue;
	    	}
	    	
	    	//Customising the format of the chains
		   	HashSet<String> mentions = new HashSet<String>();  //A hash to avoid duplicates
		   	HashSet<String> mentionsComplete = new HashSet<String>();  
		   	for (CorefMention temp : corefChain.getMentionsInTextualOrder()) {	    		
		   		String tempClean = temp.toString().split("\"")[1]; // doing my best to avoid regexp!
		   		
		   		// We store the original output just in case before preprocessing
		   		String infoComplete = ":s"+temp.sentNum+":ts"+temp.startIndex+":te"+temp.endIndex;
		   		mentionsComplete.add("<"+tempClean+infoComplete+">");
		   		
		   		// Starting preprocessing. We don't want full sentences
		   		tempClean = cleanMention(tempClean);
		   		//System.out.println(tempClean);
		   		if(tempClean.split("\\s+").length>3 || tempClean.equals("^\\s*$")){
		   			continue;
		   		}
		   		// Non-NEs can be lowercased
		   		if (temp.mentionType.name() == "PRONOMINAL" || temp.mentionType.name() == "NOMINAL"){
		   			mentions.add("<"+tempClean.toLowerCase()+">");
		   		} else {
		   			mentions.add("<"+tempClean+">");
		   		}
			}
	   		String completeChain = StringUtils.join(mentionsComplete, " "); //without preprocessing
		   	String fullChain = StringUtils.join(mentions, " "); // with preprocessing
		   	
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
				CorefMention headChain = corefChain.getRepresentativeMention();
				String head = headChain.mentionSpan;
				String headType = headChain.mentionType.toString();
				String headGender = headChain.gender.toString();
				String headAnim = headChain.animacy.toString();
				String headNumber = headChain.number.toString();
				info.put("head", head);
				info.put("headType", headType);
				info.put("headGender", headGender);
				info.put("headAnim", headAnim);
				info.put("headNumber", headNumber);
				String shortenedHead = shortenHead(headChain);
				info.put("headShortened", shortenedHead);

				// Coreference chain, with and without the current word
  				//System.out.println("mention: "+corefMention.mentionSpan);
   				//System.out.println(fullChain);
				String cleanMention = cleanMention(corefMention.mentionSpan);
				String mentionType = corefMention.mentionType.toString();
				String mentionGender = corefMention.gender.toString();
				String mentionAnim = corefMention.animacy.toString();
				String mentionNumber = corefMention.number.toString();

   				String restChain = fullChain.replaceAll("<\\s*\\Q"+cleanMention+"\\E>\\s*","");
      			restChain = restChain.replaceAll("<\\s*\\Q"+cleanMention.toLowerCase()+"\\E>\\s*","");
      			restChain = addCorefTags(restChain);
      			info.put("restChain", restChain);
      			info.put("originalChain", completeChain);
				info.put("mentionType", mentionType);
				info.put("mentionGender", mentionGender);
				info.put("mentionAnim", mentionAnim);
				info.put("mentionNumber", mentionNumber);
   							
    			doc.accumulate(String.valueOf(corefMention.sentNum), info);
    		}
	    
	    }
	    //System.out.println(doc.toString(2));
	    // Creating the output object
	    CorefDocs cd = new CorefDocs();
	    cd.doc = doc;
	    cd.tokSentences = getTokSentences(document);
	    
		return cd;

	}
	
	


	/**
	 * Removes undesired tokens in a mention
	 * For English: the, 's, and leading spaces
	 * 
	 * @param mention
	 * @return
	 */
	private String cleanMention(String mention){
		String cleanMention;
		// removes articles, saxon genitives and leading spaces
   		cleanMention = mention.replaceAll("\\bThe\\b","");
   		cleanMention = cleanMention.replaceAll("\\bthe\\b","");
  		cleanMention = cleanMention.replaceAll("\\s*'s\\b","");
   		cleanMention = cleanMention.trim();
		
		return cleanMention;		
	}
	


	/**
	 * Removes undesired tokens in a head similarly to other mentions,
	 * discards a head when it is a pronoun, and discards sentences (>3 tokens)
	 * 
	 * @param mention
	 * @return
	 */
	private String shortenHead(CorefMention mention){
		
		String cleanMention = "-";

		if (mention.mentionType.name() == "PRONOMINAL"){
			return cleanMention;
		} 
		if (mention.mentionSpan.split("\\s+").length>3 || mention.mentionSpan.equals("^\\s*$")){
			return cleanMention;
	   	}

		// removes articles, saxon genitives and leading spaces
   		cleanMention = mention.mentionSpan.replaceAll("\\bThe\\b","");
   		cleanMention = cleanMention.replaceAll("\\bthe\\b","");
  		cleanMention = cleanMention.replaceAll("\\s*'s\\b","");
   		cleanMention = cleanMention.trim();
		
		return cleanMention;		
	}
	
	/**
	 * Given a CoreNLP annotation document, the method retrieves the tokenised form of the input sentences
	 * 
	 * @param document
	 * @return a list with the input sentences tokenised
	 */
	private List<String> getTokSentences(Annotation document) {
		 
		    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
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
		    return tokSentences;
	}

	
	// just playing here
	public static void main(String[] args) throws Exception {

		String text = "Barack Obama was born in Honolulu and his wife in Chicago. \n " 
	                     + "He is the president. She is a lawer. I know him. \n "
	                     + "Obama was elected in 2008 and she is tall.";
	    
	    System.out.println("---");
	    System.out.println("coref chains");
	    System.out.println("JUST PLAYING! --not the real example");

 	    Annotation document = new Annotation(text);
 	    // Preparing the annotators
	    Properties props = new Properties();
	    props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,mention,coref");
	    props.setProperty("coref.algorithm", "neural");
	    props.put("ssplit.eolonly", "true");
	    
	    // Annotate
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
	    pipeline.annotate(document);

	    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
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
		    	
		   	
		   	//String fullChain = "";
		   	HashSet<String> mentions = new HashSet<String>();
		   	for (CorefMention temp : corefChain.getMentionsInTextualOrder()) {
	    		// doing my best to avoid regexp!
		   		String tempClean = temp.toString().split("\"")[1];
		   		if (temp.mentionType.name() == "PRONOMINAL"){
		   			mentions.add("<"+tempClean.toString().toLowerCase()+">");
		   		} else {
		   			mentions.add("<"+tempClean.toString()+">");
		   		}
				//fullChain = fullChain + "<" + temp.toString().split("\"")[1] + "> ";
			}
		   	String fullChain = StringUtils.join(mentions, " ");
		   	
		   	for(CorefMention corefMention:corefChain.getMentionsInTextualOrder()){
		   		//JSONObject elems = new JSONObject();
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
      			String restChain = fullChain.replace("<\\E"+corefMention.mentionSpan+"\\Q> ","");
      			restChain = restChain.replace("<\\E"+corefMention.mentionSpan.toLowerCase()+"\\Q> ","");
      			info.put("restChain", restChain);
       			/*	
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
				*/	
	    		doc.accumulate(String.valueOf(corefMention.sentNum), info);
	    		//doc.accumulate(String.valueOf(corefMention.sentNum), elems);
	    		
	    	}
		    
	    }
	    //System.out.println(doc.toString(2));
		   
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

