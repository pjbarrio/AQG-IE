package sample.generation.relation.database;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
import sample.generation.relation.attributeSelection.impl.ChiSquaredWithYatesCorrectionAttributeEval;
import sample.generation.relation.attributeSelection.impl.SMOAttributeEval;
import searcher.Searcher;
import searcher.impl.OnLineSearcher;
import utils.persistence.InteractionPersister;
import utils.persistence.PersistenceImplementation;
import utils.persistence.persistentWriter;
import utils.persistence.impl.DiskBasedInteractionPersister;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import execution.workload.querygeneration.TextQueryGenerator;
import execution.workload.tuple.Tuple;
import exploration.model.Database;
import exploration.model.Document;
import exploration.model.DocumentHandle;
import exploration.model.Version;
import exploration.model.WorkloadModel;
import extraction.relationExtraction.RelationExtractionSystem;
import extraction.relationExtraction.impl.OCRelationExtractionSystem;

public class FindingTuplesForTraining {

	
	private static OnlineDocumentHandler odh;
	private static HTMLTagCleaner htmlTagCleaner;
	private static HashSet<Document> processedDocs;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		ASEvaluation[] evalArr = new ASEvaluation[]{new SMOAttributeEval(),new InfoGainAttributeEval()};
	    
		String[][] databasesArray = {{"http://www.mauconline.net/","http://www.carmeuse.com","http://diversifiedproduct.com/"},
				{"http://joehollywood.com/","http://northeasteden.blogspot.com/"},
				{"http://www.paljorpublications.com/","http://www.brannan.co.uk/","http://www.improv.ca/","http://www.avclub.com/"},
				{"http://www.shopcell.com/","http://www.123aspx.com/","http://www.infoaxon.com/"},
				{"http://www.thecampussocialite.com/","http://www.time.com/","http://www.ddj.com/","http://www.biostat.washington.edu/","http://micro.magnet.fsu.edu/"},
				{"http://www.worldenergy.org","http://travel.state.gov/","http://www.aminet.net/","http://www.codecranker.com/","http://www.eulerhermes.com/","http://www.pokkadots.com/"},
				{"http://www.muffslap.com/","http://keep-racing.de","http://www.canf.org/","http://www.jamesandjames.com"}};

		String[] databases = databasesArray[Integer.valueOf(args[1])];
		
		ASEvaluation eval = evalArr[Integer.valueOf(args[0])];
		
//		ASEvaluation eval = new InfoGainAttributeEval();
		
	    ASSearch search = new Ranker();
		
		int docs_per_query = 20;
		
		int queries_per_database = 200;
		
		String versionName = "INDEPENDENT";
		
		String collection = "TREC";
		
		int workloadModelBase = 6;
		
		persistentWriter pW = PersistenceImplementation.getWriter();
		
		ContentExtractor ce = new TikaContentExtractor();
		
		InteractionPersister interactionPersister = new DiskBasedInteractionPersister("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/",
				"/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalDataIndex.txt", pW);
		
		
		String[][] relationses = {{"PersonCareer"},{"NaturalDisaster"},{"ManMadeDisaster"},{"Indictment"},{"Arrest"},{"Trial"},{"PersonTravel"},
				{"VotingResult"},{"ProductIssues"},{"Quotation"},{"PollsResult"}};
		
		TextQueryGenerator tq = new TextQueryGenerator();
		
		List<String> must = new ArrayList<String>(1);
		
		List<String> must_not = new ArrayList<String>(0);
		
		processedDocs = new HashSet<Document>();
		
		RelationExtractionSystem res = new OCRelationExtractionSystem(pW);
		
		int size = 20000;

		int[] spl = {1/*,2,3,4,5,6,7,8,9,10*/};
		
		boolean[] tuplesAsStopWords = {true/*,false*/};
		
		for (int i = 0; i < databases.length; i++) {
			
			Database database = pW.getDatabaseByName(databases[i]);
			
			Searcher searcher = new OnLineSearcher(100,"UTF-8",database,
					"/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalSelection/FinalForms/"+database.getId()+".html",
					"/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalSelection/FinalInputNames/"+database.getId()+".txt",10,3,getOnlineDocumentHandler(),getSearchRoundId(),getHtmlTagCleaner(),interactionPersister);
			
			for (int rel = 0; rel < relationses.length; rel++) {
				
				int workloadModelId = workloadModelBase + rel;
				
				WorkloadModel model = pW.getWorkloadModel(workloadModelId);
				
				Version version = Version.generateInstance(versionName, model);
				
				for (int tasw = 0; tasw < tuplesAsStopWords.length; tasw++) {
					
					for (int sp = 0; sp < spl.length; sp++) {
						
						int realsize = size * spl[sp];
						
						processedDocs.clear();
						
						File output = pW.getForTrainingMatchingTuplesWithSourcesFile(database, version, model,eval);
						
						File outputFiles = pW.getForTrainingSourcesFile(database,version,model,eval);
						
						BufferedWriter bw = new BufferedWriter(new FileWriter(output));
						
						BufferedWriter bw2 = new BufferedWriter(new FileWriter(outputFiles));
						
						RelationExtractionSystem instance = res.createInstance(database, interactionPersister, ce, relationses[rel]);
						
						File ff = new File(pW.getRelationWordsFromSplitModel(collection,relationses[rel][0],realsize,spl[sp],eval,search,tuplesAsStopWords[tasw]));
						
						if (!ff.exists())
							continue;
						
						List<String> words = WordsDistributionLoader.loadWordsOnly(ff,",",1);
						
						for (int q = 0; q < words.size() && q < queries_per_database; q++) {
							
							utils.query.QueryParser.parseQuery(tq.generateQuery(words.get(q)), must, must_not);
							
							searcher.doSearch(must, must_not);
							
							List<Document> documents = searcher.retrieveMaxAllowedDocuments(must, must_not);
							
							int added = 0;
							
							for (int j = 0; j < documents.size() && added < docs_per_query; j++) {
								
								if (hasBeenProcessed(documents.get(j))){
									continue;
								}
								
								bw2.write(documents.get(j));
								bw2.newLine();
								
								processedDocs.add(documents.get(j));
								
								added++;
								
								Tuple[] t = instance.execute(/*database.getId(), */documents.get(j),relationses[rel]);
								
								for (int k = 0; k < t.length; k++) {
									
									bw.write(documents.get(j) + "," + t[k]);
									bw.newLine();
									
								}
								
							}
							
							searcher.cleanQuery(must, must_not);
							
						}
						
						bw2.close();
						
						bw.close();
						
						instance.clear();
						
					}
					
				}
				
				
				
			}
			
			searcher.cleanSearcher();
			
		}
		
		
		

	}

	private static boolean hasBeenProcessed(Document doc) {
		return processedDocs.contains(doc);
	}

	private static int getSearchRoundId() {
		return 0;
	}

	private static OnlineDocumentHandler getOnlineDocumentHandler() {
		if (odh == null){
			odh = new OnlineDocumentHandler(new TreeEditDistanceBasedWrapper(), new ClusterHeuristicNavigationHandler(getSearchRoundId()), new AllHrefResultDocumentHandler(),getHtmlTagCleaner());
		}
		return odh;
	}

	private static HTMLTagCleaner getHtmlTagCleaner() {
		
		if (htmlTagCleaner == null){
			
			htmlTagCleaner = new HTMLCleanerBasedCleaner();
			
		}
		
		return htmlTagCleaner;
		
	}
	
	
}
