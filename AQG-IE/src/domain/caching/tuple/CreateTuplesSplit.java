package domain.caching.tuple;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.SgmlContentExtraction;
import online.documentHandler.contentExtractor.impl.TikaContentExtractor;
import utils.arff.myArffHandler;
//import online.documentHandler.contentExtractor.impl.SgmlContentExtraction;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import domain.caching.candidatesentence.tool.RelationConfiguration;
import exploration.model.Database;
import exploration.model.Document;
import exploration.model.enumerations.ExperimentStatusEnum;

public class CreateTuplesSplit {

	
	
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

		boolean askforCompletion = Boolean.valueOf(args[4]);
		
		boolean aleatorize = Boolean.valueOf(args[5]);
		
		boolean checkforExisting = Boolean.valueOf(args[6]);
		
		boolean fromSplit = Boolean.valueOf(args[7]);
		
		int spl = Integer.valueOf(args[8]);
		
		int informationExtractionId = RelationConfiguration.getInformationExtractionSystemId(infEsys);
		
		int cachedInformationExtractionId = RelationConfiguration.getCachedInformationExtractionSystemId(infEsys);
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		List<Database> databases = pW.getSamplableDatabases(group);
		
		ContentExtractor ce = new SgmlContentExtraction();//new TikaContentExtractor();//
		
		int relationConf = RelationConfiguration.getRelationConf(relation);
		
		int requiredExperiment = pW.getExperiment(relationConf,informationExtractionId);

		int experiment = requiredExperiment + 45; //it's tuples, not operable structure
		
		Set<Integer> completedDatabases = pW.getDatabasesByStatus(requiredExperiment,ExperimentStatusEnum.FINISHED); //at least one
		
		int hgj = 0;
		
		for (Database database : databases) {

			if (askforCompletion && !completedDatabases.contains(database.getId()))
				continue;
			myArffHandler.generateInstanceWithMissingValues(data, classIndex)
//			if (!pW.isExperimentAvailable(experiment,database.getId(),pW.getComputerName())){
//				continue;
//			}
			
			hgj++;
			
			System.out.println("Database: " + database.getId() + " - " + hgj + " out of: " + databases.size());
			
			pW.clearExperimentSplit(database,experiment);
			
			Set<Long> map = loadOperableStructures(pW, database, relationConf, ce, informationExtractionId, cachedInformationExtractionId, checkforExisting,fromSplit,database.getId(),requiredExperiment,spl);
			
			if (!map.isEmpty())
				pW.makeExperimentAvailable(experiment, database.getId());
				
			int split = 1;
			
			int procDocs = 1;
			
			List<Long> docs = new ArrayList<Long>(map);
			
			if (aleatorize){
				Collections.shuffle(docs);
			}
			
			Set<Long> done = new HashSet<Long>();
			
			if (!checkforExisting){
				Set<Document> doneDocs = pW.getExtractionTable(database.getId(), pW.getRelationExtractionSystemId(relationConf, cachedInformationExtractionId),ce).keySet();
				for (Document document : doneDocs) {
					done.add(document.getId());
				}
			}
				
			for (Long idDoc : docs) {
				
				procDocs++;
				
				if ((procDocs % numberOfDocs) == 0){
					split++;
					System.out.println("Split : " + split);
					pW.executeExperimentSplit();
				}
				if (!done.contains(idDoc))
					pW.prepareExperimentSplit(database,experiment,idDoc,split);
				else
					pW.prepareExperimentSplit(database,experiment,idDoc,split*(-1));
			}
			
			pW.executeExperimentSplit();
			
		}
		
	}

	private static Set<Long> loadOperableStructures(
			persistentWriter pW, Database database, int relationConf,
			ContentExtractor ce, int informationExtractionId, int cachedInformationExtractionId, boolean checkForExisting, boolean fromSplit, int db, int idOSExperiment, int split) throws IOException, ClassNotFoundException {
		
		Map<Document,String> opStructMap = null;
		
		if (fromSplit){
			
			opStructMap = pW.getExtractionTable(database.getId(), pW.getRelationExtractionSystemId(relationConf, informationExtractionId),ce,pW.getDocumentsBelowSplit(db, idOSExperiment, split));
			
		}else{
			opStructMap = pW.getExtractionTable(database.getId(), pW.getRelationExtractionSystemId(relationConf, informationExtractionId),ce);
		}
		
		
		
		Map<Document,String> already = new HashMap<Document, String>(0);
		
		if (checkForExisting)
			already = pW.getExtractionTable(database.getId(), pW.getRelationExtractionSystemId(relationConf, cachedInformationExtractionId),ce);
		
		opStructMap.keySet().removeAll(already.keySet());
		
		already.clear();
		
		Set<Long> res = new HashSet<Long>(opStructMap.size());
		
		for (Document d: opStructMap.keySet()) {
			
			res.add(d.getId());
			
		}
		
		return res;
		
	}


	
}
