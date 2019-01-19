package cat.trachemys.coref;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cat.trachemys.generic.FileIO;


/**
 * Implementation of all the common methods for any coreference annotator
 * 
 * @author cristinae
 * @since 05.05.2017
 */
public abstract class CorefererCommons {

	public static final String COREF_TAG = "_c";
	public static final String HEAD_TAG = "_h";
	public static final String COREF_BEGIN ="<b_crf>";	
	public static final String COREF_END ="<e_crf>";	
	public static final String NEUTRAL_TAG = "it";
	public static final String NEUTRAL_PL_TAG = "they";
	public static final String FEM_TAG = "she";
	public static final String MASC_TAG = "he";
	/**
	 * Object to store the output of an annotation
	 */
	class CorefDocs{
		public JSONObject doc;
		public List<String> tokSentences;		
	}

	/**
	 * Reads the content of a file into a string throwing the appropriate exception if
	 * not possible.
	 * 
	 * @param file
	 * @return String with the content of the file
	 */
	public String loadFile(String file) {
		
		String text = null;
		try {
			text = FileIO.fileToString(new File(file));
		} catch (IOException e) {
			System.out.println("File " +file+ " could not be read.");
			e.printStackTrace();
		}
		return text;
	}

	
	/**
	 * Adds a tag to each word in a chain. It is meant to be used as coreference
	 * information in a NMT system
	 * 
	 * @param chain
	 * @return
	 */
	public String addCorefTags(String chain) {		
		String[] words = chain.split("\\s+");
		String outputChain = "";
		for(String word : words){
			outputChain = outputChain +" "+ word +COREF_TAG;			
		}
		outputChain = outputChain.trim();
		if (outputChain.equals(COREF_TAG))
			outputChain = "";
		return outputChain;
	}

	
	/**
	 * Includes coreference tags available in the json object into the source file
	 * 
	 * TODO this will probably depend on the language since the JSON file might differ
	 * 
	 * @param dc
	 * @param outputFile
	 */
	public void writeCoreferences(CorefDocs cd, String outputFile){
		
		List<String> document = cd.tokSentences;
		JSONObject doc = cd.doc;
		
	    // Traversing the json
	    Iterator<String> iter = doc.keys();
	    while (iter.hasNext()) {
	        String sentenceNum = iter.next();
	        try {
	        	JSONArray value = new JSONArray();
	        	// If there is only a mention in a sentence, we don't have an array of JSONObjects but 
	        	// only a JSON object. We need to put in in an array.
	        	if (doc.get(sentenceNum).getClass().getName() == "org.json.JSONObject"){
	        		value.put(0, doc.get(sentenceNum));
	        	} else {
	        		value = (JSONArray) doc.get(sentenceNum);
	        	}
	            int num = Integer.valueOf(sentenceNum)-1;
       	        String tempSentence = document.get(num);
       	        String[] tokens = tempSentence.split(" ");
       	     	for (Object chains : value){
	            	JSONObject jsonChain = (JSONObject) chains;
            	    int index = jsonChain.getInt("start")-1;  //Before the mention
            	    String shortenedHead = jsonChain.getString("headShortened");
	            	// We don't enrich the header of the chain with itself
	            	if (!jsonChain.getBoolean("isHead") ){
	            	    // Add the chain
	            	    // Version 1: old representation with everything
	            	    //tokens[index] = jsonChain.getString("restChain")+" "+tokens[index];
            	    	//tokens[index] = "<"+shortenedHead+">"+ HEAD_TAG +" "+tokens[index];
	            	    // Version2: new representation supershortened version
                        /* if (shortenedHead != "-" && !tokens[index].matches("^I$")) { 
    	            	if (tokens.length <= index+1){
            	    		if (!tokens[index].toLowerCase().matches("\\Q"+shortenedHead.toLowerCase()+"\\E") ){
    	            	    	tokens[index] = COREF_BEGIN+" "+shortenedHead+" "+COREF_END+" "+tokens[index];
            	    		}	            	    	
            	    	} else {
		            	    String tokensLastTwo = tokens[index]+tokens[index+1];
            	    		if (!tokens[index].toLowerCase().matches("\\Q"+shortenedHead.toLowerCase()+"\\E")     
            	    				//articles before a name
            	    				&& (!tokens[index+1].toLowerCase().matches("\\Q"+shortenedHead.toLowerCase()+"\\E"))
                    	    	    && (!tokensLastTwo.toLowerCase().matches("\\Q"+shortenedHead.toLowerCase()+"\\E") ) ){
    	            	    	tokens[index] = COREF_BEGIN+" "+shortenedHead+" "+COREF_END+" "+tokens[index];
            	    		}	            	    	
            	    	}*/
                        // Version 3
	            		// For pronouns (unless "I") we enrich with the head when it is not the same word
	            	    if (shortenedHead != "-" && !tokens[index].matches("^I$") && jsonChain.getString("mentionType").contentEquals("PRONOMINAL")) { 
	    	            	if (tokens.length <= index+1){
	            	    		if (!tokens[index].toLowerCase().matches("\\Q"+shortenedHead.toLowerCase()+"\\E") ){
	    	            	    	tokens[index] = COREF_BEGIN+" "+shortenedHead+" "+COREF_END+" "+tokens[index];
	            	    		}	            	    	
	            	    	} else {
			            	    String tokensLastTwo = tokens[index]+tokens[index+1];
	            	    		if (!tokens[index].toLowerCase().matches("\\Q"+shortenedHead.toLowerCase()+"\\E")     
	            	    				//articles before a name
	            	    				&& (!tokens[index+1].toLowerCase().matches("\\Q"+shortenedHead.toLowerCase()+"\\E"))
	                    	    	    && (!tokensLastTwo.toLowerCase().matches("\\Q"+shortenedHead.toLowerCase()+"\\E") ) ){
	    	            	    	tokens[index] = COREF_BEGIN+" "+shortenedHead+" "+COREF_END+" "+tokens[index];
	            	    		}	            	    	
	            	    	}
	    	            // For nominal structures we enrich with the gender of the head
	            	    } else if(shortenedHead != "-" && 
	            	      (jsonChain.getString("mentionType").contentEquals("NOMINAL") || jsonChain.getString("mentionType").contentEquals("PROPER")) 
	            	       || jsonChain.getString("mentionType").contentEquals("PROPER")){
	    	            	if (tokens.length <= index+1){
	            	    		if (!tokens[index].toLowerCase().matches("\\Q"+shortenedHead.toLowerCase()+"\\E") ){
	   		            	        String anota = "-";
	   		            	        anota = tagWithGender(jsonChain);
	   		            	        if (anota != "-"){
	   		            	        	tokens[index] = COREF_BEGIN+" "+anota+" "+COREF_END+" "+tokens[index];
	   		            	        }	
	            	    		}
	    	            	}else {
			            	    String tokensLastTwo = tokens[index]+tokens[index+1];
	            	    		if (!tokens[index].toLowerCase().matches("\\Q"+shortenedHead.toLowerCase()+"\\E")     
	            	    				//articles before a name
	            	    				&& (!tokens[index+1].toLowerCase().matches("\\Q"+shortenedHead.toLowerCase()+"\\E"))
	                    	    	    && (!tokensLastTwo.toLowerCase().matches("\\Q"+shortenedHead.toLowerCase()+"\\E") ) ){
	            	    			String anota = "-";
	   		            	        anota = tagWithGender(jsonChain);
	   		            	        if (anota != "-"){
	   		            	        	tokens[index] = COREF_BEGIN+" "+anota+" "+COREF_END+" "+tokens[index];
	   		            	        }	
	            	    		}	            	    	
	            	    	}
	            	    }
	            	    
	            	    //System.out.println("join: "+String.join(" ",tokens[index]));
	            	    //System.out.println("-: ");            	    
	            	} else { // for the head
	            	    if (shortenedHead != "-") {
	            	    	String anota = "-";
	            	    	anota = tagWithGender(jsonChain);
            	    		if (anota != "-"){
    	            	    	tokens[index] = COREF_BEGIN+" "+anota+" "+COREF_END+" "+tokens[index];
            	    		}
	            	    }
	            		
	            	}
	            }
       	     	
        	    document.set(num, String.join(" ", tokens));
	        } catch (JSONException e) {
	            System.out.println("Error traversing the json object");
				e.printStackTrace();
	        }
	    }

	    String finalDoc = StringUtils.join(document, System.getProperty("line.separator"));
	    finalDoc = finalDoc.concat(System.getProperty("line.separator"));
	    try {
			FileIO.stringToFile(new File(outputFile), finalDoc, true);
		} catch (IOException e) {
            System.out.println("Error writing "+finalDoc+" output file.");
			e.printStackTrace();
		}
			
	}


	/**
	 * Returns a string indicating the gender of the header when known. If it is not known, but the
	 * subject is inanimate, we assign the neutral gender.
	 * Tags are currently defined as: he/she/it
	 * 
	 * @param jsonChain
	 * @return
	 */
	private String tagWithGender(JSONObject jsonChain) {
		
    	String anota = "-";
    	String gender = jsonChain.getString("headGender");
    	String animacy = jsonChain.getString("headAnim");   	
		if(gender.equalsIgnoreCase("NEUTRAL")) {
			if(jsonChain.getString("mentionNumber").equalsIgnoreCase("PLURAL")) anota = NEUTRAL_PL_TAG;
			else {anota = NEUTRAL_TAG;}
		}
		else if(gender.equalsIgnoreCase("FEMALE")) {anota = FEM_TAG;}
		else if(gender.equalsIgnoreCase("MALE")) {anota = MASC_TAG;}
		else if (animacy.equalsIgnoreCase("INANIMATE")){
			if(jsonChain.getString("mentionNumber").equalsIgnoreCase("PLURAL")) anota = NEUTRAL_PL_TAG;
			else {anota = NEUTRAL_TAG;}
		} 

		return anota;
	}

}
