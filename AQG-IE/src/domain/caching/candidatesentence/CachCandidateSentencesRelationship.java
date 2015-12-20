package domain.caching.candidatesentence;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Semaphore;

import com.google.gdata.util.common.base.Pair;

import domain.caching.candidatesentence.CreateCandidateSentencesSplit.CandidateSentenceMapReaderRunnable;
import domain.caching.candidatesentence.tool.CandidateSentenceGenerator;
import domain.caching.candidatesentence.tool.RelationConfiguration;
import domain.caching.entity.CreateEntitySplits;

import online.documentHandler.contentExtractor.ContentExtractor;
//import online.documentHandler.contentExtractor.impl.GoogleContentExtractor;
//import online.documentHandler.contentExtractor.impl.SgmlContentExtraction;
import online.documentHandler.contentExtractor.impl.TikaContentExtractor;
import online.documentRetriever.DocumentRetriever;
import utils.counter.Counter;
import utils.counter.command.impl.PersistCandidateSentenceCommand;
import utils.counter.command.impl.PersistEntityCommand;
import utils.document.DocumentHandler;
import utils.id.Idhandler;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;

import edu.columbia.cs.ref.model.constraint.role.impl.EntityTypeConstraint;
import edu.columbia.cs.ref.model.relationship.RelationshipType;
import exploration.model.Database;
import exploration.model.Document;
import exploration.model.enumerations.ExperimentStatusEnum;

public class CachCandidateSentencesRelationship {

	class DocumentCacher implements Runnable {

		private Document document;
		private Semaphore sp;
		private persistentWriter pW;

		public DocumentCacher(Document document, Semaphore sp, persistentWriter pW) {
			this.document = document;
			this.sp = sp;
			this.pW = pW;
		}

		@Override
		public void run() {
			
			document.getContent(pW);
			
			sp.release();
			
		}
		
	}
	
	private static boolean thereAreMore;

	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 * @throws ClassNotFoundException 
	 * @throws ClassCastException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException, ClassCastException, ClassNotFoundException {
		
		int group = Integer.valueOf(args[0]); //1 to 9
		
		int relationexperiment = Integer.valueOf(args[1]); /*between 7 and 12 */
		
		int relationConf = RelationConfiguration.getRelationConf(relationexperiment);
		
		int runningInstances = Integer.valueOf(args[2]);
		
		int numberOfDbs = Integer.valueOf(args[3]);
		
		int storeAfter = Integer.valueOf(args[4]);

		boolean fromPool = Boolean.valueOf(args[5]);
		
		int numberOfDocs = -1;
		
		if (!fromPool)
			numberOfDocs = Integer.valueOf(args[6]);
			
		int experimentId = -1; //1 for 3000,3001,3002 databases.
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		Counter counter = new Counter(storeAfter,new PersistCandidateSentenceCommand(pW));
		
		Map<Integer,ContentExtractor> contentExtractors = new HashMap<Integer, ContentExtractor>(runningInstances);
		
		List<Integer> requiredExperiments = null;
		
		Set<Integer> completedDatabases = null;
		
		if (!fromPool){
		
			requiredExperiments = pW.getRequiredExperiments(relationexperiment);
			
			completedDatabases = pW.getDatabasesByStatus(requiredExperiments.get(0),ExperimentStatusEnum.FINISHED); //at least one
			
			for (int i = 1; i < requiredExperiments.size(); i++) {
				
				completedDatabases.retainAll(pW.getDatabasesByStatus(requiredExperiments.get(i),ExperimentStatusEnum.FINISHED));
				
			}

		}
		List<Database> databases = pW.getSamplableDatabases(group);
		
		Collections.shuffle(databases);
		
		Map<Integer, CandidateSentenceGenerator> generators = new HashMap<Integer, CandidateSentenceGenerator>(runningInstances);
		
		Map<Integer,String> entitiesTable = getEntitiesTable(pW);
		
		int[][] entities = RelationConfiguration.getEntities(relationConf);
		
		Set<String> tags = RelationConfiguration.getTags(relationConf);
		
		Set<RelationshipType> relationshipTypes = getRelationshipType(relationConf,tags);
		
		for (int i = 0; i < databases.size() && numberOfDbs > 0; i++) {
			
			if (!fromPool && !completedDatabases.contains(databases.get(i).getId()))
				continue;
			
			if (!pW.isExperimentAvailable(relationexperiment,databases.get(i).getId(),pW.getComputerName())){
				continue;
			}
			
			//so that other processes can read them
			pW.makeExperimentAvailable(relationexperiment,databases.get(i).getId());
			
//			pW.reportExperimentStatus(relationexperiment,databases.get(i).getId(),pW.getComputerName(),ExperimentStatusEnum.RUNNING);

			Map<Long,Map<Integer,List<Pair<Long,Pair<Integer, Integer>>>>> allEntities = loadEntities(pW,databases.get(i),entities,relationConf,new TikaContentExtractor(),fromPool,numberOfDocs, relationexperiment);
			
			if (allEntities.isEmpty())
				pW.insertExperimentStatus(relationexperiment, databases.get(i).getId(),pW.getComputerName(), ExperimentStatusEnum.FINISHED);
			
			DocumentHandler dh = new DocumentHandler(databases.get(i), experimentId, pW,true, new ArrayList<Long>(allEntities.keySet()));
						
			Document[] files = loadDocuments(allEntities.keySet(),dh, databases.get(i));
			
			if (files.length > 0)
				numberOfDbs--;
			else
				continue;
			
			cachContent(files,pW);
			
			if (generators.isEmpty()){
				
				for (int pp = 0; pp < runningInstances; pp++) {
					
					generators.put(pp, createCandidateSentenceGenerator(pW,relationConf, tags, relationshipTypes));
					
					contentExtractors.put(pp, new TikaContentExtractor());
					
				}
				
			}
			
			Map<Integer,Document[]> splits = createSplits(files,runningInstances);
			
			Map<Integer,Map<Long,Map<Integer,List<Pair<Long,Pair<Integer, Integer>>>>>> entitiesSplit = generateEntitySplit(allEntities,splits);
			
			dh.clear();
			
			System.gc();
			
			List<Thread> ts = new ArrayList<Thread>(runningInstances);
			
			for (int j = 0; j < runningInstances; j++) {
				
				ts.add(new CachCandidateSentencesRelationship().generateCandidateSentences(j,generators.get(j),splits.get(j), databases.get(i),pW,contentExtractors.get(j), entitiesSplit.remove(j),entitiesTable,counter));
				
			}
		
			for (int k = 0; k < runningInstances; k++) {
				
				if (ts.get(k) != null)
					ts.get(k).join();
				
			}
			
			pW.persistCandidateSentences();
			
			if (thereAreMore && !fromPool)
				pW.makeExperimentAvailable(relationexperiment,databases.get(i).getId());
			else if (!thereAreMore && !fromPool)
				pW.insertExperimentStatus(relationexperiment,databases.get(i).getId(),pW.getComputerName(),ExperimentStatusEnum.FINISHED);
			
		}
			
	}

	private static void cachContent(Document[] files, persistentWriter pW) {
		
		System.out.println("Caching content...");
		
		Semaphore sp = new Semaphore(50);
		
		List<Thread> ts = new ArrayList<Thread>(files.length);
		
		for (Document document : files) {
			
			try {
				sp.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if ((ts.size() % 50)==0)
				System.out.println("Status ... " + ts.size());
			
			Thread t = new Thread(new CachCandidateSentencesRelationship().new DocumentCacher(document,sp,pW));
			
			t.start();
			
			ts.add(t);
			
		}
		
		for (Thread thread : ts) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

	private static Map<Long, Map<Integer, List<Pair<Long, Pair<Integer, Integer>>>>> loadEntities(
			persistentWriter pW, Database database, int[][] entities,
			int relationConf, ContentExtractor ce,
			boolean fromPool, int numberOfDocs, int relationExperiment) {
		
		thereAreMore = false;
		
		Set<Long> documents = null;
		
		if (fromPool){
			
			documents = CreateEntitySplits.loadFromSplit(pW, database, relationExperiment);
			
			if (documents.isEmpty()){
				return new HashMap<Long, Map<Integer,List<Pair<Long,Pair<Integer,Integer>>>>>(0);
			}
		}
		
		if (fromPool)
			return generateMap(pW.getEntitiesMap(database, entities, new ArrayList<Long>(documents), ce, true),-1);
		else
			return generateMap(pW.getEntitiesMap(database, entities, pW.getProcessedDocumentsForCandidateSentences(database,relationConf,ce), ce, false),numberOfDocs);
		
	}

	public static Map<Integer, String> getEntitiesTable(persistentWriter pW) {
		return reverse(pW.getEntityTypeTable());
	}

	public static Set<RelationshipType> getRelationshipType(int relationConf, Set<String> tags) {
		
		Set<RelationshipType> ret = new HashSet<RelationshipType>(1);
		
		RelationshipType relationshipType = new RelationshipType(RelationConfiguration.getType(relationConf),tags.toArray(new String[tags.size()]));
		
		for (String tag : tags) {
			
			relationshipType.setConstraints(new EntityTypeConstraint(tag), tag);
			
		}
		
		ret.add(relationshipType);
		
		return ret;
		
	}

	

	private static <A, B> Map<A, B> reverse(
			Map<B, A> table) {
		
		Map<A,B> ret = new HashMap<A, B>();
		
		for (Entry<B, A> entry : table.entrySet()) {
			
			ret.put(entry.getValue(), entry.getKey());
			
		}
		
		return ret;
		
	}


	public static Map<Integer, Map<Long, Map<Integer, List<Pair<Long,Pair<Integer, Integer>>>>>> generateEntitySplit(
			Map<Long, Map<Integer, List<Pair<Long,Pair<Integer, Integer>>>>> allEntities,
			Map<Integer, Document[]> idMaps) {
		
		Map<Integer, Map<Long, Map<Integer, List<Pair<Long,Pair<Integer, Integer>>>>>> ret = new HashMap<Integer, Map<Long,Map<Integer,List<Pair<Long,Pair<Integer,Integer>>>>>>();
		
		for (Entry<Integer, Document[]> entry : idMaps.entrySet()) {
			
			ret.put(entry.getKey(), generateEntityValueSplit(entry.getValue(),allEntities));
			
		}
		
		return ret;
	}

	

	private static Map<Long, Map<Integer, List<Pair<Long,Pair<Integer, Integer>>>>> generateEntityValueSplit(
			Document[] ids,
			Map<Long, Map<Integer, List<Pair<Long,Pair<Integer, Integer>>>>> allEntities) {
		
		Map<Long, Map<Integer, List<Pair<Long,Pair<Integer, Integer>>>>> ret = new HashMap<Long, Map<Integer,List<Pair<Long,Pair<Integer,Integer>>>>>();
		
		for (int i = 0; i < ids.length ; i++) {
			
			ret.put(ids[i].getId(), allEntities.remove(ids[i].getId()));
			
		}
		
		return ret;
		
	}

	public static Document[] loadDocuments(Set<Long> ids, DocumentHandler dh, Database db) {
		
		Document[] files = new Document[ids.size()];
		
		int i = 0;
		
		for (Long id : ids) {
			
			files[i] = dh.getDocument(db,id);
			
			i++;
			
		}
		
		return files;
	}

	

	public static Map<Integer, Map<String, Long>> createIds(
			Map<Integer, Document[]> splits, persistentWriter pW) {
		
		Map<Integer,Map<String,Long>> ret = new HashMap<Integer, Map<String,Long>>();
		
		for (Entry<Integer, Document[]> entry : splits.entrySet()) {
			
			ret.put(entry.getKey(), createMap(entry.getValue(),pW));
			
		}
		
		return ret;
		
	}

	private static Map<String, Long> createMap(Document[] files, persistentWriter pW) {
		
		Map<String,Long> ret = new HashMap<String, Long>(files.length);
		
		for (int i = 0; i < files.length; i++) {
			
			ret.put(files[i].getFilePath(pW).getAbsolutePath(), files[i].getId());
			
		}
		
		return ret;
	}

	private Thread generateCandidateSentences(int threadId, CandidateSentenceGenerator candidateSentenceGenerator, Document[] files, Database database, persistentWriter pW, ContentExtractor ce, Map<Long, Map<Integer, List<Pair<Long,Pair<Integer, Integer>>>>> entitiesMap, Map<Integer, String> entitiesTable, Counter counter) throws InterruptedException, IOException {
		
		Thread t = new Thread(new CandidateSentencesGeneratorRunnable(threadId,candidateSentenceGenerator,files,database,pW,ce,entitiesMap, entitiesTable,counter));
		
		t.start();
		
		return t;
		
	}

	public static Map<Integer, Document[]> createSplits(Document[] files,
			int runningInstances) {
		
		Map<Integer, Document[]> ret = new HashMap<Integer, Document[]>();
		
		int size = (int)Math.ceil((double)files.length / (double)runningInstances);
		
		int offset = 0;
		
		for (int i = 0; i < runningInstances; i++) {
			
			ret.put(i, Arrays.copyOfRange(files, Math.min(offset, files.length), Math.min(offset+size, files.length)));
			
			offset += size;
			
		}
		
		return ret;
	}

	public static CandidateSentenceGenerator createCandidateSentenceGenerator(persistentWriter pW, int relationId, Set<String> tags, Set<RelationshipType> relationshipTypes) throws ClassCastException, IOException, ClassNotFoundException {

		return new CandidateSentenceGenerator(pW, relationId,relationshipTypes,tags);
		
	}

	private static Map<Long, Map<Integer, List<Pair<Long,Pair<Integer, Integer>>>>> generateMap(
			Map<Long, Map<Integer, List<long[]>>> entitiesMap, int numberOfDocs) {
		
		Map<Long, Map<Integer, List<Pair<Long,Pair<Integer, Integer>>>>> ret = new HashMap<Long, Map<Integer,List<Pair<Long,Pair<Integer,Integer>>>>>();
		
		int procDocs = 0;
		
		for (Entry<Long, Map<Integer, List<long[]>>> entry : entitiesMap.entrySet()) {
			
			procDocs++;
			
			if (numberOfDocs > 0 && procDocs > numberOfDocs){
				thereAreMore = true;
				break;
			}
				
			ret.put(entry.getKey(), generateMapValue(entry.getValue()));
			
		}
		
		return ret;
		
	}

	private static Map<Integer, List<Pair<Long,Pair<Integer, Integer>>>> generateMapValue(
			Map<Integer, List<long[]>> value) {
		
		Map<Integer,List<Pair<Long,Pair<Integer,Integer>>>> ret = new HashMap<Integer, List<Pair<Long,Pair<Integer,Integer>>>>();
		
		for (Entry<Integer,List<long[]>> entry : value.entrySet()) {
			
			ret.put(entry.getKey(), generateList(entry.getValue()));
			
		}
		
		return ret;
		
	}

	private static List<Pair<Long,Pair<Integer, Integer>>> generateList(List<long[]> value) {
		
		List<Pair<Long,Pair<Integer,Integer>>> ret = new ArrayList<Pair<Long,Pair<Integer,Integer>>>();
		
		for (int i = 0; i < value.size(); i++) {
			
			ret.add(i,new Pair<Long,Pair<Integer,Integer>>(value.get(i)[0],new Pair<Integer, Integer>((int)value.get(i)[1], (int)value.get(i)[2])));
			
		}
		
		return ret;
		
	}

	public static Map<Long, Map<Integer, List<Pair<Long, Pair<Integer, Integer>>>>> loadEntitiesFromSplit(
			persistentWriter pW, Database database, int relationExperiment, int relationConf, ContentExtractor ce) {
		
		Set<Long> set = CreateEntitySplits.loadFromSplit(pW, database, relationExperiment);
		
		Map<Long, Map<Integer, List<Pair<Long, Pair<Integer, Integer>>>>> ret = new HashMap<Long, Map<Integer,List<Pair<Long,Pair<Integer,Integer>>>>>(set.size());
		
		List<Thread> ts = new ArrayList<Thread>(set.size());
		
		for (Long idDoc : set) {
			
			Thread t = new Thread(new CreateCandidateSentencesSplit().new CandidateSentenceMapReaderRunnable(ret, idDoc, new File(pW.getFileForCandidateSentenceMap(database, relationExperiment, idDoc))));
			
			ts.add(t);
			
			t.start();
			
		}
			
		try {
			for (Thread thread : ts) {
				thread.join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return ret;
		
	}
	

}
