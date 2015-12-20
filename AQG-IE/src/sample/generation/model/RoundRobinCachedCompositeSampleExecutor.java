package sample.generation.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.TikaContentExtractor;

import com.google.gdata.util.common.base.Pair;

import domain.caching.candidatesentence.tool.RelationConfiguration;

import sample.generation.model.cardinality.CardinalityFunction;
import sample.generation.model.impl.CachedSampleGenerator;
import utils.id.Idhandler;
import utils.id.TuplesLoader;
import utils.persistence.InteractionPersister;
import utils.persistence.persistentWriter;
import utils.persistence.impl.DiskBasedInteractionPersister;
import execution.workload.tuple.Tuple;
import exploration.model.Database;
import exploration.model.Document;
import exploration.model.Sample;
import exploration.model.clusterfunction.ClusterFunction;
import extraction.relationExtraction.RelationExtractionSystem;
import extraction.relationExtraction.impl.TupleRelationExtractionSystem;

public abstract class RoundRobinCachedCompositeSampleExecutor extends SampleExecutor {

	
	
	private ClusterFunction clusterFunction;
	private CardinalityFunction cardinalityFunction;
	private SampleConfiguration sampleConfiguration;
	private Map<Integer, int[]> generatedQueries;
	private RelationExtractionSystem res;
	private boolean aleatorize;
	private Hashtable<Integer, Map<Document, List<Tuple>>> tuplesTable;

	public RoundRobinCachedCompositeSampleExecutor(ClusterFunction clusterFunction, CardinalityFunction cardinalityFunction, int sampleConfiguration, persistentWriter pW, RelationExtractionSystem res, boolean aleatorize) {
		
		this.clusterFunction = clusterFunction;
		this.cardinalityFunction = cardinalityFunction;
		this.sampleConfiguration = pW.getSampleConfigution(sampleConfiguration);
		this.res = res;
		this.aleatorize = aleatorize;
		
	}

	@Override
	public boolean generateSample(persistentWriter pW, Sample sample,
			SampleConfiguration sampleConfiguration, int version_seed_pos,int version_seed_neg) {
		
		getTuplesTable().clear();
		
//		try {
		
		List<Database> databases = pW.getDatabasesInGroup(sample.getDatabase().getId(),clusterFunction,sample.getVersion(),sample.getWorkload());
		
		cardinalityFunction.calculateRequests(databases,sample.getSampleConfiguration());
		
		int usefulDocuments = 0;
		
		int uselessDocuments = 0;
		
		if (aleatorize)
			Collections.shuffle(databases);
		
		Map<Integer, List<int[]>> valuesTable = new HashMap<Integer, List<int[]>>();
		
		Map<Integer, List<Pair<int[], Long>>> valuesQuery = new HashMap<Integer, List<Pair<int[], Long>>>();
		
		int maxQueries = -1;
		
		for (int i = 0; i < databases.size(); i++) {
			
			usefulDocuments = cardinalityFunction.getUsefulDocuments(databases.get(i));
			
			uselessDocuments = cardinalityFunction.getUselessDocuments(databases.get(i));

			int sampleNumber = pW.getSampleId(this.sampleConfiguration.getId(),databases.get(i).getId(),version_seed_pos,version_seed_neg);
			
			List<int[]> cachedValues = pW.getSampleGeneration(sampleNumber,usefulDocuments+uselessDocuments);
			
			List<Pair<int[], Long>> cachedQuery = pW.getSampleGenerationQueries(sampleNumber,getMax(cachedValues,persistentWriter.QUERY_GENERATED_POSITION));
			
			Map<Document,List<Tuple>> relexSys = pW.getSampleTuples(sampleNumber);
			
			getTuplesTable().put(databases.get(i).getId(), relexSys);
			
			if (cachedQuery.size() > maxQueries)
				maxQueries = cachedQuery.size();
			
			valuesTable.put(databases.get(i).getId(),cachedValues);
			
			valuesQuery.put(databases.get(i).getId(),cachedQuery);
			
		}

		//round robin adding of the queries.
		
		for (int ddbb = 0; ddbb < databases.size(); ddbb++) {
			
			getGeneratedQueries().put(databases.get(ddbb).getId(), new int[valuesQuery.get(databases.get(ddbb).getId()).size() + 1]);
			
		}
		
		
		int auxInd = 0;
		
		for (int index = 0; index < maxQueries; index++) {
			
			for (int datb = 0; datb < databases.size(); datb++) {
				
				if (valuesQuery.get(databases.get(datb).getId()).size() <= index)
					continue;
				
				Pair<int[], Long> aux = valuesQuery.get(databases.get(datb).getId()).get(index);
			
//				sample.reportGeneratedQuery();
//				
//				pW.WriteSentQuery(databases.get(datb).getId(),sample.getGeneratedQueries(),sample,aux.getSecond());
				
				auxInd++;
				
				getGeneratedQueries().get(databases.get(datb).getId())[aux.getFirst()[persistentWriter.QUERY_GENERATED_POSITION]] = auxInd; //Gives it the new Generated Position.
				
			}
			
		}
			
		generateSample(sample, databases, pW, sampleConfiguration, valuesTable, valuesQuery);
		
		valuesTable.clear();
		
		valuesQuery.clear();
		
//		} catch (IOException e) {
//
//			e.printStackTrace();
//
//		}
	
		if (sampleConfiguration.ready(sample))
			return true;
		
		return false;
		
	}

	private Map<Integer, int[]> getGeneratedQueries() {
		
		if (generatedQueries == null){
			generatedQueries = new HashMap<Integer, int[]>();
		}
		return generatedQueries;
	}

	private void generateSample(Sample sample, List<Database> databases,
			persistentWriter pW, SampleConfiguration sampleConfiguration, Map<Integer, List<int[]>> valuesTable, Map<Integer, List<Pair<int[], Long>>> valuesQuery) {
		
		int dbIndex = 0;
		
		Map<Integer,Integer> lastSubmittedQueryTable = new HashMap<Integer,Integer>();
		
		for (int i = 0; i < databases.size(); i++) {
			
			lastSubmittedQueryTable.put(databases.get(i).getId(), -1);
			
		}
				
		while (!valuesTable.isEmpty() && !sampleConfiguration.ready(sample) && sampleConfiguration.keepProcessing(sample)){
			
			Database db = databases.get(dbIndex);
			
			dbIndex = (dbIndex + 1) % databases.size();
			
			if (!valuesTable.containsKey(db.getId()))
				continue;
			
			if (valuesTable.get(db.getId()).isEmpty()){
				valuesTable.remove(db.getId());
				continue;
			}
			
			int[] arr = valuesTable.get(db.getId()).remove(0);
			
			//ask if a new query is submitted
			
			int lastSubmittedQuery = lastSubmittedQueryTable.remove(db.getId());
			
			if (lastSubmittedQuery != arr[persistentWriter.QUERY_SUBMITTED_POSITION]){
				
				lastSubmittedQuery = arr[persistentWriter.QUERY_SUBMITTED_POSITION];
				
//XXX This might be the reason if something goes wrong.
				sample.reportGeneratedQuery();

				pW.WriteSentQuery(db.getId(),sample.getGeneratedQueries(),sample,valuesQuery.get(db.getId()).get(arr[persistentWriter.QUERY_GENERATED_POSITION]-1).getSecond());
				
				sample.addQuery(valuesQuery.get(db.getId()).get(arr[persistentWriter.QUERY_GENERATED_POSITION]-1).getSecond());
							
			}
			
			lastSubmittedQueryTable.put(db.getId(), lastSubmittedQuery);
			
			int doc_pos_in_sample = -1;
			
//			Document document = getIdHandler().get(db.getId()).getDocument((long)arr[persistentWriter.DOCUMENT_ID]);
			
			Document document = new Document(db,(long)arr[persistentWriter.DOCUMENT_ID]);
			
			if (arr[persistentWriter.DOCUMENT_ID] > 0){
			
				sample.addProcessedDocument(document);
							
				if (arr[persistentWriter.DOCUMENT_POSITION_IN_SAMPLE] > 0){ //if is in the sample
					
					if (arr[persistentWriter.USEFUL_TUPLES] > 0){ //if is useful
					
						sample.addUsefulDocument(document, sampleConfiguration);

						List<Tuple> t = getTuplesTable().get(db.getId()).get(document);
						
						if (t == null || t.isEmpty())
							System.err.println("Round Robin found a tuple that is empty: " + document.getDatabase().getId() + " - " + document.getId());
						else
							for (int j = 0; j < t.size(); j++) {
								
								sample.addTuple(document, t.get(j));
								
							}
						
					}else {
						
						sample.addUselessDocument(document, sampleConfiguration);
						
					}
					
					doc_pos_in_sample = sample.getSize();
					
				}
									
			}
						
			pW.addProcessedDocument(sample.getId(), getGeneratedQueries().get(db.getId())[arr[persistentWriter.QUERY_GENERATED_POSITION]], sample.getQueriesSent(), 
					arr[persistentWriter.QUERY_ROUND], sample.getProcessedDocuments().size(), arr[persistentWriter.DOCUMENT_IN_QUERY_POSITION], 
					doc_pos_in_sample, arr[persistentWriter.USEFUL_TUPLES], document);
			
		}
				
	}

	private int getMax(List<int[]> cachedValues, int index) {
		
		int max = -1;
		
		for (int i = 0; i < cachedValues.size(); i++) {
			
			if (cachedValues.get(i)[index] > max)
				max = cachedValues.get(i)[index];
			
		}
		
		return max;
	
	}
	
	@Override
	public abstract boolean samples(Database database);
	
	private Hashtable<Integer,Map<Document,List<Tuple>>> getTuplesTable() {
		
		if (tuplesTable == null){
			tuplesTable = new Hashtable<Integer,Map<Document,List<Tuple>>>();
		}
		return tuplesTable;
	}
	
}
