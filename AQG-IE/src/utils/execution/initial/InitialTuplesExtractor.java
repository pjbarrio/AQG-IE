package utils.execution.initial;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.queryParser.QueryParser;

import online.documentHandler.OnlineDocumentHandler;
import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.GoogleContentExtractor;
import online.documentHandler.contentExtractor.impl.TikaContentExtractor;
import online.documentHandler.navigation.impl.ClusterHeuristicNavigationHandler;
import online.navigation.utils.NavigationUtils;
import online.queryResultPageHandler.impl.TreeEditDistanceBasedWrapper;
import online.resultHandler.impl.AllHrefResultDocumentHandler;
import online.sample.wordsDistribution.WordsDistributionLoader;
import online.tagcleaner.HTMLTagCleaner;
import online.tagcleaner.impl.HTMLCleanerBasedCleaner;
import execution.model.factory.InteractionPersisterFactory;
import execution.workload.querygeneration.TextQueryGenerator;
import execution.workload.tuple.Tuple;
import exploration.model.Database;
import exploration.model.Document;
import exploration.model.DocumentHandle;
import exploration.model.Version;
import exploration.model.WorkloadModel;
import exploration.model.enumerations.ExecutionAlternativeEnum;
import extraction.relationExtraction.RelationExtractionSystem;
import extraction.relationExtraction.impl.LocalCachedRelationExtractionSystem;
import extraction.relationExtraction.impl.OCRelationExtractionSystem;
import extraction.relationExtraction.impl.TupleRelationExtractionSystem;
import gov.nist.atlas.impl.IdImpl;
import searcher.Searcher;
import searcher.impl.CachedSearcher;
import searcher.impl.OnLineSearcher;
import searcher.interaction.formHandler.TextQuery;
import utils.persistence.InteractionPersister;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import utils.persistence.impl.DiskBasedInteractionPersister;

public class InitialTuplesExtractor {

	class InitialRunnable implements Runnable{

		private Database database;
		private RelationExtractionSystem instance;
		private Searcher searcher;
		private Semaphore semaphore;
		private Version version;
		private int docs_per_query;
		private int queries_per_database;
		private WorkloadModel model;
		private String resName;
		private persistentWriter pW;

		public InitialRunnable(persistentWriter pW,Database database,
				RelationExtractionSystem instance, Searcher searcher,
				Semaphore sp, Version version, int docs_per_query,
				int queries_per_database, WorkloadModel model, String resName) {
			this.pW = pW;
			this.database = database;
			this.instance = instance;
			this.searcher = searcher;
			this.semaphore = sp;
			this.version = version;
			this.docs_per_query = docs_per_query;
			this.queries_per_database = queries_per_database;
			this.model = model;
			this.resName = resName;
		}

		@Override
		public void run() {
			
			List<String> must = new ArrayList<String>(1);
			
			List<String> must_not = new ArrayList<String>(1);
			
			Set<Document> processedDocs = new HashSet<Document>();
			
			File output = pW.getInitialMatchingTuplesWithSourcesFile(database, version, model,resName);
			
			BufferedWriter bw = null;
			try {
				bw = new BufferedWriter(new FileWriter(output));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			List<TextQuery> queries = pW.getQueriesForTuplesGeneration(getSearchRoundId(),database.getId());
			
			for (int q = 0; q < queries.size() && q < queries_per_database; q++) {
				
				must.clear();
				
				utils.query.QueryParser.parseQuery(queries.get(q), must, must_not);
				
				searcher.doSearch(must, must_not);
				
				List<Document> documents = searcher.retrieveMaxAllowedDocuments(must, must_not);
				
				int added = 0;
				
				for (int j = 0; j < documents.size() && added < docs_per_query; j++) {
					
					if (processedDocs.contains(documents.get(j))){
						continue;
					}
					
					processedDocs.add(documents.get(j));
					
					added++;
					
					Tuple[] t = instance.execute(/*database.getId(), */documents.get(j));
					
					System.out.println("Tuples: " + t.length);
					
					for (int k = 0; k < t.length; k++) {
						
						try {
							bw.write(documents.get(j).getId() + "," + t[k].toString());
							bw.newLine();
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						
					}
					
				}
				
				searcher.cleanQuery(must, must_not);
				
			}
			
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			instance.clear();
			
			semaphore.release();
			
		}
		
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		int group = Integer.valueOf(args[0]);
		
		int idWorkload = Integer.valueOf(args[1]); //17 to 22
		
		int idInformationExtractionSystem = Integer.valueOf(args[2]); //20 for instance
		
		int idRelationConfiguration = Integer.valueOf(args[3]); //1 for instance
		
		int permits = Integer.valueOf(args[4]);
		
		Semaphore sp = new Semaphore(permits);		
		
		int docs_per_query = 200;
		
		int queries_per_database = 50;
		
		String versionName = "INDEPENDENT";
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		ContentExtractor ce = new TikaContentExtractor();
		
		InteractionPersister interactionPersister = new DiskBasedInteractionPersister("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/",
				"/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalDataIndex.txt", pW);
		
		List<Database> databases = pW.getSamplableDatabases(group);
		
		RelationExtractionSystem res = new TupleRelationExtractionSystem(pW,idRelationConfiguration, idInformationExtractionSystem,true,false);
		
		WorkloadModel model = pW.getWorkloadModel(idWorkload);
		
		String[] relation = model.getRelations();
		
		Version version = Version.generateInstance(versionName, model);
		
		List<Thread> ts = new ArrayList<Thread>();
		
		for (int i = 0; i < databases.size(); i++) {
		
			System.out.println("Datbase: " + i + " out of: " + databases.size());
			
			try {
				
				sp.acquire();
				
				Database database = databases.get(i);
				
				RelationExtractionSystem instance = res.createInstance(database, interactionPersister, ce, relation);
				
				Searcher searcher = interactionPersister.getSearcher(database);
				
				Thread t = new Thread(new InitialTuplesExtractor().new InitialRunnable(PersistenceImplementation.getNewWriter(),database,instance,searcher,sp,version,docs_per_query,queries_per_database,model,res.getName()));
				
				ts.add(t);
				
				t.start();
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}

		for (Thread thread : ts) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

	private static int getSearchRoundId() {
		return 3;
	}
	
}
