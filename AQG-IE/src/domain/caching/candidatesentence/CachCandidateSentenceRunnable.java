package domain.caching.candidatesentence;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gdata.util.common.base.Pair;

import domain.caching.candidatesentence.tool.CandidateSentenceGenerator;

import online.documentHandler.contentExtractor.ContentExtractor;

import utils.clock.Clock;
import utils.persistence.persistentWriter;

import edu.columbia.cs.ref.model.CandidateSentence;
import edu.columbia.cs.ref.tool.io.CandidatesSentenceWriter;
import exploration.model.Database;

public class CachCandidateSentenceRunnable implements Runnable {

	private persistentWriter pW;
	private ContentExtractor ce;
	private String content;
	private Database database;
	private long idDocument;
	private CandidateSentenceGenerator candsentGenerator;
	private Map<Integer, List<Pair<Long,Pair<Integer, Integer>>>> entitiesMap;
	private Map<Integer, String> entitiesTable;
	private Set<CandidateSentence> set;

	public CachCandidateSentenceRunnable(Database database,CandidateSentenceGenerator candsentGenerator, long idDocument, String content,
			persistentWriter pW, ContentExtractor ce, Map<Integer, List<Pair<Long,Pair<Integer, Integer>>>> entitiesMap, Map<Integer,String> entitiesTable, Set<CandidateSentence> set) {
		this.database = database;
		this.candsentGenerator = candsentGenerator;
		this.idDocument = idDocument;
		this.pW = pW;
		this.ce = ce;
		this.content = content;
		this.entitiesMap = entitiesMap;
		this.entitiesTable = entitiesTable;
		this.set = set;
	}

	@Override
	public void run() {
				
		String id = database.getId() + "-" + idDocument;
		
		Clock.startTime(id);
		
		Set<CandidateSentence> auxSet;
		
		try{
		
			auxSet = candsentGenerator.generateCandidateSentences(content, entitiesMap, entitiesTable);
		}catch (Exception e) {
			
			e.printStackTrace();
			System.err.println("Running anyway");
			auxSet = new HashSet<CandidateSentence>(0);
		
		}
		
		set.addAll(auxSet);
		
		Clock.stopTime(id);
		
		long time = Clock.getMeasuredTime(id);
		
		String file = pW.getCandidateSentencesFile(database.getId(),idDocument,ce, candsentGenerator.getRelationConfigurationId());
		
		CandidatesSentenceWriter.prepareCandidateSentences(set, file);

		pW.prepareCandidateSentenceGeneration(database.getId(),idDocument,ce,candsentGenerator.getRelationConfigurationId(),file, time,auxSet.size());
		
		pW.prepareGeneratedCandidateSentence(database.getId(),idDocument,candsentGenerator.getRelationConfigurationId(),ce);
		
	}

}