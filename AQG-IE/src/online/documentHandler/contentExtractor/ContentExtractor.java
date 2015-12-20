package online.documentHandler.contentExtractor;

import exploration.model.enumerations.ContentExtractionSystemEnum;

public abstract class ContentExtractor {

	public abstract String extractContent(String content);

	public abstract String getName();

	public abstract ContentExtractor newInstance();

	public abstract ContentExtractionSystemEnum getEnum();
}
