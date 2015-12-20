package domain.caching.tuple;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.axis.encoding.Base64;

import online.documentHandler.contentExtractor.ContentExtractor;
import utils.clock.Clock;
import utils.persistence.persistentWriter;
import edu.columbia.cs.ref.model.CandidateSentence;
import edu.columbia.cs.ref.model.core.structure.OperableStructure;
import edu.columbia.cs.ref.model.re.Model;
import execution.workload.tuple.Tuple;
import exploration.model.Database;
import exploration.model.Document;
import extraction.relationExtraction.RelationExtractionSystem;
import extraction.relationExtraction.impl.LocalCachedRelationExtractionSystem;

public class TupleExtractorRunnable implements Runnable {

	private Document doc;
	private Set<OperableStructure> opStruct;
	private Model relationExtractionSystemInstance;
	private Database database;
	private ContentExtractor ce;
	private persistentWriter pW;
	private int tuplesRelationExtractionId;
	private String relation;
	private List<Tuple> t;

	public TupleExtractorRunnable(Document doc,
			Set<OperableStructure> opStruct,
			Model relationExtractionSystemInstance,
			Database database, ContentExtractor ce, persistentWriter pW,
			int tuplesRelationExtractionId, String relation, List<Tuple> tuples){
		this.doc = doc;
		this.opStruct = opStruct;
		this.relationExtractionSystemInstance = relationExtractionSystemInstance;
		this.database = database;
		this.ce = ce;
		this.pW = pW;
		this.tuplesRelationExtractionId = tuplesRelationExtractionId;
		this.relation = relation;
		this.t = tuples;
	}

	@Override
	public void run() {
		
		long time;
		
		synchronized (relationExtractionSystemInstance) {

			String id = database.getId() + "-" + doc.getDatabase().getId() + "-" + doc.getId() + "-" + tuplesRelationExtractionId;
			
			Clock.startTime(id);
			
			for (OperableStructure operableStructure : opStruct) {
				
				if (relationExtractionSystemInstance.predictLabel(operableStructure).contains(relation)){
					
					t.add(LocalCachedRelationExtractionSystem.generateTuple(operableStructure));
					
				}
				
			}

			Clock.stopTime(id);

			time = Clock.getMeasuredTime(id);
			
		}	
		
		pW.prepareInternalExtraction(tuplesRelationExtractionId,doc,t,time,ce);
		
	}

}
