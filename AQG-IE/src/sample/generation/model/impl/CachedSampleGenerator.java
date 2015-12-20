package sample.generation.model.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.TikaContentExtractor;

import com.google.gdata.util.common.base.Pair;

import domain.caching.candidatesentence.tool.RelationConfiguration;

import execution.workload.tuple.Tuple;
import exploration.model.Database;
import exploration.model.Document;
import exploration.model.Sample;
import exploration.model.Version;
import extraction.relationExtraction.RelationExtractionSystem;
import extraction.relationExtraction.impl.TupleRelationExtractionSystem;
import sample.generation.model.SampleConfiguration;
import sample.generation.model.SampleGenerator;
import utils.id.TuplesLoader;
import utils.persistence.InteractionPersister;
import utils.persistence.persistentWriter;
import utils.persistence.impl.DiskBasedInteractionPersister;

public class CachedSampleGenerator extends SampleGenerator {

	private static Hashtable<String,Hashtable<Document, ArrayList<Tuple>>> tuplesTable;

	private SampleConfiguration sampleConfiguration;

	private RelationExtractionSystem res;
	
	public CachedSampleGenerator(SampleConfiguration sampleConfigution, RelationExtractionSystem res) {
		this.sampleConfiguration = sampleConfigution;
		this.res = res;
	}

	@Override
	public boolean generateSample(Sample sample, Database database,
			persistentWriter pW, SampleConfiguration sampleConfiguration,
			int version_seed_pos,int version_seed_neg){
	
			sampleConfiguration = this.sampleConfiguration.createReducedCopy(sampleConfiguration.getUsefulNumber()-sample.getUseful().size(), sampleConfiguration.getUselessNumber()-sample.getUseless().size());
				
//			String mts = pW.getMatchingTuplesWithSourcesFile(database.getName(), sampleConfiguration.getVersion().getName(), sampleConfiguration.getWorkloadModel());
//			
//			Hashtable<Document, ArrayList<Tuple>> tupsTable = getTuplesTable(mts);

			int sampleNumber = pW.getSampleId(sampleConfiguration.getId(),database.getId(),version_seed_pos,version_seed_neg);
			
			List<int[]> cachedValues = pW.getSampleGeneration(sampleNumber,sampleConfiguration.getUsefulNumber()+sampleConfiguration.getUselessNumber());
			
			List<Pair<int[], Long>> cachedQuery = pW.getSampleGenerationQueries(sampleNumber,getMax(cachedValues,persistentWriter.QUERY_GENERATED_POSITION)); 
			
			Map<Document,List<Tuple>> tuples = pW.getSampleTuples(sampleNumber);
			
			//write the generatedQueries.
			
			int initialgenerated = sample.getGeneratedQueries();
						
			for (int i = 0; i < cachedQuery.size(); i++) {
				
				sample.reportGeneratedQuery();
				
				pW.WriteSentQuery(database.getId(),sample.getGeneratedQueries(),sample,cachedQuery.get(i).getSecond());
				
			}
			
			int lastSubmittedQuery = -1;
			
			for (int i = 0; i < cachedValues.size(); i++) {
				
				int[] arr = cachedValues.get(i);
				
				//ask if a new query is submitted
				
				if (lastSubmittedQuery != arr[persistentWriter.QUERY_SUBMITTED_POSITION]){
					
					lastSubmittedQuery = arr[persistentWriter.QUERY_SUBMITTED_POSITION];
					
					sample.addQuery(cachedQuery.get(arr[persistentWriter.QUERY_GENERATED_POSITION]-1).getSecond());
				
				}
					
				int doc_pos_in_sample = -1;
				
				Document document = new Document(database,(long)arr[persistentWriter.DOCUMENT_ID]);
				
				if (arr[persistentWriter.DOCUMENT_ID] > 0){
				
//					document = idHandler.getDocument((long)arr[persistentWriter.DOCUMENT_ID]);
				
					sample.addProcessedDocument(document);
								
					if (arr[persistentWriter.DOCUMENT_POSITION_IN_SAMPLE] > 0){ //if is in the sample
						
						if (arr[persistentWriter.USEFUL_TUPLES] > 0){ //if is useful
						
							sample.addUsefulDocument(document, sampleConfiguration);

							List<Tuple> t = tuples.get(document);
							
							if (t == null)
								System.out.println(document);
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
				
				pW.addProcessedDocument(sample.getId(), initialgenerated + arr[persistentWriter.QUERY_GENERATED_POSITION], sample.getQueriesSent(), 
						arr[persistentWriter.QUERY_ROUND], sample.getProcessedDocuments().size(), arr[persistentWriter.DOCUMENT_IN_QUERY_POSITION], 
						doc_pos_in_sample, arr[persistentWriter.USEFUL_TUPLES], document);
				
			}		
				
		if (sampleConfiguration.ready(sample)){
			return true;
		}

		return false;
	
	}

	private int getMax(List<int[]> cachedValues, int index) {
		
		int max = -1;
		
		for (int i = 0; i < cachedValues.size(); i++) {
			
			if (cachedValues.get(i)[index] > max)
				max = cachedValues.get(i)[index];
			
		}
		
		return max;
	
	}

//	private Hashtable<Document, ArrayList<Tuple>> getTuplesTable(
//			String matchingTuplesFile) throws IOException {
//		
//		Hashtable<Document, ArrayList<Tuple>> idtuplesTable =  getTuplesTable().get(matchingTuplesFile);
//		
//		if (idtuplesTable == null){
//			idtuplesTable = TuplesLoader.loadDocumenttuplesTuple(matchingTuplesFile);
//			getTuplesTable().put(matchingTuplesFile, idtuplesTable);
//		}
//		
//		return idtuplesTable;
//	}
	
//	private Hashtable<String,Hashtable<Document, ArrayList<Tuple>>> getTuplesTable() {
//		
//		if (tuplesTable == null){
//			tuplesTable = new Hashtable<String,Hashtable<Document, ArrayList<Tuple>>>();
//		}
//		return tuplesTable;
//	}
	
	

}
