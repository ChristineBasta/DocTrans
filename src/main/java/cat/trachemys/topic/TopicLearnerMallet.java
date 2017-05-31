package cat.trachemys.topic;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.Input2CharSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;


/**
 * Runs Mallet for topic modelling
 * 
 * @author cristinae
 * @since 26.05.2017
 */

public class TopicModellerMallet {

	public TopicModellerMallet(String extension) {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Sequence of steps to apply
	 *  
	 * @return Pipe
	 */
    private Pipe buildPipe() {
    	
        ArrayList pipeList = new ArrayList();
        
        // Get file from resources folder
    	ClassLoader classLoader = getClass().getClassLoader();
    	File stopwords = new File(classLoader.getResource("en.sw").getFile());


        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add( new Input2CharSequence("UTF-8"));
        pipeList.add( new CharSequenceLowercase());
        pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
        pipeList.add( new TokenSequenceRemoveStopwords(stopwords, "UTF-8", false, false, false) );
        pipeList.add( new TokenSequence2FeatureSequence() );

        return new SerialPipes(pipeList);
    }

}
