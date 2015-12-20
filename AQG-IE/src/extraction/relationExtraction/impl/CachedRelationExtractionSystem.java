package extraction.relationExtraction.impl;

import java.io.File;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import online.documentHandler.contentExtractor.ContentExtractor;
import utils.id.TuplesLoader;
import utils.persistence.InteractionPersister;
import utils.persistence.persistentWriter;
import execution.workload.tuple.Tuple;
import execution.workload.tuple.TupleReader;
import exploration.model.Database;
import exploration.model.Document;
import exploration.model.dummy.DummyWorkload;
import extraction.relationExtraction.RelationExtractionSystem;

public class CachedRelationExtractionSystem extends RelationExtractionSystem {

	private Database db;
	private String version;
	private int workload;
	private Hashtable<Long, ArrayList<String>> docTuplesTable;

	public CachedRelationExtractionSystem(persistentWriter pW, Database db, String version, int workload) {
		super(pW);
		this.db = db;
		this.version = version;
		this.workload = workload;
	}

	public CachedRelationExtractionSystem(Database db,persistentWriter pW,
			Map<Document,String> extractionTable, File extractionFolder,
			ContentExtractor contentExtractor, String... relations) {
		super(db,pW, relations, extractionTable, extractionFolder,
				contentExtractor);
	}

	private CachedRelationExtractionSystem(Database db,
			persistentWriter pW, String[] relations, Map<Document,String> extractionTable, File extractionFolder, ContentExtractor contentExtractor, Hashtable<Long, ArrayList<String>> docTuplesTable) {
		
		super(db,pW,relations,extractionTable,extractionFolder,contentExtractor);
		
		this.docTuplesTable = docTuplesTable;
	
	}

	@Override
	protected void extract(String content, Writer writer) {
		;
		//It will never come in
	}

	@Override
	protected String getExtractedFormat() {
		return "tuples";
	}

	@Override
	protected RelationExtractionSystem createInstance(Database db,persistentWriter pW, Map<Document,String> extractionTable, File extractionFolder,
			ContentExtractor contentExtractor, String...relations) {
		
		String matchingTuples = pW.getMatchingTuplesWithSourcesFile(db.getName(), version,new DummyWorkload(workload));
		
		Hashtable<Long, ArrayList<String>> docTuplesTable = TuplesLoader.loadIdtuplesTable(matchingTuples);
		
		return new CachedRelationExtractionSystem(db,pW,relations,extractionTable,extractionFolder,contentExtractor,docTuplesTable);
	}

	@Override
	public String getName() {
		return "CACHED_RELATION_SYSTEM";
	}

	@Override
	protected List<Tuple> extractProcessed(String relation, String identifier) {
		
		ArrayList<String> aux = docTuplesTable.get(Long.valueOf(identifier));
		
		List<Tuple> tuples = new ArrayList<Tuple>(aux.size());
		
		for (String tuple : aux) {
			
			tuples.add(TupleReader.generateTuple(tuple));
			
		}
		
		return tuples;
	}

	@Override
	protected List<Tuple> extractRecentlyProcessed(String relation,
			String string) {
		// will never get in here
		return null;
	}

	@Override
	protected void _clear() {
		docTuplesTable.clear();
		
	}

	@Override
	protected String generateId(Database database, String[] relations,
			InteractionPersister interactionPersister,
			ContentExtractor contentExtractor, int id) {
		
		Arrays.sort(relations);
		
		return new String(id + "-" + database.getId()+"-"+Arrays.toString(relations)+"-"+interactionPersister.getName()+"-"+contentExtractor.getName());
		
	}

	@Override
	protected int getId() {
		return -1;
	}

}
