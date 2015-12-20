package online.documentHandler.contentExtractor.impl;

import com.ctc.wstx.util.URLUtil;
import com.google.gdata.util.common.html.HtmlToText;

import exploration.model.enumerations.ContentExtractionSystemEnum;

import online.documentHandler.contentExtractor.ContentExtractor;

public class GoogleContentExtractor extends ContentExtractor {

	@Override
	public synchronized String extractContent(String content) {
		
		return HtmlToText.htmlToPlainText(content);
		
	}

	@Override
	public String getName() {
		
		return ContentExtractionSystemEnum.GOOGLE_CONTENT_EXTRACTOR.name();

	}

	@Override
	public ContentExtractor newInstance() {
		return new GoogleContentExtractor();
	}

	@Override
	public ContentExtractionSystemEnum getEnum() {
		return ContentExtractionSystemEnum.GOOGLE_CONTENT_EXTRACTOR;
	}
}
