package domain.caching.operablestructure;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import online.documentHandler.contentExtractor.ContentExtractor;
import utils.counter.Counter;
import utils.persistence.persistentWriter;
import edu.columbia.cs.ref.model.CandidateSentence;
import edu.columbia.cs.ref.model.StructureConfiguration;
import edu.columbia.cs.ref.model.core.structure.OperableStructure;
import exploration.model.Database;
import exploration.model.Document;

public class GenerateOperableStructuresRunnable implements Runnable {

	private Database database;
	private Map<Long, Set<CandidateSentence>> candSents;
	private int relationConf;
	private ContentExtractor ce;
	private int informationExtractionId;
	private StructureConfiguration structureConfiguration;
	private persistentWriter pW;
	private Counter counter;
	
	public GenerateOperableStructuresRunnable(persistentWriter pW, StructureConfiguration structureConfiguration, Database database,
			Map<Long, Set<CandidateSentence>> candSents, int relationConf,
			ContentExtractor ce, int informationExtractionId, Counter counter) {
		this.pW = pW;
		this.structureConfiguration = structureConfiguration;
		this.database = database;
		this.candSents = candSents;
		this.relationConf = relationConf;
		this.ce = ce;
		this.informationExtractionId = informationExtractionId;
		this.counter = counter;
	}

	@Override
	public void run() {
		
		for (Entry<Long, Set<CandidateSentence>> candSent : candSents.entrySet()) {
			
			//only get here documents that have not yet been explored.
			
			try {
				
				Thread t = generateOperableStructure(database,structureConfiguration,new Document(database,candSent.getKey()),pW,ce, candSent.getValue(),relationConf, informationExtractionId);
				
				t.join();
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			counter.inform();
			
		}
		
	}

	private Thread generateOperableStructure(Database database,
			StructureConfiguration structureConfiguration, Document document,
			persistentWriter pW, ContentExtractor ce,
			Set<CandidateSentence> candidateSentences, int relationConf, int informationExtractionId) {
		
		Thread t = new Thread(new OperableStructureGeneratorRunnable(database,structureConfiguration,document,pW,ce,candidateSentences,relationConf,informationExtractionId,new HashSet<OperableStructure>(0)));
	
		t.start();
		
		return t;
	}

}
