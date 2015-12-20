package utils.document;

import java.util.HashMap;
import java.util.Map;

import utils.persistence.persistentWriter;
import exploration.model.Database;
import exploration.model.Document;

public class MixedDocumentHandler extends DocumentHandler {

	private Map<Integer, Map<Long, Document>> dbMap;

	public MixedDocumentHandler(Database database, int experimentId,
			persistentWriter pW) {
		
		super(database, experimentId, pW);
		
		dbMap = new HashMap<Integer,Map<Long,Document>>();
		
	}

	@Override
	public Document getDocument(Database db, long id) {
		
		Map<Long,Document> idMap = dbMap.get(db.getId());
		
		if (idMap == null){
			idMap = new HashMap<Long, Document>();
			dbMap.put(db.getId(), idMap);
		}
		
		Document d = idMap.get(id);
		
		if (d == null){
			
			d = pW.getDocument(db,experimentId,id);
			
			idMap.put(id, d);
			
		}
		
		return d;
	}
	
	
}
