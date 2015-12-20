package online.navigation.textTransformer;

public class DummyTextTransformer implements TextTransformer {

	@Override
	public String transformImageFile(String imageFile) {
		return imageFile;
	}

	@Override
	public String transformText(String text) {
		return text;
	}

	@Override
	public String transformImageText(String imageText) {
		return imageText;
	}

	@Override
	public TextTransformer personalize(String qur) {
		return this;
	}

}
