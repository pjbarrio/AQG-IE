package execution.model.factory;

import exploration.model.enumerations.ContentLoaderEnum;
import online.documentHandler.contentLoader.ContentLoader;
import online.documentHandler.contentLoader.impl.FileContentLoader;
import online.documentHandler.contentLoader.impl.LynxContentLoader;
import utils.persistence.persistentWriter;

public class ContentLoaderFactory {

	public static ContentLoader generateInstance(String string) {
		
		switch (ContentLoaderEnum.valueOf(string)) {
		
		case LYNX:
			return new LynxContentLoader();
		case FILE:
			return new FileContentLoader();
		default:
			return null;
		
		}
		
	}

}
