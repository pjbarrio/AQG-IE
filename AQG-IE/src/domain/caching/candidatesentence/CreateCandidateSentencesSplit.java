package domain.caching.candidatesentence;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.SgmlContentExtraction;
import online.documentHandler.contentExtractor.impl.TikaContentExtractor;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;

import com.google.gdata.util.common.base.Pair;

import domain.caching.candidatesentence.tool.RelationConfiguration;

import exploration.model.Database;

public class CreateCandidateSentencesSplit {

	class CandidateSentenceMapReaderRunnable implements Runnable{

		private Map<Long, Map<Integer, List<Pair<Long, Pair<Integer, Integer>>>>> ret;
		private Long idDoc;
		private File fil;

		public CandidateSentenceMapReaderRunnable(
				Map<Long, Map<Integer, List<Pair<Long, Pair<Integer, Integer>>>>> ret,
				Long idDoc, File file) {
			this.ret = ret;
			this.idDoc = idDoc;
			this.fil = file;
		}

		@Override
		public void run() {
			
			InputStream file;
			try {
				file = new FileInputStream( fil );
				InputStream buffer = new BufferedInputStream( file );
				ObjectInput input = new ObjectInputStream ( buffer );
				Map<Integer, List<Pair<Long, Pair<Integer, Integer>>>> candidates = (Map<Integer, List<Pair<Long, Pair<Integer, Integer>>>>)input.readObject();
				input.close();
				synchronized (ret) {
					ret.put(idDoc, candidates);
				}

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			
		}
		
	}

	class CandidateSentenceMapSaverRunnable implements Runnable{

		private File file;
		private Map<Integer, List<Pair<Long, Pair<Integer, Integer>>>> map;

		public CandidateSentenceMapSaverRunnable(File file,
				Map<Integer, List<Pair<Long, Pair<Integer, Integer>>>> map) {
			
			this.file = file;
			this.map = map;
			
		}

		@Override
		public void run() {
			
			
			try {
				ObjectOutput out = new ObjectOutputStream(new FileOutputStream(file));
				out.writeObject(map);
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		int group = Integer.valueOf(args[0]); //1 to 9
		
		int relationexperiment = Integer.valueOf(args[1]); /*between 7 and 12 */
		
		int relationConf = RelationConfiguration.getRelationConf(relationexperiment);
		
		int numberOfDocs = Integer.valueOf(args[2]);
		
		int firstRes = Integer.valueOf(args[3]);
		
		int lastRes = Integer.valueOf(args[4]);
		
		String where = args[5];
		
		persistentWriter pW = PersistenceImplementation.getWriter();

		int[][] entities = RelationConfiguration.getEntities(relationConf);
		
		ContentExtractor ce = new TikaContentExtractor();//new SgmlContentExtraction(); //
		
		List<Database> databases = pW.getSamplableDatabases(group);
		
//		List<Integer> requiredExperiments = pW.getRequiredExperiments(relationexperiment);
//		
//		Set<Integer> completedDatabases = pW.getDatabasesByStatus(requiredExperiments.get(0),ExperimentStatusEnum.FINISHED); //at least one
//		
//		for (int i = 1; i < requiredExperiments.size(); i++) {
//			
//			completedDatabases.retainAll(pW.getDatabasesByStatus(requiredExperiments.get(i),ExperimentStatusEnum.FINISHED));
//			
//		}
//		
//		databases.retainAll(completedDatabases);
		
		for (Database database : databases) {
			
			System.out.println("Database : " + database.getId());
			
			pW.clearExperimentSplit(database,relationexperiment);
			
			Set<Long> docs = loadNonProcessedCandidateSentenceDocuments(pW, database, entities, relationConf, ce, firstRes, lastRes, where);
			
			if (!docs.isEmpty())
				pW.makeExperimentAvailable(relationexperiment, database.getId());
			
			int split = 1;
			
			int procDocs = 1;
			
			for (Long  doc : docs) {
				
				procDocs++;
				
				if ((procDocs % numberOfDocs) == 0)
					split++;
				
				pW.prepareExperimentSplit(database,relationexperiment,doc,split);
				
//				prepareCandidateSentenceMap(pW,database,relationexperiment,entry.getKey(),entry.getValue());

			}
			
			pW.executeExperimentSplit();
			
//			executeCandidateSentenceMap();
			
		}
		
	}

	public static Set<Long> loadNonProcessedCandidateSentenceDocuments(
			persistentWriter pW, Database database, int[][] entities, int relationConf,ContentExtractor ce, int firstRes, int lastRes, String where) {
		
		List<Long> processedDocuments = pW.getProcessedDocumentsForCandidateSentences(database,relationConf,ce);
		
		return pW.getDocumentsCandidateSentenceForSplits(database,entities,processedDocuments,ce,firstRes,lastRes,where.contains("q"),where.contains("n"),where.contains("t"));
		
	}

	

	private static Map<String, Map<Integer, List<Pair<Long, Pair<Integer, Integer>>>>> candidateSentenceMap;
	
	public static void executeCandidateSentenceMap() {
		
		List<Thread> ts = new ArrayList<Thread>(getCandidateSentenceMap().size());
		
		for (Entry<String, Map<Integer,List<Pair<Long,Pair<Integer,Integer>>>>> entry : getCandidateSentenceMap().entrySet()) {
			
			Thread t = new Thread(new CreateCandidateSentencesSplit().new CandidateSentenceMapSaverRunnable(new File(entry.getKey()),entry.getValue()));
			
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
		
	}
	
	public static void prepareCandidateSentenceMap(persistentWriter pW, Database database,
			int idExperiment, long idDocument,
			Map<Integer, List<Pair<Long, Pair<Integer, Integer>>>> map) {
		
		String f = pW.getFileForCandidateSentenceMap(database,idExperiment,idDocument);
		
		getCandidateSentenceMap().put(f,map);
		
	}

	private static Map<String, Map<Integer, List<Pair<Long, Pair<Integer, Integer>>>>> getCandidateSentenceMap() {
		
		if (candidateSentenceMap == null){
			candidateSentenceMap = new HashMap<String, Map<Integer,List<Pair<Long,Pair<Integer,Integer>>>>>();
		}
		return candidateSentenceMap;
	}

	
}
