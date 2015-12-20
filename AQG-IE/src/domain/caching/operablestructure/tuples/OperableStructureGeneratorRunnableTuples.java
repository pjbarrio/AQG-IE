package domain.caching.operablestructure.tuples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import opennlp.tools.util.InvalidFormatException;

import com.hp.hpl.jena.util.Tokenizer;

import online.documentHandler.contentExtractor.ContentExtractor;
import utils.clock.Clock;
import utils.persistence.persistentWriter;
import edu.columbia.cs.ref.algorithm.feature.generation.FeatureGenerator;
import edu.columbia.cs.ref.algorithm.feature.generation.impl.OpenNLPTokenizationFG;
import edu.columbia.cs.ref.model.CandidateSentence;
import edu.columbia.cs.ref.model.Span;
import edu.columbia.cs.ref.model.StructureConfiguration;
import edu.columbia.cs.ref.model.core.structure.OperableStructure;
import edu.columbia.cs.ref.model.feature.impl.SequenceFS;
import edu.columbia.cs.ref.model.re.Model;
import edu.columbia.cs.ref.tool.io.CandidatesSentenceWriter;
import edu.columbia.cs.ref.tool.io.CoreWriter;
import execution.workload.tuple.Tuple;
import exploration.model.Database;
import exploration.model.Document;
import extraction.relationExtraction.impl.LocalCachedRelationExtractionSystem;

public class OperableStructureGeneratorRunnableTuples implements Runnable {

	private Database database;
	private StructureConfiguration structureConfiguration;
	private persistentWriter pW;
	private ContentExtractor ce;
	private Set<CandidateSentence> candidateSenteces;
	private int relationConf;
	private int informationExtractionId;
	private Document document;
	private Set<OperableStructure> result;
	private Model model;
	private String relation;
	private int tuplesRelationExtractionId;

	public OperableStructureGeneratorRunnableTuples(Database database, Model model,
			StructureConfiguration structureConfiguration, Document document,
			persistentWriter pW, ContentExtractor ce,
			Set<CandidateSentence> candidateSentences, int relationConf,
			int informationExtractionId, Set<OperableStructure> result, String relation, int tuplesRelationExtractionId) {
		this.database = database;
		this.model = model;
		this.structureConfiguration = structureConfiguration;
		this.document = document;
		this.pW = pW;
		this.ce = ce;
		this.candidateSenteces = candidateSentences;
		this.relationConf = relationConf;
		this.informationExtractionId = informationExtractionId;
		this.result = result;
		this.relation = relation;
		this.tuplesRelationExtractionId = tuplesRelationExtractionId;
		
	} 

	@Override
	public void run() {
		
		if (candidateSenteces.isEmpty()){
			
			pW.prepareOperableStructureGeneration(document,ce,relationConf,informationExtractionId,"empty.os", 0);
			
			pW.prepareGeneratedOperableStructure(document,ce,relationConf,informationExtractionId);
			
			return;
			
		}
		
		String id = database.getId() + "-" + document + "-" + relationConf + "-" + informationExtractionId;
		
		System.out.println(id);
		
		Clock.startTime(id);
		
		
		for(CandidateSentence sent : candidateSenteces){
			
			int si = sent.getSentence().getLength();
			
			System.out.println("Size: " + si);
			
			if (si <= 2000){
				
				if (result == null)
					System.out.println("Result null");
				if (structureConfiguration == null)
					System.out.println("Structure Conf null");
				
				result.add(structureConfiguration.getOperableStructure(sent));
			}
		}
		
		Clock.stopTime(id);
		
		long time = Clock.getMeasuredTime(id);
		
		String file = pW.getOperableStructureFile(document,ce, relationConf,informationExtractionId);
		
		CoreWriter.prepareOperableStructures(result, file);

		pW.prepareOperableStructureGeneration(document,ce,relationConf,informationExtractionId,file, time);
		
		//will insert only the ones that contain candidate sentences.
		
		pW.prepareExtraction(pW.getRelationExtractionSystemId(relationConf,informationExtractionId), document, file,ce);
			
		pW.prepareGeneratedOperableStructure(document,ce,relationConf,informationExtractionId);
		
		//Now, the tuples
		
		List<Tuple> t = new ArrayList<Tuple>();
		
		synchronized (model) {

			id = database.getId() + "-" + document.getDatabase().getId() + "-" + document.getId() + "-" + tuplesRelationExtractionId;
			
			Clock.startTime(id);
			
			for (OperableStructure operableStructure : result) {
				
				if (model.predictLabel(operableStructure).contains(relation)){
					
					t.add(LocalCachedRelationExtractionSystem.generateTuple(operableStructure));
					
				}
				
			}

			Clock.stopTime(id);

			time = Clock.getMeasuredTime(id);
			
		}	
		
		pW.prepareInternalExtraction(tuplesRelationExtractionId,document,t,time,ce);
		
	}

}
