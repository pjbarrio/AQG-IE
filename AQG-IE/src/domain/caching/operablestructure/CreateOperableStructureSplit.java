package domain.caching.operablestructure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import cern.colt.Arrays;

import com.google.gdata.util.common.base.Pair;

import domain.caching.candidatesentence.tool.RelationConfiguration;

import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.SgmlContentExtraction;
import online.documentHandler.contentExtractor.impl.TikaContentExtractor;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import edu.columbia.cs.ref.model.CandidateSentence;
import edu.columbia.cs.ref.tool.io.CandidatesSentenceReader;
import exploration.model.Database;
import exploration.model.Document;

public class CreateOperableStructureSplit {

	
	
	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		
		int group = Integer.valueOf(args[0]); //1 to 9
		
		int relation = Integer.valueOf(args[1]); /*between 7 and 12 */
		
		int infEsys = Integer.valueOf(args[2]); /* 1 to 5  for the relation extractionsystem*/

		int numberOfDocs = Integer.valueOf(args[3]);
		
		String where = args[4];
		
		boolean aleatorize = Boolean.valueOf(args[5]);
		
		boolean checkExisting = Boolean.valueOf(args[6]);
		
		int firstRes = Integer.valueOf(args[7]);
		
		int lastRes = Integer.valueOf(args[8]);

		System.out.println(Arrays.toString(args));
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		List<Database> databases = pW.getSamplableDatabases(group);
		
		ContentExtractor ce = new TikaContentExtractor(); //new SgmlContentExtraction(); //new TikaContentExtractor(); // 
		
		int informationExtractionId = RelationConfiguration.getInformationExtractionSystemId(infEsys);
		
		int cachedInformationExtractionSystem = RelationConfiguration.getCachedInformationExtractionSystemId(infEsys);
		
		int relationConf = RelationConfiguration.getRelationConf(relation);
		
		int experiment = pW.getExperiment(relationConf,informationExtractionId);
		
		int hgj = 0;
		
		for (Database database : databases) {
			
			hgj++;
			
			System.out.println("Database: " + database.getId() + " - " + hgj + " out of: " + databases.size());
			
			pW.clearExperimentSplit(database,experiment);
			
			Set<Long> map = loadNonProcessedDocsCandidateSentences(pW, database, relationConf, ce, informationExtractionId, cachedInformationExtractionSystem, where, checkExisting, firstRes, lastRes);
			
			if (!map.isEmpty())
				pW.makeExperimentAvailable(experiment, database.getId());
				
			int split = 1;
			
			int procDocs = 1;
			
			List<Long> docs = new ArrayList<Long>(map);
			
			if (aleatorize)
				Collections.shuffle(docs);
			
			Set<Long> already = new HashSet<Long>();
			
			if (!checkExisting)
				already = pW.getProcessedCandidateSentences(database,relationConf,ce,informationExtractionId);
			
			for (Long idDoc : docs) {
				
				procDocs++;
				
				if ((procDocs % numberOfDocs) == 0){
					split++;
					System.out.println("split: " + split);
					pW.executeExperimentSplit();
				}
				if (already.contains(idDoc)){
					pW.prepareExperimentSplit(database,experiment,idDoc,split*(-1));
				}else{
					pW.prepareExperimentSplit(database,experiment,idDoc,split);
				}
			}
			
			pW.executeExperimentSplit();
			
		}
		
	}

	public static Set<Long> loadNonProcessedDocsCandidateSentences(
			persistentWriter pW, Database database, int relationConf,
			ContentExtractor ce, int informationExtractionSystem, int cachedInformationExtractionSystem, String where, boolean checkExisting,int firstRes, int lastRes) throws IOException, ClassNotFoundException {
		
		Map<Long, Pair<Integer, String>> candidateSentences = pW.getCandidateSentencesMap(getSearchRound(),database,relationConf,ce,cachedInformationExtractionSystem,where.contains("q"),where.contains("n"),where.contains("t"),where.contains("a"), firstRes,lastRes);
		
		if (checkExisting){		
			Set<Long> processedCandidateSentences = pW.getProcessedCandidateSentences(database,relationConf,ce,informationExtractionSystem);
			candidateSentences.keySet().removeAll(processedCandidateSentences);

		}

		return candidateSentences.keySet();
	}

	private static int getSearchRound() {
		return 3;
	}
}
