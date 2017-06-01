package cat.trachemys.topic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.iterator.FileIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.InstanceList;


/**
 * Runs Mallet for topic modeling
 * 
 * @author cristinae
 * @since 26.05.2017
 */

public class TopicLearnerMallet extends Commons{

	// Default parameters
	// TODO read from a config file and/or command line
	private int numTopics = 100;
	private float alpha =  0.01f; 
	private float beta =  0.01f;
	private int threads = 20; 
	private int iters = 2000;
	
	
	/**
	 * Constructor. Runs the training
	 * 
	 * @param input
	 * @param extension
	 * @param language
	 */
	public TopicLearnerMallet(String input, String extension, String language, String modelName) {
		
        FileIterator iterator =  new FileIterator(new File[] {new File(input)},
                                                  new ExtFilter(extension), FileIterator.LAST_DIRECTORY);
        Pipe pipe = Commons.buildPipe(language);
        InstanceList instances = new InstanceList(pipe);
        instances.addThruPipe(iterator);

        // Name for the model file
        if (modelName == null){
        	modelName = generateName(input);
        }
        
        // Create a model with 100 topics, alpha_t = 0.01, beta_w = 0.01
        modeler(instances, numTopics, alpha, beta, threads, iters, modelName);
		
	}
	
	
	/**
	 * Generates a name for the model file so that different trainings do not collide
	 * 
	 * @param input
	 * @return
	 */
	private String generateName(String input) {
		
		String folder = input.substring(input.lastIndexOf(File.separator) + 1);
		Date date = new Date();
		Format formatter = new SimpleDateFormat("YYYY-MM-dd_hh-mm-ss");
		folder = folder.concat("_"+formatter.format(date));
		
		return folder;
	}

	
	/**
	 * Method to run the modeling given some default/input parameters
	 * 
	 * @param instances
	 * @param numTopics
	 * @param alpha
	 * @param beta
	 * @param threads
	 * @param iters
	 */
    private void modeler(InstanceList instances, int numTopics, float alpha, float beta, int threads, int iters, String name) {
 
	    //  Note that the first parameter is passed as the sum over topics, while
	    //  the second is the parameter for a single dimension of the Dirichlet prior.
	    ParallelTopicModel model = new ParallelTopicModel(numTopics, alpha*numTopics, beta);
	    model.addInstances(instances);
	
	    // Use n parallel samplers, which each look at 1/nth of the corpus and combine
	    //  statistics after every iteration.
	    model.setNumThreads(threads);
	
	    // Run the model for m iterations and stop 
	    // (for real applications, use 1000 to 2000 iterations)
	    model.setNumIterations(iters);
	    try {
			model.estimate();
		} catch (IOException e) {
			System.out.println("Topic model could not be estimated.");
			e.printStackTrace();
		}
	    
	    //https://stackoverflow.com/questions/14141195/folding-in-estimating-topics-for-new-documents-in-lda-using-mallet-in-java    
	    try {
	        FileOutputStream outFile = new FileOutputStream(name);
	        ObjectOutputStream oos = new ObjectOutputStream(outFile);
	        oos.writeObject(model);
	        oos.close();
	    } catch (FileNotFoundException e) {
	    	System.out.println("Output file for the model could not be created.");
			e.printStackTrace();
	    } catch (IOException e) {
	    	System.out.println("Output file for the model could not be written.");
			e.printStackTrace();
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

		options.addOption("l", "language", true, 
				"Language of the input text (en)");		
		options.addOption("e", "extension", true, 
				"Extension of the input documents (if different from the language)");		
		options.addOption("i", "input", true, 
				"Input folder to annotate -one file per raw document-");		
		options.addOption("o", "output", true, 
				"File where to save model");		
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
	 *      -o Output file with the model
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
		
		TopicLearnerMallet topicM = new TopicLearnerMallet(input, extension, language, output);
			
	}


}
