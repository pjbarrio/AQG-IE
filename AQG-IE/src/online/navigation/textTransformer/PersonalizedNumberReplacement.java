package online.navigation.textTransformer;

public class PersonalizedNumberReplacement extends NumberReplaceMent implements
		TextTransformer {

	private String query;

	public PersonalizedNumberReplacement(String qur) {
		this.query = qur;
	}

	@Override
	public TextTransformer personalize(String qur) {
		return new PersonalizedNumberReplacement(qur);
	}
	
	@Override
	public String transformText(String text) {
		
		String ret = text.toLowerCase().replace(query, "");
		
		return super.transformText(ret);
		
	}
	
	@Override
	public String transformImageText(String imageText) {
		return transformText(imageText);
	}
	
}
