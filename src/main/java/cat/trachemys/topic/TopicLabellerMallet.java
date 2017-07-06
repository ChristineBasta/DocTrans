package cat.trachemys.topic;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.iterator.FileIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.InstanceList;


/**
 * Infers Mallet topics for a set of documents given a model or an inferencer
 * 
 * @author cristinae
 * @since 31.05.2017
 */

public class TopicLabellerMallet extends Commons{
	
    ParallelTopicModel model = null;
    
	// Default parameters
	// TODO read from a config file and/or command line
    int numIters = 20;        // The total number of iterations of sampling per document
    int thinning = 5;         // The number of iterations between saved samples
    int burnIn = 20;          // The number of iterations before the first saved sample
    double threshold = 0.01;  // The minimum proportion of a given topic that will be written
    int max = 5;              // The total number of topics to report per document] 
   
   	 
	public TopicLabellerMallet(String input, String extension, String language, TopicInferencer inferencer, String output) {
		
	       Inferer(input, extension,language, inferencer, output);
		    			
	}

	public TopicLabellerMallet(String input, String extension, String language, String modelFile, String output) {
			
		// Load the model
		model = loadModel(modelFile);
		       
	    if (model != null){
		    TopicInferencer inferencer = model.getInferencer();	
		    Inferer(input, extension,language, inferencer, output);
		        		
	    } else {
	    	System.out.println("The model could not be loaded.");
	    }
		    			
	}


	/**
	 * Runs the inferencer
	 * 
	 * @param input
	 * @param extension
	 * @param language
	 * @param inferencer
	 * @param output
	 */
	private void Inferer(String input, String extension, String language, TopicInferencer inferencer, String output){
		
		FileIterator iterator =  new FileIterator(new File[] {new File(input)},
                new ExtFilter(extension), FileIterator.LAST_DIRECTORY);
		Pipe pipe = Commons.buildPipe(language);
		InstanceList testInstances = new InstanceList(pipe);
		testInstances.addThruPipe(iterator);

		File outputFile = new File(output);

		try {
			inferencer.writeInferredDistributions(testInstances, outputFile, numIters, thinning, burnIn, threshold, max);
		} catch (IOException e) {
			e.printStackTrace();
	    	System.out.println("Statistics could not be extracted.");
		}        		

	}

		/**
		 * Loads a previously trained model from a file into a ParallelTopicModel object
		 * 
		 * @param modelFile
		 * @return ParallelTopicModel
		 */
		private ParallelTopicModel loadModel(String modelFile) {
			
		    try {
		        FileInputStream outFile = new FileInputStream(modelFile);
		        ObjectInputStream oos = new ObjectInputStream(outFile);
		        model = (ParallelTopicModel) oos.readObject();
			    oos.close();
		    } catch (IOException e) {
		        System.out.println("Could not read model from file: " + e);
		    } catch (ClassNotFoundException e) {
		        System.out.println("Could not load the model: " + e);
		    }

		    return model;
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
			options.addOption("m", "model", true, 
					"Previously trained model");		
			options.addOption("f", "inferencer", true, 
					"Previously trained inferencer");		
			options.addOption("o", "output", true, 
					"File where to save the statistics");		
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

			if ( !(cLine.hasOption("i")) ) {
				System.out.println("Please, specify the folder where documents are\n");
				formatter.printHelp(Annotator.class.getSimpleName(),options );
				System.exit(1);
			}		

			if ( !(cLine.hasOption("m")) && !(cLine.hasOption("f")) ) {
				System.out.println("Please, supply either the model or the inferencer\n");
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
		 *      -o File where to save the statistics
		 *      -m Previously trained model
		 *      -f Previously trained inferencer
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
			String input = cLine.getOptionValue("i");
						
			// Output files
			String output = null;
			if (cLine.hasOption("o")){
				output = cLine.getOptionValue("o");
			} 

			if(cLine.hasOption("m")){
				String model = cLine.getOptionValue("m");
				TopicLabellerMallet topicL = 
						new TopicLabellerMallet(input, extension, language, model, output);				
			} else if(cLine.hasOption("f")){
				String inferencer = cLine.getOptionValue("f");
				TopicLabellerMallet topicL = 
						new TopicLabellerMallet(input, extension, language, inferencer, output);				
				
			}
				
		}

}
