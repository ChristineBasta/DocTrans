package cat.trachemys.coref;

import java.io.File;
import java.io.IOException;

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
	 * @param doc
	 * @param outputFile
	 */
	public void writeCoreferences(JSONObject doc, String outputFile){
		
	}

}
