package domain.caching.coreference;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import domain.caching.coreference.tools.CoreferenceResolutor;

import online.documentHandler.contentExtractor.ContentExtractor;
import utils.clock.Clock;
import utils.persistence.persistentWriter;
import edu.columbia.cs.ref.model.CandidateSentence;
import edu.columbia.cs.ref.model.entity.CorefEntity;
import edu.columbia.cs.ref.tool.io.CandidatesSentenceWriter;
import edu.columbia.cs.ref.tool.tagger.entity.impl.StanfordNLPCoreference;
import edu.columbia.cs.ref.tool.tagger.span.impl.CorefEntitySpan;
import edu.columbia.cs.utils.Pair;
import exploration.model.Database;

public class ResolveCoreferencesRunnable implements Runnable {

	private Database database;
	private CoreferenceResolutor coreference;
	private long idDocument;
	private String content;
	private persistentWriter pW;
	private Map<Integer, List<Pair<Long, Pair<Integer, Integer>>>> entities;
	private Map<Integer, String> entitiesTable;
	private ContentExtractor contenExtractor;
	private int informationExtractionSystem;
	private int entityType;

	public ResolveCoreferencesRunnable(Database database,
			CoreferenceResolutor coreference, long idDocument,
			String content, persistentWriter pW,
			ContentExtractor contentExtractor,
			Map<Integer, List<Pair<Long, Pair<Integer, Integer>>>> entities,
			Map<Integer, String> entitiesTable, int informationExractionSystem,
			int entityType) {
		
		this.database = database;
		this.coreference = coreference;
		this.idDocument = idDocument;
		this.content = content;
		this.pW = pW;
		this.contenExtractor = contentExtractor;
		this.entities = entities;
		this.entitiesTable = entitiesTable;
		this.informationExtractionSystem = informationExractionSystem;
		this.entityType = entityType;

	}

	@Override
	public void run() {
		
		String id = database.getId() + "-" + idDocument + "-" + informationExtractionSystem + "-" + entityType;
		
		Clock.startTime(id);
		
		List<CorefEntitySpan> result = coreference.generateCoreferenceResolutions(content, entities, entitiesTable);
		
		Clock.stopTime(id);
		
		long time = Clock.getMeasuredTime(id);
		
		System.out.println("time for " +idDocument + ": " + time);
		
		for (CorefEntitySpan corefEntitySpan : result){
			
			pW.saveCorefEntity(database.getId(),idDocument,contenExtractor,informationExtractionSystem,entityType, Long.valueOf(corefEntitySpan.getRootEntity().getId()),corefEntitySpan.getOffset(),corefEntitySpan.getOffset()+corefEntitySpan.getLength(),time);
			
		}
		
		pW.saveCoreferenceResolution(database.getId(),idDocument,contenExtractor,informationExtractionSystem,entityType, time);

	}

}
