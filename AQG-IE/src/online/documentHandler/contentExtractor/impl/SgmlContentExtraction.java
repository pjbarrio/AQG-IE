package online.documentHandler.contentExtractor.impl;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import org.mitre.jawb.io.SgmlDocument;

import exploration.model.enumerations.ContentExtractionSystemEnum;

import online.documentHandler.contentExtractor.ContentExtractor;

public class SgmlContentExtraction extends ContentExtractor {

	@Override
	public String extractContent(String content) {
		try {
			return new SgmlDocument(new StringReader(content)).getSignalText();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getName() {
		return "SgmlContentExtractor";
	}

	@Override
	public ContentExtractor newInstance() {
		return new SgmlContentExtraction();
	}

	@Override
	public ContentExtractionSystemEnum getEnum() {
		return ContentExtractionSystemEnum.SGML_CONTENT_EXTRACTOR;
	}
	
	
}
