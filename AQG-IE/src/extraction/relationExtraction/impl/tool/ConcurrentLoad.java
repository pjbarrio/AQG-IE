package extraction.relationExtraction.impl.tool;

import utils.persistence.persistentWriter;
import exploration.model.Document;

public class ConcurrentLoad implements Runnable{

	private Document doc;
	private persistentWriter pW;
	
	public ConcurrentLoad(Document document, persistentWriter pW) {
		this.doc = document;
		this.pW = pW;
	}

	@Override
	public void run() {
		doc.getContent(pW);			
	}
	
}