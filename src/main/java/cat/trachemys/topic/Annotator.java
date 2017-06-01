/**
 * 
 */
package cat.trachemys.topic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Main class for annotating sentences in documents with topic information
 * TODO: this should conduct all the pipeline if needed
 * 
 * @author cristinae
 * @since 08.05.2017
 */
public class Annotator {
	
	/** Language */
	//private final String lang;
	
	/**
	 * Constructor 
	 * 
	 * @param input
	 */
	public Annotator(File input){
		
		traverseInputFile(input);

	}
	

	/**
	 * Method to traverse an input file, extract the documents and the corresponding topic for 
	 * each document. Only the first topic is considered. For each document, the tagger is called.
	 * 
	 * @param input
	 */
	private void traverseInputFile(File input) {
		
		// Read the input
		FileInputStream inputStream = null;
		Scanner sc = null;
		try {
		    inputStream = new FileInputStream(input);
		    sc = new Scanner(inputStream, "UTF-8");
		    //int i = 0;
		    while (sc.hasNext()) {
		        String line = sc.nextLine();
		        Matcher m = Pattern.compile("file:(\\S+)\t(\\d+)\t").matcher(line);
		        if (m.find()){
		        	String document = m.group(1);
		        	String tag = "<"+m.group(2)+Commons.TOPIC_TAG+">";
		        	tagFile(document, tag);
		        } else {
					System.out.println("No matching file in line: " + line);			
		        }
		        //i++;
		    }
		    if (sc.ioException() != null) {
		        throw sc.ioException();
		    }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// Close everything
		    if (inputStream != null) {
		        try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		    }
		    if (sc != null) {
		        sc.close();
		    }
		}
		
	}


	/**
	 * Method to add a tag with the topic of the document to at the beginning of every line
	 * 
	 * @param document
	 * @param tag
	 */
	private void tagFile(String document, String tag) {
		
		//Define output file
		File fOut = new File(document+Commons.TOPIC_EXT);
		
		// Initialise the writer
		fOut.delete();
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			fw = new FileWriter(fOut, true);
			bw = new BufferedWriter(fw);
			bw.write("");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Read the input
		FileInputStream inputStream = null;
		Scanner sc = null;
		try {
		    inputStream = new FileInputStream(new File(document));
		    sc = new Scanner(inputStream, "UTF-8");
		    // Write the modified line
		    while (sc.hasNext()) {
		        String line = sc.nextLine();
		        bw.write(tag+" "+line+System.lineSeparator());
		    }
		    if (sc.ioException() != null) {
		        throw sc.ioException();
		    }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// Close everything
		    if (inputStream != null) {
		        try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		    }
		    if (sc != null) {
		        sc.close();
		    }
		    try {
				bw.close();
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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

		options.addOption("i", "input", true, 
				"Input file with the topic information for each document");		
		options.addOption("h", "help", false, "This help");

		try {			
		    cLine = parser.parse(options, args);
		} catch( ParseException exp ) {
			System.out.println("Unexpected exception:" + exp.getMessage() );			
		}	
		
		if (cLine.hasOption("h")) {
			formatter.printHelp(Annotator.class.getSimpleName(),options );
			System.exit(0);
		}
		
		if (cLine == null || !(cLine.hasOption("i")) ) {
			System.out.println("Please, point to the input file\n");
			formatter.printHelp(Annotator.class.getSimpleName(),options );
			System.exit(1);
		}		

		return cLine;		
	}


	/**
	 * Main function to run the class, serves as example
	 * 
	 * @param args 
	 *      -i Input file with the topic information for every document
	 */
	public static void main(String[] args) {
		CommandLine cLine = parseArguments(args);
		
		// Input file
		File input = new File(cLine.getOptionValue("i"));
				
		Annotator ann = new Annotator(input);
			
	}
	

}
