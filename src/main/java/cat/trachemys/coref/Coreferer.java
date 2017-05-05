package cat.trachemys.coref;

import org.json.JSONObject;

/**
 * Interface defining the methods for the coreference annotator
 * 
 * @author cristinae
 * @since 05.05.2017
 */
public interface Coreferer {

	String loadFile(String file);
	
	JSONObject annotateText(String text);
	
	void writeCoreferences(JSONObject doc, String outputFile);

}
