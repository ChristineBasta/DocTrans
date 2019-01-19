/**
 * 
 */
package cat.trachemys.coref;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cat.trachemys.coref.CorefererCommons.CorefDocs;

/**
 * Test class for {@link cat.trachemys.coref.CorefMarkerStandford)}.
 * 
 * @author cristinae
 * @since 08.05.2017
*/
public class TestCorefMarkerStandford {

	private Coreferer cf;

	private final String text = "Barack Obama was born in Honolulu and his wife in Chicago. \n" 
            + "He is the president. She is a lawer.";

	private final String expectedTokSentence1 = 
			"Barack Obama was born in Honolulu and his wife in Chicago . ";
	private final String expectedTokSentence2 = 
			"He is the president . She is a lawer . ";

	// first version
	/*private final String expectedJSON = "{\"1\":[{\"originalChain\":\"<his:s1:ts8:te9> "
			+ "<He:s2:ts1:te2> <Barack Obama:s1:ts1:te3>\",\"restChain\":\"<his>_c <he>_c\","
			+ "\"start\":1,\"isHead\":true,\"end\":2,\"tokens\":\"Barack Obama\","
			+ "\"type\":\"PROPER\"},{\"originalChain\":\"<his:s1:ts8:te9> <He:s2:ts1:te2> "
			+ "<Barack Obama:s1:ts1:te3>\",\"restChain\":\"<Barack_c Obama>_c <he>_c\","
			+ "\"start\":8,\"isHead\":false,\"end\":8,\"tokens\":\"his\","
			+ "\"type\":\"PRONOMINAL\"}],\"2\":{\"originalChain\":\"<his:s1:ts8:te9> "
			+ "<He:s2:ts1:te2> <Barack Obama:s1:ts1:te3>\",\"restChain\":\"<Barack_c Obama>_c <his>_c\","
			+ "\"start\":1,\"isHead\":false,\"end\":1,\"tokens\":\"He\",\"type\":\"PRONOMINAL\"}}";
    */
	// More info added, version 2
	/*private final String expectedJSON = "{\"1\":[{\"head\":\"Barack Obama\","
			+ "\"originalChain\":\"<his:s1:ts8:te9> <He:s2:ts1:te2> <Barack Obama:s1:ts1:te3>\","
		    + "\"restChain\":\"<his>_c <he>_c\",\"headShortened\":\"Barack Obama\",\"start\":1,"
		    + "\"isHead\":true,\"end\":2,\"tokens\":\"Barack Obama\",\"type\":\"PROPER\"},"
		    + "{\"head\":\"Barack Obama\",\"originalChain\":\"<his:s1:ts8:te9> <He:s2:ts1:te2> "
		    + "<Barack Obama:s1:ts1:te3>\",\"restChain\":\"<Barack_c Obama>_c <he>_c\","
		    + "\"headShortened\":\"Barack Obama\",\"start\":8,\"isHead\":false,\"end\":8,"
		    + "\"tokens\":\"his\",\"type\":\"PRONOMINAL\"}],\"2\":{\"head\":\"Barack Obama\","
		    + "\"originalChain\":\"<his:s1:ts8:te9> <He:s2:ts1:te2> <Barack Obama:s1:ts1:te3>\","
		    + "\"restChain\":\"<Barack_c Obama>_c <his>_c\",\"headShortened\":\"Barack Obama\","
		    + "\"start\":1,\"isHead\":false,\"end\":1,\"tokens\":\"He\",\"type\":\"PRONOMINAL\"}}";
	*/
	
	// heuristics changed, version 3
	private final String expectedJSON = "{\"1\":[{\"headGender\":\"MALE\",\"restChain\":\"<his>_c <he>_c\","
	  		+"\"headType\":\"PROPER\",\"headShortened\":\"Barack Obama\",\"headAnim\":\"ANIMATE\","
	  		+"\"start\":1,\"isHead\":true,\"type\":\"PROPER\",\"head\":\"Barack Obama\","
	  		+"\"originalChain\":\"<his:s1:ts8:te9> <He:s2:ts1:te2> <Barack Obama:s1:ts1:te3>\","
	  		+"\"mentionGender\":\"MALE\",\"mentionAnim\":\"ANIMATE\",\"mentionNumber\":\"SINGULAR\",\"end\":2,"
	  		+"\"tokens\":\"Barack Obama\",\"mentionType\":\"PROPER\",\"headNumber\":\"SINGULAR\"},"
            +"{\"headGender\":\"MALE\",\"restChain\":\"<Barack_c Obama>_c <he>_c\",\"headType\":\"PROPER\","
            +"\"headShortened\":\"Barack Obama\",\"headAnim\":\"ANIMATE\",\"start\":8,\"isHead\":false,"
            +"\"type\":\"PRONOMINAL\",\"head\":\"Barack Obama\","
            +"\"originalChain\":\"<his:s1:ts8:te9> <He:s2:ts1:te2> <Barack Obama:s1:ts1:te3>\","
            +"\"mentionGender\":\"MALE\",\"mentionAnim\":\"ANIMATE\",\"mentionNumber\":\"SINGULAR\","
            +"\"end\":8,\"tokens\":\"his\",\"mentionType\":\"PRONOMINAL\",\"headNumber\":\"SINGULAR\"}],"
	        +"\"2\":{\"headGender\":\"MALE\",\"restChain\":\"<Barack_c Obama>_c <his>_c\","
	        +"\"headType\":\"PROPER\",\"headShortened\":\"Barack Obama\",\"headAnim\":\"ANIMATE\","
	        +"\"start\":1,\"isHead\":false,\"type\":\"PRONOMINAL\",\"head\":\"Barack Obama\","
	        +"\"originalChain\":\"<his:s1:ts8:te9> <He:s2:ts1:te2> <Barack Obama:s1:ts1:te3>\","
	        +"\"mentionGender\":\"MALE\",\"mentionAnim\":\"ANIMATE\",\"mentionNumber\":\"SINGULAR\","
	        +"\"end\":1,\"tokens\":\"He\",\"mentionType\":\"PRONOMINAL\",\"headNumber\":\"SINGULAR\"}}";

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		CorefererFactory corefererFactory = new CorefererFactory();				
		cf = corefererFactory.loadCoreferer("en");
	}

	/**
	 * 
	 */
	@After
	public void tearDown() throws Exception {		
	}

	/**
	 * Test method for {@link cat.trachemys.coref.CorefMarkerStandford#annotateText(java.lang.String)}.
	 */
	@Test
	public final void testAnnotateText() {
		CorefDocs cd = cf.annotateText(text);
		
		//Tokenised input (non-relevant)
		Assert.assertEquals(expectedTokSentence1, cd.tokSentences.get(0));
		Assert.assertEquals(expectedTokSentence2, cd.tokSentences.get(1));
		// Output of the coreference resolutor
		Assert.assertEquals(expectedJSON, cd.doc.toString());
	}

}
