package online.documentHandler.contentLoader;

import utils.persistence.persistentWriter;
import exploration.model.Document;
import exploration.model.enumerations.ContentLoaderEnum;

public abstract class ContentLoader {

	public abstract ContentLoaderEnum getLoaderEnum();
	
	public abstract String loadContent(Document document, persistentWriter pW);
	
}
