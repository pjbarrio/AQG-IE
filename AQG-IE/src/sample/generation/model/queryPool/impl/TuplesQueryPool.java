package sample.generation.model.queryPool.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import online.documentHandler.contentExtractor.ContentExtractor;

import org.apache.commons.io.FileUtils;

import execution.workload.impl.condition.UsefulCondition;
import execution.workload.querygeneration.QueryGenerator;
import execution.workload.tuple.Tuple;
import execution.workload.tuple.TupleReader;
import execution.workload.tuple.querygeneration.TupleQueryGenerator;
import exploration.model.Database;
import exploration.model.Document;
import exploration.model.DocumentHandle;
import exploration.model.Version;
import exploration.model.WorkloadModel;
import extraction.relationExtraction.RelationExtractionSystem;
import sample.generation.model.queryPool.QueryPool;
import utils.id.TuplesLoader;
import utils.persistence.InteractionPersister;
import utils.persistence.persistentWriter;

public class TuplesQueryPool extends QueryPool<Tuple> {

	private RelationExtractionSystem relationExtractionSystem;
	private RelationExtractionSystem relationExtractionSystemInstance;
	private UsefulCondition usefulCondition;
	private Database database;
	private Version version;
	private WorkloadModel workload;
	private ContentExtractor contentExtractor;
	private String[] relations;
	private InteractionPersister interactionPersister;
	private int maxSize;

	public TuplesQueryPool(boolean isForUseful, RelationExtractionSystem re, UsefulCondition uf, QueryGenerator<Tuple> qg, Version version, WorkloadModel workload,
			ContentExtractor contentExtractor, String[] relations, InteractionPersister interactionPersister, int maxSize) {
		super(qg,isForUseful);
		this.relationExtractionSystem = re;
		this.usefulCondition = uf;
		this.version = version;
		this.workload = workload;
		this.contentExtractor = contentExtractor;
		this.relations = relations;
		this.interactionPersister = interactionPersister;
		this.maxSize = maxSize;

	}

	@Override
	public void updateQueries(Document document) {
		
		Tuple[] t = relationExtractionSystemInstance.execute(/*database.getId(), */document);
		
		for (int i = 0; i < t.length; i++) {
			if (usefulCondition.isItUseful(t[i])){
				addQuery(t[i]);
			}
		}
		
	}

	@Override
	public void _initialize(Database database,persistentWriter pW, int version_seed, boolean reverse, int numberOfQueries) {

		this.database = database;
		
		relationExtractionSystemInstance = relationExtractionSystem.createInstance(database, interactionPersister, contentExtractor, relations);
		
		List<Tuple> initialSeed = loadInitialSeed(database,pW,version,workload, version_seed,relationExtractionSystemInstance.getName(),maxSize);
		
		for (Tuple tuple : initialSeed) {
			addQuery(tuple);
		}

		
	}

	private List<Tuple> loadInitialSeed(Database database, persistentWriter pW, Version version, WorkloadModel workload, int version_seed, String resName, int maxSize) {
		
		File tuplesFile = new File(pW.getSeedTuples(database.getName(), version.getName(), workload, version_seed,resName));
		
		List<String> tuples;
		try {
			
			tuples = FileUtils.readLines(tuplesFile);
			
			int limit = Math.min(tuples.size(), maxSize);
			
			List<Tuple> ret = new ArrayList<Tuple>(limit);
			
			for (int i = 0; i < limit; i++) {
				
				ret.add(TupleReader.generateTuple(tuples.get(i)));
				
			}
			
			return ret;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
		
	}

	@Override
	public void fetchQuery() {
		;		
	}

	@Override
	protected void _initialize(List<Tuple> initialList) {
		
		for (Tuple tuple : initialList) {
			
			addQuery(tuple);
			
		}
		
	}

}
