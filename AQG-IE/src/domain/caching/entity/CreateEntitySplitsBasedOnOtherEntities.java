package domain.caching.entity;

import java.io.IOException;
import java.util.Arrays;
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

public class CreateEntitySplitsBasedOnOtherEntities {

	class CreateEntitySplitRunnable implements Runnable {

		private int i;
		private Semaphore sp;
		private Database database;
		private ContentExtractor contentExtractor;
		private int extractor;
		private int[] entities;
		private persistentWriter pW;
		private int entity;
		private int numberOfDocsInSplit;
		private int size;
		private List<Integer> ents;

		public CreateEntitySplitRunnable(int size, int numberOfDocsInSplit, int entity,persistentWriter pW, int i, Semaphore sp,
				Database database, ContentExtractor contentExtractor,
				int extractor, int[] entities,List<Integer> ents) {
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
			this.ents = ents;
			
		}

		@Override
		public void run() {
			
			System.out.println(database.getId() + " - Database: " + i + " out of: " + size);
			
			Set<Long> docs = loadNonProcessedDocuments(pW, database, ents, extractor, contentExtractor,entities);
			
			pW.clearExperimentSplit(database,entity);
						
			if (!docs.isEmpty())
				pW.makeExperimentAvailable(entity, database.getId());
			
			int split = 1;
			
			int procDocs = 1;
			
			for (Long idDoc : docs) {
				
				procDocs++;
				
				System.out.println(procDocs);
				
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
		
		int group = Integer.valueOf(args[0]); 
		
		int numberofDocumentsInSplit = Integer.valueOf(args[1]);
		
		int concurrentDbs = Integer.valueOf(args[2]);
		
		int[] entities = RelationConfiguration.getEntitiesForExp(4); //Always Person-Location

		List<Integer> ents = Arrays.asList(new Integer(3),new Integer(4),new Integer(5),new Integer(6),new Integer(7));//Arrays.asList(Integer.valueOf(args[3]));
		
		int extractor = RelationConfiguration.getExtractor(4);
		
		ContentExtractor contentExtractor = new TikaContentExtractor();//new SgmlContentExtraction();//
		
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
			
			new Thread(new CreateEntitySplitsBasedOnOtherEntities().new CreateEntitySplitRunnable(size,numberofDocumentsInSplit,4, pW, i, sp, databases.get(i), contentExtractor, extractor,entities,ents)).start();
			
		}
		
	}

	public static Set<Long> loadNonProcessedDocuments(persistentWriter pW, Database database,
			List<Integer> entities, int extractor, ContentExtractor contentExtractor,int[] ents) {
		
		Set<Long> ids = pW.getDocumentsInExtractedEntities(database, entities);
		
		System.out.println("Total: " + ids.size());
		
		Set<Long> processed = pW.getExtractedDocuments(database,ents,extractor, contentExtractor); //has processed all.
		
		System.out.println("Processed: " + processed.size());
		
		ids.removeAll(processed);
		
		System.out.println("To store: " + ids.size());
		
		return ids;
	}

	public static Set<Long> loadFromSplit(persistentWriter pW,
			Database database, int idExperiment) {
		
		int split = 1;
		
		while (!pW.isAvailable(database,idExperiment,split)){
			split++;
		}
		
		return pW.getDocumentsInSplit(database,idExperiment,split);
		
	}

}
