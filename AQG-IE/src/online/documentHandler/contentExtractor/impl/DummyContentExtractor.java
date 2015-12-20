package online.documentHandler.contentExtractor.impl;

import exploration.model.enumerations.ContentExtractionSystemEnum;
import online.documentHandler.contentExtractor.ContentExtractor;

public class DummyContentExtractor extends ContentExtractor {

	@Override
	public String extractContent(String content) {
		return content;
	}

	@Override
	public String getName() {
		return "DummyContentExtractor";
	}

	@Override
	public ContentExtractor newInstance() {
		return new DummyContentExtractor();
	}

	@Override
	public ContentExtractionSystemEnum getEnum() {
		return ContentExtractionSystemEnum.DUMMY_CONTENT_EXTRACTOR;
	}

	

}
