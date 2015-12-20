package execution.model.factory;

import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.GoogleContentExtractor;
import online.documentHandler.contentExtractor.impl.TikaContentExtractor;
import exploration.model.enumerations.ContentExtractionSystemEnum;

public class ContentExtractorFactory {

	public static ContentExtractor generateInstance(String contentExtractor) {
		
		switch (ContentExtractionSystemEnum.valueOf(contentExtractor)) {
		
		case GOOGLE_CONTENT_EXTRACTOR:
			
			return new GoogleContentExtractor();

		case TIKA_CONTENT_EXTRACTOR:
			
			return new TikaContentExtractor();	
			
		default:
			
			return null;
		}
		
	}

		
	
}
