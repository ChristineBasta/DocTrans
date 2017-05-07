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
	            	// We don't include the header of a chain, it can belong to different chains
	            	if (!jsonChain.getBoolean("isHead") ){
	            	    int index = jsonChain.getInt("start")-1;  //Before the mention
	            	    // Add the chain
	            	    tokens[index] = jsonChain.getString("restChain")+" "+tokens[index];
	            	}
	            }
        	    document.set(num, String.join(" ", tokens));
	        } catch (JSONException e) {
	            System.out.println("Error traversing the json object");
				e.printStackTrace();
	        }
	    }

	    String finalDoc = StringUtils.join(document, System.getProperty("line.separator"));
	    try {
			FileIO.stringToFile(new File(outputFile), finalDoc, true);
		} catch (IOException e) {
            System.out.println("Error writing "+finalDoc+" output file.");
			e.printStackTrace();
		}
			
	}

}