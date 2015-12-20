package online.documentHandler.contentLoader.impl;

import java.io.File;

import exploration.model.Document;
import exploration.model.enumerations.ContentLoaderEnum;

import online.documentHandler.contentLoader.ContentLoader;
import utils.persistence.persistentWriter;

public class InMemoryContentLoader extends ContentLoader {

	@Override
	public String loadContent(Document document,persistentWriter pW) {
		return document.getContent(pW);
	}

	@Override
	public ContentLoaderEnum getLoaderEnum() {
		return ContentLoaderEnum.MEMORY;
	}

	
}
