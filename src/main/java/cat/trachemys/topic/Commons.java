package cat.trachemys.topic;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Pattern;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.CharSequenceReplace;
import cc.mallet.pipe.Input2CharSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;


/**
 * Implementation of all the common methods for topic modeling
 * 
 * @author cristinae
 * @since 31.05.2017
 */
public abstract class Commons {

	// mark added to the number of topic for the translation system
	public static final String TOPIC_TAG = "_t";
	// file extension of the documents with every sentence tagged with the
	// topic of the document
	public static final String TOPIC_EXT = ".7topic";
	
	/**
	 * Sequence of steps to apply as a preprocessing to the documents
	 *  
	 * @param language
	 * @return Pipe
	 */
    public static Pipe buildPipe(String language) {
    	
        ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
        
        // Get file from resources folder
    	ClassLoader classLoader = Commons.class.getClassLoader();
    	//ClassLoader classLoader = getClass().getClassLoader();

    	InputStream input = Commons.class.getResourceAsStream(language+".sw");
    	File stopwords = getResourceAsFile(input);

    	//System.out.println(language+".sw");
    	//File stopwords = new File(classLoader.getResource(language+".sw").getFile());

        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add( new Input2CharSequence("UTF-8"));
        pipeList.add( new CharSequenceLowercase());
        // This tokeniser is not a tokeniser...
        pipeList.add( new CharSequenceReplace(Pattern.compile("'s"),""));
        pipeList.add( new CharSequenceReplace(Pattern.compile("'")," ' "));
        //pipeList.add( new SimpleTokenizer(SimpleTokenizer.USE_DEFAULT_ENGLISH_STOPLIST) );
        pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
        pipeList.add( new TokenSequenceRemoveStopwords(stopwords, "UTF-8", false, false, false) );
        pipeList.add( new TokenSequence2FeatureSequence() );

        return new SerialPipes(pipeList);
    }


    /**
     * Loads a resource as a file given an input stream
     * https://stackoverflow.com/questions/676097/java-resource-as-file
     * 
     * @param in
     * @return
     */
    public static File getResourceAsFile(InputStream in) {
        try {
            if (in == null) {
                return null;
            }

            File tempFile = File.createTempFile(String.valueOf(in.hashCode()), ".tmp");
            tempFile.deleteOnExit();

            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                //copy stream
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    
    /** File filter for the desired extension */
    class ExtFilter implements FileFilter {

        /** Test whether the string representation of the file 
         *   ends with the correct extension. Note that {@ref FileIterator}
         *   will only call this filter if the file is not a directory,
         *   so we do not need to test that it is a file.
         */
        private String ext;
        
        public ExtFilter(String ext){
            this.ext = ext.toLowerCase();
        }

        public boolean accept(File file) {
            return file.toString().endsWith(ext);
        }
    }

}
