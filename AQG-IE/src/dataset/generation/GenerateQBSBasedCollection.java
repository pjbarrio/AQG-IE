package dataset.generation;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import com.google.gdata.util.common.base.Pair;

import online.documentHandler.OnlineDocumentHandler;
import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.contentExtractor.impl.TikaContentExtractor;
import online.documentHandler.contentLoader.ContentLoader;
import online.documentHandler.contentLoader.impl.InMemoryContentLoader;
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
import utils.word.extraction.WordExtractor;
import exploration.model.Database;
import exploration.model.Document;

public class GenerateQBSBasedCollection {

	private static List<String> must_not_words = new ArrayList<String>(0);

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		int group = Integer.valueOf(args[0]); //-10 has all the databases
		
		int[] splits = {1,2,3,4,5};
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		List<Database> databases = pW.getSamplableDatabases(group);
		
		String navHandler = getNavigationHandler().getName();
		String extTechnique = getQueryResultPageHandler().getName();
		String resExtTechnique = getResultHandler().getName();
		
		ContentExtractor ce = new TikaContentExtractor();
		
		ContentLoader cl = new InMemoryContentLoader();
		
		WordExtractor wordExtractor = new WordExtractor(ce , cl);
		
		for (Database database : databases) {

			if (new File("cachingExperiments/" + database.getId() + ".ser").exists()){
				continue;
			}

			new File("cachingExperiments/" + database.getId() + ".ser").createNewFile();
			
			Set<Long> sentQueries = new HashSet<Long>();
			
			List<Pair<Long,String>> list = new ArrayList<Pair<Long,String>>();
			
			Searcher searcher = new OnLineSearcher(10000,"UTF-8",database,
					"/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalSelection/FinalForms/"+database.getId()+".html",
					"/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalSelection/FinalInputNames/"+database.getId()+".txt",20,3,getOnlineDocumentHandler(),getSearchRoundId(),getHtmlTagCleaner(),getInteractionPersister(pW),false,40000.0);
			
			for (int i = 0; i < splits.length; i++) {
				
				List<Long> queryIds = pW.getQueriesUsedToGenerateNegativeSample(getSearchRoundId(),database,splits[i],navHandler,extTechnique,resExtTechnique) ; //Obtain the negative queries for db,split, and everything else. 
				
				for (Long query : queryIds) {
					
					if (sentQueries.add(query)){
						
						TextQuery tq = pW.getTextQueryFromId(query);
						
						boolean success = searcher.doSearch(tq.getWords(), must_not_words ,true);
						
						if (success){
							
							List<Document> docs = searcher.retrieveMaxAllowedDocuments(tq.getWords(), must_not_words);
							
							for (Document document : docs) {
								list.add(new Pair<Long, String>(document.getId(), document.getFilePath(pW).getAbsolutePath()));
							}
							
						}else{
							System.err.println("Something went wrong with an already issued query...");
						}
						
						searcher.cleanQuery(tq.getWords(), must_not_words);
						
					}else{
						System.err.println("already added...");
					}
					
				}
				
			}
			
			try{
				//use buffering


				OutputStream file = new FileOutputStream( "cachingExperiments/" + database.getId() + ".ser" );
				OutputStream buffer = new BufferedOutputStream( file );
				ObjectOutput output = new ObjectOutputStream( buffer );

				try{
					output.writeObject(list);
				}
				finally{
					output.close();
				}
			}  
			catch(IOException ex){
				ex.printStackTrace();
			}
			
			new File("cachingExperiments/" + database.getId() + ".ser.done").createNewFile();
			
			searcher.cleanSearcher();
			
			System.gc();
			
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
