package online.documentHandler.contentLoader.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.mitre.jawb.io.SgmlDocument;

import exploration.model.Document;
import exploration.model.enumerations.ContentLoaderEnum;

import online.documentHandler.contentLoader.ContentLoader;
import utils.persistence.persistentWriter;

public class SgmlContentLoader extends ContentLoader {

	
	
	@Override
	public String loadContent(Document document,persistentWriter pW) {
		try {
			return new SgmlDocument(new FileReader(document.getFilePath(pW))).getSignalText();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public ContentLoaderEnum getLoaderEnum() {
		return ContentLoaderEnum.SGML;
	}
	
}
