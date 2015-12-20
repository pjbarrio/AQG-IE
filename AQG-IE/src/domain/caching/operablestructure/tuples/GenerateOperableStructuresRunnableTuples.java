package domain.caching.operablestructure.tuples;

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
import edu.columbia.cs.ref.model.re.Model;
import exploration.model.Database;
import exploration.model.Document;

public class GenerateOperableStructuresRunnableTuples implements Runnable {

	private Database database;
	private Map<Long, Set<CandidateSentence>> candSents;
	private int relationConf;
	private ContentExtractor ce;
	private int informationExtractionId;
	private StructureConfiguration structureConfiguration;
	private persistentWriter pW;
	private Counter counter;
	private Model model;
	private String relation;
	private int tuplesExtractionSystemId;
	
	public GenerateOperableStructuresRunnableTuples(persistentWriter pW, Model model, StructureConfiguration structureConfiguration, Database database,
			Map<Long, Set<CandidateSentence>> candSents, int relationConf,
			ContentExtractor ce, int informationExtractionId, Counter counter, String relation, int tuplesExtractionSystemId) {
		this.pW = pW;
		this.model = model;
		this.structureConfiguration = structureConfiguration;
		this.database = database;
		this.candSents = candSents;
		this.relationConf = relationConf;
		this.ce = ce;
		this.informationExtractionId = informationExtractionId;
		this.counter = counter;
		this.relation = relation;
		this.tuplesExtractionSystemId = tuplesExtractionSystemId;
		
	}

	@Override
	public void run() {
		
		for (Entry<Long, Set<CandidateSentence>> candSent : candSents.entrySet()) {
			
			//only get here documents that have not yet been explored.
			
			try {
				
				Thread t = generateOperableStructure(database,model,structureConfiguration,new Document(database,candSent.getKey()),pW,ce, candSent.getValue(),relationConf, informationExtractionId, relation, tuplesExtractionSystemId);
				
				t.join();
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			counter.inform();
			
		}
		
	}

	private Thread generateOperableStructure(Database database,Model model,
			StructureConfiguration structureConfiguration, Document document,
			persistentWriter pW, ContentExtractor ce,
			Set<CandidateSentence> candidateSentences, int relationConf, int informationExtractionId, String relation, int tuplesExtractionSystemId) {
		
		Thread t = new Thread(new OperableStructureGeneratorRunnableTuples(database,model,structureConfiguration,document,pW,ce,candidateSentences,relationConf,informationExtractionId,new HashSet<OperableStructure>(0),relation, tuplesExtractionSystemId));
	
		t.start();
		
		return t;
	}

}
