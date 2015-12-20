package sample.generation.catching;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

import online.documentHandler.OnlineDocumentHandler;
import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.GoogleContentExtractor;
import online.documentHandler.contentExtractor.impl.TikaContentExtractor;
import online.documentHandler.contentLoader.ContentLoader;
import online.documentHandler.contentLoader.impl.FileContentLoader;
import online.documentHandler.contentLoader.impl.InMemoryContentLoader;
import online.documentHandler.navigation.NavigationHandler;
import online.documentHandler.navigation.impl.ClusterHeuristicNavigationHandler;
import online.navigation.utils.NavigationUtils;
import online.queryResultPageHandler.QueryResultPageHandler;
import online.queryResultPageHandler.impl.TreeEditDistanceBasedWrapper;
import online.resultHandler.ResultDocumentHandler;
import online.resultHandler.impl.AllHrefResultDocumentHandler;
import online.tagcleaner.HTMLTagCleaner;
import online.tagcleaner.impl.HTMLCleanerBasedCleaner;

import org.apache.commons.io.FileUtils;

import execution.workload.querygeneration.QueryGenerator;
import execution.workload.querygeneration.TextQueryGenerator;
import exploration.model.Database;
import exploration.model.Document;
import exploration.model.enumerations.ExperimentEnum;
import exploration.model.enumerations.ExperimentStatusEnum;
import exploration.model.enumerations.QueryStatusEnum;

import sample.generation.model.WordSelectionStrategy;
import sample.generation.model.executor.QueryPoolExecutor;
import sample.generation.model.executor.impl.SimpleQueryPoolExecutor;
import sample.generation.model.queryPool.QueryPool;
import sample.generation.model.queryPool.impl.OtherSourceQueryPool;
import sample.generation.model.wordSelectionStrategy.OneAtATimeWordSelectionStrategy;
import sample.generation.model.wordSelectionStrategy.SequentialWordSelection;
import sample.generation.model.wordSelectionStrategy.SimpleAvgTFWordSelection;
import searcher.Searcher;
import searcher.impl.OnLineSearcher;
import searcher.interaction.formHandler.TextQuery;
import utils.persistence.InteractionPersister;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import utils.persistence.impl.DiskBasedInteractionPersister;
import utils.query.QueryParser;
import utils.word.extraction.WordExtractor;

public class SendQueriesBatchNegative {

	private static OnlineDocumentHandler odh;
	private static HTMLTagCleaner htmlTagCleaner;
	private static InteractionPersister interactionPersister;
	private static ResultDocumentHandler resultHandler;
	private static QueryResultPageHandler queryResultPageHandler;
	private static NavigationHandler navigationHandler;

	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		
		class QueryBachNegativeRunnable implements Runnable{

			persistentWriter pW;
			Database database; int experimentId;
			List<String> must_not_words; Semaphore semaphore;
			boolean finishedSuccessfully; int storeAfter;
			String collection; int max_number_of_docs;
			int max_number_or_queries; int allowed_size;
			String navHandler; String extTechnique;
			String resExtTechnique;
			private int split;
			private Set<String> stopWords;
			private Control control;

			public QueryBachNegativeRunnable(persistentWriter pW,
					Database database, int experimentId,
					List<String> must_not_words, Semaphore semaphore,
					boolean finishedSuccessfully, int storeAfter,
					String collection, int max_number_of_docs,
					int max_number_or_queries, int allowed_size,
					String navHandler, String extTechnique,
					String resExtTechnique, int split, Set<String> stopWords, Control control) {
				super();
				this.pW = pW;
				this.database = database;
				this.experimentId = experimentId;
				this.must_not_words = must_not_words;
				this.semaphore = semaphore;
				this.finishedSuccessfully = finishedSuccessfully;
				this.storeAfter = storeAfter;
				this.collection = collection;
				this.max_number_of_docs = max_number_of_docs;
				this.max_number_or_queries = max_number_or_queries;
				this.allowed_size = allowed_size;
				this.navHandler = navHandler;
				this.extTechnique = extTechnique;
				this.resExtTechnique = resExtTechnique;
				this.split = split;
				this.stopWords = stopWords;
				this.control = control;
			}

			@Override
			public void run() {
				
				QueryGenerator<String> queryGenerator = new TextQueryGenerator();
				
				ContentExtractor ce = new TikaContentExtractor();
				
				ContentLoader cl = new InMemoryContentLoader();
				
				WordExtractor wordExtractor = new WordExtractor(ce , cl);
				
				List<String> must_words = new ArrayList<String>();
				
				if (!finishedSuccessfully){
				
					List<Integer> toRemoveQueries = pW.getQueriesBatchNegativeLastExecutedQuery(getSearchRoundId(),database,split, allowed_size,extTechnique, navHandler,resExtTechnique);
				
					if (!toRemoveQueries.isEmpty()){

						pW.removeInformAsNotProcessed(getSearchRoundId(),toRemoveQueries, database, getQueryResultPageHandler().getName(),getNavigationHandler().getName(),getResultHandler().getName());
					
					}

				}

				pW.removeNegativeSampleEntries(getSearchRoundId(),database,split,allowed_size,navHandler,extTechnique,resExtTechnique);
				
				WordSelectionStrategy wordSelectionStrategy = new OneAtATimeWordSelectionStrategy(new SequentialWordSelection(collection), new SimpleAvgTFWordSelection(wordExtractor,stopWords));
				
				QueryPool<String> queryPool = new OtherSourceQueryPool(false, wordSelectionStrategy, queryGenerator);

				QueryPoolExecutor queries = new SimpleQueryPoolExecutor<String>(queryPool);
				
				queries.initialize(database,pW,split);
				
				Searcher searcher = new OnLineSearcher(10000,"UTF-8",database,
						"/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalSelection/FinalForms/"+database.getId()+".html",
						"/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalSelection/FinalInputNames/"+database.getId()+".txt",20,3,getOnlineDocumentHandler(),getSearchRoundId(),getHtmlTagCleaner(),getInteractionPersister(pW),true,40000.0);
				
				int position = 0;
				
				boolean broken = false;
				
				int numberOfDocs = 0;
				
				int processed = storeAfter;
				
				Set<TextQuery> procQueries = new HashSet<TextQuery>();
				
				TextQuery t = null;
				
				while (queries.hasMoreQueries() && position < max_number_or_queries && numberOfDocs < max_number_of_docs && control.keepProcessing()){
					
					must_words.clear();
					
					TextQuery quer = queries.getNextQuery();
					
					QueryParser.parseQuery(quer, must_words, must_not_words);
					
					if (!procQueries.contains(quer))
						procQueries.add(quer);
					else
						continue;

					System.out.println("Processing: " + database.getId());
					
					t = new TextQuery(must_words);
					
					position++;
					
					long qId = pW.getTextQuery(t);
					
					pW.prepareNegativeSampleEntry(getSearchRoundId(),database,split,allowed_size,position,qId,navHandler,extTechnique,resExtTechnique);
					
//					if (!pW.existsQuery(t)){
//						
//						long qId = pW.writeTextQuery(t);
//					
//						if (qId == -1L){ //someone else wrote the query in that time and it's not available
//						
//							pW.reloadQueryTable();
//							
//							if (pW.existsQuery(t)) //it's added now
//								pW.prepareNegativeSampleEntry(getSearchRoundId(),database,split,allowed_size,position,t,navHandler,extTechnique,resExtTechnique);
//							else{
//								pW.reportExperimentStatus(experimentId,database.getId(),pW.getComputerName(),ExperimentStatusEnum.ERROR);
//								broken = true;
//								break;
//							}
//						}else
//							pW.prepareNegativeSampleEntry(getSearchRoundId(),database,split,allowed_size,position,qId,navHandler,extTechnique,resExtTechnique);
//
//					}		
//					else{
//						pW.prepareNegativeSampleEntry(getSearchRoundId(),database,split,allowed_size,position,t,navHandler,extTechnique,resExtTechnique);
//					}
					
					boolean success = searcher.doSearch(must_words, must_not_words,true);
					
					if (!success){
						pW.reportExperimentStatus(experimentId,database.getId(),pW.getComputerName(),ExperimentStatusEnum.ERROR);
						pW.reportInteractionError(database.getId(),2);
						broken = true;
						break;
					}
					
					pW.reportNegativeQueryConsistency(database.getId(),(int)(long)pW.getTextQuery(t),pW.getNegativeConsistensy(split));
					
					pW.reportExperimentStatus(experimentId,database.getId(),pW.getComputerName(),position+1);
					
					searcher.storeMaxAllowedDocuments(must_words, must_not_words);
					
					List<Document> docs = searcher.retrieveMaxAllowedDocuments(must_words, must_not_words);
					
					for (int k = 0; k < docs.size() && k < allowed_size; k++) {
						
						queries.updateQueries(docs.get(k));
						
						numberOfDocs++;
						
					}
					
					processed--;
					
					if (processed == 0){
						processed = storeAfter;
						searcher.finishNegativeBatchDownloader(pW.getNegativeConsistensy(split),database.getId(),pW.getComputerName(),(int)(long)pW.getTextQuery(t),split);
					}
					
					searcher.cleanQuery(must_words, must_not_words);
					
					try {
						Thread.sleep(1000 + (long)Math.random()*3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
				}
				
				if (!broken)
					pW.reportExperimentStatus(experimentId,database.getId(),pW.getComputerName(),ExperimentStatusEnum.FINISHED);
				
				searcher.finishNegativeBatchDownloader(pW.getNegativeConsistensy(split),database.getId(),pW.getComputerName(),(int)(long)pW.getTextQuery(t),split);
				
				searcher.cleanSearcher();
				
				pW.makeExperimentAvailable(ExperimentEnum.QUERYING, database.getId());
				
				semaphore.release();
				
				System.out.println("RELEASED/THREAD: " + semaphore.availablePermits());
				
			}
			
		}
		
		int group = Integer.valueOf(args[0]); //from 1 to 9
		
		int baseexperimentId = 44; //querying negative add the split value

		int split = Integer.valueOf(args[1]);
		
		int numDatabases = Integer.valueOf(args[2]);
		
		int permits = Integer.valueOf(args[3]);
		
		Semaphore semaphore = new Semaphore(permits);
		
		int storeAfter = Integer.valueOf(args[4]);
		
		int experimentId = baseexperimentId + split - 1;
		
		String collection = "UBUNTU";
		
		int max_number_of_docs = 10000;
		
		int max_number_or_queries = 2000;
		
		int allowed_size = 10;

		persistentWriter pW = PersistenceImplementation.getWriter();
		
		List<Database> databases = pW.getSamplableDatabases(group);
		
		Collections.shuffle(databases);
		
		Control control = new Control();
		
//		databases.clear();
//		
//		databases.add(pW.getDatabaseById(2127));
		
		
		
		List<String> must_not_words = new ArrayList<String>(0);
		
		Set<String> stopWords = new HashSet<String>(FileUtils.readLines(new File(pW.getStopWords())));
		
		String navHandler = getNavigationHandler().getName();
		String extTechnique = getQueryResultPageHandler().getName();
		String resExtTechnique = getResultHandler().getName();
		
		for (int i = 0; i < databases.size() && numDatabases > 0; i++) {
		
			semaphore.acquire();
			
			System.out.println("ACQUIRED: " + semaphore.availablePermits());
			
//			if (split > 0) //we want to explore splits as they are done with the previous
//			if (!(pW.isExperimentInStatus(databases.get(i), experimentId-1, ExperimentStatusEnum.ERROR, ExperimentStatusEnum.FINISHED)))
//				continue;
			
			if (!pW.isExperimentAvailable(ExperimentEnum.QUERYING, databases.get(i).getId(),pW.getComputerName())){ //someone is querying
				semaphore.release();
				System.out.println("RELEASED: " + semaphore.availablePermits());
				continue;
			}
			
			boolean finishedSuccessfully = true;
			
			if (!pW.isExperimentAvailable(experimentId, databases.get(i).getId(),pW.getComputerName())){
				
				if (pW.isExperimentInStatus(databases.get(i), experimentId, ExperimentStatusEnum.FINISHED)){
					pW.makeExperimentAvailable(ExperimentEnum.QUERYING, databases.get(i).getId()); //if it got here is because it was available.
					semaphore.release();
					System.out.println("RELEASED: " + semaphore.availablePermits());
					continue;
				}
				
				finishedSuccessfully = pW.isExperimentInStatus(databases.get(i), experimentId, ExperimentStatusEnum.ERROR);
				
			}
			
			numDatabases--;
			
			pW.reportExperimentStatus(experimentId,databases.get(i).getId(),pW.getComputerName(),ExperimentStatusEnum.RUNNING);
			
			//will write only if it does not exist
			pW.InitializeExperimentStatus(pW.getNegativeConsistensy(split),databases.get(i).getId(),pW.getComputerName());
			
			//will insert only when it does not exist.
			pW.insertInteractionError(databases.get(i).getId(),2);
			
			
			Thread t = new Thread(new QueryBachNegativeRunnable(pW,databases.get(i),experimentId,must_not_words,semaphore,finishedSuccessfully,storeAfter,collection,max_number_of_docs,max_number_or_queries,allowed_size,navHandler,extTechnique,resExtTechnique,split,stopWords,control));
			
			t.start();
			
		}

		int f =0;
		
		while (true){
			
			semaphore.acquire();
			
			f++;
			
			if (f > 0.7 * (double)permits){
				control.terminate();
				break;
			}
			
		}
		
	}

	private static OnlineDocumentHandler getOnlineDocumentHandler() {
		return new OnlineDocumentHandler(getQueryResultPageHandler(), getNavigationHandler(), getResultHandler(),getHtmlTagCleaner());
	}

	private static ResultDocumentHandler getResultHandler() {
		
		return new AllHrefResultDocumentHandler();
	}

	private static QueryResultPageHandler getQueryResultPageHandler() {
		
		return new TreeEditDistanceBasedWrapper();
		
		
	}

	private static NavigationHandler getNavigationHandler() {
		
		return new ClusterHeuristicNavigationHandler(getSearchRoundId());
	}

	private static int getSearchRoundId() {
		return 3;
	}
	
	private static HTMLTagCleaner getHtmlTagCleaner() {

		return new HTMLCleanerBasedCleaner();

	}
	
	private static InteractionPersister getInteractionPersister(persistentWriter pW) {
		
		return new DiskBasedInteractionPersister("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/", "/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalDataIndex.txt", pW);
		
	}
	
}
