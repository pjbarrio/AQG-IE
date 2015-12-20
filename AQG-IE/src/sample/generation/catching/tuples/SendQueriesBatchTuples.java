package sample.generation.catching.tuples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

import online.documentHandler.OnlineDocumentHandler;
import online.documentHandler.navigation.NavigationHandler;
import online.documentHandler.navigation.impl.ClusterHeuristicNavigationHandler;
import online.queryResultPageHandler.QueryResultPageHandler;
import online.queryResultPageHandler.impl.TreeEditDistanceBasedWrapper;
import online.resultHandler.ResultDocumentHandler;
import online.resultHandler.impl.AllHrefResultDocumentHandler;
import online.tagcleaner.HTMLTagCleaner;
import online.tagcleaner.impl.HTMLCleanerBasedCleaner;
import searcher.Searcher;
import searcher.impl.OnLineSearcher;
import searcher.interaction.formHandler.TextQuery;
import utils.persistence.InteractionPersister;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import utils.persistence.impl.DiskBasedInteractionPersister;
import exploration.model.Database;
import exploration.model.enumerations.ExperimentEnum;
import exploration.model.enumerations.ExperimentStatusEnum;

public class SendQueriesBatchTuples {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		
		class TuplesQueryRunnable implements Runnable{

			private Database database;
			private int searchRoundId;
			private persistentWriter pW;
			private Semaphore semaphore;
			private int experimentId;
			private String extTechnique;
			private String resExtTechnique;
			private String navHandler;
			private List<String> must_not_words;
			private boolean finishedSucessfully;
			private int max_number_of_queries;
			private int storeAfter;
			
			public TuplesQueryRunnable(int searchRoundId, Database database,
					String extTechnique, String navHandler, String resExtTechnique, persistentWriter pW, Semaphore semaphore, int experimentId, List<String> must_not_words, boolean finishedSuccessfully, int max_number_of_queries, int storeAfter) {
				this.searchRoundId = searchRoundId;
				this.database = database;
				this.extTechnique = extTechnique;
				this.navHandler = navHandler;
				this.resExtTechnique = resExtTechnique;
				this.pW = pW;
				this.semaphore = semaphore;
				this.experimentId = experimentId;
				this.must_not_words = must_not_words;
				this.finishedSucessfully = finishedSuccessfully;
				this.max_number_of_queries = max_number_of_queries;
				this.storeAfter = storeAfter;
			}

			@Override
			public void run() {
				
				if (!finishedSucessfully){
				
					int lastStoredQuery = pW.getQueriesBatchLastExecutedQuery(searchRoundId,database, getNavigationHandler().getName(),pW.getTupleConsistensy());
					
					List<Integer> queriesId = pW.loadIdQueriesForTuple(searchRoundId,database,lastStoredQuery);
					
					if (!queriesId.isEmpty()){
						pW.removeInformAsNotProcessed(searchRoundId,queriesId, database, getQueryResultPageHandler().getName(),getNavigationHandler().getName(),getResultHandler().getName());

					}

				}

				pW.removeQueriesForTupleSent(getSearchRoundId(),database, extTechnique, navHandler, resExtTechnique);
				
				List<List<String>> queries = pW.loadQueriesForTuple(getSearchRoundId(),database);
				
				Searcher searcher = new OnLineSearcher(1000,"UTF-8",database,
						"/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalSelection/FinalForms/"+database.getId()+".html",
						"/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalSelection/FinalInputNames/"+database.getId()+".txt",20,3,getOnlineDocumentHandler(),getSearchRoundId(),getHtmlTagCleaner(),getInteractionPersister(pW),true,40000.0);
				
				boolean broken = false;
				
				int procQuer = storeAfter;
				
				TextQuery tq = null;
				
				for (int j = 0; j < queries.size() && j < max_number_of_queries; j++) {
					
					System.out.println("Processing: " + database.getId());
					
					System.err.println("Q:" + queries.get(j));
					
					tq = new TextQuery(queries.get(j));
					
					pW.reportQueryForTupleSent(getSearchRoundId(),database,j+1, tq, extTechnique, navHandler, resExtTechnique);
					
					if (pW.hasProcessedQuery(getSearchRoundId(), database.getId(), tq,getNavigationHandler().getName())){
						System.out.println("has Processed query " + queries.get(j).toString());
						pW.reportExperimentStatus(experimentId,database.getId(),pW.getComputerName(),j+1);
						pW.reportInteractionError(database.getId(),3);
						continue;
					}
					
					boolean success = searcher.doSearch(queries.get(j), must_not_words,true);
					
					if (!success){
						
						pW.reportExperimentStatus(experimentId,database.getId(),pW.getComputerName(),ExperimentStatusEnum.ERROR);
						
						pW.reportInteractionError(database.getId(),3);
						
						broken = true;
						
						break;
					}
					
					pW.reportQueryConsistency(database.getId(),(int)(long)pW.getTextQuery(tq),pW.getTupleConsistensy());
					
					pW.reportExperimentStatus(experimentId,database.getId(),pW.getComputerName(),j+1);
					
					searcher.storeMaxAllowedDocuments(queries.get(j), must_not_words);
					
					procQuer--;
					
					if (procQuer == 0){
						
						searcher.finishBatchDownloader(pW.getTupleConsistensy(),database.getId(),pW.getComputerName(),(int)(long)pW.getTextQuery(tq));
						
						procQuer = storeAfter;
						
					}
					
					searcher.cleanQuery(queries.get(j), must_not_words);
					
					try {
						Thread.sleep(1000 + (long)Math.random()*3000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
				}
				
				if (tq!=null) //processed at least one.
					searcher.finishBatchDownloader(pW.getTupleConsistensy(),database.getId(),pW.getComputerName(),(int)(long)pW.getTextQuery(tq));
				
				if (!broken)
					pW.reportExperimentStatus(experimentId,database.getId(),pW.getComputerName(),ExperimentStatusEnum.FINISHED);
				
				searcher.cleanSearcher();
				
				pW.makeExperimentAvailable(ExperimentEnum.QUERYING, database.getId());

				semaphore.release();
				System.out.println("RELEASED: " + semaphore.availablePermits());
				
			}
			
		}
		
		//All the query tuples have to be already loaded
		
		int group = Integer.valueOf(args[0]); //from 1 to 9
		
		int numDatabases = Integer.valueOf(args[1]);
		
		int permits = Integer.valueOf(args[2]);
		
		int storeAfter = Integer.valueOf(args[3]);
		
		Semaphore semaphore = new Semaphore(permits); //simultaneous instances.
		
		int experimentId = 43; //tuples
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		List<Database> databases = pW.getSamplableDatabases(group);
		
		List<String> must_not_words = new ArrayList<String>(0);
		
		int max_number_of_queries = 50;
		
		//clean Not finished
		
		String extTechnique = getQueryResultPageHandler().getName();
		String navHandler = getNavigationHandler().getName();
		String resExtTechnique = getResultHandler().getName();
		
		Collections.shuffle(databases);
		
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
			pW.InitializeExperimentStatus(pW.getTupleConsistensy(),databases.get(i).getId(),pW.getComputerName());
			
			//will insert only when it does not exist.
			pW.insertInteractionError(databases.get(i).getId(),3);
			
			Thread t = new Thread(new TuplesQueryRunnable(getSearchRoundId(),databases.get(i), extTechnique,navHandler,resExtTechnique,pW,semaphore,experimentId,must_not_words,finishedSuccessfully, max_number_of_queries,storeAfter));
			
			t.start();
			
		}

	}

	private static NavigationHandler getNavigationHandler() {
		
		return new ClusterHeuristicNavigationHandler(getSearchRoundId());

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
