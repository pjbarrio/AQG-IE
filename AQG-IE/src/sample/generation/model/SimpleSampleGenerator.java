package sample.generation.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.plaf.ListUI;

import com.hp.hpl.jena.sparql.function.library.round;

import online.documentHandler.OnlineDocumentHandler;
import online.documentHandler.contentExtractor.ContentExtractor;
import online.documentHandler.navigation.impl.ClusterHeuristicNavigationHandler;
import online.navigation.utils.NavigationUtils;
import online.queryResultPageHandler.impl.TreeEditDistanceBasedWrapper;
import online.resultHandler.impl.AllHrefResultDocumentHandler;
import online.tagcleaner.HTMLTagCleaner;
import online.tagcleaner.impl.HTMLCleanerBasedCleaner;

import sample.generation.model.executor.QueryPoolExecutor;
import sample.generation.model.queryPool.QueryPool;
import searcher.Searcher;
import searcher.impl.OnLineSearcher;
import searcher.interaction.formHandler.TextQuery;
import utils.id.Idhandler;
import utils.persistence.InteractionPersister;
import utils.persistence.persistentWriter;
import utils.persistence.impl.DiskBasedInteractionPersister;
import utils.query.QueryParser;
import execution.workload.tuple.Tuple;
import exploration.model.Database;
import exploration.model.Document;
import exploration.model.DocumentHandle;
import exploration.model.Sample;
import exploration.model.Version;
import exploration.model.WorkloadModel;
import exploration.model.database.SimpleDatabase;
import extraction.relationExtraction.RelationExtractionSystem;



public abstract class SimpleSampleGenerator extends SampleGenerator{

	private ArrayList<String> must_words;
	private ArrayList<String> must_not_words;
	private RelationExtractionSystem relationExtractionSystem;
	private Map<String,List<Document>> processed;
	private Set<Document> processedDoc;
	private ContentExtractor contentExtractor;
	private String[] relations;
	private InteractionPersister interactionPersister;
	private RelationExtractionSystem relationExtractionSystemInstance;
	private QueryPoolExecutor firstQueryPool;
	private QueryPoolExecutor secondQueryPool;
	private QueryPoolExecutor currentQueryPool;
	private Set<Document> addedDocs;
	private Map<String, Integer> queryIndex;
	private Map<String, Integer> seenDocumentsTable;

	public SimpleSampleGenerator(ContentExtractor contentExtractor, String[] relations, InteractionPersister interactionPersister, QueryPoolExecutor firstQueryPool, 
			QueryPoolExecutor secondQueryPool, RelationExtractionSystem relationExtractionSystem) {

		this.contentExtractor = contentExtractor;
		this.relations = relations;
		this.interactionPersister = interactionPersister;

		processed = new HashMap<String,List<Document>>();
		processedDoc = new HashSet<Document>();

		this.relationExtractionSystem = relationExtractionSystem;

		this.firstQueryPool = firstQueryPool;
		this.secondQueryPool = secondQueryPool;

	}


	@Override
	public boolean generateSample(Sample sample, Database database, persistentWriter pW, SampleConfiguration sampleConfiguration, int version_seed_pos,int version_seed_neg) {

		Searcher searcher = interactionPersister.getSearcher(database);

		return generateSample(sample, database, pW, searcher, sampleConfiguration, version_seed_pos,version_seed_neg);

	}

	private boolean generateSample(Sample sample, Database database, persistentWriter pW, Searcher searcher, SampleConfiguration sampleConfiguration, int version_seed_pos,int version_seed_neg) {

		int queriedDB = database.getId();

		initialize(sample,database,pW,version_seed_pos,version_seed_neg);

		must_words = new ArrayList<String>();

		must_not_words = new ArrayList<String>();

		//		getCurrentQueryPool().fetchQuery();

		int seenDocumentssInQuery = 0;
		
		int query_index = 0;
		
		int positionQuery = 0;
		
		int updated = 0;
		
		TextQuery previousQuery = null;
		
		//It's avoiding the whole thing when there are not initial queries at all...
		
		while (getCurrentQueryPool().hasMoreQueries() && sampleConfiguration.keepProcessing(sample) && updated < 2){
			
			System.out.println("Again in the loop");

			TextQuery query = getCurrentQueryPool().getNextQuery();

			if (query == null){ //it can happen from Tuples that remove one of the attributes.
				System.err.println("DB: " + queriedDB);
				System.err.println(getCurrentQueryPool().getClass());
				System.err.println(query.getWords());
				continue;
			}

			positionQuery++;
			
			if (query.getText().trim().isEmpty()){
				getCurrentQueryPool().informExhausted();
				continue;
			}
			
			System.out.println(positionQuery + " - Found next query: " + query);

			must_words.clear();

			must_not_words.clear();

			QueryParser.parseQuery(query, must_words, must_not_words);

			List<Document> documents;

			if (hasProcessed(must_words,must_not_words)){

				seenDocumentssInQuery = getSeenDocuments(must_words,must_not_words);
				
				System.out.println("Has processed the query");

				documents = getDocuments(must_words,must_not_words);

				query_index = getQueryIndex(must_words,must_not_words);
				
			}else {

				seenDocumentssInQuery = 0;
				
				setSeenDocumentsInQuery(must_words,must_not_words,0);
				
				System.out.println("About to search on " + database.getId());

				searcher.doSearch(must_words, must_not_words);

				System.out.println("Search Done!");

				documents = searcher.retrieveMaxAllowedDocuments(must_words, must_not_words);

				markAsProcessed(must_words,must_not_words,documents);
				
				addGeneratedQuery(queriedDB, query,sample,pW);
				
				query_index = sample.getGeneratedQueries();
				
			}

			TextQuery tq = new TextQuery(must_words);
			
			if (previousQuery == null || !previousQuery.equals(tq))
				sample.addQuery(pW.getTextQuery(tq));
					
			previousQuery = tq;
			
			System.out.println("Documents retrieved...");

			int queryProcessed = 0;

			Document document = new Document(database,Document.NOT_EXISTING_ID);
			
			pW.addProcessedDocument(sample.getId(),query_index, sample.getQueriesSent(), calculateRound(seenDocumentssInQuery,sampleConfiguration.getResultsPerQuery()), sample.getProcessedDocuments().size(), 0, -1, 0, document);
		
			while (!documents.isEmpty()  && queryProcessed < sampleConfiguration.getResultsPerQuery() && !ready(sample, sampleConfiguration)) {

				Document doc = documents.remove(0); 

				seenDocumentssInQuery++;
				
				if (sampleConfiguration.countsAll())
					queryProcessed++;
				
				if (hasProcessed(doc))
					continue;
				
				if (!sampleConfiguration.countsAll())
					queryProcessed++;


				sample.addProcessedDocument(doc);
				
				processedDoc.add(doc);

				Tuple[] tuples = relationExtractionSystemInstance.execute(/*database.getId(), */doc);				

				System.out.println("extracted...");

				getCurrentQueryPool().updateQueries(doc);

				System.out.println("queries updated ...");

				int tupCount = 0;

				boolean wasadded = false;

				if (sample.getVersion().getCondition().isItUseful(tuples)){

					for (int j = 0; j < tuples.length; j++) {

						if (sample.getVersion().getCondition().isItUseful(tuples[j])){
							sample.addTuple(doc,tuples[j]);
							tupCount++;
						}
					}

					if (addRetrievedDocument(doc,sample,true, sampleConfiguration)){

						wasadded = true;						
					}

				}
				else {
					if (addRetrievedDocument(doc, sample,false, sampleConfiguration)){

						wasadded = true;

					}
				}
				if (wasadded){	

					getCurrentQueryPool().informHit();

					addedDocs.add(doc);

					addRetrievedDocument(query_index,sample.getQueriesSent(),calculateRound(seenDocumentssInQuery,sampleConfiguration.getResultsPerQuery()),queryProcessed,doc,tupCount,sample,sample.getSize(),pW,queriedDB);

					System.out.println("document added...");

				}

				else {

					getCurrentQueryPool().informDocument();

					addRetrievedDocument(query_index,sample.getQueriesSent(),calculateRound(seenDocumentssInQuery,sampleConfiguration.getResultsPerQuery()),queryProcessed,doc,tupCount,sample,-1,pW,queriedDB);

					System.out.println("document processed...");

				}


			}

			if (documents.isEmpty()){

				getCurrentQueryPool().informExhausted();

			}			

			if (!getCurrentQueryPool().hasMoreQueries() || ready(sample, sampleConfiguration)){

				updated++;
				
				processedDoc.clear();

				processedDoc.addAll(addedDocs);

				if (updated < 1)
				
					update(sample,database,pW,version_seed_pos,version_seed_neg);

			}

			setSeenDocumentsInQuery(must_words, must_not_words, seenDocumentssInQuery);
			
			System.out.println("sample updated...");

			searcher.cleanQuery(must_words, must_not_words);

			//			getCurrentQueryPool().fetchQuery();

			System.out.println("query Fetched");

		}

		if (sampleConfiguration.ready(sample)){
			return true;
		}

		return false;
	
	}

	private int calculateRound(int seenDocumentssInQuery, int resultsPerQuery) {
		
		int round = (int)Math.ceil((double)seenDocumentssInQuery / (double)resultsPerQuery);
		
		return Math.max(1, round); //Even when we did not see any document, we are still in round one. (we are submitting the query)
		
	}


	private Integer getQueryIndex(ArrayList<String> must_words,
			ArrayList<String> must_not_words) {
		
		return getQueryIndex().get(getId(must_words, must_not_words));
		
	}


	private int getSeenDocuments(ArrayList<String> must_words,
			ArrayList<String> must_not_words) {
		
		return getSeenDocument().remove(getId(must_words, must_not_words));

	}


	private void setSeenDocumentsInQuery(ArrayList<String> must_words,
			ArrayList<String> must_not_words, int seenDocuments) {
		
		getSeenDocument().put(getId(must_words, must_not_words),seenDocuments);
		
	}


	private Map<String, Integer> getSeenDocument() {
		
		if (seenDocumentsTable == null){
			seenDocumentsTable = new HashMap<String, Integer>();
		}
		return seenDocumentsTable;
	}


	private List<Document> getDocuments(ArrayList<String> must_words,
			ArrayList<String> must_not_words) {
		return processed.get(getId(must_words, must_not_words));
	}


	protected boolean ready(Sample sample, SampleConfiguration sampleConfiguration){

		if (currentQueryPool.retrievesUseful() && sample.getUseful().size() == sampleConfiguration.getUsefulNumber())
			return true;

		if (!currentQueryPool.retrievesUseful() && sample.getUseless().size() == sampleConfiguration.getUselessNumber())
			return true;

		return false;

	}

	protected void initialize(Sample sample, Database database, persistentWriter pW, int version_seed_pos,int version_seed_neg){
		getQueryIndex().clear();
		getSeenDocument().clear();
		processed = new HashMap<String,List<Document>>();
		addedDocs = new HashSet<Document>();
		processedDoc = new HashSet<Document>();
		relationExtractionSystemInstance = relationExtractionSystem.createInstance(database, interactionPersister, contentExtractor, relations);
		currentQueryPool = firstQueryPool;
		if (firstQueryPool.retrievesUseful())
			firstQueryPool.initialize(database, pW,version_seed_pos);
		else
			firstQueryPool.initialize(database, pW,version_seed_neg);
	}

	private boolean hasProcessed(Document document) {
		return processedDoc.contains(document);
	}

	private void markAsProcessed(ArrayList<String> must_words,
			ArrayList<String> must_not_words, List<Document> documents) {

		processed.put(getId(must_words,must_not_words),documents);

	}

	private String getId(ArrayList<String> must_words2,
			ArrayList<String> must_not_words2) {
		return new String(must_words.toString() + " - " + must_not_words.toString());
	}


	private boolean hasProcessed(ArrayList<String> must_words,
			ArrayList<String> must_not_words) {

		return processed.containsKey(getId(must_words, must_not_words));

	}

	private void addRetrievedDocument(int queryIndex, int sentQueries, int round, int doc_position_inQuery, Document document, int tupleCount, Sample sample, int docPosition_inSample, persistentWriter pW, int queriedDB) {

		//TODO The database has to be the one the document belongs to. It might happen that the sample is for a clustered database.

		

		pW.addProcessedDocument(sample.getId(),queryIndex, sentQueries,round, sample.getProcessedDocuments().size(), doc_position_inQuery, docPosition_inSample, tupleCount, document);

		//		pW.addProcessedDocument(queriedDB,idHandler.get(document),docPosition,sentQueries,queryProcessed,tupleCount,sample,sample.getProcessedDocuments().size());

	}

	protected QueryPoolExecutor getCurrentQueryPool(){
		return currentQueryPool;
	}

	protected void update(Sample sample, Database database, persistentWriter pW, int version_seed_pos,int version_seed_neg){
		currentQueryPool = secondQueryPool;
		if (secondQueryPool.retrievesUseful())
			secondQueryPool.initialize(database, pW, version_seed_pos);
		else
			secondQueryPool.initialize(database, pW, version_seed_neg);
		getQueryIndex().clear();
		getSeenDocument().clear();
		processed = new HashMap<String,List<Document>>();
	}

	protected void addGeneratedQuery(int queriedDB, TextQuery query, Sample sample,persistentWriter pW){

		sample.reportGeneratedQuery();
			
//		TextQuery tq = new TextQuery(must_words);
		
//replace		pW.WriteSentQuery(queriedDB,sample.getGeneratedQueries(),sample,tq);

		pW.WriteSentQuery(queriedDB,sample.getGeneratedQueries(),sample,query);
		
		saveQueryIndex(must_words,must_not_words,sample.getGeneratedQueries());
		
	}

	private void saveQueryIndex(ArrayList<String> must_words,
			ArrayList<String> must_not_words, int index) {
		
		getQueryIndex().put(getId(must_words, must_not_words),index);
		
	}


	private Map<String, Integer> getQueryIndex() {
		
		if (queryIndex == null){
			queryIndex = new HashMap<String, Integer>();
		}
		
		return queryIndex;
		
	}


	protected abstract boolean addRetrievedDocument(Document document, Sample sample, boolean isUseful, SampleConfiguration sampleConfiguration);

	//	private static InteractionPersister getInteractionPersister(
	//			persistentWriter pW) {
	//		
	//		if (interactionPersister == null){
	//			
	//			interactionPersister = new DiskBasedInteractionPersister("/proj/db-files2/NoBackup/pjbarrio/Experiments/Bootstrapping/", "/proj/db-files2/NoBackup/pjbarrio/Experiments/FinalDataIndex.txt", pW);
	//		}
	//		return interactionPersister;
	//		
	//	}




}
