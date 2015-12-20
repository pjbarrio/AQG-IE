package sample.generation.catching;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

import online.documentHandler.OnlineDocumentHandler;
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
import org.jfree.chart.plot.PeriodMarkerPlot;

import exploration.model.Database;
import exploration.model.enumerations.ExperimentEnum;
import exploration.model.enumerations.ExperimentStatusEnum;
import exploration.model.enumerations.QueryStatusEnum;

import searcher.Searcher;
import searcher.impl.OnLineSearcher;
import searcher.interaction.formHandler.TextQuery;
import utils.persistence.InteractionPersister;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import utils.persistence.impl.DiskBasedInteractionPersister;

public class SendQueriesBatch {

	private static OnlineDocumentHandler odh;
	private static HTMLTagCleaner htmlTagCleaner;
	private static InteractionPersister interactionPersister;
	private static NavigationHandler navigationHandler;
	private static QueryResultPageHandler queryResultPageHandler;
	private static ResultDocumentHandler resultHandler;

	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		
		class QueryBachRunnable implements Runnable{

			private persistentWriter pW;
			private Database database;
			private List<String> must_not_words;
			private int experimentId;
			private Semaphore semaphore;
			private int numQueries;
			private boolean finishedSuccessfuly;
			private int storeafter;
			private int relationConf;
			private Control control;
			private boolean loadNotProcessed;

			public QueryBachRunnable(persistentWriter pW, Database database, int experimentId, List<String> must_not_words, Semaphore semaphore, int numQueries, boolean finishedSuccessfully, int storeAfter, int relationConf, Control control, boolean loadNotProcessed) {
				this.pW = pW;
				this.database = database;
				this.experimentId = experimentId;
				this.must_not_words = must_not_words;
				this.semaphore = semaphore;
				this.numQueries = numQueries;
				this.finishedSuccessfuly = finishedSuccessfully;
				this.storeafter = storeAfter;
				this.relationConf = relationConf;
				this.control = control;
				this.loadNotProcessed = loadNotProcessed;
			}

			@Override
			public void run() {
					
				try {
					
					Searcher searcher = new OnLineSearcher(10000,"UTF-8",database,
							"/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalSelection/FinalForms/"+database.getId()+".html",
							"/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalSelection/FinalInputNames/"+database.getId()+".txt",20,3,getOnlineDocumentHandler(),getSearchRoundId(),getHtmlTagCleaner(),getInteractionPersister(pW),true,40000.0);
					
					boolean broken = false;
					
					//will get here if has been stopped.
					
					if (!loadNotProcessed && !finishedSuccessfuly){
					
						int lastStoredQuery = pW.getQueriesBatchLastExecutedQuery(getSearchRoundId(),database, getNavigationHandler().getName(),pW.getConsistensy(relationConf));
						
						if (lastStoredQuery != 0){
							
							int lastSentQuery = pW.getLastSentQuery(getSearchRoundId(),database, getNavigationHandler().getName(),relationConf);
							
							List<Integer> queries = pW.loadQueriesforSampleRelation(getSearchRoundId(),database,getNavigationHandler().getName(),relationConf, lastStoredQuery, lastSentQuery);
							
							if (!queries.isEmpty())
								pW.removeInformAsNotProcessed(getSearchRoundId(),queries,database, getQueryResultPageHandler().getName(),getNavigationHandler().getName(),getResultHandler().getName());
						
						}
					}	
					
					List<List<String>> queries = pW.loadNonProcessedQueriesforSample(getSearchRoundId(),database,getNavigationHandler().getName(),relationConf);
					
//					if (loadNotProcessed)
//						queries = pW.loadNotProcessedHurry(database,relationConf);
					
					int j = 0;
					
					int procQuer = storeafter;
					
					TextQuery t = null;
					
					for (; j < queries.size() && numQueries > 0 && control.keepProcessing(); j++) {
						
						numQueries--;
						
						System.out.println("Processing: " + database.getId() + " query " + j + " out of " + queries.size() + " - " + queries.get(j));
						
						t = new TextQuery(queries.get(j));
						
						boolean success = searcher.doSearch(queries.get(j), must_not_words,true);
						
						if (!success){
							
							pW.reportExperimentStatus(experimentId,database.getId(),pW.getComputerName(),ExperimentStatusEnum.ERROR);
							
							pW.reportInteractionError(database.getId(),1);
							
							broken = true;
							
							break;
							
						}
						
						pW.reportQueryConsistency(database.getId(),(int)(long)pW.getTextQuery(t),pW.getConsistensy(relationConf));
						
						pW.reportExperimentStatus(experimentId,database.getId(),pW.getComputerName(),j+1);
						
						searcher.storeMaxAllowedDocuments(queries.get(j), must_not_words);
						
						procQuer--;
						
						if (procQuer == 0){
							
							searcher.finishBatchDownloader(pW.getConsistensy(relationConf),database.getId(),pW.getComputerName(),(int)(long)pW.getTextQuery(t));
							
							procQuer = storeafter;
							
						}
						
						searcher.cleanQuery(queries.get(j), must_not_words);
						
					}
					
					if (!broken && j == queries.size()){
						pW.reportExperimentStatus(experimentId,database.getId(),pW.getComputerName(),ExperimentStatusEnum.FINISHED);
					}else if (!broken && j < queries.size()){
						pW.reportExperimentStatus(experimentId,database.getId(),pW.getComputerName(),ExperimentStatusEnum.PARTIALLY_FINISHED);
					}
					
					searcher.finishBatchDownloader(pW.getConsistensy(relationConf),database.getId(),pW.getComputerName(),(int)(long)pW.getTextQuery(t));
					
					searcher.cleanSearcher();				
				
				} catch (Exception e) {
					
					e.printStackTrace();
					
				}
				
				pW.makeExperimentAvailable(ExperimentEnum.QUERYING, database.getId());
				
				semaphore.release();
				
				System.out.println("RELEASED/THREAD: " + semaphore.availablePermits());

			}
						
		}
		
		int baseExperiment = 87;
		
		int group = Integer.valueOf(args[0]); //from 1 to 9
		
		int numDatabases = Integer.valueOf(args[1]);

		int permits = Integer.valueOf(args[2]);
		
		int numQueries = Integer.valueOf(args[3]);
		
		int storeAfter = Integer.valueOf(args[4]);
		
		int relationConf = Integer.valueOf(args[5]);
		
		boolean loadNotProcessed = Boolean.valueOf(args[6]);
		
		Semaphore semaphore = new Semaphore(permits); //simultaneous instances.
		
		int experimentId = baseExperiment + relationConf; //querying
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		List<Database> databases = pW.getSamplableDatabases(group);
		
		Collections.shuffle(databases);
		
		List<String> must_not_words = new ArrayList<String>(0);
		
		Control control = new Control();
		
		//clean Not finished
		
		for (int i = 0; i < databases.size() && numDatabases > 0; i++) {

			semaphore.acquire();
			System.out.println("ACQUIRED: " + semaphore.availablePermits());
			
			if (!pW.isExperimentAvailable(ExperimentEnum.QUERYING, databases.get(i).getId(),pW.getComputerName())){ //someone is querying
				semaphore.release();
				System.out.println("RELEASED: " + semaphore.availablePermits());
				continue;
			}
			
			boolean finishedSuccessfully = true;
			
			if (!pW.isExperimentAvailable(experimentId, databases.get(i).getId(),pW.getComputerName())){
				
				if (pW.isExperimentInStatus(databases.get(i), experimentId, ExperimentStatusEnum.FINISHED)){ //it finished;
					pW.makeExperimentAvailable(ExperimentEnum.QUERYING, databases.get(i).getId()); //if it got here is because it was available.
					semaphore.release();
					System.out.println("RELEASED: " + semaphore.availablePermits());
					continue;
				}
				
				if (!loadNotProcessed)
					finishedSuccessfully = pW.isExperimentInStatus(databases.get(i), experimentId, ExperimentStatusEnum.PARTIALLY_FINISHED, ExperimentStatusEnum.ERROR);
				
			}
			
			numDatabases--;
			
			pW.reportExperimentStatus(experimentId,databases.get(i).getId(),pW.getComputerName(),ExperimentStatusEnum.RUNNING);
						
			//will write only if it does not exist
			pW.InitializeExperimentStatus(pW.getConsistensy(relationConf),databases.get(i).getId(),pW.getComputerName());
			//will write only if it does not exist.
			pW.insertInteractionError(databases.get(i).getId(),1);
			
			Thread t = new Thread(new QueryBachRunnable(PersistenceImplementation.getNewWriter(),databases.get(i),experimentId,must_not_words,semaphore,numQueries,finishedSuccessfully,storeAfter,relationConf,control,loadNotProcessed));
			
			t.start();
			
		}

		double f =0.0;
		
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

//		if (odh == null){
//			odh = new OnlineDocumentHandler(getQueryResultPageHandler(), getNavigationHandler(), getResultHandler());
//		}
//		return odh;
		
		return new OnlineDocumentHandler(getQueryResultPageHandler(), getNavigationHandler(), getResultHandler(),getHtmlTagCleaner());

	}

	private static ResultDocumentHandler getResultHandler() {
		
//		if (resultHandler == null){
//			resultHandler = new AllHrefResultDocumentHandler();
//		}
//		return resultHandler;
		
		return new AllHrefResultDocumentHandler();
	}

	private static QueryResultPageHandler getQueryResultPageHandler() {
		
//		if (queryResultPageHandler == null){
//			queryResultPageHandler = new TreeEditDistanceBasedWrapper();
//		}
//		return queryResultPageHandler;
		
		return new TreeEditDistanceBasedWrapper();
		
	}

	private static NavigationHandler getNavigationHandler() {
		
//		if (navigationHandler == null){
//			navigationHandler = new ClusterHeuristicNavigationHandler(getSearchRoundId());
//		}
//		
//		return navigationHandler;
		
		return new ClusterHeuristicNavigationHandler(getSearchRoundId());
	}

	private static int getSearchRoundId() {
		return 3;
	}
	
	private static HTMLTagCleaner getHtmlTagCleaner() {

		return new HTMLCleanerBasedCleaner();

	}
	
	private static InteractionPersister getInteractionPersister(persistentWriter pW) {
		
//		if (interactionPersister == null){
//			
//			interactionPersister = new DiskBasedInteractionPersister("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/", "/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalDataIndex.txt", pW);
//		}
//		return interactionPersister;

		return new DiskBasedInteractionPersister("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/", "/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalDataIndex.txt", pW);

	
	}
	
	
}
