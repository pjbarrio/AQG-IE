package online.navigation.textTransformer;

public interface TextTransformer {

	public String transformImageFile(String imageFile);
	public String transformText(String text);
	public String transformImageText(String imageText);
	public TextTransformer personalize(String qur);
	
	
}
