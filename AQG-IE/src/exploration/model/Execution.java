package exploration.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import searcher.Searcher;
import searcher.lucene.LuceneSearcher;
import utils.FileHandlerUtils;
import utils.id.Idhandler;
import utils.persistence.persistentWriter;
import utils.query.QueryParser;
import execution.dispatcher.SchedulableDispatcher;
import execution.model.documentCollector.DocumentCollector;
import execution.model.documentCollector.ExecutionDocumentCollector;
import execution.model.policy.LimitedNumberPolicy;
import execution.model.scheduler.Scheduler;
import execution.model.statistics.ExecutionStatisticsCollector;
import execution.model.statistics.StatisticsCollector;
import execution.workload.impl.condition.UsefulCondition;
import execution.workload.tuple.Tuple;

public class Execution {

	private static Hashtable<Integer, Idhandler> idHandlers;
	private int id;
	private List<Evaluation> evaluations;
	private persistentWriter pW;
	
	private ExecutionAlternative executionAlternative;

	private Hashtable<String,Scheduler<Query,Query,LimitedNumberPolicy>> queryScheduler;
	private StatisticsCollector statisticsCollector;
	private Hashtable<Integer,SchedulableDispatcher<Query>> databaseDispatchers;
	private SchedulableDispatcher<String> currentInformationExtractionDispatcher;
	private ArrayList<SchedulableDispatcher<String>> informationExtractionDispatchers;
	private Hashtable<String,Hashtable<String,ArrayList<Document>>> evaluationQueries;
	private Hashtable<String,Hashtable<String,Integer>> queryPositions;
	private DocumentCollector sampleCollector;
	private int informationExtractionindex;
	private Hashtable<String, Integer> evaluationPositions;
	private int documentsProcessed;
	private int usefulTuples;
	private UsefulCondition condition;
	private ArrayList<String> must_words;
	private ArrayList<String> must_not_words;
	private int submittedQuery;
	private Hashtable<Integer,LimitedNumberPolicy> databasePolicy;
	private Hashtable<Integer,HashSet<Document>> processedDocuments;
	private HashSet<Evaluation> submittedEvaluations;
	private Scheduler<Query, Query, LimitedNumberPolicy> queryscheduler;
	private LimitedNumberPolicy retDatabasePolicy;
	private ArrayList<Document> retDocumentQueries;
	private Hashtable<String, ArrayList<Document>> retQueryEvaluations;
	private SchedulableDispatcher<Query> dispatcher;
	private Document document;
	private Hashtable<String, Integer> queries;
	private Tuple[] tuples;
	private List<Evaluation> toProcess;
	private ArrayList<Evaluation> retSubmittedEvaluations;
	private Hashtable<String, ArrayList<Document>> retSetAsProcessed;
	private Searcher lc;
	private Hashtable<String, ArrayList<Document>> tableRemove;
	private HashSet<Document> databaseProcessedDocuments;
	private long minTimeCurrentTime;
	private long auxCurrentTime;
	private Long IdDocument;
	
	public Execution(int id, ExecutionAlternative executionAlternative, persistentWriter pW) {
		
		this.id = id;
		
		processedDocuments = new Hashtable<Integer, HashSet<Document>>();
		
		submittedEvaluations = new HashSet<Evaluation>();
		
		this.executionAlternative = executionAlternative;
		
		queryScheduler = new Hashtable<String,Scheduler<Query,Query,LimitedNumberPolicy>>();
		
		databasePolicy = new Hashtable<Integer, LimitedNumberPolicy>();
		
		submittedQuery = 0;
		
		statisticsCollector = new ExecutionStatisticsCollector();
		
		databaseDispatchers = new Hashtable<Integer, SchedulableDispatcher<Query>>();
		
		informationExtractionDispatchers = new ArrayList<SchedulableDispatcher<String>>();
		
		for (int i = 0; i < executionAlternative.getExecutionPolicy().NumberOfInformationExtractionSystems(); i++) {
			
			informationExtractionDispatchers.add(createInformationExtractionDispatcher());
			
		}
		
		informationExtractionindex = 0;
		
		currentInformationExtractionDispatcher = informationExtractionDispatchers.get(0);
		
		evaluationQueries = new Hashtable<String, Hashtable<String,ArrayList<Document>>>();

		sampleCollector = new ExecutionDocumentCollector();
		
		evaluationPositions = new Hashtable<String, Integer>();
		
		queryPositions = new Hashtable<String, Hashtable<String,Integer>>();
		
		documentsProcessed = 0;
		
		condition = executionAlternative.getVersion().getCondition();
		
		this.pW = pW;
		
		must_not_words = new ArrayList<String>();
		
		must_words = new ArrayList<String>();
		
	}

	private SchedulableDispatcher<String> createInformationExtractionDispatcher() {
		return new SchedulableDispatcher<String>(executionAlternative.getExecutionPolicy().getExtractionsASecond(), 
				executionAlternative.getExecutionPolicy().getExtractionTime(), 
				executionAlternative.getExecutionPolicy().isExtractionSequential());
	}

	public Execution(int id, List<Evaluation> evaluations) {
		
		this.id = id;
		
		this.evaluations = evaluations;
		
	}

	public List<Evaluation> getEvaluations() {
		return evaluations;
	}

	public int getId() {
		return id;
	}

	public boolean equals(Object o){
		return id == ((Execution)o).id;
	}

	public Scheduler<Query,Query,LimitedNumberPolicy> getQueryScheduler(Evaluation eval) {

		queryscheduler = queryScheduler.get(eval.getId());
		
		if (queryscheduler == null){
			
			queryscheduler = executionAlternative.getQueryScheduler().newInstance(getDatabasePolicy(eval.getEvaluableDatabase()));
			
			for (Query query : eval.getCombination().getQueries()) {
				
				queryscheduler.addSchedulable(query, query.getGenerationTime(), getDispatcher(eval.getEvaluableDatabase()));
				
				queryscheduler.addInitialTime(query, query.getGenerationTime(), query);
				
			}
			
			queryScheduler.put(eval.getId(), queryscheduler);
			
		}
		
		return queryscheduler;
	}

	private LimitedNumberPolicy getDatabasePolicy(Database database) {
		
		retDatabasePolicy = databasePolicy.get(database.getId());
		
		if (retDatabasePolicy == null){
			
			retDatabasePolicy = new LimitedNumberPolicy(executionAlternative.getExecutionPolicy().getQueriesPerDatabase(database));
			
			databasePolicy.put(database.getId(),retDatabasePolicy);
		}
		
		return retDatabasePolicy;
	}

	public boolean isStillOn(Evaluation eval, Query query) {
		
		if (getDocumentQueries(getQueryEvaluations(eval),query).size() == 0)
			return false;
		
		if (getProcessedDocuments(eval,query) == getMaximumAllowedByQuery(eval.getEvaluableDatabase()))
			return false;
		
		if (getProcessedDocuments(eval.getEvaluableDatabase()) == getMaximumAllowedByDatabase(eval.getEvaluableDatabase()))
			return false;
			
		return executionAlternative.getFinishingStrategy().keepProcessingDocuments(eval, query,statisticsCollector.filter(eval).filter(query),sampleCollector.filter(eval).filter(query));
		
	}

	private ArrayList<Document> getDocumentQueries(
			Hashtable<String, ArrayList<Document>> documentQueries, Query query) {
		
		retDocumentQueries = documentQueries.get(query.getId());
		
		if (retDocumentQueries == null){
			retDocumentQueries = new ArrayList<Document>();
			documentQueries.put(query.getId(), retDocumentQueries);
		}
		
		return retDocumentQueries;
	}

	private Hashtable<String,ArrayList<Document>> getQueryEvaluations(Evaluation eval) {
		
		retQueryEvaluations = evaluationQueries.get(eval.getId());
		
		if (retQueryEvaluations == null){
			
			retQueryEvaluations = new Hashtable<String, ArrayList<Document>>();
			
			evaluationQueries.put(eval.getId(),retQueryEvaluations);
			
		}
		
		return retQueryEvaluations;
	}

	private int getMaximumAllowedByDatabase(Database database) {
		
		return executionAlternative.getExecutionPolicy().getRetrievablebyDatabase(database);
	
	}

	private double getProcessedDocuments(Database evaluableDatabase) {
		
		return statisticsCollector.filter(evaluableDatabase).getProcessedDocuments();

	}

	private int getMaximumAllowedByQuery(Database database) {
		return executionAlternative.getExecutionPolicy().getDatabaseLimit(database);
	}

	private double getProcessedDocuments(Evaluation eval, Query query) {
		
		return statisticsCollector.filter(eval).filter(query).getProcessedDocuments();
		
	}

	public long getReadyTime(Evaluation eval) {
		return eval.getCombination().getTime();
	}

	public boolean isStillOn(Evaluation eval) {
		
		return executionAlternative.getFinishingStrategy().keepProcessingEvaluation(eval,statisticsCollector,sampleCollector);
	
	}

	public boolean needsAnUpdate(Evaluation eval) {
		
		return executionAlternative.getAdaptiveStrategy().updateIsNeeded(eval,executionAlternative.getSelector().selectStatisticsCollector(eval, statisticsCollector),
				executionAlternative.getSelector().selectDocumentCollector(eval, sampleCollector));
	
	}

	public Evaluation update(Evaluation eval) {

		Evaluation evaluation =  executionAlternative.getAdaptiveStrategy().update(
				eval,executionAlternative.getSelector().selectStatisticsCollector(eval, statisticsCollector),executionAlternative.getSelector().selectDocumentCollector(eval, sampleCollector),
				executionAlternative.getSource(),executionAlternative.getGeneration(),
				executionAlternative.getExecutionPolicy(),executionAlternative.getDatabaseSelection(),executionAlternative.getAlgorithmSelection());
	
		if (executionAlternative.getAdaptiveStrategy().generatedNewEvaluation())
			pW.insertEvaluation(evaluation,this);
		
		sampleCollector.update(executionAlternative.getUpdateStrategy());
		
		statisticsCollector.notifyUpdated();
		
		return evaluation;
		
	}

	public SchedulableDispatcher<Query> getDispatcher(Database database) {
		
		dispatcher = databaseDispatchers.get(database.getId());
		
		if (dispatcher == null){
		
			dispatcher = createSchedulableDispatcher();
			
			databaseDispatchers.put(database.getId(), dispatcher);
		
		}
		
		return dispatcher;
		
	}

	private SchedulableDispatcher<Query> createSchedulableDispatcher() {
		return new SchedulableDispatcher<Query>(executionAlternative.getExecutionPolicy().getQueriesAtAtaTime(), 
				executionAlternative.getExecutionPolicy().getQueryIssuingTime(), executionAlternative.getExecutionPolicy().isQueryingSequential());
	}

	public Document getNextDocument(Evaluation eval, Query query) {
		
		documentsProcessed++;
		
		updateEvaluationPositions(eval);
		
		updateQueryPositions(eval,query);
		
		document = getDocumentQueries(getQueryEvaluations(eval), query).remove(0);
		
		extractTuples(eval,query,document);
		
//		sampleCollector.addDocument(documentString,query,eval,usefulTuples>0);
		
		statisticsCollector.setProcessedDocument(query,eval,document,usefulTuples);
		
		return document;
	}

	private static Long getDocumentId(String document,
			Database database, persistentWriter pW) {
		
		try {
			return getIdHandler(database, pW).get(document);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	
	}

	private static Idhandler getIdHandler(Database database, persistentWriter pW) throws IOException {
		
		Idhandler ret = getIdHandlerTable().get(database.getId());
		
		if (ret == null){
			
			if (database.isOnline())
				ret = new Idhandler(database,pW,true);
			else
				ret = new Idhandler(database,pW,true);
			getIdHandlerTable().put(database.getId(),ret);
		}
		
		return ret;
	}

	private static Hashtable<Integer, Idhandler> getIdHandlerTable() {
		
		if (idHandlers == null){
			idHandlers = new Hashtable<Integer,Idhandler>();
		}
		return idHandlers;
	}
	
	private void updateQueryPositions(Evaluation eval, Query query) {
		
		queries = queryPositions.get(eval.getId());
		
		if (queries == null){
			
			queries = new Hashtable<String, Integer>();
			
			queries.put(query.getId(), 1);
			
			queryPositions.put(eval.getId(), queries);
			
		} else {
			
			if (!queries.containsKey(query.getId()))
				queries.put(query.getId(), queries.size()+1);
		
		}
		
	}

	private void updateEvaluationPositions(Evaluation eval) {
		
		if (!evaluationPositions.containsKey(eval.getId())){
			
			int aux = evaluationPositions.size()+1;
			
			evaluationPositions.put(eval.getId(),aux);
			
		}
		
	}

	private boolean extractTuples(Evaluation eval, Query query, Document document) {
		
//		Document docLoc = getIdHandlerTable().get(eval.getEvaluableDatabase().getId()).getDocument(document);
		
		tuples = executionAlternative.getInformationExtractor(eval.getEvaluableDatabase(),eval.getCombination().getWorkload(),pW).execute(/*eval.getEvaluableDatabase().getId(),*/document);
		
		usefulTuples = 0;
		
		for (int i = 0; i < tuples.length; i++) {
			if (condition.isItUseful(tuples[i]))
				usefulTuples++;
		}
		
		return usefulTuples>0;
	}

	

	public SchedulableDispatcher<String> getInformationExtractionDispatcher(
			Evaluation eval, Query query) {
		
		return currentInformationExtractionDispatcher;
	
	}

	public long getCurrentTime() {
		
		minTimeCurrentTime = currentInformationExtractionDispatcher.availableTime("",0);
		
		int auxIndex = informationExtractionindex;
		
		for (SchedulableDispatcher<String> sd : informationExtractionDispatchers) {
			
			auxCurrentTime = sd.availableTime("",0);
			
			if (auxCurrentTime < minTimeCurrentTime){
				
				minTimeCurrentTime = auxCurrentTime;
				
				currentInformationExtractionDispatcher = sd;
				
				informationExtractionindex = auxIndex;
				
			}
			
			auxIndex++;
		}
		
		return minTimeCurrentTime;
	}

	public void saveInitialEvaluation(Evaluation eval) {
			
		pW.insertEvaluation(eval,this);
		
		sampleCollector.setInitialSample(eval,FileHandlerUtils.getAllResourceNames(new File(pW.getSampleFilteredFile(eval.getCombination().getGeneratorSample()))));
		
	}

	public void writeProcessedDocuments(Query query,
			Evaluation eval, long finishingTime) {
		
		cleanAlreadySubmitted(document,eval.getEvaluableDatabase());
		
		getProcessedDocuments(eval.getEvaluableDatabase().getId()).add(document);
		
		pW.writeProcessedDocuments(this,eval,evaluationPositions.get(eval.getId()), query, queryPositions.get(eval.getId()).get(query.getId()), document, documentsProcessed ,finishingTime,usefulTuples);
		
	}

	private HashSet<Document> getProcessedDocuments(int databaseId) {
		
		HashSet<Document> ret = processedDocuments.get(databaseId);
		
		if (ret == null){
			
			ret = new HashSet<Document>();
			
			processedDocuments.put(databaseId, ret);
			
		}
		
		return ret;
	}

	private void cleanAlreadySubmitted(Document document,
			Database database) {

		toProcess = filterSubmittedEvaluations(database);
		
		for (Evaluation evaluation : toProcess) {
			
			Hashtable<String, ArrayList<Document>> aux = getQueryEvaluations(evaluation);
			
			for (Enumeration<String> e = aux.keys(); e.hasMoreElements();){
				
				aux.get(e.nextElement()).remove(document);
				
			}
			
		}
		
	}

	private List<Evaluation> filterSubmittedEvaluations(Database database) {
		
		retSubmittedEvaluations = new ArrayList<Evaluation>();
		
		for (Evaluation evaluation : submittedEvaluations) {
			
			if (database.equals(evaluation.getEvaluableDatabase())){
				retSubmittedEvaluations.add(evaluation);
			}
			
		}
		
		return retSubmittedEvaluations;
	}

	public long getNextDownloadedTime(long querySubmissionFinishedTime, Query query, Evaluation eval) {
		return executionAlternative.getExecutionPolicy().getRetrievedDocumentReadyTime(querySubmissionFinishedTime, query, eval,(long)statisticsCollector.filter(eval).filter(query).getProcessedDocuments());
	}

	public void setAsProcessed(Evaluation eval, Query query) {
		
		getDocumentQueries(getQueryEvaluations(eval), query).clear();
		
		getQueryEvaluations(eval).remove(query.getId());
		
		statisticsCollector.setAsProcessed(eval,query);
		
	}

	public void setAsProcessed(Evaluation eval) {
		
		retSetAsProcessed = evaluationQueries.remove(eval.getId());
		
		if (retSetAsProcessed == null)
			return;
		
		for(Enumeration<String> e = retSetAsProcessed.keys(); e.hasMoreElements();){
			
			retSetAsProcessed.get(e.nextElement()).clear();
			
		}
		
		retSetAsProcessed.clear();
		
		statisticsCollector.setAsProcessed(eval);
		
	}

	public void submitQuery(Query query, Evaluation eval) {
	
		if (submitted(eval,query))
			return;
		
		submittedEvaluations.add(eval);
		
		submittedQuery++;
		
		getQueryEvaluations(eval).put(query.getId(), new ArrayList<Document>());
		
		must_words.clear();
		
		must_not_words.clear();
		
		lc = executionAlternative.getSearcher(eval.getEvaluableDatabase().getIndex(),eval.getEvaluableDatabase(),pW.getStopWords(),pW);
		
		QueryParser.parseQuery(query.getText(), must_words, must_not_words);
		
		lc.doSearch(must_words, must_not_words);
		
		databaseProcessedDocuments = getProcessedDocuments(eval.getEvaluableDatabase().getId());
		
		for (Document document : lc.retrieveMaxAllowedDocuments(must_words, must_not_words)){
			
//			IdDocument = getDocumentId(document,eval.getEvaluableDatabase(),pW);
			
			if (!databaseProcessedDocuments.contains(document))
				getDocumentQueries(getQueryEvaluations(eval),query).add(document);
		}
		
		lc.cleanQuery(must_words,must_not_words);
	}

	private boolean submitted(Evaluation eval, Query query) {
		return getQueryEvaluations(eval).containsKey(query.getId());
	}

	public Scheduler<Evaluation, Query, LimitedNumberPolicy> getScheduler() {
		return executionAlternative.getScheduler().newInstance(new LimitedNumberPolicy(executionAlternative.getExecutionPolicy().getDatabasesToContact()));
	}

	public void cleanExecution() {
		
		evaluations = null;
		
		queryScheduler = null;
		statisticsCollector.clean();
		statisticsCollector = null;
		databaseDispatchers = null;
		currentInformationExtractionDispatcher = null;
		informationExtractionDispatchers = null;
		
		for (Enumeration<Hashtable<String,ArrayList<Document>>> e = evaluationQueries.elements();e.hasMoreElements();){
			
			tableRemove = e.nextElement();
			
			for (Enumeration<ArrayList<Document>> docs = tableRemove.elements(); e.hasMoreElements(); ) {
				
				docs.nextElement().clear();
				
			}
			
		}
		
		evaluationQueries = null;
		queryPositions = null;
		sampleCollector.clean();
		sampleCollector = null;
		informationExtractionindex = 0;
		evaluationPositions = null;
		documentsProcessed = 0;
		usefulTuples = 0;
		condition = null;
		must_words = null;
		must_not_words = null;
		submittedQuery = 0;
		databasePolicy = null;
		
		processedDocuments.clear();
		processedDocuments= null;
		
		submittedEvaluations = null;

		
	}

	

	
}
