package cat.trachemys.topic;

//http://www.javatips.net/api/TACIT-master/edu.usc.cssl.tacit.topicmodel.lda/src/edu/usc/cssl/tacit/topicmodel/lda/services/Vectors2Topics.java
//https://www.coursera.org/learn/text-mining-analytics/lecture/3fDGw/6-3-description-of-topic-modeling-with-mallet

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
import cc.mallet.topics.PolylingualTopicModel;
import cc.mallet.types.InstanceList;


/**
 * Runs Mallet for topic modeling on comparable corpora with multiple languages
 * 
 * @author cristinae
 * @since 05.07.2017
 */

public class PolyTopicLearnerMallet extends Commons{

	// Default parameters
	// TODO read from a config file and/or command line
	private int numTopics = 100;
	private float alpha =  0.1f; 
	private int threads = 20; 
	private int iters = 2000;
	// optimisation
	private int interval =  10;
	private int burning =  20;
	
	
	/**
	 * Constructor. Runs the training
	 * 
	 * @param input
	 * @param extension
	 * @param language
	 */
	public PolyTopicLearnerMallet(String inputFolder, String[] languages, String modelName) {
		
		InstanceList[] instances = new InstanceList[languages.length ];
		for (int i=0; i < instances.length; i++) {
	        FileIterator iterator =  new FileIterator(new File[] {new File(inputFolder)},
                    new ExtFilter(languages[i]), FileIterator.LAST_DIRECTORY);

			Pipe pipe = Commons.buildPipe(languages[i]);
            instances[i] = new InstanceList(pipe);
            instances[i].addThruPipe(iterator);
		}

        // Name for the model file
        if (modelName == null){
        	modelName = generateName(inputFolder);
        }
        
        // Create a model with 100 topics, alpha_t = 0.01, beta_w = 0.01
        modeler(instances, numTopics, alpha, burning, interval, iters, modelName);
		
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
		folder = folder.concat("_PT_"+formatter.format(date));
		
		return folder;
	}

	
	/**
	 * Method to run the modeling given some default/input parameters
	 * 
	 * @param instances
	 * @param numTopics
	 * @param alpha
	 * @param burning
	 * @param threads
	 * @param iters
	 */
    private void modeler(InstanceList[] instances, int numTopics, float alpha, int burning, int interval, int iters, String name) {
 
    	PolylingualTopicModel model = new PolylingualTopicModel (numTopics, alpha*numTopics);
		
		model.addInstances(instances);

		//model.setTopicDisplay(showTopicsInterval.value, topWords.value);
		model.setTopicDisplay(3, 15);
		
		
	    // Run the model for m iterations and stop 
	    // (for real applications, use 1000 to 2000 iterations)
	    model.setNumIterations(iters);            
        model.setOptimizeInterval(interval);
        model.setBurninPeriod(burning);

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
					
		options.addOption("l", "languages", true, 
				"Languages of the input texts separated by commas (en,de,it,nl,ro)");		
		options.addOption("i", "input", true, 
				"Input folder to annotate -one file per raw document and language-");		
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
			System.out.println("Please, set the languages\n");
			formatter.printHelp(Annotator.class.getSimpleName(),options );
			System.exit(1);
		} 

		return cLine;		
	}


	/**
	 * Main function to run the class, serves as example
	 * 
	 * @param args 
	 * 		-l Languages of the input text s
	 *      -i Input folder
	 *      -o Output file with the model
	 */
	public static void main(String[] args) {
		CommandLine cLine = parseArguments(args);
		
		// Languages
		String lan  = cLine.getOptionValue("l");      
		String[] languages = lan.split(",");
				
		// Input folder
		String input = cLine.getOptionValue("i");
		
		// Output files
		String output = null;
		if (cLine.hasOption("o")){
			output = cLine.getOptionValue("o");
		} 
		
		PolyTopicLearnerMallet topicM = new PolyTopicLearnerMallet(input, languages, output);
			
	}


}
