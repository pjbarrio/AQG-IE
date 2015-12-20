package domain.caching.entity;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.TikaContentExtractor;
import utils.document.DocumentHandler;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import domain.caching.candidatesentence.tool.RelationConfiguration;
import exploration.model.Database;
import exploration.model.Document;

public class CreateEntitySplits {

	class CreateEntitySplitRunnable implements Runnable {

		private int i;
		private Semaphore sp;
		private Database database;
		private ContentExtractor contentExtractor;
		private int extractor;
		private int[] entities;
		private int firstRes;
		private int lastRes;
		private persistentWriter pW;
		private int entity;
		private int numberOfDocsInSplit;
		private int size;
		private String where;

		public CreateEntitySplitRunnable(int size, int numberOfDocsInSplit, int entity,persistentWriter pW, int i, Semaphore sp,
				Database database, ContentExtractor contentExtractor,
				int extractor, int[] entities, int firstRes, int lastRes, String where) {
			this.size = size;
			this.numberOfDocsInSplit = numberOfDocsInSplit;
			this.entity = entity;
			this.pW = pW;
			this.i = i;
			this.sp = sp;
			this.database = database;
			this.contentExtractor = contentExtractor;
			this.extractor = extractor;
			this.entities = entities;
			this.firstRes = firstRes;
			this.lastRes = lastRes;
			this.where = where;
		}

		@Override
		public void run() {
			
			System.out.println(database.getId() + " - Database: " + i + " out of: " + size);
			
			Set<Long> docs = loadNonProcessedDocuments(pW, database, entities, extractor, contentExtractor,firstRes,lastRes,where);
			
			pW.clearExperimentSplit(database,entity);
						
			if (!docs.isEmpty())
				pW.makeExperimentAvailable(entity, database.getId());
			
			int split = 1;
			
			int procDocs = 1;
			
			for (Long idDoc : docs) {
				
				procDocs++;
				
				if ((procDocs % numberOfDocsInSplit) == 0)
					split++;
				
				pW.prepareExperimentSplit(database,entity,idDoc,split);
				
			}
			
			pW.executeExperimentSplit();
			
			try {
				pW.endAlgorithm();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			sp.release();
			
		}
		
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.err.println("If the experiment Id changed, remember that QueryResults needs that value");
		
		
		int group = Integer.valueOf(args[0]); 
		
		int entity = Integer.valueOf(args[1]);
		
		int firstRes = Integer.valueOf(args[2]);
		
		int lastRes = Integer.valueOf(args[3]);
		
		int numberofDocumentsInSplit = Integer.valueOf(args[4]);
		
		int concurrentDbs = Integer.valueOf(args[5]);
		
		String where = args[6];
		
		int[] entities = RelationConfiguration.getEntitiesForExp(entity);

		int extractor = RelationConfiguration.getExtractor(entity);
		
		ContentExtractor contentExtractor = new TikaContentExtractor();
		
		List<Database> databases = PersistenceImplementation.getWriter().getSamplableDatabases(group);
		
		Collections.shuffle(databases);
		
		Semaphore sp = new Semaphore(concurrentDbs);
		
		int size = databases.size();
		
		for (int i = 0; i < databases.size(); i++) {
			
			try {
				sp.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			persistentWriter pW = PersistenceImplementation.getNewWriter();
			
			new Thread(new CreateEntitySplits().new CreateEntitySplitRunnable(size,numberofDocumentsInSplit,entity, pW, i, sp, databases.get(i), contentExtractor, extractor,entities,firstRes,lastRes,where)).start();
			
		}
		
	}

	public static Set<Long> loadNonProcessedDocuments(persistentWriter pW, Database database,
			int[] entities, int extractor, ContentExtractor contentExtractor, int firstRes, int lastRes,String where) {
		
		Set<Long> ids = pW.getDocumentsInQueryResults(getSearchRound(),database,entities,firstRes,lastRes,where.contains("q"),where.contains("n"),where.contains("t"));
		
		Set<Long> processed = pW.getExtractedDocuments(database,entities,extractor, contentExtractor); //has processed all.
		
		ids.removeAll(processed);
		
		return ids;
	}

	private static int getSearchRound() {
		return 3;
	}

	public static Set<Long> loadFromSplit(persistentWriter pW,
			Database database, int idExperiment) {
		
		int split = pW.getNextPossibleSplit(database, idExperiment);
		
		while (!pW.isAvailable(database,idExperiment,split)){
			split++;
		}
		
		return pW.getDocumentsInSplit(database,idExperiment,split);
		
	}

}
