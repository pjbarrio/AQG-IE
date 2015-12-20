package online.navigation.textTransformer;

public class NumberReplaceMent implements TextTransformer {

	@Override
	public String transformImageFile(String imageFile) {
		return imageFile;
	}

	@Override
	public String transformText(String text) {
		
		String t = text.replaceAll("[0-9]", "").trim();
		
		if (t.isEmpty())
			return text;
		
		return t;
	}

	@Override
	public String transformImageText(String imageText) {
		return transformText(imageText);
	}

	@Override
	public TextTransformer personalize(String qur) {
		return new PersonalizedNumberReplacement(qur);
	}

}
