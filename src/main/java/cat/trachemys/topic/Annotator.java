/**
 * 
 */
package cat.trachemys.topic;

import java.io.File;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import cat.trachemys.generic.Check;

/**
 * Main class for annotating sentences in documents with topic information
 * 
 * @author cristinae
 * @since 08.05.2017
 */
public class Annotator {
	
	/** Language */
	private final String lang;
	
	/**
	 * Constructor 
	 * 
	 * @param language
	 */
	public Annotator(String language, String extension){
		Check.notNull(language);
		Check.notNull(extension);
		this.lang = language;
	}
	

	private void annotateFile(String file, String extension, boolean txt) {
		
	}

	/**
	 * Parses the command line arguments
	 * 	
	 * @param args
	 * 			Command line arguments 
	 * @return
	 */
	private static CommandLine parseArguments(String[] args)
	{	
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cLine = null;
		Options options= new Options();
		CommandLineParser parser = new BasicParser();

		options.addOption("l", "language", true, 
				"Language of the input text (en)");		
		options.addOption("e", "extension", true, 
				"Extension of the input documents (if different from the language)");		
		options.addOption("i", "input", true, 
				"Input folder to annotate -one file per raw document-");		
		options.addOption("o", "txt", true, 
				"Save document with correferences tagged 1/0 (default: 1)");		
		options.addOption("h", "help", false, "This help");

		try {			
		    cLine = parser.parse(options, args);
		} catch( ParseException exp ) {
			System.out.println("Unexpected exception :" + exp.getMessage() );			
		}	
		
		if (cLine.hasOption("h")) {
			formatter.printHelp(Annotator.class.getSimpleName(),options );
			System.exit(0);
		}
		
		if (cLine == null || !(cLine.hasOption("l")) ) {
			System.out.println("Please, set the language\n");
			formatter.printHelp(Annotator.class.getSimpleName(),options );
			System.exit(1);
		}		

		return cLine;		
	}


	/**
	 * Main function to run the class, serves as example
	 * 
	 * @param args 
	 * 		-l Language of the input text 
	 * 		-e Extension of the input documents
	 *      -i Input folder
	 *      -o create an output file with coreferences as tags to the source file
	 */
	public static void main(String[] args) {
		CommandLine cLine = parseArguments(args);
		
		// Language
		String language = cLine.getOptionValue("l");
		// Files extension
		String extension = language;
		if (cLine.hasOption("e")){
			extension = cLine.getOptionValue("e");
		} 	
		
		// Input folder
		File input = new File(cLine.getOptionValue("i"));
		
		// Output files
		boolean txt = true;
		if (cLine.hasOption("o")){
			txt = Integer.valueOf(cLine.getOptionValue("o")) != 0;
		} 
		
		// Run
		//List<String> files = FileIO.getFilesExt(input, extension);
		//for (String file : files) {
			//Annotator ann = new Annotator(language);
			//ann.annotateFile(file, extension, json, txt);
		//}
		Annotator ann = new Annotator(language, extension);
			
	}
	

}
