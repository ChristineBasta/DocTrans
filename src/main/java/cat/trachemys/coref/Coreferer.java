package cat.trachemys.coref;

import cat.trachemys.coref.CorefererCommons.CorefDocs;

/**
 * Interface defining the methods for the coreference annotator
 * 
 * @author cristinae
 * @since 05.05.2017
 */
public interface Coreferer {

	/**
	 * Reads the content of a file into a string throwing the appropriate exception if
	 * not possible. 
	 * Common to any language/tool
	 * 
	 * @param file
	 * @return String with the content of the file
	 */
	String loadFile(String file);

	/**
	 * Does the actual correference annotation of a text
	 * 
	 * @param text
	 * @return CorefDocs object: json object with the coreference chains and the complete tokenised document 
	 */
	CorefDocs annotateText(String text);
	
	/**
	 * Includes coreference tags available in the json object into the source file
	 * 
	 * @param cd
	 * @param outputFile
	 */
	void writeCoreferences(CorefDocs cd, String outputFile);

}
