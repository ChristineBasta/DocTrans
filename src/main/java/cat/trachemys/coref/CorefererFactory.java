package cat.trachemys.coref;


/**
 * Factory for including a coreferer annotator for different languages.
 * 
 * @author cristinae
 * @since 05.05.2017
 */
public class CorefererFactory {
	
	protected static Coreferer coreferer; 


	/** correference annotator for different languages.
	 * 
	 * @param language
	 * @return The correference annotator for the required language; null if not available.
	 */
	public Coreferer loadCoreferer(String language){
				
		
		switch(language){
		case "de":
			break;
		case "en":
			coreferer = new CorefMarkerStandford();
			break;
		/*case "es":
			break;
		case "fr":
			break;*/
		default:
			System.out.println("Only \"en\" can be choosen for your annotation now");
			return null;
		}
		return coreferer;
	}

	


}
