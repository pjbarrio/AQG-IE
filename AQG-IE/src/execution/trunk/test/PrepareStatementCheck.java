package execution.trunk.test;

import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.TikaContentExtractor;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;

public class PrepareStatementCheck {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		persistentWriter pW = PersistenceImplementation.getWriter();

		pW.prepareExtractedDocument(-5, 2,new TikaContentExtractor(), 3,new int[]{2},0);
		
	}

}
