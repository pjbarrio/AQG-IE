package domain.caching.tuple;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import domain.caching.operablestructure.OperableStructureGeneratorRunnable;

import online.documentHandler.contentExtractor.ContentExtractor;
import utils.counter.Counter;
import utils.persistence.persistentWriter;
import edu.columbia.cs.ref.model.core.structure.OperableStructure;
import edu.columbia.cs.ref.model.re.Model;
import execution.workload.tuple.Tuple;
import exploration.model.Database;
import exploration.model.Document;

public class ObtainTuplesRunnable implements Runnable{

	private Model relationExtractionSystemInstance;
	private Database database;
	private ContentExtractor ce;
	private Map<Document, Set<OperableStructure>> table;
	private persistentWriter pW;
	private int tuplesRelationExtractionId;
	private String relation;
	private Counter counter;

	public ObtainTuplesRunnable(
			Model model,
			Database database, Map<Document, Set<OperableStructure>> table,
			ContentExtractor ce, persistentWriter pW,
			int tuplesRelationExtractionId, String relation, Counter counter) {
		
		this.relationExtractionSystemInstance = model;
		this.database = database;
		this.table = table;
		this.ce = ce;
		this.pW = pW;
		this.tuplesRelationExtractionId = tuplesRelationExtractionId;
		this.relation = relation;
		this.counter = counter;
	}

	@Override
	public void run() {
		
		for (Entry<Document,Set<OperableStructure>> entry : table.entrySet()) {
			
			Thread t = obtaiTuples(entry.getKey(),entry.getValue(),relationExtractionSystemInstance,database,ce,pW,tuplesRelationExtractionId,relation);
			
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			counter.inform();
			
		}
		
	}

	private Thread obtaiTuples(Document doc, Set<OperableStructure> opStruct,
			Model relationExtractionSystemInstance2,
			Database database, ContentExtractor ce, persistentWriter pW,
			int tuplesRelationExtractionId, String relation) {
		
		Thread t = new Thread(new TupleExtractorRunnable(doc,opStruct,relationExtractionSystemInstance2,database,ce,pW,tuplesRelationExtractionId,relation,new ArrayList<Tuple>(0)));
		
		t.start();
		
		return t;
		
	}

}
