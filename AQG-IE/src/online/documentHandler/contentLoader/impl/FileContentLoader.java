package online.documentHandler.contentLoader.impl;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import exploration.model.Document;
import exploration.model.enumerations.ContentLoaderEnum;

import online.documentHandler.contentLoader.ContentLoader;
import utils.persistence.persistentWriter;

public class FileContentLoader extends ContentLoader {

	
	
	@Override
	public String loadContent(Document document, persistentWriter pW) {
		try {
			return FileUtils.readFileToString(document.getFilePath(pW));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public ContentLoaderEnum getLoaderEnum() {
		return ContentLoaderEnum.FILE;
	}

}
