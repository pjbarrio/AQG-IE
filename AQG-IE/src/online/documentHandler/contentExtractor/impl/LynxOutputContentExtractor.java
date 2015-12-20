package online.documentHandler.contentExtractor.impl;

import exploration.model.enumerations.ContentExtractionSystemEnum;
import online.documentHandler.contentExtractor.ContentExtractor;

public class LynxOutputContentExtractor extends ContentExtractor {

	@Override
	public String extractContent(String content) {
		
		String result = content.replaceAll("(\\[LINK\\]|\\[IMAGE\\]|\\[INLINE\\]|\\[_\\]|\\(_\\))", " ");//.replaceAll("(\\[.+?\\])", " ");
		
//		result = result.replaceAll("([\\W|_])"," ");
		
//		result = result.replaceAll("[^\\s]*\\d+[^\\s]*", " ");

		return result;
		
	}

	@Override
	public String getName() {
		
		return "LynxContentExtractor";
	}

	@Override
	public ContentExtractor newInstance() {
		return new LynxOutputContentExtractor();
	}

	@Override
	public ContentExtractionSystemEnum getEnum() {
		return ContentExtractionSystemEnum.LYNX_CONTENT_EXTRACTOR;
	}
	
	
}
